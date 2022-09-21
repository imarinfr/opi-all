package org.lei.opi.monitor;

import org.lei.opi.core.CSListener;
import org.lei.opi.core.OpiManager;


public class Server {
    CSListener server;
    
    public Server(int port) {
        server = new CSListener(port, new OpiManager());
    
        System.out.println(String.format("Monitor started at %s:%s", server.getAddress(), port));
    }
}
