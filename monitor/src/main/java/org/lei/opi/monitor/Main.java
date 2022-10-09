package org.lei.opi.monitor;

import org.lei.opi.core.CSListener;
import org.lei.opi.core.OpiManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("w", false, "Use GUI/window");
        options.addOption("p", true, "Port number to use on localhost");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        int port = Integer.parseInt(cmd.getOptionValue("p", "50002"));

        /* 
        class ListenerThread extends Thread {
            public void run() {
                System.out.println("Open");
                CSListener csl = new CSListener(port, new OpiManager());
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException e) {
                        System.out.println("Interrupted");
                        break;
                    }
                }
                System.out.println("Close");
                csl.close();
            }
        };
        System.out.println("Starting listener thread");
        Thread th = new ListenerThread();
        th.start();

        if(cmd.hasOption("w")) {
            OpiMonitor opiM = new OpiMonitor(port);
            opiM.launch();
        } else {
            System.out.println("Starting monitor - listening on " + port);
            while (true) Thread.onSpinWait();
        }    
        */
        Monitor.main(args);
    }
}
