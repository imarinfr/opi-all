package org.lei.opi.jovp;

import java.util.HashMap;
import java.util.stream.Stream;

import org.lei.opi.core.definitions.Setup;
import org.lei.opi.core.OpiManager;
import org.lei.opi.core.OpiManager.Command;
import org.lei.opi.core.definitions.MessageProcessor;
import org.lei.opi.core.definitions.Present;
import org.lei.opi.core.definitions.Query;
import org.lei.opi.core.definitions.Response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import es.optocom.jovp.Monitor;
import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.structures.ViewMode;

/**
 * The OPI JOVP driver
 *
 * @since 0.0.1
 */
public class OpiDriver extends MessageProcessor {

  /** Machine state */
  protected enum State {IDLE, INIT, SETUP, PRESENT, WAIT, RESPONDED, CLOSE};

  /** A background record to communicate with OpiLogic */
  protected final Settings settings;
  /** Message prefix */
  private final String prefix;
  /** The psychoEngine */
  private PsychoEngine psychoEngine = null;
  /** A background record to communicate with OpiLogic */
  protected Setup[] backgrounds;
  /** A stimulus record to communicate with OpiLogic */
  protected Present stimulus;
  /** A record to record the results after a stimulus prsentation */
  protected Response response;
  /** Whether opiInitialized has been invoked and not closed later on by opiClose */
  protected State state;

  /**
   * The OpiDriver
   *
   * @param settings Driver settings
   * 
   * @return the PsychoLogic for the PsychoEngine
   * 
   * @since 0.1.0
   */
  OpiDriver(Settings settings) {
    this.settings = settings;
    this.prefix = "OPI JOVP " + settings.machine() + ": ";
    switch (settings.viewMode()) {
      case MONO -> backgrounds = new Setup[] {null};
      case STEREO -> backgrounds = new Setup[] {null, null};
    }
  }

  /**
   * Initialize the driver with a psychoEngine
   *
   * @param psychoEngine The psychoEngine
   * 
   * @since 0.1.0
   */
  void init(PsychoEngine psychoEngine) {
    this.psychoEngine = psychoEngine;
  }

  /**
   * Process incoming Json commands. If it is a 'choose' command, then
   * set the private field machine to a new instance of that machine.
   * If it is another command, then process it using the machine object.
   *
   * @param jsonStr A JSON object that at least contains the name 'command'.
   * 
   * @return JSON-formatted message with feedback
   * 
   * @since 0.1.0
   */
  public MessageProcessor.Packet process(String jsonStr) {
    System.out.println(jsonStr);
    Gson gson = new Gson();
    HashMap<String, Object> pairs;
    try {
      pairs = gson.fromJson(jsonStr, new TypeToken<HashMap<String, Object>>() {}.getType());
    } catch (JsonSyntaxException e) {
      return OpiManager.error(OpiManager.BAD_JSON);
    }
    if (!pairs.containsKey("command")) // needs a command
      return OpiManager.error(OpiManager.NO_COMMAND_FIELD);
      String cmd = pairs.get("command").toString();
    // check it is a valid command from Command.*
    if (!Stream.of(Command.values()).anyMatch((e) -> e.name().equalsIgnoreCase(cmd)))
      return OpiManager.error(OpiManager.BAD_COMMAND_FIELD);
    return switch (Command.valueOf(cmd.toUpperCase())) {
      case INITIALIZE -> initialize();
      case QUERY -> query();
      case SETUP -> setup(pairs);
      case PRESENT -> present(pairs);
      case CLOSE -> close();
      default -> OpiManager.error("Wrong OPI command, you silly goose. OPI command received was: " + cmd.toUpperCase());
    };
  }

  /**
   * Return results of query
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet query() {
    try {
      double[] fov = new double[] {-1, -1};
      Monitor monitor = null;
      if (psychoEngine != null) {
        fov = psychoEngine.getFieldOfView();
        if (settings.viewMode() == ViewMode.STEREO) fov[0] /= 2;
        monitor = psychoEngine.getWindow().getMonitor();
      }
      Query query = new Query(false, "", settings.distance(), settings.viewMode(), settings.input(), settings.depth(), fov, monitor);
      return OpiManager.ok(prefix + "opiQuery successful: " + query, false); 
    } catch (Exception e) {
      Query query = new Query(true, "", -1, null, null, -1, null, null);
      return OpiManager.error(prefix + "problem with opiQuery", e);
    }
  }

  /**
   * Start the psychoEngine
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet initialize() {
    state = State.INIT;
    while (state != State.IDLE) Thread.onSpinWait();
    return OpiManager.ok(prefix + "opiInitialize successful", false);
  }

  /**
   * Change settings of background and fixation target
   * 
   * @param args A map of name:value pairs for parameters
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet setup(HashMap<String, Object> args) {
/**
    try {
      // Get eye for the instruction
      Eye eye = Eye.valueOf(((String) args.get("eye")).toUpperCase());
      if(settings.viewMode() == ViewMode.MONO || eye == Eye.BOTH || eye == Eye.LEFT)
        backgrounds[0] = Background.set(args, settings.calibration());
      if(settings.viewMode() == ViewMode.STEREO && (eye == Eye.BOTH || eye == Eye.RIGHT))
        backgrounds[1] = Background.set(args, settings.calibration());
      state = State.SETUP;
      return OpiManager.ok(prefix + "opiSetup successful", false);
    } catch (ClassCastException e) {
      return OpiManager.error(prefix + "problem with opiSetup", e);
    }
*/
    return OpiManager.ok(prefix + "TODO", false);
  }

  /**
   * Present a stimulus
   *
   * @param args A map of name:value pairs for parameters
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet present(HashMap<String, Object> args) {
/**
    try {
      stimulus = Present.set(args);
      state = State.PRESENT;
      while (state != State.RESPONDED) Thread.onSpinWait(); // wait for response
      return OpiManager.ok(prefix + "opiPresent successful: " + response, false);
    } catch (Exception e) {
      return OpiManager.error(prefix + "problem with opiPresent", e);
    }
*/
    return OpiManager.ok(prefix + "TODO", false);
  }

  /**
   * Stop the psychoEngine
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet close() {
    state = State.CLOSE;
    return OpiManager.ok("Closed"); // ignored
  }

}
