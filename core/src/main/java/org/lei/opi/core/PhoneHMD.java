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

    @FXML
    void initialize() {
        setupJavaFX("PhoneHMD");
    }
}
