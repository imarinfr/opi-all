package org.lei.opi.jovp;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.lei.opi.core.OpiListener;
import org.lei.opi.core.definitions.Packet;

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
 * As the psychoEngine has to be kicked off in the main thread, we need to have 
 * this application 'busy-waiting' on the main thread. Actions are triggered by
 * changing the `action` variable to values of `Action`.
 * 
                OpiListener Thread                      |               Main thread
                    (this class)                        |              (OpiLogic class)
                                                        |
Receive initialise --> action = SHOW                    |  SHOW----->  Create psychoEngine with OpiLogic
                                                        |
Receive query -------> if configuration & psychoengine  |  SETUP ----> Set backgrounds & fixations
                       exist, return data, else return  |
                       Jovp engine not ready            |  PRESENT --> Begin a stimulus presentation
                       Have you called initialise?      |
                                                        |  CLOSE ----> Shut down everything
Receive setup -------> Set some things                  |
                       action = SETUP                   |   null       Check if we are presenting or
                                                        |              waiting for a response.
Receive present------> action = PRESENT                 |              If we have a response, pass back
                       busy-wait until response is set  |              to the server (OpiJovp).
                       (ie do not change action)        |

  
 * @since 0.0.1
 */
public class OpiJovp extends OpiListener {

    /** {@value BAD_COMMAND} */
    private static final String BAD_COMMAND = "Wrong OPI command, you silly goose. OPI command received was: ";
    /** {@value INITIALIZED} */
    private static final String INITIALIZED = "INITIALIZE successful";
    /** {@value INITIALIZE_FAILED =} */
    private static final String INITIALIZE_FAILED = "INITIALIZE failed";
    /** {@value SETUP_FAILED} */
    private static final String SETUP_FAILED = "SETUP failed";
    /** {@value PRESENT_FAILED} */
    protected static final String PRESENT_FAILED = "An error occurred during PRESENT command";
    /** {@value NO_LEFT_BACKGROUND} */
    protected static final String NO_LEFT_BACKGROUND = "You have asked to PRESENT in the left/both eye/s but you have not called `setup` on the left/both eye/s.";
    /** {@value NO_RIGHT_BACKGROUND} */
    protected static final String NO_RIGHT_BACKGROUND = "You have asked to PRESENT in the right/both eye/s but you have not called `setup` on the right/both eye/s.";
    /** {@value CLOSED} */
    private static final String CLOSED = "CLOSE successful";
    /** {@value UNIMPLEMENTED_FORMAT} */
    private static final String UNIMPLEMENTED_FORMAT = "%s: Parameter %s is not implemented for value %s in function %s.";
   
    /** Prefix for all success messages */
    private String prefix;
    /** A configuration to communicate with OpiLogic */
    private Configuration configuration = null;
    /** The psychoEngine */
    private PsychoEngine psychoEngine;
    /** A background array to communicate with OpiLogic. backgrounds[0] is for left eye, [1] for right */
    private Setup[] backgrounds;
    /** An array of stimulus records that sit here for OpiLogic to interegate */
    private Stimulus[] stimuli;
    /** A record to record the results after a stimulus presentation */
    private Response response = null;

    /** Machine actions to trigger actions on the main thread. */
    public enum Action {
        SHOW,    // initialise obtained, configuration done, create psychoengine
        SETUP,   // psychoengine is up and running, execute setup 
        PRESENT, // begin a presentation
        CLOSE};  // all done

    private Action action;  // set by calls from the server OpiListner thread and acted upon on main thread (and reset to null there)

    public Configuration getConfiguration() { return configuration; }
    public Action getAction() { return action; }
    public Setup[] getBackgrounds() { return backgrounds; }
    public Stimulus getStimulus(int i) throws ArrayIndexOutOfBoundsException { return stimuli[i]; }
    public int getStimuliLength() { return stimuli.length; }

    public void setResponse(Response response) { this.response = response; }

