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

/**
 * Create a thread that streams raw images from one or more "webcams".
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

    /** Whether it is connected to a client */
    private boolean connected;

    /**
     * Create a CameraStreamer that streams images from a camera on the local machine and sends them over UDP
     * @param port The port number on this machine that will serve images 
     * @param deviceNumber  Camera number on the local machine running the CameraStreamer
     * @throws IOException
     */
    public CameraStreamer(int port, int []deviceNumber) throws IOException {
        this.port = port;
        this.deviceNumber = deviceNumber;
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
            return;
        }

        int current_device = 0; // rotate through each device in turn

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        try {
            ServerSocket server = new ServerSocket(this.port); //, 0, this.address);
            socket = server.accept();
            this.connected = true;

            while (this.connected) {
                Frame frame = grabber[current_device].grab();
                if (frame.image != null) {
                    Mat con = converter.convert(frame);
                    int n = con.channels() * con.rows() * con.cols();
                    byte []bytes = new byte[n];
                    con.data().get(bytes);
                        // 1 byte for device numebr
                        // 4 bytes for length of data, n
                        // n bytes of Mat
                    socket.getOutputStream().write(current_device);
                    socket.getOutputStream().write(n >> 24);
                    socket.getOutputStream().write((n >> 16) & 0xFF);
                    socket.getOutputStream().write((n >>  8) & 0xFF);
                    socket.getOutputStream().write( n        & 0xFF);
                    socket.getOutputStream().write(bytes);
                }
                Thread.sleep(50);
                current_device = (current_device + 1) % this.deviceNumber.length;
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
}