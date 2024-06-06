package org.lei.opi.jovp;

import java.io.IOException;
import java.util.HashMap;

import org.lei.opi.core.CameraStreamer;

/**
 * Holds information that the opiJovp server needs about any local eye cameras
 * and the TCP socket that will stream their images.
 * 
 * @date 6 Jun 2024
 */
public class WebCamConfiguration {
    /** The port number on this machine that will serve images */
    private int port;
    /** Device number of left eye camera (or only eye camera if just one). */
    private int srcDeviceLeft;
    /** Device number of right eye camera (or -1 if there is no such device). */
    private int srcDeviceRight;

    /** CameraStreamer for left and right eyes. null if {@link use} is false */
    public CameraStreamer leftCS, rightCS;

    /**
     * Set up CameraStreamers {@link leftCS} and {@link rightCS} and set {@link use}.
     *
     * @param port The port number on this machine that will serve images. Use -1 for no streaming.
     * @param srcDeviceLeft Device number of left eye camera (or only eye camera if just one). 
     * @param srcDeviceRight Device number of right eye camera (or -1 if there is no such device). 
    */
    public WebCamConfiguration(int port, int srcDeviceLeft, int srcDeviceRight) {
        this.port = port;
        this.srcDeviceLeft = srcDeviceLeft;
        this.srcDeviceRight = srcDeviceRight;

        leftCS = null;
        rightCS = null;

        if (port == -1)
            return;

        try {
            leftCS = new CameraStreamer(port, srcDeviceLeft);
            if (srcDeviceRight != -1)
                rightCS = new CameraStreamer(port, srcDeviceRight);
        } catch(IOException e) {
            System.out.println("Could not start eye tracking cameras in OpiJovp.");
            e.printStackTrace();
        }
        System.out.println(this.toString());
    }

    /**
     * Create WebCamConfiguration object from a map of parameters.
     * If any of the params are missing, just return a non-functioning WebCamConfiguration object.
     * 
     * @param args OpiJovp key-value pairs for web cam configuration
     * @return new WebCamConfiguration object
     */
    public static WebCamConfiguration set(HashMap<String, Object> args) {
System.out.println("WebCam set()");
System.out.println(args.keySet());
        if (!args.containsKey("eyeStreamPort")
        ||  !args.containsKey("deviceNumberCameraLeft")
        ||  !args.containsKey("deviceNumberCameraRight"))
            return new WebCamConfiguration(-1, 0, 0);

        return new WebCamConfiguration(
          ((Double) args.get("eyeStreamPort")).intValue(),
          ((Double) args.get("deviceNumberCameraLeft")).intValue(), 
          ((Double) args.get("deviceNumberCameraRight")).intValue());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Webcam: ");
        if (port != -1)
            return sb.append(" port: ").append(port)
            .append(" srcDeviceLeft: ").append(srcDeviceLeft)
            .append(" srcDeviceRight: ").append(srcDeviceRight)
            .toString();
        else
            return "Webcam: not active";
    }
}