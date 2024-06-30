package org.lei.opi.core;

import org.lei.opi.core.definitions.CircularBuffer;
import org.lei.opi.core.definitions.FrameInfo;
import org.lei.opi.core.definitions.PupilRequest;
import org.lei.opi.core.definitions.PupilResponse;
import org.opencv.videoio.VideoCapture;

import com.diffplug.common.base.Errors;

import es.optocom.jovp.definitions.ViewEye;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
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

    /*
    * A queue of files to read instead of grabbing from camera.
    * If there is an entry for eye it will be preferred over deviceNumbers[eye]
    */
    protected final HashMap<ViewEye, ArrayDeque<File>> deviceFiles = new HashMap<ViewEye, ArrayDeque<File>>(); 

    /** The port number on this machine that will serve images. -1 for no streaming */
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
     * Create a CameraStreamer that streams images from camera(s) on the local machine given by deviceNumber(s)
     * @param port The port number on this machine that will serve images 
     * @param deviceNumberLeft Camera number on the local machine running the CameraStreamer for left eye (or just one camera)
     * @param deviceNumberRight Camera number on the local machine running the CameraStreamer for right eye (could be -1 for just one camera)
     * @throws IOException
     */
    public CameraStreamer(final int port, final int deviceNumberLeft, final int deviceNumberRight) throws IOException {
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
     * Create a CameraStreamer that streams images from a files in fodlers given
     * @param port The port number on this machine that will serve images 
     * @param deviceNumberLeft Camera number on the local machine running the CameraStreamer for left eye (or just one camera)
     * @param deviceNumberRight Camera number on the local machine running the CameraStreamer for right eye (could be -1 for just one camera)
     * @throws IOException
     */
    public CameraStreamer(final int port, final String leftFolder, final String rightFolder) throws IOException {
        this.port = port;

            // This is for the subclass so that framebuffers are initialised and also for a few other things...
        if (leftFolder != null) {
            this.deviceNumber.put(ViewEye.LEFT, null);
            deviceFiles.put(ViewEye.LEFT, new ArrayDeque<File>(listFiles(leftFolder)));
        }
        if (rightFolder != null) {
            this.deviceNumber.put(ViewEye.RIGHT, null);
            deviceFiles.put(ViewEye.RIGHT, new ArrayDeque<File>(listFiles(rightFolder)));
        }

        requestQueue = new LinkedBlockingDeque<PupilRequest>(10);
        responseQueue = new LinkedBlockingQueue<PupilResponse>(10);

        this.start();
    }
    
    /*
     * @param folder 
     */
    private List<File> listFiles(final String folder) throws IOException {
        final File f = new File(folder);
        return Arrays.asList(f.listFiles());
    }
        
    /**
     * Loop grabbing frames into the frameBuffer(s) and both
     *  (1) Checking if there is a Request - if so service it with {@link processRequest}
     *  (2) Send the frame over the TCP socket if there is a client connected
     */
    @Override
    public void run() {
            // Try and connect to a camera if deviceFiles is not available
        HashMap <ViewEye, VideoCapture> grabber = new HashMap<ViewEye, VideoCapture>();
        for (ViewEye e : this.deviceNumber.keySet()) {
            if (!deviceFiles.containsKey(e)) {
                grabber.put(e, new VideoCapture((int)deviceNumber.get(e)));
                if (!grabber.get(e).isOpened()) {
                    System.out.println(String.format("Cannot open camera %s for eye %s", deviceNumber.get(e), e));
                    this.connected = false;
                    return;
                }
            } 
        }

        try {
            ServerSocket server = null;
            if (port != -1) {
                server = new ServerSocket(this.port);
                server.setSoTimeout(10);
            }

            while (!isInterrupted()) {
                    // See if someone wants to connect and stream...
                if (port != -1 && !connected)
                    try {
                        socket = server.accept();
                        this.connected = true;
                    } catch (SocketTimeoutException e) { ; }

                    // Try and grab each device as close as possible in time, then add pupils
                for (ViewEye e : this.frameBuffer.keySet())
                    if (deviceFiles.containsKey(e)) {
                        final File file = deviceFiles.get(e).poll();
                        deviceFiles.get(e).addLast(file);
                        frameBuffer.get(e).put(Errors.rethrow().wrap((FT f) -> f.grab(file)));
                    } else 
                        frameBuffer.get(e).put(Errors.rethrow().wrap((FT f) -> f.grab(grabber.get(e))));

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
     * @return Eye read. ViewEye.NONE for error.
     */
    public abstract ViewEye readBytes(Socket socket, byte []dst) throws IndexOutOfBoundsException;
}