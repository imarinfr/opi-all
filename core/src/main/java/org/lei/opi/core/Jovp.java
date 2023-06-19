package org.lei.opi.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.lei.opi.core.OpiListener.Command;
import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;

import es.optocom.jovp.definitions.ViewMode;

/**
 * JOVP client - will send messages to JOVP server...
 *
 * @since 0.0.1
 */
public abstract class Jovp extends OpiMachine {

    public static class Settings extends OpiMachine.Settings {
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

    /**
     * @param parentScene The Scene to return to when this object is closed.
     *                    If null, then do not create a connection. (Used for GUI to probe class.)
     * @throws InstantiationException if cannot connect to JOVP server
     */
    public Jovp(javafx.scene.Scene parentScene) throws InstantiationException {
        super(parentScene);
        this.settings = (Settings) OpiMachine.fillSettings(this.getClass().getSimpleName());

        setVFCanvas(
          settings.viewMode.toLowerCase().equals(ViewMode.MONO.toString().toLowerCase()),
          settings.tracking
        );

        if (parentScene != null) {
            if (!this.connect(settings.ip, settings.port)) {
              throw new InstantiationException(String.format("Cannot connect to %s:%s", settings.ip, settings.port));
            }
        }
    }

    /**
    * opiInitialise: initialize OPI
    * 
    * @param args A map of name:value pairs for Params - ignored here.
    * 
    * @return A JSON object with machine specific initialise information
    * 
    * @since 0.0.1
    */
    public Packet initialize(HashMap<String, Object> args) {
        try {
            this.send(initConfiguration());
            Packet p = this.receive();
            return p;
        } catch (IOException e) {
            return Packet.error(COULD_NOT_INITIALIZE, e);
        }
    }

    /**
     * opiQuery: Query device
     * 
     * @return settings and state machine state
     *
     * @since 0.0.1
     */
    public Packet query() {
        if (!this.socket.isConnected()) return Packet.error(DISCONNECTED_FROM_HOST);
        try {
            String q = toJson(Command.QUERY);
            this.send(q);
            Packet rec = this.receive();
            return rec;
        } catch (ClassCastException | IllegalArgumentException | IOException e) {
            return Packet.error(COULD_NOT_QUERY, e);
        }
    };

