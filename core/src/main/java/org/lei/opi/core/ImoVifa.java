package org.lei.opi.core;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;

import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.ReturnMsg;

import es.optocom.jovp.Controller;
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
        settings.setPhysicalSize(new int[] {121, 68});
        settings.setScreen(1);
        settings.setViewMode("STEREO");

        args.put("check_input_com_port_exists", null); // ask opi-jovp to check input is a valid COM port

        return super.initialize(null);
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
        args.put("eyeStreamPortLeft", settings.eyeStreamPortLeft);
        args.put("eyeStreamPortRight", settings.eyeStreamPortRight);
        args.put("eyeStreamIP", OpiListener.obtainPublicAddress().getHostAddress().toString());
System.out.println(args.get("eyeStreamIP") + " " + args.get("eyeStreamPortLeft") + " " + args.get("eyeStreamPortRight") + " " + args.get("deviceNumberCameraLeft") + " " + args.get("deviceNumberCameraRight"));
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

        // Create a thread that will get UDP packets from udp_socket and put them in imageViewLeft
        Thread t = new Thread() {
            public void run() { 
                DatagramSocket []udp_socket = {null, null};
                int []ports = {settings.eyeStreamPortLeft, settings.eyeStreamPortRight};
                try {
                    for (int i = 0 ; i < udp_socket.length ; i++)
                        udp_socket[i] = new DatagramSocket(ports[i]);
                } catch (IOException e) {
                    System.out.print("Could not open UDP socket on ports:");
                    for (int i = 0 ; i < ports.length ; i++)
                        System.out.print(" " + ports[i]);
                    System.out.println("");
                    e.printStackTrace();
                    return;
                }

                int image_size = 640 * 480 * 1;
                byte [] data = new byte[image_size];
                DatagramPacket p = new DatagramPacket(data, data.length);

                int errorCount = 0;

                boolean isRunning = true;
                while (isRunning) {
                    try {
                        Thread.sleep(20);
                        udp_socket[0].receive(p);   // TODO both eyes

                        Image img = new Image(new ByteArrayInputStream(p.getData()));
                        Platform.runLater(() -> {
                            imageViewLeft.setImage(img);
                        });
                    } catch (InterruptedException e) {
                        isRunning = false;
                    } catch (IOException e) {
                        System.out.println("Error receiving UDP packet from ImoVifa camera");
                        e.printStackTrace();
                        errorCount++;
                        if (errorCount > 20) {
                            System.out.println("Received more than 20 errors from UDP socket: giving up.");
                            isRunning = false;
                        }
                    }
                }
            }
        };
        t.start();
        /*
        */
    }
}