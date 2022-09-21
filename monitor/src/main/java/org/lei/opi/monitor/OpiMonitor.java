package org.lei.opi.monitor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;

/**
 * JavaFX App
 */
public class OpiMonitor extends Application {

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        Label label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + "." 
            + "\nIvan & Turps were here, Sep 2022.");

        TextArea textArea = new TextArea();

        var scene = new Scene(new StackPane(label, textArea), 640, 480);

        stage.setScene(scene);
        stage.show();
    }

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

        if(!cmd.hasOption("nw")) {
            launch();
        }
        else {
            System.out.println("Starting server");
            Server s = new Server(51434);
            System.exit(0);
        }
    }
}