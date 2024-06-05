package org.lei.opi.jovp;

import java.io.IOException;
import java.util.HashMap;

import org.lei.opi.core.CameraStreamer;

public class WebCamConfiguration {
    /** Destination IP address to which eye images are streamed. No streaming if empty string (default). */
    private String destIP;
    /** Destination UDP Port to which left eye images are streamed. */
    private int destPortLeft;
    /** Destination UDP Port to which right eye images are streamed. */
    private int destPortRight;
    /** Device number of left eye camera (or only eye camera if just one). */
    private int srcDeviceLeft;
    /** Device number of right eye camera (or -1 if there is no such device). */
    private int srcDeviceRight;

    /** active True if streaming should be activated, false otherwise*/
    public boolean use;

    /** CameraStreamer for left and right eyes. null if {@link use} is false */
    public CameraStreamer leftCS, rightCS;

    /**
     * Set up CameraStreamers {@link leftCS} and {@link rightCS} and set {@link use}.
     *
     * @param destIP Destination IP address to which eye images are streamed. No streaming if empty string.
     * @param destPortLeft  Destination UDP Port to which left eye images are streamed. 
     * @param destPortRight Destination UDP Port to which right eye images are streamed. 
     * @param srcDeviceLeft Device number of left eye camera (or only eye camera if just one). 
     * @param srcDeviceRight Device number of right eye camera (or -1 if there is no such device). 
    */
    public WebCamConfiguration(String destIP, int destPortLeft, int destPortRight, int srcDeviceLeft, int srcDeviceRight) {
        this.destIP = destIP;
        this.destPortLeft = destPortLeft;
        this.destPortRight = destPortRight;
        this.srcDeviceLeft = srcDeviceLeft;
        this.srcDeviceRight = srcDeviceRight;

        this.use = !destIP.equals("");

        leftCS = null;
        rightCS = null;
        if (this.use)
            try {
                leftCS = new CameraStreamer(destIP, destPortLeft, srcDeviceLeft);
                if (srcDeviceRight != -1)
                    rightCS = new CameraStreamer(destIP, destPortRight, srcDeviceRight);
            } catch(IOException e) {
                System.out.println("Could not start eye tracking cameras in OpiJovp.");
                this.use = false;
            }
    }

    /**
     * Create WebCamConfiguration object from a map of parameters.
     * If any of the params are missing, just return a non-functioning WebCamConfiguration object.
     * 
     * @param args OpiJovp key-value pairs for web cam configuration
     * @return new WebCamConfiguration object
     */
    public static WebCamConfiguration set(HashMap<String, Object> args) {
        if (!args.containsKey("eyeStreamIP")
        ||  !args.containsKey("eyeStreamPortLeft")
        ||  !args.containsKey("eyeStreamPortRight")
        ||  !args.containsKey("srcDeviceLeft")
        ||  !args.containsKey("srcDeviceRight"))
            return new WebCamConfiguration("", 0, 0, 0, 0);

        return new WebCamConfiguration(
          args.get("eyeStreamIP").toString(), 
          ((Double) args.get("eyeStreamPortLeft")).intValue(),
          ((Double) args.get("eyeStreamPortRight")).intValue(),
          ((Double) args.get("srcDeviceLeft")).intValue(), 
          ((Double) args.get("srcDeviceRight")).intValue());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Webcam: ");
        if (this.use)
            return sb.append("destIP: ").append(destIP)
            .append("destPortLeft: ").append(destPortLeft)
            .append("destPortRight: ").append(destPortRight)
            .append("srcDeviceLeft: ").append(srcDeviceLeft)
            .append("srcDeviceRight: ").append(srcDeviceRight)
            .toString();
        else
            return "Webcam: not active";

    }
}