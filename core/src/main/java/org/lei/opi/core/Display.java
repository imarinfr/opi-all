package org.lei.opi.core;

import java.util.HashMap;

import org.lei.opi.core.definitions.Packet;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.Node;

/**
 * Opens up a window wherever the JOVP wants it
 */
public class Display extends Jovp {
    
    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI

    public Display(Scene parentScene) throws InstantiationException { 
        super(parentScene); 
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
    }
}