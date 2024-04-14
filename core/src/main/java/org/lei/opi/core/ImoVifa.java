package org.lei.opi.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.lei.opi.core.definitions.Packet;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.Node;

/**
 * Opens up a window wherever the JOVP wants it
 */
public class ImoVifa extends Jovp {

    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI

    public ImoVifa(Scene parentScene) throws InstantiationException { 
        super(parentScene); 

        this.fxmlFileName = String.format("%s_%s.fxml", "stereo", "yes_tracking");
    }
    public ImoVifa(Scene parentScene, boolean connect) throws InstantiationException { 
        super(parentScene, connect); 

        this.fxmlFileName = String.format("%s_%s.fxml", "stereo", "yes_tracking");
    }

     /**
     * opiInitialise: initialize OPI
     * Update GUI, call super.initialize().
     * @param args A map of name:value pairs for Params
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

        return super.initialize(args);
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
        StringBuffer sb = new StringBuffer();
        sb.append("Setup:\n");
        for (String k : args.keySet())
            sb.append(String.format("\t%s = %s\n", k, args.get(k).toString()));
        output(sb.toString());
        return super.setup(args);
    }
 
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * Update GUI, call super.persent() 
     * @param args pairs of argument name and value
     * @return A packet containing a JSON object
     * @since 0.2.0
     */
    //TODO
    //@ReturnMsg(name = "eyex", className = Double.class, desc = "x co-ordinates of pupil at times eyet (degrees).")
    //@ReturnMsg(name = "eyey", className = Double.class, desc = "y co-ordinates of pupil at times eyet (degrees).")
    //@ReturnMsg(name = "eyed", className = Double.class, desc = "Diameter of pupil at times eyet (mm).")
    //@ReturnMsg(name = "eyet", className = Double.class, desc = "Time of (eyex, eyey) pupil from stimulus onset (ms).", min = 0)
    public Packet present(HashMap<String, Object> args) {
        updateGUIOnPresent(args);
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
        /*
        Thread t = new Thread() {
            public void run() { 
                int image_size = 640 * 480 * 1;
                byte [] data = new byte[image_size];
                DatagramPacket p = new DatagramPacket(data, data.length);

                int errorCount = 0;

                boolean isRunning = true;
                while (isRunning) {
                    try {
                        Thread.sleep(20);
                        udp_socket.receive(p);

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
        */
    }
}