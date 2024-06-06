package org.lei.opi.core;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.Socket;
import java.util.HashMap;

import org.bytedeco.librealsense.device;
import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.ReturnMsg;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.Node;

/**
 * Opens up a window wherever the JOVP wants it
 */
public class ImoVifa extends Jovp {

    public static class Settings extends Jovp.Settings { }  // here to trick GUI

    public ImoVifa(Scene parentScene) throws InstantiationException { 
        super(parentScene); 

        this.fxmlFileName = String.format("%s_%s.fxml", "stereo", "yes_tracking");
    }
    public ImoVifa(Scene parentScene, boolean connect) throws InstantiationException { 
        super(parentScene, connect); 

        this.fxmlFileName = String.format("%s_%s.fxml", "stereo", "yes_tracking");
    }

     /**
     * opiQueryDevice: initialize OPI
     * Update GUI, call super.initialize().
     * @param args A map of name:value pairs for Params (ignored)
     * @return A Package containing a JSON object with machine specific initialise information
     * @since 0.2.0
     */
    public Packet initialize(HashMap<String, Object> args) {
        output("OPI Monitor: OPI Initialized");

            // Make sure screen = 1 and viewMode = "STEREO" is present for ImoVifa
            // Also it seems the monitor does not report it's correct size...
        settings.machine = "imoVifa";
        settings.setPhysicalSize(new int[] {121, 68});
        settings.setScreen(1);
        settings.setViewMode("STEREO");

        return super.initialize(null);  // super uses settings, not args! why!???
    };
  
    /**
     * opiQuery: Query device
     * call super.query(), update GUI.
     * @return A packet containing a JSON object describing settings and machine state
     * @since 0.2.0
     */
    public Packet query() { 
        Packet p = super.query();
        output("OPI Query result\n" + p.getMsg().toString());
        return p;
    }
  
    /**
     * opiSetup: Change device background and overall settings
     * Update GUI, call super.setup() 
     * @param args pairs of argument name and value
     * @return A packet containing a JSON object as for `query()`
     * @since 0.2.0
     */
    public Packet setup(HashMap<String, Object> args) {
            // add in the device numbers of the left and right eye cameras
            // and my address and ports for streaming eye images
        args.put("deviceNumberCameraLeft", settings.deviceNumberCameraLeft);
        args.put("deviceNumberCameraRight", settings.deviceNumberCameraLeft);
        args.put("eyeStreamPort", settings.eyeStreamPort);

        StringBuffer sb = new StringBuffer();
        sb.append("Setup:\n");
        for (String k : args.keySet())
            sb.append(String.format("\t%s = %s\n", k, args.get(k).toString()));
        output(sb.toString());
        
        return super.setup(args);
    }
 
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * Update GUI, call super.present() 
     * @param args pairs of argument name and value
     * @return A packet containing a JSON object
     * @since 0.2.0
     */
    //TODO
    //@ReturnMsg(name = "eyed", className = Double.class, desc = "Diameter of pupil at times eyet (mm).")
    //@ReturnMsg(name = "eyet", className = Double.class, desc = "Time of (eyex, eyey) pupil from stimulus onset (ms).", min = 0)
    @ReturnMsg(name = "eyex", className = Double.class, desc = "x co-ordinates of pupil at ??? (degrees).")
    @ReturnMsg(name = "eyey", className = Double.class, desc = "y co-ordinates of pupil at ??? (degrees).")
    public Packet present(HashMap<String, Object> args) {
        updateGUIOnPresent(args);

        //System.out.println(img.getWidth() + " x " + img.getHeight());
        //args.put("units", new ArrayList<String>(Arrays.asList(new String[] {"ANGLES"})));
        //double sx[] = Jovp.toDoubleArray(args.get("sx"));
        //double sy[] = Jovp.toDoubleArray(args.get("sy"));
        //double x[] = Jovp.toDoubleArray(args.get("x"));
        //double y[] = Jovp.toDoubleArray(args.get("y"));
        //args.put("units", new ArrayList<String>(Arrays.asList(new String[] {"PIXELS"})));
        //args.put("sx", new ArrayList<Double>(Arrays.stream(sx).map(d -> 18.34 * d).boxed().collect(Collectors.toList())));
        //args.put("sy", new ArrayList<Double>(Arrays.stream(sy).map(d -> 18.34 * d).boxed().collect(Collectors.toList())));
        //args.put("x", new ArrayList<Double>(Arrays.stream(x).map(d -> 18.34 * d).boxed().collect(Collectors.toList())));
        //args.put("y", new ArrayList<Double>(Arrays.stream(y).map(d -> 18.34 * d).boxed().collect(Collectors.toList())));
        return super.present(args);
    }

    /**
   * opiClose: Update GUI, call super.close()
   * 
   * @param args pairs of argument name and value
   *
   * @return A Packet containing JSON object with return messages
   *
   * @since 0.0.1
   */
    public Packet close() {
        output("OPI Monitor: Close received.\n");
        returnToParentScene((Node)textAreaCommands);

        return super.close();
    }

 //--------------- FXML stuff
    
    @FXML
    void initialize() {
        setupJavaFX("ImoVifa");

        File file = new File("imo_eye_OS.png");
        Image image = new Image(file.toURI().toString());
        imageViewLeft.setImage(image);
        file = new File("imo_eye_OD.png");
        image = new Image(file.toURI().toString());
        imageViewRight.setImage(image);

        // Create a thread that will get images from server and put them in imageViewLeft
        Thread t = new Thread() {
            public void run() { 
                int port = settings.eyeStreamPort;
                if (port == -1)
                    return;
                
                Socket socket = null;

                    // we have to wait for the server to be initialised
                while (socket == null) {
                    try {
                        Thread.sleep(1000);
                        socket = new Socket(settings.ip, port);
                    } catch (IOException e) {
                        System.out.println("Waiting for eye camera socket on server on port: " + port);
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                byte []data = new byte[1024];

                boolean isRunning = true;
                while (isRunning) {
                    try {
                        Thread.sleep(20);
                        int deviceNum = (int)socket.getInputStream().read();
                        if (deviceNum < 0) {
                            isRunning = false;
                            break;
                        }
                        int n1 = (int)socket.getInputStream().read();
                        int n2 = (int)socket.getInputStream().read();
                        int n3 = (int)socket.getInputStream().read();
                        int n4 = (int)socket.getInputStream().read();
                        int n = (n1 << 24) | (n2 << 16) | (n3 << 8) | n4;
System.out.println("Looking for " + n + " bytes from camera.");

                        if (data.length != n)
                            data = new byte[n];

                        socket.getInputStream().read(data);
                        //Image img = new Image(socket.getInputStream());  will this work?

                        Image img = new Image(new ByteArrayInputStream(data));
                        Platform.runLater(() -> {
                            if (deviceNum == 0)                 // TODO need to allow for mono
                                imageViewLeft.setImage(img);
                            else 
                                imageViewRight.setImage(img);
                        });
                    } catch (InterruptedException e) {
                        isRunning = false;
                    } catch (IOException e) {
                        System.out.println("Error receiving images from ImoVifa camera");
                        e.printStackTrace();
                        isRunning = false;
                    }
                }
            }
        };
        t.start();
    }
}