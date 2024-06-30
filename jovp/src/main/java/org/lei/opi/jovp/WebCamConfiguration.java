package org.lei.opi.jovp;

import java.io.IOException;
import java.util.HashMap;

import org.lei.opi.core.CameraStreamerImo;
import org.lei.opi.core.CameraStreamerNone;
import org.lei.opi.core.definitions.FrameInfo;
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
    /** Device number of left eye camera (or only eye camera if just one). Ignored if {@link srcFolderLeft} is not null.*/
    public int srcDeviceLeft;
    /** Device number of right eye camera (or -1 if there is no such device). Ignored if {@link srcFolderRight} is not null.*/
    public int srcDeviceRight;

    /** Folder name of images for fake left device. */
    public String srcFolderLeft;
    /** Folder name of images for fake right device. */
    public String srcFolderRight;

    /** CameraStreamer for left and right eyes. null if {@link use} is false */
    public CameraStreamer<? extends FrameInfo> cameraStreamer;

    /**
     * Set up a cameraStreamers {@link leftCS} and {@link rightCS} and set {@link use}.
     *
     * @param port The port number on this machine that will serve images. Use -1 for no streaming.
     * @param srcDeviceLeft Device number of left eye camera (or only eye camera if just one). 
     * @param srcDeviceRight Device number of right eye camera (or -1 if there is no such device). 
     * @param machine Machine name for which to start cameras.
    */
    public WebCamConfiguration(int port, int srcDeviceLeft, int srcDeviceRight, String machine) {
        this.port = port;
        this.srcDeviceLeft = srcDeviceLeft;
        this.srcDeviceRight = srcDeviceRight;
        this.srcFolderLeft = null;
        this.srcFolderRight = null;

        try {
            if (machine.toLowerCase().equals("imovifa"))
                cameraStreamer = new CameraStreamerImo(port, srcDeviceLeft, srcDeviceRight);
            else
                cameraStreamer = new CameraStreamerNone(port, srcDeviceLeft, srcDeviceLeft);
        } catch(IOException e) {
            System.out.println("Could not start eye tracking cameras in OpiJovp.");
            e.printStackTrace();
        }
        System.out.println(this.toString());
    }

    /**
     * Set up CameraStreamers {@link leftCS} and {@link rightCS} and set {@link use}.
     *
     * @param port The port number on this machine that will serve images. Use -1 for no streaming.
     * @param srcDeviceLeft Device number of left eye camera (or only eye camera if just one). 
     * @param srcDeviceRight Device number of right eye camera (or -1 if there is no such device). 
     * @param machine Machine name for which to start cameras.
    */
    public WebCamConfiguration(int port, String srcDeviceLeft, String srcDeviceRight, String machine) {
        this.port = port;
        this.srcFolderLeft = srcDeviceLeft;
        this.srcFolderRight = srcDeviceRight;

        try {
            if (machine.toLowerCase().equals("imovifa") || machine.toLowerCase().equals("display"))
                cameraStreamer = new CameraStreamerImo(port, srcDeviceLeft, srcDeviceRight);
            else
                cameraStreamer = new CameraStreamerNone(port, srcDeviceLeft, srcDeviceLeft);
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
        if (!args.containsKey("eyeStreamPort")
        ||  !args.containsKey("deviceNumberCameraLeft")
        ||  !args.containsKey("deviceNumberCameraRight")
        ||  !args.containsKey("machine"))
            return new WebCamConfiguration(-1, 0, 0, null);

        try {
            return new WebCamConfiguration(
                ((Double) args.get("eyeStreamPort")).intValue(),
                Integer.parseInt((String)args.get("deviceNumberCameraLeft")), 
                Integer.parseInt((String)args.get("deviceNumberCameraRight")),
                args.get("machine").toString());
        } catch (NumberFormatException e) {  // device numbers are folder names of images 
            return new WebCamConfiguration(
            ((Double) args.get("eyeStreamPort")).intValue(),
            (String)args.get("deviceNumberCameraLeft"),
            (String)args.get("deviceNumberCameraRight"),
            args.get("machine").toString());
        }
    }

    public String toString() {
        StringBuilder sb;
        if (srcFolderLeft != null)
            sb = new StringBuilder("Fake-Webcam: ")
            .append(" port: ").append(port)
            .append(" srcFolderLeft: ").append(srcFolderLeft)
            .append(" srcFolderRight: ").append(srcFolderRight);
        else
            sb = new StringBuilder("Webcam(s): ")
            .append(" port: ").append(port)
            .append(" srcDeviceLeft: ").append(srcDeviceLeft)
            .append(" srcDeviceRight: ").append(srcDeviceRight);

        return sb.toString();
    }
}