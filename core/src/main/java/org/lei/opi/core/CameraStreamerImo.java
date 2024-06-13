package org.lei.opi.core;

import java.io.IOException;

import org.bytedeco.javacv.Frame;

public class CameraStreamerImo extends CameraStreamer {
    
    public CameraStreamerImo(int port, int []deviceNumber) throws IOException {
        super(port, deviceNumber);
    }

    protected void getImageValues(Frame frame) {
        processingResults.put("x", 66);
        processingResults.put("y", 77);
        processingResults.put("d", 22);
    }
}
