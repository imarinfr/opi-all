package org.lei.opi.core;

import java.io.IOException;
import java.net.Socket;
import java.util.ConcurrentModificationException;

import org.opencv.core.Mat;

public class CameraStreamerNone extends CameraStreamer {
    
    /** Weak instance just to allow calling of readBytes. Why not make it static? */
    public CameraStreamerNone() { ; }

    public CameraStreamerNone(int port, int []deviceNumber) throws IOException {
        super(port, deviceNumber);
    }

    protected void getImageValues(Mat frame) {; }

    /**
     * Fill bytes with the image on socket. Assumes it has been written with writeBytes
     * @param socket An open socket from which to read
     * @return Device number read. -1 for error
     */
    public int readBytes(Socket socket) { return -1; }

    /**
     * Write static bytes array out on socket as 
     *       1 byte for device number
     *       4 bytes for length of data, n
     *       n bytes
     *
     * @param socket Open socket on which to write byets
     * @param deviceNumber To write before bytes
     * @throws IOException
     * @throws ConcurrentModificationException You should CameraStreamerImo.bytesLock.lock() before calling this.
     */
    public void writeBytes(Socket socket, int deviceNumber) throws IOException, ConcurrentModificationException {
        return;
    }
    

}