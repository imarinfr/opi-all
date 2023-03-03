package org.lei.opi.jovp;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;

import org.lei.opi.core.OpiListener;
import org.lei.opi.core.OpiMachine;
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
 * Makes use of the OpiListener to get a SocketServer thread, and cerates a fake OpiMachine
 * with a processPairs method that simply calls these local methods.
 *
 * @since 0.0.1
 */
public class OpiJovp {

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
    protected static final String PRESENT_FAILED = "An error occured during PRESENT command";
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

    private OpiListener server;

    public OpiJovp(int port) {
        this.server = new OpiListener(port);
        this.server.setMachine(new fakeOpiMachine()); 
        System.out.println("Machine address is " + this.server.getIP() + ":" + this.server.getPort());

        this.configuration = null;

        startPsychoEngine(); // TODO I suspect that this will take over and no messages will be processed, but let's see

        this.server.closeListener(); // assuming that startPsycho will be busy-waiting loop
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

    /*
    * A fake OpiMachine so that we can ust he machinery of OpiListener as a server.
    * The processPairs method is called by the OpiListener server, so we define that here.
    */
    class fakeOpiMachine extends OpiMachine {
        /** Settings junk we don't need */
        public static class Settings extends OpiMachine.Settings { };
        private Settings settings;
        public Settings getSettings() { return this.settings; }

        public fakeOpiMachine() {
            super(null); // no need to pass it a javafx.Scene.scene as there is no GUI here.
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
        public OpiListener.Packet processPairs(HashMap<String, Object> pairs) {
            if (!pairs.containsKey("command")) // needs a command
                 return OpiListener.error(prefix + OpiListener.NO_COMMAND_FIELD);
            String cmd = pairs.get("command").toString();

             // check it is a valid command from Command.*
            if (!Stream.of(OpiListener.Command.values()).anyMatch((e) -> e.name().equalsIgnoreCase(cmd)))
                 return OpiListener.error(prefix + OpiListener.BAD_COMMAND_FIELD);

            return switch (OpiListener.Command.valueOf(cmd.toUpperCase())) {
                 case INITIALIZE -> initialize(pairs);
                 case QUERY -> query();
                 case SETUP -> setup(pairs);
                 case PRESENT -> present(pairs);
                 case CLOSE -> close();
                 default -> OpiListener.error(prefix + BAD_COMMAND + cmd.toUpperCase());
             };
         }

        /**
         * Start the psychoEngine
         *
         * @since 0.1.0
         */
        public OpiListener.Packet initialize(HashMap<String, Object> args) {
            try {
            // get congiguration
            configuration = Configuration.set(args);
            prefix = "OPI JOVP " + configuration.machine() + ": ";
            switch (configuration.viewMode()) {
                case MONO -> backgrounds = new Setup[] {null};
                case STEREO -> backgrounds = new Setup[] {null, null};
            }
            return OpiListener.ok(INITIALIZED);
            } catch (IllegalArgumentException | ClassCastException | IOException e) {
            return OpiListener.error(INITIALIZE_FAILED, e);
            }
        }

        /**
         * Return results of query
         *
         * @since 0.1.0
         */
        public OpiListener.Packet query() {
            return OpiListener.ok((new Query(configuration.distance(), psychoEngine.getFieldOfView(), configuration.viewMode(),
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
        public OpiListener.Packet setup(HashMap<String, Object> args) {
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
            return OpiListener.error(prefix + SETUP_FAILED, e);
            }
        }

        /**
         * Present a stimulus
         *
         * @param args A map of name:value pairs for parameters
         *
         * @since 0.1.0
         */
        public OpiListener.Packet present(HashMap<String, Object> args) {
            try {
            stimulus = Present.set(args);
            state = State.PRESENT;
            while (response == null) Thread.onSpinWait(); // wait for response
            String jsonStr = response.toJson(configuration.tracking());
            response = null;
            return OpiListener.ok(jsonStr);
            } catch (Exception e) {
            return OpiListener.error(prefix + PRESENT_FAILED, e);
            }
        }

        /**
         * Stop the psychoEngine
         *
         * @since 0.1.0
         */
        public OpiListener.Packet close() {
            state = State.CLOSE;
            return OpiListener.ok(CLOSED, true);
        }
    }// end of fakeOpiMachine class

    // args[0] = port number
    public static void main(String args[]) {
        try {
            new OpiJovp(Integer.parseInt(args[0])); 
            //while (true) Thread.onSpinWait();  // not sure why, but there you go.
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
