package org.lei.opi.core;

import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.opencv_core.Mat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
    /** The destination ipAddress of machine to receive the images - we send to this. */
    private InetAddress ipAddress; 
    /** The destination udp port number on the target machine - we send to this. */
    private int udpPortNumber; 

    /** The socket on which frames will be sent. */
    private DatagramSocket socket;

    /**
     * Create a CameraStreamer that streams images from a camera on the local machine and sends them over UDP
     * @param ipAddress Destination IP Address of the target machine that will receive images
     * @param udpPortNumber Destination UDP port number on the target machine that will receive images
     * @param deviceNumber  Camera number on the local machine running the CameraStreamer
     * @throws IOException
     */
    public CameraStreamer(String ipAddress, int udpPortNumber, int deviceNumber) throws IOException {
        this.ipAddress = InetAddress.getByName(ipAddress);
        this.udpPortNumber = udpPortNumber;
        this.deviceNumber = deviceNumber;

        try {
            this.socket = new DatagramSocket();
        } catch (IOException e) {
            System.out.println("Could not open socket on " + udpPortNumber);
            throw(e);
        }

        this.start();
    }

    @Override
    public void run() {
        boolean done = false;

        Mat image = new Mat();
        VideoCapture capturer = new VideoCapture(this.deviceNumber);

        if (capturer.isOpened()) {
            while (!done) {
                try {
                    if (capturer.read(image)) {
                        byte []bytes = image.data().asByteBuffer().array();
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, this.ipAddress, this.udpPortNumber);

                        //String encoded = Base64.getEncoder().encodeToString(bytes);
                        //byte []eBytes = encoded.getBytes();
                        //DatagramPacket packet = new DatagramPacket(eBytes, eBytes.length, this.ipAddress, this.udpPortNumber);

                        try {
                            socket.send(packet);
                        } catch (IOException e) {
                            System.out.println("Error trying to send packet in CameraStreamer for device " + this.deviceNumber);
                            e.printStackTrace();
                        }
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    done = true;
                }
            }
            capturer.close();
        }
    }
}