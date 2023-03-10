package org.lei.opi.core;

import java.util.HashMap;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.event.ActionEvent;

/**
 * Opens up a window wherever the JOVP wants it
 */
public class Display extends Jovp {
    
    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI

    public Display(Scene parentScene) throws InstantiationException { 
        super(parentScene); 
    }

     /**
     * opiInitialise: initialize OPI
     * Update GUI, call super.initialize().
     * @param args A map of name:value pairs for Params
     * @return A JSON object with machine specific initialise information
     * @since 0.2.0
     */
    public Packet initialize(HashMap<String, Object> args) {
        Platform.runLater(()-> {
            if (textAreaCommands != null)
                textAreaCommands.appendText("OPI Initialized");
        });
        return super.initialize(args);
    };
  
    /**
     * opiQuery: Query device
     * call super.query(), update GUI.
     * @return settings and state machine state
     * @since 0.2.0
     */
    public Packet query() { 
        Packet p = super.query();
        Platform.runLater(()-> {
            if (textAreaCommands != null)
                textAreaCommands.appendText(p.getMsg().toString());
        });
        return p;
    }
  
    /**
     * opiSetup: Change device background and overall settings
     * Update GUI, call super.setup() 
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet setup(HashMap<String, Object> args) {
        Platform.runLater(()-> {
            if (textAreaCommands != null) {
                this.textAreaCommands.appendText("Setup:\n");
                for (String k : args.keySet())
                    this.textAreaCommands.appendText(String.format("\t%s = %s\n", k, args.get(k).toString()));
            }
        });
        return super.setup(args);
    }
  
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * Update GUI, call super.persent() 
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet present(HashMap<String, Object> args) {
        Platform.runLater(()-> {
            if (textAreaCommands != null) {
                textAreaCommands.appendText("Present:\n");
                for (String k : args.keySet())
                    textAreaCommands.appendText(String.format("\t%s = %s\n", k, args.get(k).toString()));
            }
        });

        Platform.runLater(() -> {
            try {
                ArrayList<Double> xList = (ArrayList<Double>)args.get("x");
                ArrayList<Double> yList = (ArrayList<Double>)args.get("y");

                for (int i = 0 ; i < xList.size(); i++)
                    dataSeries.getData().add(new XYChart.Data<Number, Number>(xList.get(i), yList.get(i)));
            } catch (Exception e) { 
                System.out.println("Display chart troubles");
                e.printStackTrace();
            }
          });

        return super.present(args);
    }

    /**
   * opiClose: Update GUI, call super.close()
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
    public Packet close() {
        Platform.runLater(() -> {
            if (textAreaCommands != null) // allows testing without GUI
                textAreaCommands.appendText("Close received.\n");
        });
        returnToParentScene((Node)textAreaCommands);
        return super.close();
    }
  
    //-------------- FXML below here ---

    @FXML
    private Button btnClose;

    @FXML
    private ScatterChart<Number, Number> scatterChartVF;
    private XYChart.Series<Number, Number> dataSeries;

    @FXML
    private TextArea textAreaCommands;

    @FXML
    void initialize() {
        assert btnClose != null : "fx:id=\"btnClose\" was not injected: check your FXML file 'Display.fxml'.";
        assert scatterChartVF != null : "fx:id=\"scatterChartLeft\" was not injected: check your FXML file 'Display.fxml'.";
        assert textAreaCommands != null : "fx:id=\"textAreaCommands\" was not injected: check your FXML file 'Display.fxml'.";

        textAreaCommands.setFont(new Font("Arial", 10));
        dataSeries = new XYChart.Series<Number, Number>();
        scatterChartVF.getData().add(dataSeries);
        scatterChartVF.getXAxis().setAutoRanging(false);
        scatterChartVF.getYAxis().setAutoRanging(false);
    }

    @FXML
    void actionBtnClose(ActionEvent event) {
        returnToParentScene((Node)event.getSource());
    }

}