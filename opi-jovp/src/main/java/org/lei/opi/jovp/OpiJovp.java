package org.lei.opi.jovp;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;

import org.lei.opi.core.Jovp;
import org.lei.opi.core.MessageProcessor;
import org.lei.opi.core.OpiManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.Timer;

/**
 * OPI JOVP manager
 *
 * @since 0.0.1
 */
public class OpiJovp extends MessageProcessor {

  /** JOVP configuration */
  Settings settings;
  /** The PsychoEngine */
  PsychoEngine psychoEngine;
  /** A background record to communicate with JOVP */
  Background bgRecord;
  /** A stimulus record to communicate with JOVP */
  Stimulus stRecord;
  /** A record to record the results after a stimulus prsentation */
  Response resRecord;
  /** A timer to control, well, you know, er, time? */
  Timer timer;

  /**
   * OPI JOVP manager
   *
   * @param color an Enum with a color label 
   * 
   * @return the rgba values for that color
   * 
   * @since 0.0.1
   */
  static double[] color2rgba(Jovp.Color color) {
    return switch(color) {
      case WHITE -> new double[] {1, 1, 1, 1};
      case RED -> new double[] {1, 0, 0, 1};
      case GREEN -> new double[] {0, 1, 0, 1};
      case BLUE -> new double[] {0, 0, 1, 1};
    };
  }

  /**
   * Start the OPI JOVP comms and driver with default settings settings
   *
   * @param implementation A record with machine-dependent settings
   * @throws IOException
   * 
   * @since 0.0.1
   */
  public OpiJovp(Settings.Machine machine) throws IOException {
    this.settings = Settings.defaultSettings(machine);
  }

  /**
   * Start the OPI JOVP comms and driver from a record of settings
   * 
   * @param settings A record with machine-dependent settings
   *
   * @since 0.0.1
   */
  public OpiJovp(Settings settings) {
    this.settings = settings;
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
    if (!Stream.of(OpiManager.Command.values()).anyMatch((e) -> e.name().equalsIgnoreCase(cmd)))
      return OpiManager.error(OpiManager.BAD_COMMAND_FIELD);
    return switch (OpiManager.Command.valueOf(cmd.toUpperCase())) {
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
      Query queryRecord = new Query(settings.distance(), settings.viewMode(), settings.input(), settings.depth(), new double[] {1, 2}, null);
      return OpiManager.ok("OPI JOVP " + settings.machine() + " settings: " + queryRecord, false);
    } catch (Exception e) {
      return OpiManager.error("OPI JOVP " + settings.machine() + ": problem while querying.", e);
    }
  }

  /**
   * Start the psychoEngine
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet initialize() {
    return OpiManager.ok("OPI JOVP " + settings.machine() + ": initialized", false);
    //TODO: launch psychoEngine
/**
    try {
      psychoEngine = new PsychoEngine(new OpiLogic(bgSettings, stSettings, timer),
          settings.distance(), settings.viewMode(), settings.input(),
          Settings.PARADIGM, Settings.VALIDATION_LAYERS, Settings.API_DUMP);
      psychoEngine.setWindowMonitor(settings.screen());
      if (settings.fullScreen()) psychoEngine.setFullScreen();
      psychoEngine.start();
      return OpiManager.ok("OPI JOVP " + settings.machine() + ": initialized", false);
    } catch (Exception e) {
      return OpiManager.error("OPI JOVP " + settings.machine() + ": problem while initializing.", e);
    }
*/
  }

  /**
   * Change settings of background and fixation target
   * 
   * @param args A map of name:value pairs for parameters
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet setup(HashMap<String, Object> args) {
    try {
      bgRecord = Background.set(args);
      return OpiManager.ok("OPI JOVP " + settings.machine() + ": background and fixation target set", false);
    } catch (ClassCastException e) {
      return OpiManager.error("OPI JOVP " + settings.machine() + ": problem while setting background and fixation target.", e);
    }
  }

  /**
   * Present a stimulus
   *
   * @param args A map of name:value pairs for parameters
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet present(HashMap<String, Object> args) {
    try {
      stRecord = Stimulus.set(args);
      resRecord = new Response(true, 537, 0.3, 0.8, 6.2, 258);
      return OpiManager.ok("OPI JOVP " + settings.machine() + " present: " + resRecord, false);
    } catch (Exception e) {
      return OpiManager.error("OPI JOVP " + settings.machine() + ": problem while presenting.", e);
    }
  }

  /**
   * Stop the psychoEngine
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet close() {
    return OpiManager.ok("OPI JOVP " + settings.machine() + ": closed", true);
    //TODO: close psychoEngine
/**
    try {
      psychoEngine.cleanup();
      psychoEngine = null;
      return OpiManager.ok("OPI JOVP " + settings.machine() + ": closed", true);
    } catch (Exception e) {
      return return OpiManager.error("OPI JOVP " + settings.machine() + ": problem while closing.", e);
    }
*/
  }

}