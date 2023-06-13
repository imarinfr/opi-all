package org.lei.opi.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;

public abstract class Icare extends OpiMachine {
  
    /** Allowed eye values */
    public enum Eye {LEFT, RIGHT}
    /** Allowed fixation types */
    public enum Fixation {SPOT, SQUARE}
  
    /** {@value OPI_OPEN} */
    private static final String OPI_OPEN = "OPI-OPEN";
    /** {@value OPI_SET_FIXATION} */
    private static final String OPI_SET_FIXATION = "OPI-SET-FIXATION ";
    /** {@value OPI_SET_TRACKING} */
    private static final String OPI_SET_TRACKING = "OPI-SET-TRACKING ";
    /** {@value OPI_PRESENT_STATIC} */
    private static final String OPI_PRESENT_STATIC = "OPI-PRESENT-STATIC ";
    /** {@value OPI_CLOSE} */
    private static final String OPI_CLOSE = "OPI-CLOSE";
    /** {@value BAD_OPEN} */
    private static final String BAD_OPEN = "Bad open";
    /** {@value INVALID_FIXATION_SETTING} */
    private static final String INVALID_FIXATION_SETTING = "Fixation position %s is invalid for fixation type %s";
    /** {@value INVALID_TRACKING_SETTING} */
    private static final String INVALID_TRACKING_SETTING = "Tracking can only have value 0 (false) and 1 (true). It has ";
    /** {@value OPI_OPEN_FAILED} */
    private static final String OPI_OPEN_FAILED = "Problem with opening connection to iCare device. Probably need to restart.";
    /** {@value OPI_SET_FIXATION_FAILED} */
    private static final String OPI_SET_FIXATION_FAILED = "Problem with OPI-SET-FIXATION";
    /** {@value OPI_SET_TRACKING_FAILED} */
    private static final String OPI_SET_TRACKING_FAILED = "Problem with OPI-SET-TRACKING";
    /** {@value OPI_PRESENT_FAILED} */
    private static final String OPI_PRESENT_FAILED = "Problem with OPI-PRESENT";
  
    /** PRL x-coordinate measured on successful OPI-OPEN */
    private float prlx;
    /** PRL y-coordinate measured on successful OPI-OPEN */
    private float prly;
    /** ONH x-coordinate measured on successful OPI-OPEN */
    private float onhx;
    /** ONH y-coordinate measured on successful OPI-OPEN */
    private float onhy;
    /** IR Image captured on successful OPI-OPEN */
    private byte[] image;

    public static class Settings extends OpiMachine.Settings {
      public double minX;
      public double maxX;
      public double minY;
      public double maxY;
      public int minPressentationTime;
      public int maxPressentationTime;
      public int minResponseWindow;
      public int maxResponseWindow;
      public double backgroundLuminance;
      public double minLuminance;
      public double maxLuminance;
      public boolean tracking;
    };
    private Settings settings;
    public Settings getSettings() { return this.settings; }

    /*
     * @param parentScene The Scene to return to when this object is closed.
     *                    If null, then do not create a connection. (Used for GUI to probe class.)
     */
    public Icare(Scene parentScene) {
        super(parentScene);
        this.settings = (Settings) OpiMachine.fillSettings(this.getClass().getSimpleName());

        setVFCanvas(true, settings.tracking);

        if (parentScene != null)
          this.connect(settings.ip, settings.port);
    }
  
