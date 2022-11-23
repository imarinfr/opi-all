package org.lei.opi.core;

import java.util.Arrays;
import java.util.HashMap;

import org.joml.Runtime;
import org.lei.opi.core.OpiManager.Command;
import org.lei.opi.core.definitions.MessageProcessor;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.Present;
import org.lei.opi.core.definitions.ReturnMsg;
import org.lei.opi.core.definitions.Setup;

import es.optocom.jovp.definitions.Eye;
import es.optocom.jovp.definitions.ModelType;
import es.optocom.jovp.definitions.TextureType;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.chart.ScatterChart;
import javafx.fxml.FXMLLoader;


/**
 * JOVP client
 *
 * @since 0.0.1
 */
public class Jovp extends OpiMachine {

  class Settings extends OpiMachine.Settings {
      public int screen;
      public int[] physicalSize;
      public boolean pseudoGray;
      public boolean fullScreen;
      public int distance;
      public String viewMode;
      public String input;
      public boolean tracking;
      public String gammaFile;
  };

  /** Settings */
  private Settings settings;
  public Settings getSettings() { return this.settings; }

  private Scene parentScene;  // return here when btnClose is clicked on our GUI

  public Jovp(Scene parentScene) throws RuntimeException {
    this.settings = (Settings) fillSettings(Settings.class);
    this.parentScene = parentScene;
    writer = new CSWriter(settings.ip, settings.port);

    listCommands = new ListView<String>(writer.messageRecord);  // GUI element
  }

  /** No parent Scene or connection to a machine. */
  public Jovp(boolean noSocket) {
    this.settings = (Settings) fillSettings(Settings.class);
  }

  /**
   * opiInitialise: initialize OPI
   * 
   * @param args A map of name:value pairs for Params
   * 
   * @return A JSON object with machine specific initialise information
   * 
   * @since 0.0.1
   */
  public MessageProcessor.Packet initialize(HashMap<String, Object> args) {
    writer.send(initConfiguration());
    while (writer.empty()) Thread.onSpinWait();
    return new MessageProcessor.Packet(writer.receive());
  };

  /**
   * opiQuery: Query device
   * 
   * @return settings and state machine state
   *
   * @since 0.0.1
   */
  public MessageProcessor.Packet query() {
    if (writer == null) return OpiManager.error(NOT_INITIALIZED);
    try {
      writer.send(toJson(Command.QUERY));
      while (writer.empty()) Thread.onSpinWait();
      return new MessageProcessor.Packet(writer.receive());
    } catch (ClassCastException | IllegalArgumentException e) {
      return OpiManager.error(COULD_NOT_QUERY);
    }
  };

