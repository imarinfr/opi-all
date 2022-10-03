package org.lei.opi.monitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class OpiMonitor extends Application {
    /**
     *  Presumably plot something representing the current state of 
     * the VF measurement going on...
     * 
     * @param view Imageview to update
     */
    private void updateVFImage(ImageView view) {
        //if (eye == "L")

        InputStream stream;
        try {
            stream = new FileInputStream("hfa.jpg");
        } catch (FileNotFoundException e) {
            System.out.println("can't find vf image");
            return;
        }
        Image image = new Image(stream);
        view.setImage(image);
        view.setFitWidth(256);
        view.setPreserveRatio(true);
    }

    /**
     * Get current position of eye from machine's camera...
     *  
     * @param eye L or R
     * @param view ImageView to update
     */
    private void updateEyeImage(String eye, ImageView view) {
        //if (eye == "L")

        InputStream stream;
        try {
            stream = new FileInputStream("eye.jpeg");
        } catch (FileNotFoundException e) {
            System.out.println("can't find eye image");
            return;
        }
        Image image = new Image(stream);
        view.setImage(image);
        view.setFitWidth(256);
        view.setPreserveRatio(true);
    }

        // IP and Port of machine
        // 
    class IpPortBox {
        GridPane box;
        TextField ip;
        TextField port;
        Label lab;
        Button testBtn;

        /*
         *    +---------+----------+--------+
         *    |  label  |  ip      |  port  | 
         *    |-----------------------------+
         *    |         |   test   |        |
         *    |         |   button |        |
         *    +---------+----------+--------+
         */                                       
        IpPortBox(String label, String ip, String port) {
            box = new GridPane();
            box.setHgap(10);
            box.setVgap(10);
            //ipBox.setAlignment(Pos.CENTER_LEFT);
            //ipBox.setPadding(new Insets(5, 20, 5, 5));
            this.lab = new Label(label);
            this.ip = new TextField(ip);
            this.port = new TextField(port);
            this.testBtn = new Button("Test Connection");
            this.lab.setPrefWidth(150);
            this.ip.setPrefSize(200, 20);
            this.port.setPrefSize(100, 20);
            this.ip.setAlignment(Pos.CENTER_LEFT);
            this.port.setAlignment(Pos.CENTER_LEFT);
            box.add(this.lab, 0, 0);
            box.add(this.ip, 1, 0);
            box.add(this.port, 2, 0);
            box.add(this.testBtn, 1, 1);
        }

        void setIp(String s) { this.ip.setText(s); }
        void setPort(String s) { this.port.setText(s); }
    }

    @Override
    public void start(Stage stage) {
        /*
         *    +---------+----------+---------------+
         *    |         |          |               |
         *    |  left   |  right   |  Buttons for  | 
         *    |   eye   |   eye    |    stuff      |
         *    |         |          |               |
         *    |---------+----------+---------------+
         *    |   VF    |  VF      | Machine info  |
         *    |   left  |  right   |               |
         *    |         |          |               |
         *    +---------+----------+---------------+
         */                                       
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ImageView leftEye = new ImageView();
        ImageView rghtEye = new ImageView();
        ImageView leftField = new ImageView();
        ImageView rghtField = new ImageView();
        grid.add(leftEye, 0, 0);
        grid.add(rghtEye, 1, 0);
        grid.add(leftField, 0, 1);
        grid.add(rghtField, 1, 1);

        TextArea messages = new TextArea();
        messages.setEditable(false);
        grid.add(messages, 2, 1);

        VBox buttonsPane = new VBox();
        buttonsPane.setSpacing(8);
        buttonsPane.getChildren().add(new IpPortBox("My IP and Port:", "localhost", "51234").box);
        buttonsPane.getChildren().add(new IpPortBox("Machine IP and Port:", "localhost", "51234").box);
        grid.add(buttonsPane, 2, 0);

        var scene = new Scene(grid, 640*1.5, 480);

        //textArea.setPrefHeight(scene.getWindow().getHeight()/2);

        stage.setScene(scene);
        stage.show();
    
        updateEyeImage("L", leftEye);
        updateEyeImage("R", rghtEye);

        updateVFImage(leftField);
        updateVFImage(rghtField);

        MessageWriter mw = new MessageWriter(messages);
        Server s = new Server(51434, mw);
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
            Server s = new Server(51001, new PrintWriter(System.out));
            System.exit(0);
        }
    }
}