package org.lei.opi.core;

import javafx.scene.Scene;
/*
 ** Needed for rgen. Might contian some specific fields later?
 */
public class Display extends Jovp {
    
    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI

    public Display(Scene parentScene) throws RuntimeException { super(parentScene); }
}
