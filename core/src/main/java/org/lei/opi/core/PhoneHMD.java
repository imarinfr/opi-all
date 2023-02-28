package org.lei.opi.core;

import javafx.scene.Scene;

/*
 ** Needed for rgen. Might contain some specific fields later?
 */
public class PhoneHMD extends Jovp {
    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI
    
    public PhoneHMD(Scene parentScene) { super(parentScene); }
}
