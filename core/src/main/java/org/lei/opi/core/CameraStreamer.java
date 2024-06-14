package org.lei.opi.core;

import org.apache.commons.lang3.ArrayUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Create a thread that serves/streams raw images from one or more "webcams" on a TCP port.
 * In addition, checks the `requestQueue` for any requests for pupil position and diameter
 * and puts responses back on the `responseQueue` as required.
 *
 * @author Andrew Turpin
 * @date 5 June 2024 
 */
public abstract class CameraStreamer extends Thread {
    /** Used to lock bytes for processing in {@link readImageToBytes} */
    public static final ReentrantLock bytesLock = new ReentrantLock();
    /** a buffer that is filled by {@link readImageToBytes} */
    public static byte []bytes = new byte[1];

    /** Whether this streamer is connected to a client */
    public boolean connected;

    /** The data of an incoming request to the camera */
    static public class Request {
        long timeStamp;           // some timestamp of the request (used to match responses, perhaps)
        int deviceNumber;         // device number for which to get the response
        int numberOfTries;         // The number of times this request has been attempted to be completed

        /** The maximum number of times/frames to try and find pupil to satisfy request */
        static final int MAX_TRIES_FOR_REQUEST = 10;

        public Request(long timeStamp, int deviceNumber) {
            this.timeStamp = timeStamp;
            this.deviceNumber = deviceNumber;
            this.numberOfTries = 0;
        }
        public boolean incTries() { 
            this.numberOfTries++;
            return this.numberOfTries < MAX_TRIES_FOR_REQUEST;
        }
            
    }

    /** The data put back on the queue for each request */
    static public record Response(
        long requestTimeStamp,       // timestamp of request object that initiated this response
        long acquisitionTimeStamp,   // timestamp of frame acquisition (approximate)
        int x,                       // pupil position in pixels with (0,0) at centre of image
        int y,                       // pupil position in pixels with (0,0) at centre of image
        int diameter                // pupil diameter in pixels
    ) {
        public Response(long requestTimeStamp, long acquisitionTimeStamp) {
            this(requestTimeStamp, acquisitionTimeStamp, -1, -1, -1);
        }
        public Response set(int x, int y, int diameter) {
            return new Response(this.requestTimeStamp, this.acquisitionTimeStamp, x, y, diameter);
        }
    }

    /** Queue of requests for image processing */
    public LinkedBlockingDeque<Request> requestQueue;
    /** Queue of results from image processing */
    public LinkedBlockingQueue<Response> responseQueue;

    /** The device numbers of the cameras to stream on the machine on which this is running. */
    private int []deviceNumber; 

    /** The port number on this machine that will serve images */
    private int port; 

    /** The socket on which frames will be sent. */
    private Socket socket;

    /** Little bit of memory for communicating with image processing subclass */
    protected HashMap<String, Integer> processingResults;

    /** Accesor method to get results from a call to {@link getImageValues} */
    public String getResults() { 
        if (processingResults.containsKey("x") && processingResults.containsKey("y") && processingResults.containsKey("d"))
            return String.format("x: %d, y: %d, d: %d", processingResults.get("x"), processingResults.get("y"), processingResults.get("d"));
        else
            return "No results yet";
    }

    /** Weak instance just to allow calling of readBytes. Why not make it static? */
    public CameraStreamer() { ; }

    /**
     * Create a CameraStreamer that streams images from a camera on the local machine and sends them over UDP
     * @param port The port number on this machine that will serve images 
     * @param deviceNumber  Camera number on the local machine running the CameraStreamer
     * @throws IOException
     */
    public CameraStreamer(int port, int []deviceNumber) throws IOException {
        this.port = port;
        this.deviceNumber = deviceNumber;
        requestQueue = new LinkedBlockingDeque<Request>(10);
        responseQueue = new LinkedBlockingQueue<Response>(10);
        processingResults = new HashMap<String, Integer>(3);
        this.start();
    }
        
