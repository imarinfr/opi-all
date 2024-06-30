package org.lei.opi.core;

import java.io.IOException;
import java.net.Socket;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

import org.lei.opi.core.definitions.FrameInfo;
import org.lei.opi.core.definitions.FrameInfoImo;
import org.lei.opi.core.definitions.CircularBuffer;

import es.optocom.jovp.definitions.ViewEye;


/*
 * Contains custom pupil detection code using some constants from Csharp code supplied by CREWt 16 March 2023.
 */
public class CameraStreamerImo extends CameraStreamer<FrameInfoImo> {
    private static final int CODE_LEFT = 0;
    private static final int CODE_RIGHT = 1;

    /** Working area for {@link writeBytes} */
    private byte []bytes = new byte[FrameInfoImo.EYE_IMAGE_HEIGHT * FrameInfoImo.EYE_IMAGE_WIDTH * 3];

    /** Weak instance just to allow calling of readBytes. Why not make it static? */
    public CameraStreamerImo() { ; }

    public CameraStreamerImo(int port, int deviceNumberLeft, int deviceNumberRight) throws IOException {
        super(port, deviceNumberLeft, deviceNumberRight);
        setup();
    }

    public CameraStreamerImo(int port, String deviceFolderLeft, String deviceFolderRight) throws IOException {
        super(port, deviceFolderLeft, deviceFolderRight);
        setup();
    }

    private void setup() {
        frameBuffer = new HashMap<ViewEye, CircularBuffer<FrameInfoImo>>(); 
        for (ViewEye e : this.deviceNumber.keySet()) 
            frameBuffer.put(e, new CircularBuffer<FrameInfoImo>(FrameInfoImo::new, 30));

        workingFrameInfo = new FrameInfoImo();
    }

    /**
     * Fill bytes with the image on socket. Assumes it has been written with writeBytes
     * @param socket An open socket from which to read
     * @return Eye read (NONE for error)
     */
    public ViewEye readBytes(Socket socket, byte []dst) throws IndexOutOfBoundsException {
        ViewEye eye = ViewEye.NONE;
        try {
            int eyeCode = (int)socket.getInputStream().read();
            if (eyeCode < 0)
                return eye;
            eye = eyeCode == CODE_LEFT ? ViewEye.LEFT : ViewEye.RIGHT;

            int n1 = (int)socket.getInputStream().read();
            int n2 = (int)socket.getInputStream().read();
            int n3 = (int)socket.getInputStream().read();
            int n4 = (int)socket.getInputStream().read();
            int n = (n1 << 24) | (n2 << 16) | (n3 << 8) | n4;

            if (dst.length != n)
                throw new IndexOutOfBoundsException("CameraStreamerImo readBytes needs a bigger destination.");

            int off = 0; // current start of buffer (offset)
            while (off < n) {
                int readN = socket.getInputStream().read(dst, off, n - off);
                off += readN;
            }
        } catch(IOException e) {
            System.out.println("CameraStreamerImo readBytes: trouble reading socket");
            e.printStackTrace();
        }
        return eye;
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
     * @throws ConcurrentModificationException You should CameraStreamerImo.bytesLock.lock() before calling this.
     */
    public void writeBytes(Socket socket, ViewEye eye, FrameInfo frame) {
        int n = frame.mat().channels() * frame.mat().rows() * frame.mat().cols();
        if (n != bytes.length)
            bytes = new byte[n];
        frame.mat().get(0, 0, this.bytes);

        try {
             socket.getOutputStream().write(eye == ViewEye.LEFT ? CODE_LEFT : CODE_RIGHT);
             socket.getOutputStream().write(n >> 24);
             socket.getOutputStream().write((n >> 16) & 0xFF);
             socket.getOutputStream().write((n >>  8) & 0xFF);
             socket.getOutputStream().write( n        & 0xFF);
             socket.getOutputStream().write(bytes);
        } catch (IOException e) {
            System.out.println("Error writing eye image bytes to socket");
            e.printStackTrace();
        }
    }
}