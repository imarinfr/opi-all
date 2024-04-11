package org.lei.opi.core;

import javafx.fxml.FXML;
import javafx.scene.Scene;

/*
 ** Needed for rgen. Might contain some specific fields later?
 */
public class PhoneHMD extends Jovp {
    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI
    
    public PhoneHMD(Scene parentScene) throws InstantiationException { 
        super(parentScene); 
    }
    public PhoneHMD(Scene parentScene, boolean connect) throws InstantiationException { 
        super(parentScene, connect); 
    }

    @FXML
    void initialize() {
        setupJavaFX("PhoneHMD");
    }
}
