package org.lei.opi.core;

import java.util.HashMap;
import java.util.function.Consumer;

import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.VFCanvas;

import es.optocom.jovp.definitions.ViewMode;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;

/**
 * Opens up a window wherever the JOVP wants it
 */
public class Display extends Jovp {
    
    public static class Settings extends Jovp.Settings { ; }  // here to trick GUI

    private record CanvasTriple(double x, double y, String label) { ; };

    public Display(Scene parentScene) throws InstantiationException { 
        super(parentScene); 

    }

     /**
     * opiInitialise: initialize OPI
     * Update GUI if parentScene != null, call super.initialize().
     * @param args A map of name:value pairs for Params
     * @return A JSON object with machine specific initialise information
     * @since 0.2.0
     */
    public Packet initialize(HashMap<String, Object> args) {
        if (parentScene != null)
            Platform.runLater(()-> {
                textAreaCommands.appendText("OPI Initialized");
            });
        return super.initialize(args);
    };
  
    /**
     * opiQuery: Query device
     * call super.query(), update GUI if parentScene != null.
     * @return settings and state machine state
     * @since 0.2.0
     */
    public Packet query() { 
        Packet p = super.query();
        if (parentScene != null)
            Platform.runLater(()-> {
                textAreaCommands.appendText(p.getMsg().toString());
            });
        return p;
    }
  
    /**
     * opiSetup: Change device background and overall settings
     * Update GUI if parentScene != null, call super.setup() 
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet setup(HashMap<String, Object> args) {
        if (parentScene != null)
            Platform.runLater(()-> {
                this.textAreaCommands.appendText("Setup:\n");
                for (String k : args.keySet())
                    this.textAreaCommands.appendText(String.format("\t%s = %s\n", k, args.get(k).toString()));
             });
        return super.setup(args);
    }
  
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * Update GUI if parentScene != null, call super.persent() 
     * @param args pairs of argument name and value
     * @return A JSON object with return messages
     * @since 0.2.0
     */
    public Packet present(HashMap<String, Object> args) {
        if (parentScene != null) {
            Platform.runLater(()-> {
                textAreaCommands.appendText("Present:\n");
                for (String k : args.keySet())
                    textAreaCommands.appendText(String.format("\t%s = %s\n", k, args.get(k).toString()));
            });

            Platform.runLater(() -> {
                try {
                    ArrayList<Double> xList = (ArrayList<Double>)args.get("x");
                    ArrayList<Double> yList = (ArrayList<Double>)args.get("y");
                    ArrayList<Double> lList = (ArrayList<Double>)args.get("lum");
                    ArrayList<String> eList = (ArrayList<String>)args.get("eye");

                    for (int i = 0 ; i < xList.size(); i++) {
                        CanvasTriple ct = new CanvasTriple(xList.get(i), yList.get(i), Long.toString(Math.round(lList.get(i))));
                        if (getSettings().viewMode.toLowerCase().equals(ViewMode.STEREO.toString().toLowerCase()))
                            updateCanvas.get(eList.get(i).toLowerCase()).accept(ct);
                        else
                            updateCanvas.get("mono").accept(ct);
                    }
                } catch (Exception e) { 
                    System.out.println("Display present() canvas troubles");
                    e.printStackTrace();
                }
            });
        }

        return super.present(args);
    }

    /**
   * opiClose: Update GUI if parentScene != null, call super.close()
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
    public Packet close() {
        if (parentScene != null) { // allows testing without GUI
            Platform.runLater(() -> {
                textAreaCommands.appendText("Close received.\n");
            });
            returnToParentScene((Node)textAreaCommands);
        }
        return super.close();
    }
  
    //-------------- FXML below here ---

    @FXML
    private Button btnClose;

    @FXML
    private Canvas canvasVF;            // mono
    private VFCanvas canvasVFModel;

    @FXML
    private Canvas canvasVFLeft;        // stereo
    private VFCanvas canvasVFModelLeft;

    @FXML
    private Canvas canvasVFRight;
    private VFCanvas canvasVFModelRight;

    @FXML
    private ImageView imageViewLeft;  // if tracking on

    @FXML
    private ImageView imageViewRight;  // if tracking on

    @FXML
    private Label labelChosen;

    @FXML
    private TextArea textAreaCommands;

    /** Set of 4 functions indexed by "mono", "left", "right", "both" to 
     * to take a (x, y, label) and update the relevant canvas */
    private HashMap<String, Consumer<CanvasTriple>> updateCanvas;

    @FXML
    void initialize() {
        assert btnClose != null : String.format("fx:id=\"btnClose\" was not injected: check your FXML file %s", fxmlFileName);
        assert textAreaCommands != null : String.format("fx:id=\"textAreaCommands\" was not injected: check your FXML file %s", fxmlFileName);
        assert labelChosen != null : String.format("fx:id=\"labelChosen\" was not injected: check your FXML file %s", fxmlFileName);

        textAreaCommands.setFont(new Font("Arial", 10));

        labelChosen.setText("Chosen OPI: Display");

        updateCanvas = new HashMap<String, Consumer<CanvasTriple>>();
        updateCanvas.put("mono", 
            (ct) -> {
                canvasVFModel.updatePoint(ct.x(), ct.y(), ct.label().toString());
                VFCanvas.draw(canvasVF, canvasVFModel);
            });
        updateCanvas.put("left", 
            (ct) -> {
                canvasVFModelLeft.updatePoint(ct.x(), ct.y(), ct.label().toString());
                VFCanvas.draw(canvasVFLeft, canvasVFModelLeft);
            });
        updateCanvas.put("right", 
            (ct) -> {
                canvasVFModelRight.updatePoint(ct.x(), ct.y(), ct.label().toString());
                VFCanvas.draw(canvasVFRight, canvasVFModelRight);
            });
        updateCanvas.put("both", 
            (ct) -> {
                canvasVFModelLeft.updatePoint(ct.x(), ct.y(), ct.label().toString());
                VFCanvas.draw(canvasVFLeft, canvasVFModelLeft);
                canvasVFModelRight.updatePoint(ct.x(), ct.y(), ct.label().toString());
                VFCanvas.draw(canvasVFRight, canvasVFModelRight);
            });

        if (canvasVF != null) {
            canvasVFModel = new VFCanvas();
            updateCanvas.get("mono").accept(new CanvasTriple(0, 0, ""));
        } else {
            canvasVFModelLeft = new VFCanvas();
            canvasVFModelRight = new VFCanvas();
            updateCanvas.get("both").accept(new CanvasTriple(0, 0, ""));
        }
    }

    @FXML
    void actionBtnClose(ActionEvent event) {
        returnToParentScene((Node)event.getSource());
    }
}