    /**
     * opiSetup: Change device background and overall settings
     * All of the @Parameters here should be fields in the jovp.Setup class.
     * 
     * @param args pairs of argument name and value
     * 
     * @return A JSON object with return messages
     *
     * @since 0.0.1
     */
    @Parameter(name = "eye", className = es.optocom.jovp.definitions.Eye.class, desc = "The eye for which to apply the settings.", defaultValue = "BOTH")
    @Parameter(name = "bgLum", className = Double.class, desc = "Background luminance for eye (cd/m^2).", min = 0, defaultValue = "10")
    @Parameter(name = "bgCol", className = Double.class, desc = "Background color for eye (rgb).", isList = true, min = 0, max = 1, defaultValue = "[1,1,1]")
    @Parameter(name = "fixShape", className = es.optocom.jovp.definitions.ModelType.class, desc = "Fixation target type for eye.", defaultValue = "MALTESE")
    @Parameter(name = "fixLum", className = Double.class, desc = "Fixation target luminance for eye.", min = 0, defaultValue = "20")
    @Parameter(name = "fixCol", className = Double.class, desc = "Fixation target color for eye.", isList = true, min = 0, max = 1, defaultValue = "[0,1,0]")
    @Parameter(name = "fixCx", className = Double.class, desc = "x-coordinate of fixation target (degrees).", min = -90, max = 90, defaultValue = "0")
    @Parameter(name = "fixCy", className = Double.class, desc = "y-coordinate of fixation target (degrees).", min = -90, max = 90, defaultValue = "0")
    @Parameter(name = "fixSx", className = Double.class, desc = "diameter along major axis of ellipse (degrees).", min = 0, defaultValue = "1")
    @Parameter(name = "fixSy", className = Double.class, desc = "diameter along minor axis of ellipse (degrees). If not received, then sy = sx.", optional = true, min = 0, defaultValue = "1")
    @Parameter(name = "fixRotation", className = Double.class, desc = "Angles of rotation of fixation target (degrees). Only useful if sx != sy specified.", optional = true, min = 0, max = 360, defaultValue = "0")
    @Parameter(name = "tracking", className = Integer.class, desc = "Whether to correct stimulus location based on eye position.", optional = true, min = 0, max = 1, defaultValue = "0")
    public Packet setup(HashMap<String, Object> args) {
      if (!this.socket.isConnected()) return Packet.error(DISCONNECTED_FROM_HOST);
      try {
        Packet p = validateArgs(args, this.opiMethods.get("setup").parameters(), "setup");
          if (p.getError()) 
            return(p);
        this.send(OpiListener.gson.toJson(args));
        return this.receive();
      } catch (IOException e) {
        return Packet.error(COULD_NOT_SETUP, e);
      }
    }
   
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * All of the @Parameters should be the fields of the jovp.Present class.  
     *
     * @param args pairs of argument name and value
     * 
     * @return A JSON object with return messages
     *
     * @since 0.0.1
     */
    @Parameter(name = "stim.length", className = Integer.class, desc = "The number of elements in this stimuli.", isList = false, min = 1, defaultValue = "1")
    @Parameter(name = "eye", className = es.optocom.jovp.definitions.Eye.class, desc = "The eye for which to apply the settings.", isList = true, defaultValue = "[LEFT]")
    @Parameter(name = "x", className = Double.class, desc = "List of x co-ordinates of stimuli (degrees).", isList = true, min = -90, max = 90, defaultValue = "[0]")
    @Parameter(name = "y", className = Double.class, desc = "List of y co-ordinates of stimuli (degrees).", isList = true, min = -90, max = 90, defaultValue = "[0]")
    @Parameter(name = "sx", className = Double.class, desc = "List of diameters along major axis of ellipse (degrees).", isList = true, min = 0, max = 180, defaultValue = "[1.72]")
    @Parameter(name = "sy", className = Double.class, desc = "List of diameters along minor axis of ellipse (degrees). If not received, then sy = sx", isList = true, optional = true, min = 0, max = 180, defaultValue = "[1.72]")
    @Parameter(name = "t", className = Double.class, desc = "List of stimuli presentation times (ms).", isList = true, min = 0, defaultValue = "[200]")
    @Parameter(name = "w", className = Double.class, desc = "Time to wit for response including presentation time (ms).", isList = false, min = 0, defaultValue = "1500")
    @Parameter(name = "lum", className = Double.class, desc = "List of stimuli luminances (cd/m^2).", isList = true, min = 0, defaultValue = "[20]")
    @Parameter(name = "colorMin", className = Double.class, desc = "List of stimulus min colors for non-FLAT shapes.", isListList = true, optional = true, min = 0, max = 1, defaultValue = "[[0,0,0]]")
    @Parameter(name = "colorMax", className = Double.class, desc = "List of stimulus max colors for all shapes", isListList = true, min = 0, max = 1, defaultValue = "[[1,1,1]]")
    @Parameter(name = "rotation", className = Double.class, desc = "List of angles of rotation of stimuli (degrees). Only useful if sx != sy specified.", isList = true, optional = true, min = 0, max = 360, defaultValue = "[0]")
    @Parameter(name = "contrast", className = Double.class, desc = "List of stimulus contrasts (from 0 to 1). Only useful if type != FLAT.", isList = true, optional = true, min = 0, max = 1, defaultValue = "[1]")
    @Parameter(name = "phase", className = Double.class, desc = "List of phases (in degrees) for generation of spatial patterns. Only useful if type != FLAT", isList = true, optional = true, min = 0, defaultValue = "[0]")
    @Parameter(name = "frequency", className = Double.class, desc = "List of frequencies (in cycles per degrees) for generation of spatial patterns. Only useful if type != FLAT", isList = true, optional = true, min = 0, max = 300, defaultValue = "[0]")
    @Parameter(name = "defocus", className = Double.class, desc = "List of defocus values in Diopters for stimulus post-processing.", isList = true, optional = true, min = 0, defaultValue = "[0]")
    @Parameter(name = "texRotation", className = Double.class, desc = "List of angles of rotation of stimuli (degrees). Only useful if type != FLAT", isList = true, optional = true, min = 0, max = 360, defaultValue = "[0]")
    @Parameter(name = "shape", className = es.optocom.jovp.definitions.ModelType.class, desc = "Stimulus shape. Values include CROSS, TRIANGLE, CIRCLE, SQUARE, OPTOTYPE.", isList = true, optional = true, defaultValue = "[CIRCLE]")
    @Parameter(name = "type", className = es.optocom.jovp.definitions.TextureType.class, desc = "Stimulus type. Values include FLAT, SINE, CHECKERBOARD, SQUARESINE, G1, G2, G3, IMAGE", isList = true, optional = true, defaultValue = "[FLAT]")
    @Parameter(name = "imageFilename", className = String.class, desc = "If type == IMAGE, the filename on the local filesystem of the machine running JOVP of the image to use", isList = true, optional = true, defaultValue = "[FLAT]")
    @Parameter(name = "optotype", className = es.optocom.jovp.definitions.Optotype.class, desc = "If shape == OPTOTYPE, the letter A to Z to use", isList = true, optional = true, defaultValue = "[E]")
    @ReturnMsg(name = "res.msg.eyex", className = Double.class, desc = "x co-ordinates of pupil at times eyet (degrees).")
    @ReturnMsg(name = "res.msg.eyey", className = Double.class, desc = "y co-ordinates of pupil at times eyet (degrees).")
    @ReturnMsg(name = "res.msg.eyed", className = Double.class, desc = "Diameter of pupil at times eyet (mm).")
    @ReturnMsg(name = "res.msg.eyet", className = Double.class, desc = "Time of (eyex, eyey) pupil from stimulus onset (ms).", min = 0)
    public Packet present(HashMap<String, Object> args) {
      if (!this.socket.isConnected()) return Packet.error(DISCONNECTED_FROM_HOST);
      try {
        Packet p = validateArgs(args, this.opiMethods.get("present").parameters(), "present");
        if (p.getError()) 
            return(p);
        this.send(OpiListener.gson.toJson(args));
        return this.receive();
      } catch (IOException e) {
        return Packet.error(COULD_NOT_PRESENT, e);
      }
    }

    /**
    * opiClose: Send close to Jovp and close my socket to it.
    * 
    * @param args pairs of argument name and value
    *
    * @return A JSON object with return messages
    *
    * @since 0.0.1
    */
    public Packet close() {
      if (!this.socket.isConnected()) return Packet.error(DISCONNECTED_FROM_HOST);

      try {
          this.send(toJson(Command.CLOSE));   // this will close the server, so no messages coming back
          this.closeSocket();
      } catch (IOException e) {
          return Packet.error(COULD_NOT_CLOSE, e);
      }
      return new Packet(true, DISCONNECTED_FROM_HOST);
    }

    /** This is what the JOVP Server/Machine is expecting for the Initialize Command */
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
}  