  /**
   * opiSetup: Change device background and overall settings
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  @Parameter(name = "eye", className = Eye.class, desc = "The eye for which to apply the settings.", defaultValue = "list('left')")
  @Parameter(name = "bgLum", className = Double.class, desc = "Background luminance for eye.", min = 0, defaultValue = "10")
  @Parameter(name = "bgCol", className = Double.class, desc = "Background color for eye.", isList = true, min = 0, max = 1, defaultValue = "list(1, 1, 1)")
  @Parameter(name = "fixShape", className = ModelType.class, desc = "Fixation target type for eye.", defaultValue = "maltese")
  @Parameter(name = "fixLum", className = Double.class, desc = "Fixation target luminance for eye.", min = 0, defaultValue = "20")
  @Parameter(name = "fixCol", className = Double.class, desc = "Fixation target color for eye.", isList = true, min = 0, max = 1, defaultValue = "list(0, 1, 0)")
  @Parameter(name = "fixCx", className = Double.class, desc = "x-coordinate of fixation target (degrees).", min = -90, max = 90, defaultValue = "0")
  @Parameter(name = "fixCy", className = Double.class, desc = "y-coordinate of fixation target (degrees).", min = -90, max = 90, defaultValue = "0")
  @Parameter(name = "fixSx", className = Double.class, desc = "diameter along major axis of ellipse (degrees).", min = 0, defaultValue = "1")
  @Parameter(name = "fixSy", className = Double.class, desc = "diameter along minor axis of ellipse (degrees). If not received, then sy = sx.", optional = true, min = 0, defaultValue = "1")
  @Parameter(name = "fixRotation", className = Double.class, desc = "Angles of rotation of fixation target (degrees). Only useful if sx != sy specified.", optional = true, min = 0, max = 360, defaultValue = "0")
  @Parameter(name = "tracking", className = Double.class, desc = "Whether to correct stimulus location based on eye position.", optional = true, min = 0, max = 1, defaultValue = "0")
  public MessageProcessor.Packet setup(HashMap<String, Object> args) {
    if (writer == null) return OpiManager.error(NOT_INITIALIZED);
    try {
      writer.send(Setup.process(args).toJson());
      while (writer.empty()) Thread.onSpinWait();
      return new MessageProcessor.Packet(writer.receive());
    } catch (ClassCastException | IllegalArgumentException e) {
      return OpiManager.error(COULD_NOT_SETUP);
    }
  }

  /**
   * opiPresent: Present OPI stimulus in perimeter
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  @Parameter(name = "eye", className = Eye.class, desc = "Eye to test.", isList = true, defaultValue = "list('left')")
  @Parameter(name = "shape", className = ModelType.class, desc = "Stimulus shape.", isList = true, optional = true, defaultValue = "list('circle')")
  @Parameter(name = "type", className = TextureType.class, desc = "Stimulus type.", isList = true, optional = true, defaultValue = "list('flat')")
  @Parameter(name = "x", className = Double.class, desc = "List of x co-ordinates of stimuli (degrees).", isList = true, min = -90, max = 90, defaultValue = "list(0)")
  @Parameter(name = "y", className = Double.class, desc = "List of y co-ordinates of stimuli (degrees).", isList = true, min = -90, max = 90, defaultValue = "list(0)")
  @Parameter(name = "sx", className = Double.class, desc = "List of diameters along major axis of ellipse (degrees).", isList = true, min = 0, max = 180, defaultValue = "list(1.72)")
  @Parameter(name = "sy", className = Double.class, desc = "List of diameters along minor axis of ellipse (degrees). If not received, then sy = sx", isList = true, optional = true, min = 0, max = 180, defaultValue = "list(1.72)")
  @Parameter(name = "lum", className = Double.class, desc = "List of stimuli luminances (cd/m^2).", isList = true, min = 0, defaultValue = "list(20)")
  @Parameter(name = "color1", className = Double.class, desc = "List of stimulus colors 1.", isListList = true, min = 0, max = 1, defaultValue = "list(list(1, 1, 1))")
  @Parameter(name = "color2", className = Double.class, desc = "List of stimulus colors 2. Only useful if stimulus type != FLAT", isListList = true, optional = true, min = 0, max = 1, defaultValue = "list(list(0, 1, 0))")
  @Parameter(name = "rotation", className = Double.class, desc = "List of angles of rotation of stimuli (degrees). Only useful if sx != sy specified.", isList = true, optional = true, min = 0, max = 360, defaultValue = "list(0)")
  @Parameter(name = "contrast", className = Double.class, desc = "List of stimulus contrasts (from 0 to 1). Only useful if type != FLAT.", isList = true, optional = true, min = 0, max = 1, defaultValue = "list(1)")
  @Parameter(name = "phase", className = Double.class, desc = "List of phases (in degrees) for generation of spatial patterns. Only useful if type != FLAT", isList = true, optional = true, min = 0, defaultValue = "list(0)")
  @Parameter(name = "frequency", className = Double.class, desc = "List of frequencies (in cycles per degrees) for generation of spatial patterns. Only useful if type != FLAT", isList = true, optional = true, min = 0, max = 300, defaultValue = "list(0)")
  @Parameter(name = "defocus", className = Double.class, desc = "List of defocus values in Diopters for stimulus post-processing.", isList = true, optional = true, min = 0, defaultValue = "list(0)")
  @Parameter(name = "textRotation", className = Double.class, desc = "List of angles of rotation of stimuli (degrees). Only useful if type != FLAT", isList = true, optional = true, min = 0, max = 360, defaultValue = "list(0)")
  @Parameter(name = "t", className = Double.class, desc = "List of stimuli presentation times (ms).", isList = true, min = 0, defaultValue = "list(200)")
  @Parameter(name = "w", className = Double.class, desc = "List of stimuli response windows (ms).", isList = true, min = 0, defaultValue = "list(1500)")
  @ReturnMsg(name = "res.msg.eyex", className = Double.class, desc = "x co-ordinates of pupil at times eyet (degrees).")
  @ReturnMsg(name = "res.msg.eyey", className = Double.class, desc = "y co-ordinates of pupil at times eyet (degrees).")
  @ReturnMsg(name = "res.msg.eyed", className = Double.class, desc = "Diameter of pupil at times eyet (mm).")
  @ReturnMsg(name = "res.msg.eyet", className = Double.class, desc = "Time of (eyex, eyey) pupil from stimulus onset (ms).", min = 0)
  public MessageProcessor.Packet present(HashMap<String, Object> args) {
    if (writer == null) return OpiManager.error(NOT_INITIALIZED);
    try {
      writer.send(Present.process(args).toJson());
      while (writer.empty()) Thread.onSpinWait();
      return new MessageProcessor.Packet(writer.receive());
    } catch (ClassCastException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
      return OpiManager.error(COULD_NOT_PRESENT, e);
    }
  }

  /**
   * opiClose: Close OPI connection
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  public MessageProcessor.Packet close() {
    if (writer == null) return OpiManager.error(NOT_INITIALIZED);
    writer.send(toJson(Command.CLOSE));
    while (writer.empty()) Thread.onSpinWait(); writer.receive(); // message ignored
    writer.close();
    writer = null;
    return OpiManager.ok(DISCONNECTED_FROM_HOST, true);
  }

  /** Initialize command with */
  private String initConfiguration() {
    return new StringBuilder("{\n  \"command\": " + Command.INITIALIZE + ",\n")
    .append("  \"machine\": " + this.getClass().getSimpleName() + ",\n")
    .append("  \"screen\": " + settings.screen + ",\n")
    .append("  \"physicalSize\": " + Arrays.toString(settings.physicalSize) + ",\n")
    .append("  \"pseudoGray\": " + settings.pseudoGray + ",\n")
    .append("  \"fullScreen\": " + settings.fullScreen + ",\n")
    .append("  \"distance\": " + settings.distance + ",\n")
    .append("  \"viewMode\": " + settings.viewMode + ",\n")
    .append("  \"input\": " + settings.input + ",\n")
    .append("  \"tracking\": " + settings.tracking + ",\n")
    .append("  \"gammaFile\": " + settings.gammaFile)
    .append("\n}").toString();
  }

  // FXML code for Monitor GUI after this line ---------------------------------------------

    @FXML
    private Button btnClose;

    @FXML
    private ScatterChart<?, ?> scatterChartVF;

    @FXML
    private ListView<String> listCommands;

    @FXML
    void actionBtnClose(ActionEvent event) {
        System.out.println("Closed Display");

        final Node source = (Node) event.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();
        stage.setScene(this.parentScene);
    }

}