    /**
     * opiInitialise: initialize OPI
     * 
     * @param args A map of name:value pairs for Params
     * 
     * @return A JSON object with prl (x,y), onh (x,y) and Base64 encoded image
     * 
     * @since 3.0.0
     */
    @ReturnMsg(name = "res.prlx", desc = "The x-coordinate of the PRL measured initialisation (pixels).")
    @ReturnMsg(name = "res.prly", desc = "The y-coordinate of the PRL measured initialisation (pixels).")
    @ReturnMsg(name = "res.onhx", desc = "The x-coordinate of the ONH measured initialisation (pixels).")
    @ReturnMsg(name = "res.onhy", desc = "The y-coordinate of the ONH measured initialisation (pixels).")
    @ReturnMsg(name = "res.image", desc = "The captured IR image at initialisation (base64 encoded).")
    public Packet initialize(HashMap<String, Object> args) {
        try {
            System.out.println("outgoing " + this.outgoing);
            this.send(OPI_OPEN);

                // Need to temporarily read this.incoming with a DataInputStream (seems very risky!)
                // Is it possible for some other thread to read this.incoming while we are in this code chunk?
            int n = this.incoming.readInt();
            if (n < 4 * 4) // 4 floats for prlx, y, onhx, y 
                return Packet.error(OPI_OPEN_FAILED + " Did not get big enough 'n' back." );

            this.prlx = this.incoming.readFloat();
            this.prly = this.incoming.readFloat();
            this.onhx = this.incoming.readFloat();
            this.onhy = this.incoming.readFloat();

            this.image = null;
            int imageSize = n - 4 * 4;
            if (imageSize > 0) {
                this.image = new byte[imageSize];
                this.incoming.read(this.image, 0, imageSize);
            }
        } catch (IOException e) {
            return Packet.error(OPI_OPEN_FAILED + e);
        }

        if (parentScene != null) 
            Platform.runLater(()-> {
            textAreaCommands.appendText("OPI Initialized");
            textAreaCommands.appendText("\nPRL x " + this.prlx);
            textAreaCommands.appendText("\nPRL y " + this.prly);
            textAreaCommands.appendText("\nONH x " + this.onhx);
            textAreaCommands.appendText("\nONH y " + this.onhy);
            textAreaCommands.appendText("\n");
            if (this.image != null && this.settings.tracking) {
                Image image = new Image(new ByteArrayInputStream(this.image));
                imageView.setImage(image);
            }
            });

        return new Packet(queryResults());
    };
  
    /**
     * opiQuery: Query device
     * 
     * @return settings and state machine state
     *
     * @since 3.0.0
     */
    public Packet query() {
      String results = queryResults();

      if (parentScene != null)
        Platform.runLater(()-> {
          textAreaCommands.appendText("\nOPI Query:\n");
          textAreaCommands.appendText(results);
        });
      return new Packet(results);
    };
  