    // Don't interrupt another action. Wait until it is finished (ie action is set to null by OPILogic)
    private void setAction(Action a) {
      while (this.action != null) {
        try { Thread.sleep(10); } catch (InterruptedException ignored) {; }
      }
      this.action = a;
    }
    public void setActionToNull() { action = null; }
   
    public OpiJovp(int port) { 
        super(port, null);   // do not give a machine to the OpiListener as we override the process() method here and the machine is not needed.
        this.action = null;
    } 

    /**
     * Run the psychoEngine. Needs to be started from the main thread
     * Connects in OpiLogic
     *
     * @since 0.1.0
     */
    public void startPsychoEngine() {
        // Have to start PsychoEngine on the main thread (as it uses GLFW)
        // so we cannot trigger it from the server OpiListener thread.
        // So we will just spin here on the main thread until we can progress (action == SHOW)
        while (this.action != Action.SHOW) {
          try { Thread.sleep(500);} catch(InterruptedException ignored) { ; }
        }

        if (configuration == null) {
            System.out.println("Cannot start the psychoengine with a null configuration");
            return;
        }

        psychoEngine = new PsychoEngine(new OpiLogic(this), configuration.distance(), Configuration.VALIDATION_LAYERS, Configuration.API_DUMP);

        psychoEngine.hide();
        psychoEngine.setMonitor(configuration.screen());

        if(configuration.physicalSize().length != 0)
            psychoEngine.setPhysicalSize(configuration.physicalSize()[0], configuration.physicalSize()[1]);

        if (configuration.fullScreen()) psychoEngine.setFullScreen();

        this.action = null; 
        psychoEngine.start(configuration.input(), Paradigm.CLICKER, configuration.viewMode());

        this.psychoEngine.cleanup();
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
            return Packet.error(prefix + Packet.BAD_JSON, e);
        }

        if (!pairs.containsKey("command")) // needs a command
            return Packet.error(prefix + OpiListener.NO_COMMAND_FIELD);
        String cmd = pairs.get("command").toString();

        // check it is a valid command from Command.*
        if (!Stream.of(OpiListener.Command.values()).anyMatch((e) -> e.name().equalsIgnoreCase(cmd)))
            return Packet.error(prefix + OpiListener.BAD_COMMAND_FIELD);

