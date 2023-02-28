package org.lei.opi.core;

import javafx.scene.Scene;

/*
 ** Needed for rgen. Might contian some specific fields later?
 */
public class PicoVR extends Jovp {
    
    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI

    public PicoVR(Scene parentScene) { super(parentScene); }
}
