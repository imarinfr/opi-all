package org.lei.opi.core;

/* 
 * A simple machine that just echos commands coming in from the client.
 * It does not establish a connection to any Machine.
 *
 * @since 0.2.0
 */
import java.util.HashMap;

import org.lei.opi.core.definitions.Parameter;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.Node;

public class Echo extends OpiMachine {
 
    public static class Settings extends OpiMachine.Settings { ; }  // keeps GUI happy
    private Settings settings;
    public Settings getSettings() { return this.settings; }

    /*
     * @param parentScene The Scene to return to when this object is closed.
     *                    If null, then do not create a connection. (Used for GUI to probe class.)
     */
    public Echo (Scene parentScene) {
        super(parentScene);
        this.settings = (Settings) OpiMachine.fillSettings(this.getClass().getSimpleName());
        this.parentScene = parentScene;
    }
  
    void myPrint(String s) {
        if (textArea != null)
            this.textArea.appendText(s);
        else
            System.out.println("Echo: " + s);
    }

    /**
     * opiInitialise: initialize OPI
     * @param args A map of name:value pairs for Params
     * @return A JSON object with machine specific initialise information
     * @since 0.2.0
     */
    public Packet initialize(HashMap<String, Object> args) {
        myPrint("Initialise:\n");
        for (String k : args.keySet())
            myPrint(String.format("\t%s = %s\n", k, args.get(k).toString()));

      return new Packet(String.format(CONNECTED_TO_HOST, settings.ip, settings.port));
    };
  
    /**
     * opiQuery: Query device
     * @return settings and state machine state
     * @since 0.2.0
     */
    public Packet query() { 
        myPrint("Query:\n\tNothing to report.\n");
        return new Packet("Query: Nothing to report"); 
    }
  
    /**
     * opiSetup: Change device background and overall settings
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    @Parameter(name = "ignored", desc = "This is here just so Echo setup() has at least one parameter.", defaultValue = "irrelevant", optional = true)
    public Packet setup(HashMap<String, Object> args) {
       this.textArea.appendText("Setup:\n");
       for (String k : args.keySet())
            myPrint(String.format("\t%s = %s\n", k, args.get(k).toString()));

        return new Packet(OpiListener.gson.toJson(args));
    }
  
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    @Parameter(name = "ignored", desc = "This is here just so Echo present() has at least one parameter.", defaultValue = "irrelevant", optional = true)
    public Packet present(HashMap<String, Object> args) {
        myPrint("Present:\n");
        for (String k : args.keySet())
            myPrint(String.format("\t%s = %s\n", k, args.get(k).toString()));

        return new Packet(OpiListener.gson.toJson(args));
    }
  
    /**
     * opiClose: Close OPI connection
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet close() {
        myPrint("Close:\n");
        if (textArea != null)
            returnToParentScene((Node)textArea);
        return new Packet(true, "Got OPI_CLOSE so closing connection.");
    };

// --------------- FXML after here ----------------------------------- 

    @FXML
    private Button btnReturnToMain;

    @FXML
    private TextArea textArea;

    /*
     * @param event
     */
    @FXML
    void actionBtnReturnToMain(ActionEvent event) {
        returnToParentScene((Node)event.getSource());
    }

    @FXML
    void initialize() {
        assert btnReturnToMain != null : "fx:id=\"btnReturnToMain\" was not injected: check your FXML file 'Echo.fxml'.";
        assert textArea != null : "fx:id=\"textArea\" was not injected: check your FXML file 'Echo.fxml'.";
        
        textArea.appendText("Connected and awaiting commands.");
    }
}