    /**
     * opiSetup: Change device background and overall settings
     * 
     * @param args pairs of argument name and value
     * 
     * @return A JSON object with return messages
     *
     * @since 3.0.0
     */
    @Parameter(name = "fixShape", className = Fixation.class, desc = "Fixation target type for eye.", optional = true, defaultValue = "SPOT")
    @Parameter(name = "fixCx", className = Double.class, desc = "x-coordinate of fixation target (degrees): Only valid values are -20, -6, -3, 0, 3, 6, 20 for fixation type 'spot' and -3, 0, 3 for fixation type 'square'.", optional = true, min = -20, max = 20, defaultValue = "0")
    @Parameter(name = "tracking", className = Double.class, desc = "Whether to correct stimulus location based on eye position.", optional = true, min = 0, max = 1, defaultValue = "0")
    public Packet setup(HashMap<String, Object> args) {
        if (!this.socket.isConnected()) return Packet.error(DISCONNECTED_FROM_HOST);

            // First the fixation marker
        int fixCx = (int) ((double) args.get("fixCx"));

        int fixShape = -1;
        switch((Fixation)args.get("fixShape")) {
            case SPOT -> {
              if (fixCx != 0 && Math.abs(fixCx) != 3 && Math.abs(fixCx) != 6 && Math.abs(fixCx) != 20)
                return Packet.error(String.format(INVALID_FIXATION_SETTING, fixCx, Fixation.SPOT));
              fixShape = 0;
            }
            case SQUARE -> {
              if (fixCx != 0 && Math.abs(fixCx) != 3)
                return Packet.error(String.format(INVALID_FIXATION_SETTING, fixCx, Fixation.SQUARE));
              fixShape = 1;
            }
        };
        try {
            this.send(OPI_SET_FIXATION + fixCx + " 0 " + fixShape);
            String result = this.readline();
            if (!result.equals("0"))
                return Packet.error(OPI_SET_FIXATION_FAILED + result);
        } catch (IOException e) {
            return Packet.error(OPI_SET_FIXATION_FAILED, e);
        }
        if (parentScene != null) 
        Platform.runLater(()-> {
            textAreaCommands.appendText("OPI Setup\n");
            textAreaCommands.appendText("\tSet Fixation to " + args.get("fixShape") + " at " + fixCx + "\n");
        });

            // And then the tracking
        int tracking = (int) ((double) args.get("tracking")); /// TODO might want to alter the canvas
        if (tracking != 0 && tracking != 1) 
            return Packet.error(INVALID_TRACKING_SETTING + tracking);
        try {
            this.send(OPI_SET_TRACKING + tracking);
            String result = this.readline();
            if (!result.equals("0"))
                return Packet.error(OPI_SET_TRACKING_FAILED + result);
            this.settings.tracking = true;
        } catch (IOException e) {
            return Packet.error(OPI_SET_TRACKING_FAILED, e);
        }
        if (parentScene != null) 
        Platform.runLater(()-> {
            textAreaCommands.appendText("\tTracking " + (this.settings.tracking ? "On" : "Off") + "\n");
        });

        return new Packet(queryResults());
    }
  
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * 
     * @param args pairs of argument name and value
     * 
     * @return A JSON object with return messages
     *
     * @since 3.0.0
     */
    @Parameter(name = "x", className = Double.class, desc = "x co-ordinates of stimulus (degrees).", min = -30, max = 30, defaultValue = "0")
    @Parameter(name = "y", className = Double.class, desc = "y co-ordinates of stimulus (degrees).", min = -30, max = 30, defaultValue = "0")
    @Parameter(name = "lum", className = Double.class, desc = "Stimuli luminance (cd/m^2).", min = 0, max = 3183.099, defaultValue = "100")
    @Parameter(name = "t", className = Double.class, desc = "Presentation time (ms).", min = 200, max = 200, defaultValue = "200")
    @Parameter(name = "w", className = Double.class, desc = "Response window (ms).", min = 200, max = 2680, defaultValue = "1500")
    @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "res.eyex", className = Double.class, desc = "x co-ordinates of pupil at times eyet (pixels).")
    @ReturnMsg(name = "res.eyey", className = Double.class, desc = "y co-ordinates of pupil at times eyet (pixels).")
    @ReturnMsg(name = "res.eyed", className = Double.class, desc = "Diameter of pupil at times eyet (mm).")
    @ReturnMsg(name = "res.eyet", className = Double.class, desc = "Time of (eyex, eyey) pupil from stimulus onset (ms).", min = 0)
    @ReturnMsg(name = "res.time_rec", className = Double.class, desc = "Time since 'epoch' when command was received at Compass or Maia (ms).", min = 0)
    @ReturnMsg(name = "res.time_resp", className = Double.class, desc = "Time since 'epoch' when stimulus response is received, or response window expired (ms).", min = 0)
    @ReturnMsg(name = "res.num_track_events", className = Double.class, desc = "Number of tracking events that occurred during presentation.", min = 0)
    @ReturnMsg(name = "res.num_motor_fails", className = Double.class, desc = "Number of times motor could not follow fixation movement during presentation.", min = 0)
    public Packet present(HashMap<String, Object> args) {
        if (!this.socket.isConnected()) return Packet.error(DISCONNECTED_FROM_HOST);
        try {
            int level = (int) Math.round(-10 * Math.log10((double) args.get("lum") / (10000 / Math.PI)));
            StringBuilder opiMessage = new StringBuilder(OPI_PRESENT_STATIC).append(" ")
                .append((int) ((double) args.get("x"))).append(" ")
                .append((int) ((double) args.get("y"))).append(" ")
                .append(level).append(" 3 ")
                .append((int) ((double) args.get("t"))).append(" ")
                .append((int) ((double) args.get("w")));
                try {
                      this.send(opiMessage.toString());
                      updateGUIOnPresent(args);
                      return new Packet(parseResult(this.readline())); 
                } catch (IOException e) {
                      return Packet.error(OPI_PRESENT_FAILED, e);
                }
          } catch (ClassCastException | IllegalArgumentException e) {
          return Packet.error(OPI_PRESENT_FAILED, e);
        }
    }
  
    /**
     * opiClose: Close OPI connection
     * 
     * @param args pairs of argument name and value
     *
     * @return A JSON object with return messages
     *
     * @since 3.0.0
     */
    @ReturnMsg(name = "res.time", desc = "The time stamp for fixation data")
    @ReturnMsg(name = "res.x", desc = "The time stamp for fixation data")
    @ReturnMsg(name = "res.y", desc = "The time stamp for fixation data")
    public Packet close() {
        try {
            this.send(OPI_CLOSE);
           
            int n = this.incoming.readInt();
            if (n == 0 || n % 12 != 0) 
                return Packet.error("OPI_CLOSE did not receive the bytes it was expecting." );
            
            StringBuffer sb = new StringBuffer((3 + 6 + 5 + 5) * n / 12);   // guestimate of length
            for (int i = 0 ; i < n ; i += 12) {
                int t = this.incoming.readInt();
                float x = this.incoming.readFloat();
                float y = this.incoming.readFloat();
           
                sb.append("\n,{");
                sb.append(String.format("\"time\" : %s,", t));
                sb.append(String.format("\"posx\" : %s,", x));
                sb.append(String.format("\"posy\" : %s", y));
                sb.append("}");
            }
            sb.delete(0, 2); // remove leading "\n,"
           
            this.closeSocket();

            if (parentScene != null) 
            Platform.runLater(()-> {
                textAreaCommands.appendText("\nOPI Closed\n");
                textAreaCommands.appendText(sb.toString());
            });
            return new Packet(true, "[" + sb.toString() + "]");
        } catch (ClassCastException | IllegalArgumentException | IOException e) {
            return Packet.error(COULD_NOT_DISCONNECT, e);
        }
    };
  
    /**
     * Parse query results
     * 
     * @return A JSON object with query results
     *
     * @since 3.0.0
     */
    private String queryResults() {
        String image = "";
        if (this.image != null) {
            Base64.Encoder encoder = Base64.getEncoder();
            image = encoder.encodeToString(this.image);
        }
        return new StringBuilder("\n  {\n")
            .append("    \"prlx\": " + prlx + ",\n")
            .append("    \"prly\": " + prly + ",\n")
            .append("    \"onhx\": " + onhx + ",\n")
            .append("    \"onhy\": " + onhy + ",\n")
            .append("    \"image\": " + image + ",\n")
            .append("    \"minX\": " + settings.minX + ",\n")
            .append("    \"maxX\": " + settings.maxX + ",\n")
            .append("    \"minY\": " + settings.minY + ",\n")
            .append("    \"maxY\": " + settings.maxY + ",\n")
            .append("    \"minPressentationTime\": " + settings.minPressentationTime + ",\n")
            .append("    \"maxPressentationTime\": " + settings.maxPressentationTime + ",\n")
            .append("    \"minResponseWindow\": " + settings.minResponseWindow + ",\n")
            .append("    \"maxResponseWindow\": " + settings.maxResponseWindow+ ",\n")
            .append("    \"backgroundLuminance\": " + settings.backgroundLuminance + ",\n")
            .append("    \"minLuminance\": " + settings.minLuminance + ",\n")
            .append("    \"maxLuminance\": " + settings.maxLuminance + ",\n")
            .append("    \"tracking\": " + settings.tracking + "\n")
            .append("\n  }").toString();
    };
  
    /**
     * Parse results obtained for OPI-PRESENT-STATIC
     * 
     * @param received Message received from Icare machine
     * 
     * @return A JSON object with present results
     *
     * @since 3.0.0
     */
    private String parseResult(String received) {
      String[] message = received.split(" ");
      if (message[0] != "0") Packet.error(OPI_PRESENT_FAILED + "Error code received is: " + message[0]);
      return new StringBuilder("\n  {\n")
        .append("    \"seen\": " + message[1] + ",\n")
        .append("    \"time\": " + message[2] + ",\n")
        .append("    \"eyex\": " + message[9] + ",\n")
        .append("    \"eyey\": " + message[10] + ",\n")
        .append("    \"eyed\": " + message[8] + ",\n")
        .append("    \"eyet\": " + message[3] + ",\n")
        .append("    \"time_rec\": " + message[4] + ",\n")
        .append("    \"time_resp\": " + message[5] + ",\n")
        .append("    \"num_track_events\": " + message[6] + ",\n")
        .append("    \"num_motor_fails\": " + message[7] + ",\n  }").toString();
    }
}
