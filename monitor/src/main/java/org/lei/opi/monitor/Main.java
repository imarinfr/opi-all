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

        Monitor.main(args);
    }
}