        return switch (OpiListener.Command.valueOf(cmd.toUpperCase())) {
            case INITIALIZE -> initialize(pairs);
            case QUERY -> query();
            case SETUP -> setup(pairs);
            case PRESENT -> present(pairs);
            case CLOSE -> close();
            default -> Packet.error(prefix + BAD_COMMAND + cmd.toUpperCase());
        };
    }

    /**
     * Start the psychoEngine with the SHOW action
     *
     * @since 0.1.0
     */
    private Packet initialize(HashMap<String, Object> args) {
        try {
            // get configuration
            configuration = Configuration.set(args);
            this.prefix = "OPI JOVP " + configuration.machine() + ": ";
            switch (configuration.viewMode()) {
              case MONO -> backgrounds = new Setup[] {null};
              case STEREO -> backgrounds = new Setup[] {null, null};
            }
            setAction(Action.SHOW);
            return new Packet(INITIALIZED);
        } catch (IllegalArgumentException | ClassCastException | IOException | NullPointerException e) {
            return Packet.error(INITIALIZE_FAILED, e);
        }
    }

  /**
   * Return results of query
   *
   * @since 0.1.0
   */
  private Packet query() {
    if (configuration == null || psychoEngine == null)
        return Packet.error("JOVP is not ready yet. Try again or call initialise()");

    Query q = new Query(configuration.distance(), psychoEngine.getFieldOfView(), configuration.viewMode(),
      configuration.input(), configuration.pseudoGray(), configuration.fullScreen(), configuration.tracking(),
      configuration.calibration().getMaxLum(), configuration.gammaFile(), psychoEngine.getWindow().getMonitor());
    return new Packet(q.toJson());
  }

  /**
   * Change settings of background and fixation target
   * trigger the SETUP action
   * Check for unimplemented values of fixShape.
   * 
   * @param args A map of name:value pairs for parameters. Should have all the fields for Setup class.
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

      if (args.containsKey("fixShape")) {
          String fs = (String)args.get("fixShape");
          if (List.of(new String[] {"HOLLOW_TRIANGLE", "HOLLOW_SQUARE", "HOLLOW_POLYGON", "ANNULUS", "OPTOTYPE", "TEXT", "MODEL"}).contains(fs.toUpperCase()))
            return Packet.error(String.format(UNIMPLEMENTED_FORMAT, prefix, "fixShape", fs, "setup()"));
      }

      setAction(Action.SETUP);
      return query();
    } catch (ClassCastException | IllegalArgumentException e) {
      return Packet.error(prefix + SETUP_FAILED, e);
    }
  }

    /**
     * Present a stimulus by
     *   (1) If 'eye' is specified, check the background relevant to that eye has been `setup`
     *   (2) Build the array if Stimulus objects
     *   (3) Check for unimplemented `type` and `shape`
     *   (4) Trigger the PRESENT action in OpiLogic and spin waiting for a response.
     *
     * @param args A map of name:value pairs for parameters
     *
     * @since 0.1.0
     */
    private Packet present(HashMap<String, Object> args) {
        if (args.containsKey("eye")) {
            List<Eye> eyes = ((List<String>)args.get("eye"))
                .stream()
                .map((String s) -> Eye.valueOf((s).toUpperCase()))
                .toList();
            for (Eye eye : eyes) {
                if ((eye == Eye.BOTH || eye == Eye.LEFT) && backgrounds[0] == null)
                    return Packet.error(prefix + NO_LEFT_BACKGROUND);
                if ((eye == Eye.BOTH || eye == Eye.RIGHT) && backgrounds[1] == null)
                    return Packet.error(prefix + NO_RIGHT_BACKGROUND);
            }
        }

        if (args.containsKey("shape"))
          for (String s : (List<String>)args.get("shape"))
            if (List.of(new String[] {"HOLLOW_TRIANGLE", "HOLLOW_SQUARE", "HOLLOW_POLYGON", "ANNULUS", "TEXT", "MODEL"}).contains(s.toUpperCase()))
                return Packet.error(String.format(UNIMPLEMENTED_FORMAT, prefix, "shape", s, "present()"));

        if (args.containsKey("type"))
          for (String s : (List<String>)args.get("type"))
              if (List.of(new String[] {"TEXT"}).contains(s.toUpperCase()))
                return Packet.error(String.format(UNIMPLEMENTED_FORMAT, prefix, "type", s, "present()"));
   
        try {
            stimuli = Stimulus.create(args);
            setAction(Action.PRESENT);
            while (response == null) {
                Thread.sleep(100);  // wait for response
            }
            String jsonStr = response.toJson(configuration.tracking());
            response = null;
            return new Packet(jsonStr);
        } catch (Exception e) {
            return Packet.error(prefix + PRESENT_FAILED, e);
        }
    }

  /**
   * Stop the psychoEngine and the socket server. (ie totally kill the JOVP with CLOSE action)
   *
   * @since 0.1.0
   */
  private Packet close() {
    setAction(Action.CLOSE);
    this.closeListener();   // this kills the server thread, so set action first.
    return new Packet(true, CLOSED);
  }

    // args[0] = port number
    // not opiJovp is `running` on a separate thread as a server
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar opiJovp.jar [port number]");
            System.exit(-1);
        }

        try {
            OpiJovp opiJovp = new OpiJovp(Integer.parseInt(args[0]));
            System.out.println("Machine address is " + opiJovp.getIP() + ":" + opiJovp.getPort());
            opiJovp.startPsychoEngine();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}