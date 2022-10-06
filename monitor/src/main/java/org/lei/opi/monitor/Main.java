package org.lei.opi.monitor;

import org.lei.opi.core.CSListener;
import org.lei.opi.core.OpiManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("nw", false, "Do not draw GUI window");
        options.addOption("p", true, "Port number to use on localhost");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        int port = Integer.parseInt(cmd.getOptionValue("p", "50001"));

        /* 
        if(!cmd.hasOption("nw")) {
            OpiMonitor opiM = new OpiMonitor(port);
            opiM.launch();
        } else {
        */
            System.out.println("Starting server");
            CSListener csl = new CSListener(port, new OpiManager());
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    break;
                }
            }
            System.exit(0);
        //}
    }    
}
