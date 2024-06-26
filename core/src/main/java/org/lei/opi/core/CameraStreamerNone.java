package org.lei.opi.core;

import java.util.HashMap;
import java.io.IOException;
import java.net.Socket;
import java.util.ConcurrentModificationException;

import org.lei.opi.core.definitions.CircularBuffer;
import org.lei.opi.core.definitions.FrameInfo;
import org.lei.opi.core.definitions.FrameInfoNone;

import es.optocom.jovp.definitions.ViewEye;

public class CameraStreamerNone extends CameraStreamer<FrameInfoNone> {
    
    /** Weak instance just to allow calling of readBytes. Why not make it static? */
    public CameraStreamerNone() { ; }

    public CameraStreamerNone(int port, int deviceNumberLeft, int deviceNumberRight) throws IOException {
        super(port, deviceNumberLeft, deviceNumberRight);

        frameBuffer = new HashMap<ViewEye, CircularBuffer<FrameInfoNone>>(); 
        for (ViewEye e : this.deviceNumber.keySet()) 
            frameBuffer.put(e, new CircularBuffer<FrameInfoNone>(FrameInfoNone::new, 30));

        workingFrameInfo = new FrameInfoNone();
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
}

/* 
Type mismatch: cannot convert from 
org.lei.opi.core.HashMap<es.optocom.jovp.definitions.ViewEye,org.lei.opi.core.definitions.CircularBuffer<org.lei.opi.core.definitions.FrameInfoNone>> 
to java.util.HashMap    <es.optocom.jovp.definitions.ViewEye,org.lei.opi.core.definitions.CircularBuffer<org.lei.opi.core.definitions.FrameInfoNone>>

*/