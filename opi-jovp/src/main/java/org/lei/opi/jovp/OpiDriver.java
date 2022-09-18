package org.lei.opi.jovp;

import java.util.HashMap;
import java.util.stream.Stream;

import org.lei.opi.core.MessageProcessor;
import org.lei.opi.core.OpiManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The OPI JOVP driver
 *
 * @since 0.0.1
 */
public class OpiDriver extends MessageProcessor {

  /** A background record to communicate with OpiLogic */
  final Settings settings;
  /** A background record to communicate with OpiLogic */
  Background[] backgrounds;
  /** A stimulus record to communicate with OpiLogic */
  Stimulus stimulus;
  /** A record to record the results after a stimulus prsentation */
  Response response;

  /** Whether driver has been initialized */
  boolean initialized = false;

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
    switch (settings.viewMode()) {
      case MONO -> backgrounds = new Background[] {null};
      case STEREO -> backgrounds = new Background[] {null, null};
    }
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
    initialized = true;
    return OpiManager.ok("OPI JOVP " + settings.machine() + ": initialized", false);
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
      backgrounds[0] = Background.set(args);
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
      stimulus = Stimulus.set(args);
      response = new Response(true, 537, 0.3, 0.8, 6.2, 258);
      return OpiManager.ok("OPI JOVP " + settings.machine() + " present: " + response, false);
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
    initialized = false;
    return OpiManager.ok("OPI JOVP " + settings.machine() + ": closed", true);
  }

}
