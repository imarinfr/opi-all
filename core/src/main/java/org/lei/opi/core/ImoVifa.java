package org.lei.opi.core;

import java.util.HashMap;

import org.lei.opi.core.definitions.Packet;

import javafx.application.Platform;
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

     /**
     * opiInitialise: initialize OPI
     * Update GUI, call super.initialize().
     * @param args A map of name:value pairs for Params
     * @return A JSON object with machine specific initialise information
     * @since 0.2.0
     */
    public Packet initialize(HashMap<String, Object> args) {
        Platform.runLater(()-> {
            if (textAreaCommands != null)
                textAreaCommands.appendText("OPI Initialized");
        });    
            // Make sure screen = 1 and viewMode = "STEREO" is present for ImoVifa
        if (args == null)
            args = new HashMap<String, Object>(){{ put("screen", 1);}};
        else
            args.put("screen", 1);

        args.put("viewMode", "STEREO");
        return super.initialize(args);
    };
  
    /**
     * opiQuery: Query device
     * call super.query(), update GUI.
     * @return settings and state machine state
     * @since 0.2.0
     */
    public Packet query() { 
        Packet p = super.query();
        Platform.runLater(()-> {
            if (textAreaCommands != null)
                textAreaCommands.appendText(p.getMsg().toString());
        });
        return p;
    }
  
    /**
     * opiSetup: Change device background and overall settings
     * Update GUI, call super.setup() 
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet setup(HashMap<String, Object> args) {
        Platform.runLater(()-> {
            if (textAreaCommands != null) {
                this.textAreaCommands.appendText("Setup:\n");
                for (String k : args.keySet())
                    this.textAreaCommands.appendText(String.format("\t%s = %s\n", k, args.get(k).toString()));
            }
        });
        return super.setup(args);
    }
  
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * Update GUI, call super.persent() 
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet present(HashMap<String, Object> args) {
        updateGUIOnPresent(args);
        return super.present(args);
    }

    /**
   * opiClose: Update GUI, call super.close()
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
    public Packet close() {
        Platform.runLater(() -> {
            if (textAreaCommands != null) // allows testing without GUI
                textAreaCommands.appendText("Close received.\n");
        });
        returnToParentScene((Node)textAreaCommands);
        return super.close();
    }
  
    @FXML
    void initialize() {
        setupJavaFX("ImoVifa");
    }
}