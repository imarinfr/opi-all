package org.lei.opi.core;

/* 
 * A simple machine that just echos commands coming in from the client.
 * @since 0.2.0
 */
import java.util.HashMap;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

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

        if (parentScene != null) 
            this.connect(settings.port, Listener.obtainPublicAddress());
    }
  
    /**
     * opiInitialise: initialize OPI
     * @param args A map of name:value pairs for Params
     * @return A JSON object with machine specific initialise information
     * @since 0.2.0
     */
    public Packet initialize(HashMap<String, Object> args) {
        this.textArea.appendText("Initialise:\n");
        for (String k : args.keySet())
            this.textArea.appendText(String.format("\t%s = %s\n", k, args.get(k).toString()));

      return OpiClient.ok(String.format(CONNECTED_TO_HOST, settings.ip, settings.port));
    };
  
    /**
     * opiQuery: Query device
     * @return settings and state machine state
     * @since 0.2.0
     */
    public Packet query() { 
        this.textArea.appendText("Query:\n\tNothing to report.\n");
        return OpiClient.ok("Query: Nothing to report"); 
    }
  
    /**
     * opiSetup: Change device background and overall settings
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet setup(HashMap<String, Object> args) {
        this.textArea.appendText("Setup:\n");
        for (String k : args.keySet())
            this.textArea.appendText(String.format("\t%s = %s\n", k, args.get(k).toString()));

        if (!this.listening) 
            return OpiClient.error(NOT_INITIALIZED);

        return OpiClient.ok(OpiMachine.gson.toJson(args));
    }
  
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet present(HashMap<String, Object> args) {
        this.textArea.appendText("Present:\n");
        for (String k : args.keySet())
            this.textArea.appendText(String.format("\t%s = %s\n", k, args.get(k).toString()));

        if (!this.listening) return OpiClient.error(NOT_INITIALIZED);
        return OpiClient.ok(OpiMachine.gson.toJson(args));
    }
  
    /**
     * opiClose: Close OPI connection
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet close() {
        this.textArea.appendText("Close:\n");
        this.closeListener();
        return OpiClient.ok("Got OPI_CLOSE so closing connection.", true);
    };

// --------------- FXML after here ----------------------------------- 


    @FXML
    private Button btnReturnToMain;

    @FXML
    private TextField fieldEchoPort;

    @FXML
    private TextField fieldLocalHost;

    @FXML
    private TextField fieldClientPort;

    @FXML
    private Label labelMessages;

    @FXML
    private TextArea textArea;

    /*
     * @param event
     */
    @FXML
    void actionBtnReturnToMain(ActionEvent event) {
        System.out.println("Returning to Main!");
    }

    @FXML
    void initialize() {
        assert btnReturnToMain != null : "fx:id=\"btnReturnToMain\" was not injected: check your FXML file 'Echo.fxml'.";
        assert fieldClientPort != null : "fx:id=\"fieldClientPort\" was not injected: check your FXML file 'Echo.fxml'.";
        assert fieldEchoPort != null : "fx:id=\"fieldEchoPort\" was not injected: check your FXML file 'Echo.fxml'.";
        assert fieldLocalHost != null : "fx:id=\"fieldLocalHost\" was not injected: check your FXML file 'Echo.fxml'.";
        assert labelMessages != null : "fx:id=\"labelMessages\" was not injected: check your FXML file 'Echo.fxml'.";
        assert textArea != null : "fx:id=\"textArea\" was not injected: check your FXML file 'Echo.fxml'.";
        
        fieldLocalHost.setText(Listener.obtainPublicAddress().toString());
        fieldEchoPort.setText("" + settings.port);
        textArea.appendText("Connected and awaiting commands.");
    }
}