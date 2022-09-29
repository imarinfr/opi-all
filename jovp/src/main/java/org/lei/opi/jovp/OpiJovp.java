package org.lei.opi.jovp;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;

import org.lei.opi.core.OpiManager;
import org.lei.opi.core.OpiManager.Command;
import org.lei.opi.core.definitions.MessageProcessor;
import org.lei.opi.core.definitions.Present;
import org.lei.opi.core.definitions.Query;
import org.lei.opi.core.definitions.Response;
import org.lei.opi.core.definitions.Setup;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.structures.Eye;
import es.optocom.jovp.structures.ViewMode;

/**
 * The OPI JOVP driver
 *
 * @since 0.0.1
 */
public class OpiJovp extends MessageProcessor {

  /** Machine state */
  protected enum State {IDLE, INIT, SETUP, PRESENT, CLOSE};

  /** {@value BAD_COMMAND} */
  private static final String BAD_COMMAND = "Wrong OPI command, you silly goose. OPI command received was: ";
  /** {@value INITIALIZE} */
  private static final String INITIALIZE = "INITIALIZE successful";
  /** {@value SETUP_FAILED} */
  private static final String SETUP_FAILED = "SETUP failed";
  /** {@value PRESENT_FAILED} */
  protected static final String PRESENT_FAILED = "An error occured during PRESENT command";
  /** {@value CLOSED} */
  private static final String CLOSED = "CLOSE successful";

  /** Prefix for all success messages */
  protected final String prefix;
  /** A background record to communicate with OpiLogic */
  protected final Settings settings;
  /** The psychoEngine */
  private PsychoEngine psychoEngine = null;
  /** A background record to communicate with OpiLogic */
  protected Setup[] backgrounds;
  /** A stimulus record to communicate with OpiLogic */
  protected Present stimulus;
  /** A record to record the results after a stimulus prsentation */
  protected Response response = null;
  /** Whether opiInitialized has been invoked and not closed later on by opiClose */
  protected State state;

    /**
   * Start the OPI JOVP driver with default settings settings
   *
   * @param machine the OPI JOVP machine
   *
   * @throws IOException
   * 
   * @since 0.0.1
   */
  OpiJovp(Settings.Machine machine) throws IOException {
      this(Settings.defaultSettings(machine));
  }

  /**
   * Start the OPI JOVP comms and driver from a record of settings
   * 
   * @param machine the OPI JOVP machine
   * @param file the file path and name
   * 
   * @throws IOException If file does not exist
   * @throws IllegalArgumentException If any of the parameters in the settings file is wrong
   *
   * @since 0.0.1
   */
  OpiJovp(Settings.Machine machine, String file) throws IOException, IllegalArgumentException {
    this(Settings.load(machine, file));
  }

  /**
   * The OpiDriver
   *
   * @param settings Driver settings
   * 
   * @return the PsychoLogic for the PsychoEngine
   * 
   * @since 0.1.0
   */
  OpiJovp(Settings settings) {
    this.settings = settings;
    this.prefix = "OPI JOVP " + settings.machine() + ": ";
    switch (settings.viewMode()) {
      case MONO -> backgrounds = new Setup[] {null};
      case STEREO -> backgrounds = new Setup[] {null, null};
    }
  }

  /**
   * Initialize the psychoEngine. Needs to be started from the main thread
   *
   * @since 0.1.0
   */
  public void start() {
    psychoEngine = new PsychoEngine(new OpiLogic(this), settings.distance(), settings.viewMode(), settings.input(),
                                    Settings.PARADIGM, Settings.VALIDATION_LAYERS, Settings.API_DUMP);
    psychoEngine.hide();
    psychoEngine.setWindowMonitor(settings.screen());
    if (settings.fullScreen()) psychoEngine.setFullScreen();
    psychoEngine.start();
    psychoEngine.cleanup();
    psychoEngine = null;
  }

  /**
   * Signal the psychoEngine to finish
   *
   * @since 0.1.0
   */
  public void finish() {
    psychoEngine.finish();
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
      return OpiManager.error(prefix + OpiManager.BAD_JSON, e);
    }
    if (!pairs.containsKey("command")) // needs a command
      return OpiManager.error(prefix + OpiManager.NO_COMMAND_FIELD);
      String cmd = pairs.get("command").toString();
    // check it is a valid command from Command.*
    if (!Stream.of(Command.values()).anyMatch((e) -> e.name().equalsIgnoreCase(cmd)))
      return OpiManager.error(prefix + OpiManager.BAD_COMMAND_FIELD);
    return switch (Command.valueOf(cmd.toUpperCase())) {
      case INITIALIZE -> initialize();
      case QUERY -> query();
      case SETUP -> setup(pairs);
      case PRESENT -> present(pairs);
      case CLOSE -> close();
      default -> OpiManager.error(prefix + BAD_COMMAND + cmd.toUpperCase());
    };
  }

  /**
   * Start the psychoEngine
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet initialize() {
    state = State.INIT;
    return new MessageProcessor.Packet(prefix + INITIALIZE);
  }

  /**
   * Return results of query
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet query() {
    return OpiManager.ok((new Query(settings.distance(), psychoEngine.getFieldOfView(),
      settings.viewMode(), settings.input(),settings.fullScreen(), settings.tracking(), settings.depth(),
      settings.gammaFile(), psychoEngine.getWindow().getMonitor())).toJson());
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
      // Get eye for the instruction
      Eye eye = Eye.valueOf(((String) args.get("eye")).toUpperCase());
      if(settings.viewMode() == ViewMode.MONO || eye == Eye.BOTH || eye == Eye.LEFT)
        backgrounds[0] = Setup.set(args);
      if(settings.viewMode() == ViewMode.STEREO && (eye == Eye.BOTH || eye == Eye.RIGHT))
        backgrounds[1] = Setup.set(args);
      state = State.SETUP;
      return OpiManager.ok((new Query(settings.distance(), psychoEngine.getFieldOfView(),
        settings.viewMode(), settings.input(),settings.fullScreen(), settings.tracking(), settings.depth(),
        settings.gammaFile(), psychoEngine.getWindow().getMonitor())).toJson());
    } catch (ClassCastException | IllegalArgumentException e) {
      return OpiManager.error(prefix + SETUP_FAILED, e);
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
      stimulus = Present.set(args);
      state = State.PRESENT;
      while (response == null) Thread.onSpinWait(); // wait for response
      String jsonStr = response.toJson(settings.tracking());
      response = null;
      return OpiManager.ok(jsonStr);
    } catch (Exception e) {
      return OpiManager.error(prefix + PRESENT_FAILED, e);
    }
  }

  /**
   * Stop the psychoEngine
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet close() {
    state = State.CLOSE;
    return OpiManager.ok(CLOSED);
  }

}
