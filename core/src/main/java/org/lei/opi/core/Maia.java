package org.lei.opi.core;

import javafx.fxml.FXML;
import javafx.scene.Scene;

/**
 * Maia client
 *
 * @since 0.0.1
 */
public class Maia extends Icare {

    public static class Settings extends Icare.Settings { ; }  // here to trick GUI

    /**
     * Maia client: fill constants.
     *
     * @since 0.0.1
     */

    public Maia (Scene parentScene) { super(parentScene); }
    public Maia (Scene parentScene, boolean connect) { super(parentScene, connect); }

    @FXML
    void initialize() {
        setupJavaFX("MAIA");
    }
}
