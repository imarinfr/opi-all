package org.lei.opi.jovp;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;

import org.lei.opi.core.CSListener;
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
import es.optocom.jovp.definitions.Eye;
import es.optocom.jovp.definitions.Paradigm;
import es.optocom.jovp.definitions.ViewMode;

/**
 * The OPI JOVP driver
 *
 * @since 0.0.1
 */
public class OpiJovp extends MessageProcessor {

  /** Machine state */
  protected enum State {INIT, SETUP, PRESENT, CLOSE};

  /** {@value BAD_COMMAND} */
  private static final String BAD_COMMAND = "Wrong OPI command, you silly goose. OPI command received was: ";
  /** {@value INITIALIZED} */
  private static final String INITIALIZED = "\"INITIALIZE successful\"";
  /** {@value INITIALIZE_FAILED =} */
  private static final String INITIALIZE_FAILED = "\"INITIALIZE failed. \"";
  /** {@value SETUP_FAILED} */
  private static final String SETUP_FAILED = "SETUP failed";
  /** {@value PRESENT_FAILED} */
  protected static final String PRESENT_FAILED = "An error occured during PRESENT command";
  /** {@value CLOSED} */
  private static final String CLOSED = "\"CLOSE successful\"";

  /** Prefix for all success messages */
  protected String prefix;
  /** A background record to communicate with OpiLogic */
  protected Configuration configuration = null;
  /** The psychoEngine */
  private PsychoEngine psychoEngine;
  /** A background record to communicate with OpiLogic */
  protected Setup[] backgrounds;
  /** A stimulus record to communicate with OpiLogic */
  protected Present stimulus;
  /** A record to record the results after a stimulus prsentation */
  protected Response response = null;
  /** Whether opiInitialized has been invoked and not closed later on by opiClose */
  protected State state;

  /**
   * Run the psychoEngine. Needs to be started from the main thread
   *
   * @since 0.1.0
   */
  public void start() {
    // not great, but necessary: wait until INITIALIZE command has been triggered
    while (configuration == null) Thread.onSpinWait();
    psychoEngine = new PsychoEngine(new OpiLogic(this), configuration.distance(),
    Configuration.VALIDATION_LAYERS, Configuration.API_DUMP);
    psychoEngine.hide();
    psychoEngine.setMonitor(configuration.screen());
    if(configuration.physicalSize().length != 0)
    psychoEngine.setPhysicalSize(configuration.physicalSize()[0], configuration.physicalSize()[1]);
    if (configuration.fullScreen()) psychoEngine.setFullScreen();
    state = State.INIT;
    psychoEngine.start(configuration.input(), Paradigm.CLICKER, configuration.viewMode());
    psychoEngine.cleanup();
    configuration = null;
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
      case INITIALIZE -> initialize(pairs);
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
  private MessageProcessor.Packet initialize(HashMap<String, Object> args) {
    try {
      // get congiguration
      configuration = Configuration.set(args);
      this.prefix = "OPI JOVP " + configuration.machine() + ": ";
      switch (configuration.viewMode()) {
        case MONO -> backgrounds = new Setup[] {null};
        case STEREO -> backgrounds = new Setup[] {null, null};
      }
      return OpiManager.ok(INITIALIZED);
    } catch (IllegalArgumentException | ClassCastException | IOException e) {
      return OpiManager.error(INITIALIZE_FAILED, e);
    }
  }

  /**
   * Return results of query
   *
   * @since 0.1.0
   */
  private MessageProcessor.Packet query() {
    return OpiManager.ok((new Query(configuration.distance(), psychoEngine.getFieldOfView(), configuration.viewMode(),
      configuration.input(), configuration.pseudoGray(), configuration.fullScreen(), configuration.tracking(),
      configuration.calibration().maxLum(), configuration.gammaFile(), psychoEngine.getWindow().getMonitor())).toJson());
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
      if(configuration.viewMode() == ViewMode.MONO || eye == Eye.BOTH || eye == Eye.LEFT)
        backgrounds[0] = Setup.set(args);
      if(configuration.viewMode() == ViewMode.STEREO && (eye == Eye.BOTH || eye == Eye.RIGHT))
        backgrounds[1] = Setup.set(args);
      state = State.SETUP;
      return query();
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
      String jsonStr = response.toJson(configuration.tracking());
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

  public static void main(String args[]) {
    try {
      CSListener listener = new CSListener(Integer.parseInt(args[0]), new OpiJovp());
      System.out.println("Machine address is " + listener.getIP() + ":" + listener.getPort());
      while (true) Thread.onSpinWait();
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }
}
