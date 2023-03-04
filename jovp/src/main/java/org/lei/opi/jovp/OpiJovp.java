package org.lei.opi.jovp;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;

import org.lei.opi.core.OpiListener;
import org.lei.opi.core.definitions.Present;
import org.lei.opi.core.definitions.Query;
import org.lei.opi.core.definitions.Response;
import org.lei.opi.core.definitions.Setup;

import com.google.gson.JsonSyntaxException;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.definitions.Eye;
import es.optocom.jovp.definitions.Paradigm;
import es.optocom.jovp.definitions.ViewMode;

/**
 * The OPI JOVP server.
 * Makes use of the OpiListener to get a SocketServer thread, but does not give it an OpiMachine, 
 * instead overriding the process() method here to not make use of an OpiMachine.
 *
 * @since 0.0.1
 */
public class OpiJovp extends OpiListener {

  /** Machine state */
  protected enum State {INIT, SETUP, PRESENT, CLOSE};

  /** {@value BAD_COMMAND} */
  private static final String BAD_COMMAND = "Wrong OPI command, you silly goose. OPI command received was: ";
  /** {@value INITIALIZED} */
  private static final String INITIALIZED = "INITIALIZE successful";
  /** {@value INITIALIZE_FAILED =} */
  private static final String INITIALIZE_FAILED = "INITIALIZE failed. ";
  /** {@value SETUP_FAILED} */
  private static final String SETUP_FAILED = "SETUP failed";
  /** {@value PRESENT_FAILED} */
  protected static final String PRESENT_FAILED = "An error occurred during PRESENT command";
  /** {@value CLOSED} */
  private static final String CLOSED = "CLOSE successful";

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

  public OpiJovp(int port) { 
    super(port, null);   // do not give a machine to the OpiListner as we overide the process() method here and the machine is not needed.
  } 

    /**
     * Run the psychoEngine. Needs to be started from the main thread
     *
     * @since 0.1.0
     */
    public void startPsychoEngine() {
        // not great, but necessary: wait until INITIALIZE command has been triggered
        while (configuration == null) Thread.onSpinWait();

        psychoEngine = new PsychoEngine(new OpiLogic(this), configuration.distance(), Configuration.VALIDATION_LAYERS, Configuration.API_DUMP);

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
    @Override
    public Packet process(String jsonStr) {
        HashMap<String, Object> pairs;
        try {
            pairs = OpiListener.jsonToPairs(jsonStr);
        } catch (JsonSyntaxException e) {
            return error(prefix + BAD_JSON, e);
        }

        if (!pairs.containsKey("command")) // needs a command
            return error(prefix + OpiListener.NO_COMMAND_FIELD);
        String cmd = pairs.get("command").toString();

        // check it is a valid command from Command.*
        if (!Stream.of(OpiListener.Command.values()).anyMatch((e) -> e.name().equalsIgnoreCase(cmd)))
            return error(prefix + OpiListener.BAD_COMMAND_FIELD);

        return switch (OpiListener.Command.valueOf(cmd.toUpperCase())) {
            case INITIALIZE -> initialize(pairs);
            case QUERY -> query();
            case SETUP -> setup(pairs);
            case PRESENT -> present(pairs);
            case CLOSE -> close();
            default -> error(prefix + BAD_COMMAND + cmd.toUpperCase());
        };
    }

  /**
   * Start the psychoEngine
   *
   * @since 0.1.0
   */
  private Packet initialize(HashMap<String, Object> args) {
    try {
      // get congiguration
      configuration = Configuration.set(args);
      this.prefix = "OPI JOVP " + configuration.machine() + ": ";
      switch (configuration.viewMode()) {
        case MONO -> backgrounds = new Setup[] {null};
        case STEREO -> backgrounds = new Setup[] {null, null};
      }
      return ok(INITIALIZED);
    } catch (IllegalArgumentException | ClassCastException | IOException e) {
      return error(INITIALIZE_FAILED, e);
    }
  }

  /**
   * Return results of query
   *
   * @since 0.1.0
   */
  private Packet query() {
    return ok((new Query(configuration.distance(), psychoEngine.getFieldOfView(), configuration.viewMode(),
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
  private Packet setup(HashMap<String, Object> args) {
    try {
      // Get eye for the instruction
      Eye eye = Eye.valueOf(((String) args.get("eye")).toUpperCase());
      if(configuration.viewMode() == ViewMode.MONO || eye == Eye.BOTH || eye == Eye.LEFT)
        backgrounds[0] = Setup.create2(args);
      if(configuration.viewMode() == ViewMode.STEREO && (eye == Eye.BOTH || eye == Eye.RIGHT))
        backgrounds[1] = Setup.create2(args);
      state = State.SETUP;
      return query();
    } catch (ClassCastException | IllegalArgumentException e) {
      return error(prefix + SETUP_FAILED, e);
    }
  }

  /**
   * Present a stimulus
   *
   * @param args A map of name:value pairs for parameters
   *
   * @since 0.1.0
   */
  private Packet present(HashMap<String, Object> args) {
    try {
      stimulus = Present.set(args);
      state = State.PRESENT;
      while (response == null) Thread.onSpinWait(); // wait for response
      String jsonStr = response.toJson(configuration.tracking());
      response = null;
      return ok(jsonStr);
    } catch (Exception e) {
      return error(prefix + PRESENT_FAILED, e);
    }
  }

  /**
   * Stop the psychoEngine
   *
   * @since 0.1.0
   */
  private Packet close() {
    state = State.CLOSE;
    this.closeListener();    // TODO revisit
    return ok(CLOSED);
  }

    // args[0] = port number
    public static void main(String args[]) {
        try {
            OpiJovp opiJovp = new OpiJovp(Integer.parseInt(args[0]) );
            System.out.println("Machine address is " + opiJovp.getIP() + ":" + opiJovp.getPort());
           
            opiJovp.startPsychoEngine(); // TODO I suspect that this will take over and no messages will be processed, but let's see
           
            //while (true) Thread.onSpinWait();  // not sure why, but there you go.
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
