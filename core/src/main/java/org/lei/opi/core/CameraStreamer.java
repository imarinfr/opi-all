package org.lei.opi.core;

import org.lei.opi.core.definitions.CircularBuffer;
import org.lei.opi.core.definitions.FrameInfo;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import es.optocom.jovp.definitions.ViewEye;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Create a thread that serves/streams raw images from one or more "webcams" on a TCP port.
 * In addition, checks the `requestQueue` for any requests for pupil position and diameter
 * and puts responses back on the `responseQueue` as required.
 *
 * @author Andrew Turpin
 * @date 5 June 2024 
 */
public abstract class CameraStreamer extends Thread {
    /** Whether this streamer is connected to a client */
    public boolean connected;

    /** A working are for processRequest  */
    private FrameInfo workingFrameInfo = new FrameInfo();

    /** A buffer of most recent frames grabbed for each device */
    private final HashMap<ViewEye, CircularBuffer<FrameInfo>> frameBuffer = new HashMap<ViewEye, CircularBuffer<FrameInfo>>(); 

    /** The data of an incoming request to the camera */
    static public class Request {
        long timeStamp;           // some timestamp of the request (used to match responses, perhaps)
        ViewEye eye;              // eye for which to get the response
        int numberOfTries;        // The number of times this request has been attempted to be completed

        /** The maximum number of times/frames to try and find pupil to satisfy request */
        static final int MAX_TRIES_FOR_REQUEST = 10;

        public Request(long timeStamp, ViewEye eye) {
            this.timeStamp = timeStamp;
            this.eye = eye;
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

    /** The device numbers of the one or two cameras to stream on the machine on which this is running. 
     * If ony one camera, then just use "Left".
    */
    private final HashMap<ViewEye, Integer> deviceNumber = new HashMap<ViewEye, Integer>(); 

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

    /** Accesor for buffers */
    public CircularBuffer<FrameInfo> getBuffer(ViewEye eye) { return frameBuffer.get(eye);} 

    /** Junk instance just to allow calling of readBytes. And testing... */
    public CameraStreamer() { ; }

    /**
     * Create a CameraStreamer that streams images from a camera on the local machine and sends them over UDP
     * @param port The port number on this machine that will serve images 
     * @param deviceNumberLeft Camera number on the local machine running the CameraStreamer for left eye (or just one camera)
     * @param deviceNumberRight Camera number on the local machine running the CameraStreamer for right eye (could be -1 for just one camera)
     * @throws IOException
     */
    public CameraStreamer(int port, int deviceNumberLeft, int deviceNumberRight) throws IOException {
        this.port = port;

        if (deviceNumberLeft > -1)
            this.deviceNumber.put(ViewEye.LEFT, Integer.valueOf(deviceNumberLeft));
        if (deviceNumberRight > -1)
            this.deviceNumber.put(ViewEye.RIGHT, Integer.valueOf(deviceNumberRight));

        for (ViewEye e : this.deviceNumber.keySet()) 
            frameBuffer.put(e, new CircularBuffer<FrameInfo>(FrameInfo::new, 30));

        requestQueue = new LinkedBlockingDeque<Request>(10);
        responseQueue = new LinkedBlockingQueue<Response>(10);

        this.start();
    }
        
    /**
     * Loop grabbing frames into the frameBuffer(s) and both
     *  (1) Checking if there is a Request - if so service it with {@link processRequest}
     *  (2) Send the frame over the TCP socket if there is a client connected
     */
    @Override
    public void run() {
        HashMap <ViewEye, VideoCapture> grabber = new HashMap<ViewEye, VideoCapture>();
        for (ViewEye e : this.deviceNumber.keySet()) {
            grabber.put(e, new VideoCapture((int)deviceNumber.get(e)));
            if (!grabber.get(e).isOpened()) {
                System.out.println(String.format("Cannot open camera %s for eye %s", deviceNumber.get(e), e));
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
                for (ViewEye e : this.frameBuffer.keySet())
                    frameBuffer.get(e).put((FrameInfo f) -> f.grab(grabber.get(e)));

                    // If there is a Request pending, process it
                Request request = requestQueue.poll();
                if (request != null)
                    processRequest(request, frameBuffer.get(request.eye));

                    // And now send most recent frames on the socket if there is a client
                if (connected) {
                    for (ViewEye e : this.frameBuffer.keySet())
                        frameBuffer.get(e).applyHead((FrameInfo f) -> writeBytes(socket, e, f));
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
            for (ViewEye e : grabber.keySet())
                grabber.get(e).release();
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

        for (int tol = 1 ; tol < 500 ; tol *= 2) {
            final Integer iTol = Integer.valueOf(tol);
            if (buffer.getHeadToTail((FrameInfo f) -> f.timeIsClose(request.timeStamp, iTol), (src, dst) -> src.copyTo(dst), workingFrameInfo))
                break;
        }

        getImageValues(workingFrameInfo.mat());
        if (pupilInfo.valid) {
            try {
                responseQueue.add(new Response(
                    request.timeStamp,
                    workingFrameInfo.timeStamp(),
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
                    responseQueue.add(new Response(request.timeStamp, workingFrameInfo.timeStamp()));
                } catch (IllegalStateException e) {
                    System.out.println("Response queue is full, apparently!");
                }
            }
        }
    }
            
    /**
     * Write the most recent image (head of {@link frameBuffer}) out on socket.
     *
     * @param socket Open socket on which to write bytes
     * @param eye Eye to write before bytes
     * @param frame Frame to process.
     * @throws IOException
     * @throws ConcurrentModificationException You should CameraStreamer.bytesLock.lock() before calling this.
     */
    public abstract void writeBytes(Socket socket, ViewEye eye, FrameInfo frame);

    /**
     * Fill dst with the image incoming on socket.
     *
     * @param socket An open socket from which to read
     * @param dst Byte array to fill.
     * @return Device number read. -1 for error
     */
    public abstract ViewEye readBytes(Socket socket, byte []dst) throws IndexOutOfBoundsException;

    /**
     * Process frame to find (x, y) and diameter of pupil and put the result in {@link pupilInfo}.
     * 
     * @param frame Frame to process looking for pupil
     * @return true if values found, false otherwise
     */
    protected abstract void getImageValues(Mat frame);
}