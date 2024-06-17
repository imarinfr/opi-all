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
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

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

    protected class FrameInfo {
        long timeStamp;
        Mat mat;
        FrameInfo() {
            this.timeStamp = -1;
            this.mat = new Mat();
        }

        /*
         * @param otherTimeStamp A time stamp to which to compare this.timeStamp
         * @param tol The tolerance for the comparison
         * @return true if the difference between this.timeStamp and otherTimeStamp is less than tol, false otherwise
         */
        public boolean timeIsClose(long otherTimeStamp, int tol) {
            return Math.abs(this.timeStamp - otherTimeStamp) < tol;
        }

        /** 
         * @param dst Destination Mat to receive a copy of me
         * Copy myself into dst 
         */
        public void copyTo(FrameInfo dst) {
            dst.timeStamp = this.timeStamp;
            this.mat.copyTo(dst.mat);
        }

        /**
         * Grab a frame from the grabber and put it in {@link mat}.
         * @param grabber
         */
        public void grab(VideoCapture grabber) {
            this.timeStamp = System.currentTimeMillis(); 
            if (!grabber.read(this.mat))
                this.timeStamp = -1;
        }
        
        /**
         * Copy mat's bytes into CameraStreamer.bytes and write them on the socket.
         * @param socket
         * @param deviceNumber
         * @throws IOException
         */
        public void sendBytes(Socket socket, int deviceNumber) throws IOException {
            int n = mat.channels() * mat.rows() * mat.cols();
            try {
                bytesLock.lock();
                if (n != bytes.length)
                    bytes = new byte[n];
                mat.get(0, 0, CameraStreamer.bytes);
                writeBytes(socket, deviceNumber);
            } finally {
                bytesLock.unlock();
            }
        }
    }

    /** A working are for processRequest  */
    private FrameInfo workingFrameInfo = new FrameInfo();

    /** A buffer of most recent frames grabbed for each device */
    CircularBuffer<FrameInfo> []frameBuffer; 

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
        double x,                    // pupil position with (0,0) at centre of image (degrees)
        double y,                    // pupil position with (0,0) at centre of image (degrees)
        double diameter              // pupil diameter in mm
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
    class PupilInfo {
        public double diameter;     // diameter of pupil in pixels??? or mm??
        public double centerX;      // x position of pupil in degrees
        public double centerY;      // y position of pupil in degrees
        public int bb_tl_x;         // bounding box of pupil (pixels)
        public int bb_tl_y;
        public int bb_width;
        public int bb_height;
        public boolean valid;       // true if these current values are from a found pixel

        /** @param srcWidth Width of source image in pixels 
         *  @param srcHeight Height of source image in pixels 
         */
        PupilInfo(int srcWidth, int srcHeight) {
            this.reset();
        }
            
        public void reset() {
            diameter = 0;
            centerX = -1;
            centerY = -1;
            bb_tl_x = 0;
            bb_tl_y = 0;
            bb_width = 0;
            bb_height = 0;
            valid = false;
        }

        public String toString() {
            if (valid)
                return String.format("x: %6.4f, y: %6.4f, d: %5.2f", centerX, centerY, diameter);
            else
                return "No valid pupil.";
        }
    }
    protected PupilInfo pupilInfo;


    /** Accessor method to get results from a call to {@link getImageValues} */
    public String getResults() { 
        if (pupilInfo.valid) 
            return String.format("x: %d, y: %d, d: %d", pupilInfo.centerX, pupilInfo.centerY, pupilInfo.diameter);
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

        frameBuffer = new CircularBuffer[deviceNumber.length];
        for (int i = 0 ; i < deviceNumber.length; i++)
            frameBuffer[i] = new CircularBuffer<FrameInfo>(FrameInfo::new, 20);

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

            while (!isInterrupted()) {
                    // See if someone wants to connect and stream...
                if (!connected)
                    try {
                        socket = server.accept();
                        this.connected = true;
                    } catch (SocketTimeoutException e) { ; }

                    // Try and grab each device as close as possible in time
                for (int i = 0 ; i < this.deviceNumber.length; i++) {
                    final Integer ii = Integer.valueOf(i);
                    frameBuffer[i].put((FrameInfo f) -> f.grab(grabber[ii]));
                }

                Request request = requestQueue.poll();
                if (request != null) {
                    int i = ArrayUtils.indexOf(deviceNumber, request.deviceNumber);
                    processRequest(request, frameBuffer[i]);
                }

                    // And now send the frames on the socket
                if (connected) {
                    for (int i = 0 ; i < this.deviceNumber.length; i++) {
//BufferedImage bi = jc.convert(frame[i]);
//System.out.println("bi type " + bi.getType());
//System.out.println("bi width " + bi.getWidth());
//System.out.println("bi height " + bi.getHeight());
                        final Integer ii = Integer.valueOf(i);
                        frameBuffer[i].apply((FrameInfo f) -> {
                            try {
                                f.sendBytes(socket, this.deviceNumber[ii]);
                            } catch (IOException e) {
                                e.printStackTrace();
                                this.connected = false;
                            }
                        });
                    }
                    Thread.sleep(50);
                }
            }
            server.close();
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
    private void processRequest(Request request, CircularBuffer<FrameInfo> buffer) {

        for (int tol = 1 ; tol < 500 ; tol += 50) {
            final Integer iTol = Integer.valueOf(tol);
            if (buffer.get((FrameInfo f) -> f.timeIsClose(request.timeStamp, iTol), (src, dst) -> src.copyTo(dst), workingFrameInfo))
                break;
        }

        getImageValues(workingFrameInfo.mat);
        if (pupilInfo.valid) {
            try {
                responseQueue.add(new Response(
                    request.timeStamp,
                    workingFrameInfo.timeStamp,
                    pupilInfo.centerX,
                    pupilInfo.centerY,
                    pupilInfo.diameter
                ));
            } catch (IllegalStateException e) {
                System.out.println("Response queue is full, apparently!");
            }
        } else {
            if (request.incTries())
                requestQueue.addFirst(request); // put it back for a go at another frame
            else {
                try {
                    responseQueue.add(new Response(request.timeStamp, workingFrameInfo.timeStamp));
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
     * Fill bytes with the image incoming on socket
     *
     * @param socket An open socket from which to read
     * @return Device number read. -1 for error
     */
    public abstract int readBytes(Socket socket);

    /**
     * Process frame to find (x, y) and diameter of pupil and put the result in {@link pupilInfo}.
     * 
     * @param frame Frame to process looking for pupil
     * @return true if values found, false otherwise
     */
    protected abstract void getImageValues(Mat frame);
}