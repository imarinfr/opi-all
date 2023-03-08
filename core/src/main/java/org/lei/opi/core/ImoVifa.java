package org.lei.opi.core;

import javafx.scene.Scene;

/*
 ** Needed for rgen. Might contian some specific fields later?
 */
public class ImoVifa extends Jovp {
    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI
    
    public ImoVifa(Scene parentScene) throws InstantiationException { 
        super(parentScene); 
    }
}