    @Override
    public void run() {
        VideoCapture []grabber = new VideoCapture[this.deviceNumber.length];
        for (int i = 0 ; i < this.deviceNumber.length; i++) {
            grabber[i] = new VideoCapture(this.deviceNumber[i]);
            if (!grabber[i].isOpened()) {
                System.out.println("Cannot open camera device " + this.deviceNumber[i]);
                this.connected = false;
                return;
            }
        }

        try {
            ServerSocket server = new ServerSocket(this.port);
            server.setSoTimeout(10);

            long []timestamp = new long[deviceNumber.length];   // -1 for invalid frame
            Mat []frame = new Mat[deviceNumber.length];

            for (int i = 0 ; i < this.deviceNumber.length; i++)
                frame[i] = new Mat(480, 640, CvType.CV_8UC3);

            while (!isInterrupted()) {
                    // See if someone wants to connect and stream...
                if (!connected)
                    try {
                        socket = server.accept();
                        this.connected = true;
                    } catch (SocketTimeoutException e) { ; }

                    // Try and grab each device as close as possible in time
                for (int i = 0 ; i < this.deviceNumber.length; i++) {
                    if (grabber[i].read(frame[i]))
                        timestamp[i] = System.currentTimeMillis();
                    else {
                        timestamp[i] = -1;
                        System.out.println("Could not get frame from " + this.deviceNumber[i]);
                    }
                }

                Request request = requestQueue.poll();
                if (request != null) {
                    int i = ArrayUtils.indexOf(deviceNumber, request.deviceNumber);
                    processRequest(request, frame[i], timestamp[i]);  
                }

                    // And now send the frames on the socket
                if (connected) {
                    for (int i = 0 ; i < this.deviceNumber.length; i++) {
//BufferedImage bi = jc.convert(frame[i]);
//System.out.println("bi type " + bi.getType());
//System.out.println("bi width " + bi.getWidth());
//System.out.println("bi height " + bi.getHeight());
                        if (timestamp[i] != -1) {
                            int n = frame[i].channels() * frame[i].rows() * frame[i].cols();
                            try {
                                bytesLock.lock();
                                if (n != bytes.length)
                                    bytes = new byte[n];
                                frame[i].get(0, 0, CameraStreamer.bytes);
                                writeBytes(socket, this.deviceNumber[i]);
                            } finally {
                                bytesLock.unlock();
                            }
                        }
                    }
                    Thread.sleep(50);
                }
            }
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
            this.connected = false;
        } catch (InterruptedException e) {
            this.connected = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0 ; i < this.deviceNumber.length; i++)
                grabber[i].release();
        } catch (Exception e) { ; }
    }

    /**
     * Process a request from the client by finding the centre and diameter of the 
     * pupil in `frame` and putting the result on responseQueue.
     *              
     *  WARNING: make sure 1 request only generates 1 response
     * 
     * @param request Contains timeStamp of request
     * @param frame Image frame to process
     * @param timestamp Timestamp that the image was acquired
     */
    private void processRequest(Request request, Mat frame, long timestamp) {
        if (getImageValues(frame)) {
            try {
                responseQueue.add(new Response(
                    request.timeStamp,
                    timestamp,
                    processingResults.get("x").intValue(),
                    processingResults.get("y").intValue(),
                    processingResults.get("d").intValue()
                ));
            } catch (IllegalStateException e) {
                System.out.println("Response queue is full, apparently!");
            }
        } else {
            if (request.incTries())
                requestQueue.addFirst(request); // put it back for a go at another frame
            else {
                try {
                    responseQueue.add(new Response(request.timeStamp, timestamp));
                } catch (IllegalStateException e) {
                    System.out.println("Response queue is full, apparently!");
                }
            }
        }
    }
            
    /**
     * Write static bytes array out on socket as 
     *       1 byte for device number
     *       4 bytes for length of data, n
     *       n bytes
     *
     * @param socket Open socket on which to write bytes
     * @param deviceNumber To write before bytes
     * @throws IOException
     * @throws ConcurrentModificationException You should CameraStreamer.bytesLock.lock() before calling this.
     */
    public abstract void writeBytes(Socket socket, int deviceNumber) throws IOException, ConcurrentModificationException;

    /**
     * Fill bytes with the image on socket
     *
     * Should override this method in a subclass to do something useful.
     * 
     * @param socket An open socket from which to read
     * @return Device number read. -1 for error
     */
    public abstract int readBytes(Socket socket);

    /**
     * Process frame to find (x, y) and diameter of pupil and put the result in map values.
     * These must be put into `processingResults` as `x`, `y`, and `d` respectively.
     * 
     * Should override this method in a subclass to do something useful.
     * 
     * @param frame Frame to process looking for pupil
     * @return true if values found, false otherwise
     */
    protected abstract boolean getImageValues(Mat frame);
}