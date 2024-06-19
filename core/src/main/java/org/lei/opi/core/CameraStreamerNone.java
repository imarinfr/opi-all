package org.lei.opi.core;

import java.io.IOException;
import java.net.Socket;
import java.util.ConcurrentModificationException;

import org.lei.opi.core.definitions.FrameInfo;
import org.opencv.core.Mat;

import es.optocom.jovp.definitions.ViewEye;

public class CameraStreamerNone extends CameraStreamer {
    
    /** Weak instance just to allow calling of readBytes. Why not make it static? */
    public CameraStreamerNone() { ; }

    public CameraStreamerNone(int port, int deviceNumberLeft, int deviceNumberRight) throws IOException {
        super(port, deviceNumberLeft, deviceNumberRight);
        pupilInfo = new PupilInfo(0, 0);
    }

    /**
     * Fill bytes with the image on socket. Assumes it has been written with writeBytes
     * @param socket An open socket from which to read
     * @return Device number read. -1 for error
     */
    public ViewEye readBytes(Socket socket, byte []dst) { return ViewEye.NONE; }

    /**
     * Write static bytes array out on socket as 
     *       1 byte for device number
     *       4 bytes for length of data, n
     *       n bytes
     *
     * @param socket Open socket on which to write bytes
     * @param deviceNumber To write before bytes
     * @throws IOException
     * @throws ConcurrentModificationException You should CameraStreamerImo.bytesLock.lock() before calling this.
     */
    public void writeBytes(Socket socket, ViewEye eye, FrameInfo frame) { return; }

    // Process frame to find (x, y) and diameter of pupil and put the result in {@link pupilInfo}.
    protected void getImageValues(Mat frame) { pupilInfo.valid = false; }
}