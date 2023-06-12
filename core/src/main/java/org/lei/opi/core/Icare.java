package org.lei.opi.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;

public class Icare extends OpiMachine {
  
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
    private static final String OPI_OPEN_FAILED = "Problem with OPI-OPEN";
    /** {@value OPI_SET_FIXATION_FAILED} */
    private static final String OPI_SET_FIXATION_FAILED = "Problem with OPI-SET-FIXATION";
    /** {@value OPI_SET_TRACKING_FAILED} */
    private static final String OPI_SET_TRACKING_FAILED = "Problem with OPI-SET-TRACKING";
    /** {@value OPI_PRESENT_FAILED} */
    private static final String OPI_PRESENT_FAILED = "Problem with OPI-PRESENT";
  
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

        setVFCanvas(true, trackingOn);

        if (parentScene != null)
          this.connect(settings.ip, settings.port);
    }
  
    /**
     * opiInitialise: initialize OPI
     * 
     * @param args A map of name:value pairs for Params
     * 
     * @return A JSON object with machine specific initialise information
     * 
     * @since 0.0.1
     */
    public Packet initialize(HashMap<String, Object> args) {
      if (parentScene != null)
        Platform.runLater(()-> {
          textAreaCommands.appendText("OPI Initialized");
        });
      return new Packet(String.format(CONNECTED_TO_HOST, settings.ip, settings.port));
    };
  
    /**
     * opiQuery: Query device
     * 
     * @return settings and state machine state
     *
     * @since 0.0.1
     */
    public Packet query() {
      String results = queryResults();

      if (parentScene != null)
        Platform.runLater(()-> {
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
     * @since 0.0.1
     */
    @Parameter(name = "fixShape", className = Fixation.class, desc = "Fixation target type for eye.", defaultValue = "spot")
    @Parameter(name = "fixCx", className = Double.class, desc = "x-coordinate of fixation target (degrees): Only valid values are -20, -6, -3, 0, 3, 6, 20 for fixation type 'spot' and -3, 0, 3 for fixation type 'square'.", min = -20, max = 20, defaultValue = "0")
    @Parameter(name = "tracking", className = Double.class, desc = "Whether to correct stimulus location based on eye position.", min = 0, max = 1, defaultValue = "0")
    public Packet setup(HashMap<String, Object> args) {
        if (!this.socket.isConnected()) return Packet.error(DISCONNECTED_FROM_HOST);

        try {
            int fixCx = (int) ((double) args.get("fixCx"));

            int fixShape = -1;
            switch(Fixation.valueOf(((String) args.get("fixShape")).toUpperCase())) {
                case SPOT -> {
                  if (fixCx != 0 && Math.abs(fixCx) != 3 && Math.abs(fixCx) != 6 && Math.abs(fixCx) != 20)
                    return Packet.error(String.format(INVALID_FIXATION_SETTING, fixCx, Fixation.SPOT));
                  fixShape = 0;
                }
                case SQUARE -> {
                  if (fixCx != 0 && Math.abs(fixCx) != 3)
                    return Packet.error(String.format(INVALID_FIXATION_SETTING, fixCx, Fixation.SPOT));
                  fixShape = 1;
                }
            };

            int tracking = (int) ((double) args.get("tracking"));
            if (tracking != 0 && tracking != 1) return Packet.error(INVALID_TRACKING_SETTING + tracking);
            String jsonStr = null;
            try {
              this.send(OPI_OPEN);
              jsonStr = this.receive().toJson(); // parseOpiOpen TODO
              if (jsonStr.equals(BAD_OPEN)) return Packet.error(OPI_OPEN_FAILED);
              this.send(OPI_SET_FIXATION + fixCx + " 0 " + fixShape);
              /* TODO 
              if (this.receive().isError())
                return Packet.error(OPI_SET_FIXATION_FAILED);
              this.send(OPI_SET_TRACKING + tracking);
              if (this.receive().isError()) // split(" ")[0].equals("0"))
                return Packet.error(OPI_SET_TRACKING_FAILED);
                */
            } catch (IOException e) {
              return Packet.error(OPI_SETUP_FAILED, e);
            }
            return new Packet(jsonStr);
        } catch (ClassCastException | IllegalArgumentException e) {
          return Packet.error(OPI_SETUP_FAILED, e);
        }
    }
  
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * 
     * @param args pairs of argument name and value
     * 
     * @return A JSON object with return messages
     *
     * @since 0.0.1
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
            return new Packet("TODO"); // parseResult(this.receive())); TODO
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
     * @since 0.0.1
     */
    @ReturnMsg(name = "res.time", desc = "The time stamp for fixation data")
    @ReturnMsg(name = "res.x", desc = "The time stamp for fixation data")
    @ReturnMsg(name = "res.y", desc = "The time stamp for fixation data")
    public Packet close() {
      try {
        this.send(OPI_CLOSE);
        String message = "TODO"; // parseOpiClose(this.receive()); TODO
        this.closeSocket();
        return new Packet(true, message);
      } catch (ClassCastException | IllegalArgumentException | IOException e) {
        return Packet.error(COULD_NOT_DISCONNECT, e);
      }
    };
  
    /**
     * Parse results obtained for OPI-OPEN
     * 
     * @param received Message received from Icare machine
     * 
     * @return A string with return messages
     *
     * @since 0.0.1
     */
    private String parseOpiOpen(String received) {
      if (received.isBlank()) return BAD_OPEN;
      byte[] bytes = ArrayUtils.toPrimitive(Arrays.stream(received.split(" ")).map(str -> Byte.parseByte(str)).toArray(Byte[]::new));
      double prlx = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 4, 8)).getFloat();
      double prly = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 8, 12)).getFloat();
      double onhx = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 12, 16)).getFloat();
      double onhy = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 16, 20)).getFloat();
      ByteBuffer imageBuffer = ByteBuffer.allocate(bytes.length - 20).put(Arrays.copyOfRange(bytes, 20, bytes.length));
      double[] image = new double[(bytes.length - 20) / 4];
      for (int i = 0; i < image.length; i++) image[i] = (double) imageBuffer.getFloat(4 * i);
      return queryResults(prlx, prly, onhx, onhy, image);
    }
  
    /**
     * Parse query results
     * 
     * @return A JSON object with query results
     *
     * @since 0.0.1
     */
    private String queryResults() {
      return new StringBuilder("\n  {\n")
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
     * Parse query results
     * 
     * @return A JSON object with query results
     *
     * @since 0.0.1
     */
    private String queryResults(double prlx, double prly, double onhx, double onhy, double[] image) {
      return new StringBuilder("\n  {\n")
        .append("    \"prlx\": " + prlx + ",\n")
        .append("    \"prly\": " + prly + ",\n")
        .append("    \"onhx\": " + onhx + ",\n")
        .append("    \"onhy\": " + onhy + ",\n")
        .append("    \"image\": " + Arrays.toString(image) + ",\n")
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
        .append("\n  }\n").toString();
    };
  
    /**
     * Parse results obtained for OPI-PRESENT-STATIC
     * 
     * @param received Message received from Icare machine
     * 
     * @return A JSON object with present results
     *
     * @since 0.0.1
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
  
    /**
     * Parse results obtained for OPI-CLOSE
     * 
     * @param received Message received from Icare machine
     * 
     * @return A string with close results
     *
     * @since 0.0.1
     */
    private String parseOpiClose(String received) {
      if (received.isBlank()) return BAD_OPEN;
      byte[] bytes = ArrayUtils.toPrimitive(Arrays.stream(received.split(" ")).map(str -> Byte.parseByte(str)).toArray(Byte[]::new));
      int[] time = new int[(bytes.length - 4) / 12];
      double[] posx = new double[(bytes.length - 4) / 12];
      double[] posy = new double[(bytes.length - 4) / 12];
      ByteBuffer imageBuffer = ByteBuffer.allocate(bytes.length - 4).put(Arrays.copyOfRange(bytes, 4, bytes.length));
      for (int i = 0; i < time.length; i++) {
        time[i] = imageBuffer.getInt(12 * i);
        posx[i] = (double) imageBuffer.getFloat(12 * i + 4);
        posy[i] = (double) imageBuffer.getFloat(12 * i + 8);
      }
      String sb = new StringBuilder("\n  {\n")
        .append("    \"time\": " + Arrays.toString(time) + ",\n")
        .append("    \"posx\": " + Arrays.toString(posx) + ",\n")
        .append("    \"posy\": " + Arrays.toString(posy))
        .append("\n  }").toString();
  
        return(sb);
    }

 //-------------- Machine Specific FXML below here ---
    @FXML
    void initialize() { ; }
}
