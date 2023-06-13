package org.lei.opi.core;

import javafx.fxml.FXML;
import javafx.scene.Scene;

/**
 * Compass client
 *
 * @since 0.0.1
 */
public class Compass extends Icare {

    public static class Settings extends Icare.Settings { ; }  // here to trick GUI

    /**
     * Compass client: fill constants.
     *
     * @since 0.0.1
     */
    public Compass(Scene parentScene) {
        super(parentScene); 
    }

    @FXML
    void initialize() {
        setupJavaFX("Compass");
    }
}