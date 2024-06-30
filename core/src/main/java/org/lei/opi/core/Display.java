package org.lei.opi.core;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import org.lei.opi.core.definitions.Packet;

import es.optocom.jovp.definitions.ViewEye;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.Node;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Opens up a window wherever the JOVP wants it
 */
public class Display extends Jovp {
    
    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI

    public Display(Scene parentScene) throws InstantiationException { 
        super(parentScene); 
    }
    public Display(Scene parentScene, boolean connect) throws InstantiationException { 
        super(parentScene, connect); 
    }

     /**
     * opiInitialise: initialize OPI
     * Update GUI if parentScene != null, call super.initialize().
     * @param args A map of name:value pairs for Params
     * @return A JSON object with machine specific initialise information
     * @since 0.2.0
     */
    public Packet initialize(HashMap<String, Object> args) {
        if (parentScene != null)
            Platform.runLater(()-> {
                textAreaCommands.appendText("OPI Initialized");
            });

        settings.machine = "Display";

        return super.initialize(args);
    };
  
    /**
     * opiQuery: Query device
     * call super.query(), update GUI if parentScene != null.
     * @return settings and state machine state
     * @since 0.2.0
     */
    public Packet query() { 
        Packet p = super.query();
        if (parentScene != null)
            Platform.runLater(()-> { textAreaCommands.appendText(p.getMsg()); });
        return p;
    }
  
    /**
     * opiSetup: Change device background and overall settings
     * Update GUI if parentScene != null, call super.setup() 
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet setup(HashMap<String, Object> args) {
        if (parentScene != null)
            Platform.runLater(()-> {
                this.textAreaCommands.appendText("Setup:\n");
                for (String k : args.keySet())
                    this.textAreaCommands.appendText(String.format("\t%s = %s\n", k, args.get(k).toString()));
             });
        return super.setup(args);
    }
  
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * Update GUI if parentScene != null, call super.persent() 
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet present(HashMap<String, Object> args) {
        updateGUIOnPresent(args);
        return super.present(args);
    }

    /**
   * opiClose: Update GUI if parentScene != null, call super.close()
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
    public Packet close() {
        if (parentScene != null) { // allows testing without GUI
            Platform.runLater(() -> {
                textAreaCommands.appendText("Close received.\n");
            });
            returnToParentScene((Node)textAreaCommands);
        }
        return super.close();
    }
  
    //-------------- Machine Specific FXML below here ---
    @Override
    @FXML
    void initialize() {
        setupJavaFX("Display");

        // Create a thread that will get images from server and put them in imageViewLeft
        Thread t = new Thread() {
            public void run() { 
                int port = settings.eyeStreamPort;
                if (port == -1)
                    return;
                
                CameraStreamerImo csImo = new CameraStreamerImo(); // just for readBytes()
                Socket socket = null;

                    // Wait for the server to be initialised
                while (socket == null) {
                    try {
                        Thread.sleep(1000);
                        socket = new Socket(settings.ip, port);
                    } catch (IOException e) {
                        System.out.println("Monitor is waiting for opiInitialise to open up eye camera socket on server on port: " + port);
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                BufferedImage im = new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
                byte[] im_array = ((DataBufferByte) im.getRaster().getDataBuffer()).getData();
                boolean isRunning = true;
                while (isRunning) {
                    try {
                        Thread.sleep(20);
                        ViewEye eye = csImo.readBytes(socket, im_array);

                        if (eye == ViewEye.NONE) {
                            Thread.sleep(100);  // try again soon 
                            //isRunning = false;
                            //break;
                            continue;
                        }

                        Image img = SwingFXUtils.toFXImage(im, null);

                        Platform.runLater(() -> {
                            if (eye == ViewEye.LEFT)           // TODO need to allow for mono
                                imageViewLeft.setImage(img);
                            else 
                                imageViewRight.setImage(img);
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        isRunning = false;
                    }
                }
            }
        };
        t.start();
    }
}