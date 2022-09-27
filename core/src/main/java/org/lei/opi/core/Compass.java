package org.lei.opi.core;

import java.io.IOException;
import java.util.HashMap;

import org.lei.opi.core.definitions.MessageProcessor;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;

/**
 * Compass client
 *
 * @since 0.0.1
 */
public class Compass extends OpiMachine {

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

  /**
   * opiInitialise: initialize OPI
   * 
   * @param args A map of name:value pairs for Params
   * 
   * @return A JSON object with machine specific initialise information
   * 
   * @since 0.0.1
   */
  public MessageProcessor.Packet initialize(HashMap<String, Object> args) {
    try {
      writer = new CSWriter((String) args.get("ip"), (int) ((double) args.get("port")));
      initialized = true;
      return OpiManager.ok(CONNECTED_TO_HOST + args.get("ip") + ":" + (int) ((double) args.get("port")));
    } catch (ClassCastException e) {
      return OpiManager.error(INCORRECT_FORMAT_IP_PORT);
    } catch (IOException e) {
      return OpiManager.error(String.format(SERVER_NOT_READY, args.get("ip") + ":" + (int) ((double) args.get("port"))));
    }
  };

  /**
   * opiQuery: Query device
   * 
   * @return settings and state machine state
   *
   * @since 0.0.1
   */
  public MessageProcessor.Packet query() {
    if (!initialized) return OpiManager.error(NOT_INITIALIZED);
    // Get from presentation parameters
/**
    .OpiEnv$Compass$MIN_X <- -30
    .OpiEnv$Compass$MAX_X <- 30  
    .OpiEnv$Compass$MIN_Y <- -30
    .OpiEnv$Compass$MAX_Y <- 30  
    .OpiEnv$Compass$MIN_RESP_WINDOW <- 0    
    .OpiEnv$Compass$MAX_RESP_WINDOW <- 2680
    .OpiEnv$Compass$MIN_DURATION <- 200
    .OpiEnv$Compass$MAX_DURATION <- 200
*/
    return new MessageProcessor.Packet("");
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
  @Parameter(name = "fixType", className = Fixation.class, desc = "Fixation target type for eye.", defaultValue = "spot")
  @Parameter(name = "fixCx", className = Double.class, desc = "x-coordinate of fixation target (degrees): Only valid values are -20, -6, -3, 0, 3, 6, 20 for fixation type 'spot' and -3, 0, 3 for fixation type 'square'.", min = -20, max = 20, defaultValue = "0")
  @Parameter(name = "tracking", className = Double.class, desc = "Whether to correct stimulus location based on eye position.", min = 0, max = 1, defaultValue = "0")
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "The success or error message.")
  public MessageProcessor.Packet setup(HashMap<String, Object> args) {
    if (!initialized) return OpiManager.error(NOT_INITIALIZED);
    String result;
    try {
      int fixCx = (int) ((double) args.get("pres"));
      int fixType = -1;
      switch(Fixation.valueOf(((String) args.get("fixType")).toUpperCase())) {
        case SPOT -> {
          if (fixCx != 0 || Math.abs(fixCx) != 3 || Math.abs(fixCx) != 6 || Math.abs(fixCx) != 20)
            return OpiManager.error(String.format(INVALID_FIXATION_SETTING, fixCx, Fixation.SPOT));
          fixType = 0;
        }
        case SQUARE -> {
          if (fixCx != 0 || Math.abs(fixCx) != 3)
            return OpiManager.error(String.format(INVALID_FIXATION_SETTING, fixCx, Fixation.SPOT));
          fixType = 1;
        }
      };
      int tracking = (int) ((double) args.get("tracking"));
      if (tracking != 0 || tracking != 1) return OpiManager.error(INVALID_TRACKING_SETTING + tracking);
      writer.send(OPI_OPEN);
      while (writer.empty()) Thread.onSpinWait();
      result = parseOpiOpen(writer.receive());
      if (!result.equals("1")) return OpiManager.error(OPI_OPEN_FAILED);
      writer.send(OPI_SET_FIXATION + fixCx + " 0 " + fixType);
      while (writer.empty()) Thread.onSpinWait();
      result = (writer.receive());
      if (!result.equals("1")) return OpiManager.error(OPI_SET_FIXATION_FAILED);
      writer.send(OPI_SET_TRACKING + tracking);
      result = (writer.receive());
      if (!result.equals("1")) return OpiManager.error(OPI_SET_TRACKING_FAILED);
    } catch (ClassCastException | IllegalArgumentException e) {
      return OpiManager.error(OPI_SETUP_FAILED, e);
    }
    return new MessageProcessor.Packet("");
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
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "Error message or a structure with the following data.")
  @ReturnMsg(name = "res.msg.seen", className = Double.class, desc = "'1' if seen, '0' if not.", min = 0, max = 1)
  @ReturnMsg(name = "res.msg.time", className = Double.class, desc = "Response time from stimulus onset if button pressed (ms).", min = 0)
  @ReturnMsg(name = "res.msg.eyex", className = Double.class, desc = "x co-ordinates of pupil at times eyet (pixels).")
  @ReturnMsg(name = "res.msg.eyey", className = Double.class, desc = "y co-ordinates of pupil at times eyet (pixels).")
  @ReturnMsg(name = "res.msg.eyed", className = Double.class, desc = "Diameter of pupil at times eyet (mm).")
  @ReturnMsg(name = "res.msg.eyet", className = Double.class, desc = "Time of (eyex, eyey) pupil from stimulus onset (ms).", min = 0)
  @ReturnMsg(name = "res.msg.time_rec", className = Double.class, desc = "Time since 'epoch' when command was received at Compass (ms).", min = 0)
  @ReturnMsg(name = "res.msg.time_resp", className = Double.class, desc = "Time since 'epoch' when stimulus response is received, or response window expired (ms).", min = 0)
  @ReturnMsg(name = "res.msg.num_track_events", className = Double.class, desc = "Number of tracking events that occurred during presentation.", min = 0)
  @ReturnMsg(name = "res.msg.num_motor_fails", className = Double.class, desc = "Number of times motor could not follow fixation movement during presentation.", min = 0)
  public MessageProcessor.Packet present(HashMap<String, Object> args) {
    if (!initialized) return OpiManager.error(NOT_INITIALIZED);
    try {
      int level = (int) Math.round(-10 * Math.log10((double) args.get("lum") / (10000 / Math.PI)));
      StringBuilder opiMessage = new StringBuilder(OPI_PRESENT_STATIC).append(" ")
        .append((int) ((double) args.get("x"))).append(" ")
        .append((int) ((double) args.get("y"))).append(" ")
        .append(level).append(" 3 ")
        .append((int) ((double) args.get("t"))).append(" ")
        .append((int) ((double) args.get("w")));
        writer.send(opiMessage.toString());
        while (writer.empty()) Thread.onSpinWait();
        return parseResults(writer.receive());  
      } catch (ClassCastException | IllegalArgumentException e) {
      return OpiManager.error(OPI_SETUP_FAILED, e);
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
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "The error message or stream of pupil fixations")
  @ReturnMsg(name = "res.msg.time", desc = "The time stamp for fixation data")
  @ReturnMsg(name = "res.msg.x", desc = "The time stamp for fixation data")
  @ReturnMsg(name = "res.msg.y", desc = "The time stamp for fixation data")
  public MessageProcessor.Packet close() {
    try {
      writer.send(OPI_CLOSE);
      while (writer.empty()) Thread.onSpinWait();
      String message = parseOpiClose(writer.receive());
      writer.close();
      initialized = false;
      return OpiManager.ok(DISCONNECTED_TO_HOST + message, true);
    } catch (IOException | ClassCastException | IllegalArgumentException e) {
      return OpiManager.error(COULD_NOT_DISCONNECT + writer, e);
    }
  };

  /**
   * Parse results obtained for OPI-OPEN
   * 
   * @param received Message received from Compass
   * 
   * @return A string with return messages
   *
   * @since 0.0.1
   */
  private String parseOpiOpen(String receive) {
    /**
    n prlx prly onhx onhy byte1 byte2 ...
    4 byte integer: the number of bytes to follow
    4 byte float: x coordinate of PRL in degrees
    4 byte float: y coordinate of PRL in degrees
    4 byte float: x coordinate of ONH in degrees
    4 byte float: y coordinate of ONH in degrees
    bytes that make up a jpeg image (the fundus photo)
    */
    return ""; // TODO return something
  }

  /**
   * Parse results obtained for OPI-PRESENT-STATIC
   * 
   * @param received Message received from Compass
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  private MessageProcessor.Packet parseResults(String receive) {
/**
    OPI-PRESENT-STATIC x y inDegrees level size duration responseWindow
    rt: in ms from stimulus onset (integer). -1 for not seen.
    th: hardware time of button press or response window expired (integer ms)
    tr: time since “epoch” when command was received at Compass (integer ms)
    tp: time since “epoch” when stimulus response is received, or response window expired (integer ms)
    nt: number of tracking events that occurred during presentation (integer)
    mt: number of times motor could not follow fixation movement during presentation (integer)
    pd: pupil diameter in mm (float)
    x: pixels integer, location in image of presentation
    y: pixels integer, location in image of presentation
*/
    return new MessageProcessor.Packet(""); // TODO return something
  }

    /**
   * Parse results obtained for OPI-OPEN
   * 
   * @param received Message received from Compass
   * 
   * @return A string with return messages
   *
   * @since 0.0.1
   */
  private String parseOpiClose(String receive) {
/**
  OPI-CLOSE
  The return values will be a list of all fixation events that were measured during the test in the following format.
  4 byte integer: the number of bytes to follow (0 for error). Should be a multiple of 12.
  Triples of 12 bytes with the format:
  4 byte integer: hardware time since of fixation event (ms)
  4 byte float: x location in degrees of fixation
  4 byte float: y location in degrees of fixation
*/
    return ""; // TODO return something
  }
 
}