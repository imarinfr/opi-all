package org.lei.opi.core;

import org.apache.commons.lang3.ArrayUtils;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Create a thread that serves/streams raw images from one or more "webcams" on a TCP port.
 * In addition, checks the `requestQueue` for any requests for pupil position and diameter
 * and puts responses back on the `responseQueue` as required.
 *
 * @author Andrew Turpin
 * @date 5 June 2024 
 */
public class CameraStreamer extends Thread {
    /** Whether this streamer is connected to a client */
    public boolean connected;

    /** The data of an incoming request to the camera */
    static public record Request(
        long timeStamp,           // some timestamp of the request (used to match responses, perhaps)
        int deviceNumber          // device number for which to get the response
    ) {;}

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
    public LinkedBlockingQueue<Request> requestQueue;
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

    /**
     * Create a CameraStreamer that streams images from a camera on the local machine and sends them over UDP
     * @param port The port number on this machine that will serve images 
     * @param deviceNumber  Camera number on the local machine running the CameraStreamer
     * @throws IOException
     */
    public CameraStreamer(int port, int []deviceNumber) throws IOException {
        this.port = port;
        this.deviceNumber = deviceNumber;
        requestQueue = new LinkedBlockingQueue<Request>(10);
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
            byte []bytes = new byte[1];
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
                            if (n != bytes.length)
                                bytes = new byte[n];
                            frame[i].get(0, 0, bytes);
                                // 1 byte for device number
                                // 4 bytes for length of data, n
                                // n bytes of Mat
//System.out.println("Putting " + n + " bytes from camera " + i + " on socket.");
                            socket.getOutputStream().write(i);
                            socket.getOutputStream().write(n >> 24);
                            socket.getOutputStream().write((n >> 16) & 0xFF);
                            socket.getOutputStream().write((n >>  8) & 0xFF);
                            socket.getOutputStream().write( n        & 0xFF);
                            socket.getOutputStream().write(bytes);
                        }
                    }
                }
                Thread.sleep(50);
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
        getImageValues(frame);
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
    }
            
    /**
     * Process frame to find (x, y) and diameter of pupil and put the result in map values.
     * These must be put into `processingResults` as `x`, `y`, and `d` respectively.
     * 
     * Should override this method in a subclass to do something useful.
     * 
     * @param frame Frame to process looking for pupil
     */
    protected void getImageValues(Mat frame) {
        processingResults.put("x", -1);
        processingResults.put("y", -1);
        processingResults.put("d", -1);
    };
}