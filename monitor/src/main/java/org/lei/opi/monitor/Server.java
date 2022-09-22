package org.lei.opi.monitor;

import org.lei.opi.core.CSListener;
import org.lei.opi.core.OpiManager;

import java.io.Writer;

public class Server {
    CSListener server;
    
    public Server(int port, Writer mw) {
        try {
            server = new CSListener(port, new OpiManager(), mw, mw);
            System.out.println(String.format("Monitor started at %s:%s", server.getAddress(), port));
            server.run();

            while (true) {
                Thread.sleep(500);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
        }
    }
}
