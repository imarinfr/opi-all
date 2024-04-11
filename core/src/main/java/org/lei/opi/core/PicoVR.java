package org.lei.opi.core;

import javafx.fxml.FXML;
import javafx.scene.Scene;

/*
 ** Needed for rgen. Might contian some specific fields later?
 */
public class PicoVR extends Jovp {
    
    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI

    public PicoVR(Scene parentScene) throws InstantiationException { 
        super(parentScene); 
    }
    public PicoVR(Scene parentScene, boolean connect) throws InstantiationException { 
        super(parentScene, connect); 
    }

    @FXML
    void initialize() {
        setupJavaFX("PicoVR");
    }

}
