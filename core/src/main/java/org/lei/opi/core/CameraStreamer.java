package org.lei.opi.core;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;

/**
 * Create a thread that streams base64 encoded images from a "webcam".
 * on a given UDP port.
 *
 * @author Andrew Turpin
 * @date 5 June 2024 
 */
public class CameraStreamer extends Thread {
    /** The device number of the camera to stream on the machine on which this is running. */
    private int deviceNumber; 
    /** The port number on this machine that will serve images */
    private int port; 

    /** The socket on which frames will be sent. */
    private Socket socket;
        /** Writer for outgoing messages to the socket */
    PrintWriter outgoing;
    /** Whether it is connected to a client */

    /** true if should be connected, false otherwise */
    private boolean connected;

    /**
     * Create a CameraStreamer that streams images from a camera on the local machine and sends them over UDP
     * @param port The port number on this machine that will serve images 
     * @param deviceNumber  Camera number on the local machine running the CameraStreamer
     * @throws IOException
     */
    public CameraStreamer(int port, int deviceNumber) throws IOException {
        this.port = port;
        this.deviceNumber = deviceNumber;
        this.start();
    }

    @Override
    public void run() {
        boolean done = false;

        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(this.deviceNumber);
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            done = true;
        }

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        try {
            ServerSocket server = new ServerSocket(this.port); //, 0, this.address);
            socket = server.accept();
            this.connected = true;

            while (!done && this.connected) {
                Frame frame = grabber.grab();
                if (frame.image != null) {
                    Mat con = converter.convert(frame);
                    int n = con.channels() * con.rows() * con.cols();
                    byte []bytes = new byte[n];
                    con.data().get(bytes);
                    socket.getOutputStream().write(this.deviceNumber);
                    socket.getOutputStream().write(n >> 24);
                    socket.getOutputStream().write((n >> 16) & 0xFF);
                    socket.getOutputStream().write((n >>  8) & 0xFF);
                    socket.getOutputStream().write( n        & 0xFF);
                    socket.getOutputStream().write(bytes);
                }
                Thread.sleep(50);
            }
            server.close();
            grabber.close();
        } catch (IOException e) {
            e.printStackTrace();
            done = true;
        } catch (InterruptedException e) {
            done = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}