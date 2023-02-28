package org.lei.opi.core;

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

    public Maia (Scene parentScene) {
        super(parentScene);
    }
}