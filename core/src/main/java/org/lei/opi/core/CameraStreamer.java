package org.lei.opi.core;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
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
    /** The device numbers of the cameras to stream on the machine on which this is running. */
    private int []deviceNumber; 

    /** The port number on this machine that will serve images */
    private int port; 

    /** The socket on which frames will be sent. */
    private Socket socket;

    /** Whether this streamer is connected to a client */
    public boolean connected;

    /** The data of an incoming request to the camera */
    static public record Request(
        long timeStamp,           // some timestamp of the request (used to match responses, perhaps)
        int deviceNumber         // 0 for all devices, otherwise the device number for which to get the response
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

    /**
     * Create a CameraStreamer that streams images from a camera on the local machine and sends them over UDP
     * @param port The port number on this machine that will serve images 
     * @param deviceNumber  Camera number on the local machine running the CameraStreamer
     * @throws IOException
     */
    public CameraStreamer(int port, int []deviceNumber) throws IOException {
        this.port = port;
        this.deviceNumber = deviceNumber;
        requestQueue = new LinkedBlockingQueue<Request>();
        responseQueue = new LinkedBlockingQueue<Response>();
        this.start();
    }
        
    @Override
    public void run() {
        OpenCVFrameGrabber []grabber = new OpenCVFrameGrabber[this.deviceNumber.length];
        try {
            for (int i = 0 ; i < this.deviceNumber.length; i++) {
                grabber[i] = new OpenCVFrameGrabber(this.deviceNumber[i]);
                grabber[i].start();
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            this.connected = false;
            return;
        }

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        try {
            ServerSocket server = new ServerSocket(this.port); //, 0, this.address);
            server.setSoTimeout(10);

            long []timestamp = new long[deviceNumber.length];
            Frame []frame = new Frame[deviceNumber.length];
            byte []bytes = new byte[1];
            while (!isInterrupted()) {
                    // See if someone wants to connect and stream...
                if (!connected)
                    try {
                        socket = server.accept();
                        this.connected = true;
                    } catch (SocketTimeoutException e) {
                        this.connected = false;
                    }

                    // Try and grab each device as close as possible in time
                for (int i = 0 ; i < this.deviceNumber.length; i++) {
                    frame[i] = grabber[i].grab();
                    timestamp[i] = System.currentTimeMillis();
                }

                Request request = requestQueue.poll();
                if (request != null) 
                    processRequest(request, frame, timestamp);

                    // And now send the frames on the socket
                for (int i = 0 ; i < this.deviceNumber.length; i++) {
//BufferedImage bi = jc.convert(frame[i]);
//System.out.println("bi type " + bi.getType());
//System.out.println("bi width " + bi.getWidth());
//System.out.println("bi height " + bi.getHeight());
                    if (frame[i].image != null) {
                        Mat con = converter.convert(frame[i]);
                        int n = con.channels() * con.rows() * con.cols();
                        if (n != bytes.length)
                            bytes = new byte[n];
                        con.data().get(bytes);
                            // 1 byte for device number
                            // 4 bytes for length of data, n
                            // n bytes of Mat
//System.out.println("Putting " + n + " bytes from camera " + i + " on socket.");
                        if (connected) {
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
                grabber[i].close();
        } catch (Exception e) { ; }
    }

    /**
     * Process a request from the client by finding the centre and diameter of the 
     * pupil in the current (relevant) frame(s) and putting the result on 
     * responseQueue.
     * 
     * @param request Details of device number(s) to process.
     */
    private void processRequest(Request request, Frame []frame, long []timestamp) {

        ArrayList<Integer> todo = new ArrayList<Integer>(Arrays.asList(request.deviceNumber));

        if (request.deviceNumber == 0) {
            todo.clear();
            for (int i = 0 ; i < this.deviceNumber.length; i++) 
                todo.add(i);
        }

        HashMap<String, Integer> res = new HashMap<String, Integer>(3);
        for (int i : todo) {
            getImageValues(frame[i], res);
            responseQueue.add(new Response(
                request.timeStamp,
                timestamp[i],
                res.get("x").intValue(),
                res.get("y").intValue(),
                res.get("d").intValue()
            ));
        }
    }
            
    /**
     * Process frame to find (x, y) and diameter of pupil and put the result in map values.
     * @param frame Frame to process looking for pupil
     * @param values Map object to update with (x,y) and diameter as `x`, `y`, and `d` respectively.
     */
    private void getImageValues(Frame frame, HashMap<String, Integer> values) {
        values.put("x", 66);
        values.put("y", 77);
        values.put("d", 22);
    }
}