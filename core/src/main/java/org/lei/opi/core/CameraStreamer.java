package org.lei.opi.core;

import org.lei.opi.core.definitions.CircularBuffer;
import org.lei.opi.core.definitions.FrameInfo;
import org.lei.opi.core.definitions.PupilRequest;
import org.lei.opi.core.definitions.PupilResponse;
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
 * This should be subclassed for each specific device, as should the FrameInfo for that device.
 *
 * @author Andrew Turpin
 * @date 5 June 2024 
 */
public abstract class CameraStreamer<FT extends FrameInfo> extends Thread {
    /** Whether this streamer is connected to a client */
    public boolean connected;

    /** A buffer of most recent frames grabbed for each device */
    protected HashMap<ViewEye, CircularBuffer<FT>> frameBuffer;

    /** Queue of requests for image processing */
    public LinkedBlockingDeque<PupilRequest> requestQueue;
    /** Queue of results from image processing */
    public LinkedBlockingQueue<PupilResponse> responseQueue;

    /** The device numbers of the one or two cameras to stream on the machine on which this is running. 
     * If ony one camera, then just use "Left".
    */
    protected final HashMap<ViewEye, Integer> deviceNumber = new HashMap<ViewEye, Integer>(); 

    /** The port number on this machine that will serve images */
    private int port; 

    /** The socket on which frames will be sent. */
    private Socket socket;

    /** Accesor for buffers  - used in tests */
    public CircularBuffer<FT> getBuffer(ViewEye eye) { return frameBuffer.get(eye);} 

    /** working area for copying out of queue */
    protected FT workingFrameInfo;

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

        requestQueue = new LinkedBlockingDeque<PupilRequest>(10);
        responseQueue = new LinkedBlockingQueue<PupilResponse>(10);

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

                    // Try and grab each device as close as possible in time, then add pupils
                for (ViewEye e : this.frameBuffer.keySet())
                    frameBuffer.get(e).put((FT f) -> f.grab(grabber.get(e)));
                for (ViewEye e : this.frameBuffer.keySet()) {
                    frameBuffer.get(e).applyHead((FT f) -> f.findPupil());
                    if (connected)
                        frameBuffer.get(e).applyHead((FT f) -> writeBytes(socket, e, f));
                    frameBuffer.get(e).conditionalPop((FT f) -> !f.hasPupil());   // throw out frames without a pupil
                }

                    // If there is a Request pending, process it
                PupilRequest request = requestQueue.poll();
                if (request != null)
                    processRequest(request, frameBuffer.get(request.eye()));
                else
                    Thread.sleep(50);
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
     * pupil in the frame in `buffer` that has closest timestamp to request.
     *              
     *  WARNING: make sure 1 request only generates 1 response
     * 
     * @param request Contains timeStamp of request
     * @param frame Image frame to process
     * @param timestamp Timestamp that the image was acquired
     */
    private void processRequest(PupilRequest request, CircularBuffer<FT> buffer) {
        if (buffer == null) {
            System.out.println("Cannot satisfy camera requests for eye " + request.eye());
            return;
        }

        for (int tol = 1 ; tol < PupilRequest.MAX_TIME_DIFFERENCE_TO_SATISFY_REQUEST ; tol *= 2) {
            final Integer iTol = Integer.valueOf(tol);
            if (buffer.getHeadToTail(
                (FT f) -> request.closeEnough(f.timeStamp(), iTol), 
                (FT src, FT dst) -> src.copyPupilInfo(dst), workingFrameInfo))
                break;
        }

        if (workingFrameInfo.hasPupil()) {
            try {
                responseQueue.add(new PupilResponse(
                    request.timeStamp(),
                    workingFrameInfo.timeStamp(),
                    workingFrameInfo.pupilX(),
                    workingFrameInfo.pupilY(),
                    workingFrameInfo.pupilDiameter()
                ));
            } catch (IllegalStateException e) {
                System.out.println("Response queue is full, apparently!");
            }
        } else {
            if (request.incTries())
                requestQueue.addFirst(request); // put it back for a go at another frame
            else {
                try {
                    responseQueue.add(new PupilResponse(request.timeStamp(), workingFrameInfo.timeStamp()));
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
}