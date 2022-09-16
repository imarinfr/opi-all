package org.lei.opi.core;

import java.util.HashMap;

import org.lei.opi.core.structures.Parameter;
import org.lei.opi.core.structures.ReturnMsg;

/**
 * Compass client
 *
 * @since 0.0.1
 */
public class Compass extends OpiMachine {

  private enum Eye {LEFT, RIGHT};
  private enum ShapeType {SPOT, SQUARE};

  /**
   * Compass constructor
   *
   * @since 0.0.1
   */
  public Compass() { super(); }

  /**
   * opiQuery: Query device
   * 
   * @return settings and state machine state
   *
   * @since 0.0.1
   */
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error message.")
  @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "msg.jovp", desc = "Any messages that the JOVP sent back.")
   public MessageProcessor.Packet query() {
    return new MessageProcessor.Packet("");
  }
  /**
   * opiInitialise: initialize OPI
   * 
   * @param args A map of name:value pairs for Params
   * 
   * @return A JSON object with return messages
   * 
   * @since 0.0.1
   */
  @Parameter(name = "ip", desc = "IP Address of the perimeter.", defaultValue = "192.126.0.1")
  @Parameter(name = "port", desc = "TCP port of the perimeter.", className = Double.class, min = 0, max = 65535, defaultValue = "50000")
  @Parameter(name = "ip_Monitor", desc = "IP Address of the OPI JOVP server.", defaultValue = "localhost")
  @Parameter(name = "port_Monitor", desc = "TCP port of the OPI JOVP server.", className = Double.class, min = 0, max = 65535, defaultValue = "50001")
  @Parameter(name = "eye", desc = "Eye to set.", className = Eye.class, defaultValue = "left")
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Imo.")
  @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "msg.jovp", desc = "Any messages that the JOVP sent back.")
  public MessageProcessor.Packet initialize(HashMap<String, Object> args) {
    setIsInitialised(true);
    return new MessageProcessor.Packet("");
  }

  /**
   * opiSetup: Change device background and overall settings
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  @Parameter(name = "fixType", desc = "Fixation target type for eye.", className = ShapeType.class, defaultValue = "maltese")
  @Parameter(name = "fixCx", desc = "x-coordinate of fixation target (degrees).", className = Double.class, min = -90, max = 90, defaultValue = "0")
  @Parameter(name = "tracking", desc = "Whether to correct stimulus location based on eye position.", className = Double.class, min = 0, max = 1, defaultValue = "0")
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from ImoVifa.")
  @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "msg.jovp", desc = "Any messages that the JOVP sent back.")
  public MessageProcessor.Packet setup(HashMap<String, Object> args) {
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
  @Parameter(name = "x", desc = "List of x co-ordinates of stimuli (degrees).", className = Double.class, min = -90, max = 90, isList = true, defaultValue = "list(0)")
  @Parameter(name = "y", desc = "List of y co-ordinates of stimuli (degrees).", className = Double.class, min = -90, max = 90, isList = true, defaultValue = "list(0)")
  @Parameter(name = "t", desc = "List of stimuli presentation times (ms).", className = Double.class, min = 0, isList = true, defaultValue = "list(200)")
  @Parameter(name = "w", desc = "List of stimuli response windows (ms).", className = Double.class, min = 0, isList = true, defaultValue = "list(1500)")
  @Parameter(name = "lum", desc = "List of stimuli luminances (cd/m^2).", className = Double.class, isList = true, min = 0, max = 3183.099, defaultValue = "list(20)")
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from ImoVifa.")
  @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "msg.seen", desc = "true if seen, false if not.", className = Boolean.class)
  @ReturnMsg(name = "msg.time", desc = "Response time from stimulus onset if button pressed, -1 otherwise (ms).", className = Double.class, min = -1)
  @ReturnMsg(name = "msg.eyex", desc = "x co-ordinates of pupil at times eyet (degrees).", className = Double.class, isList = true)
  @ReturnMsg(name = "msg.eyey", desc = "y co-ordinates of pupil at times eyet (degrees).", className = Double.class, isList = true)
  @ReturnMsg(name = "msg.eyed", desc = "Diameter of pupil at times eyet (degrees).", className = Double.class, isList = true)
  @ReturnMsg(name = "msg.eyet", desc = "Time of (eyex,eyey) pupil relative to stimulus onset t=0 (ms).", className = Double.class, isList = true)
  @ReturnMsg(name = "msg.jovp", desc = "Any JOVP-specific messages that the JOVP sent back.")
  public MessageProcessor.Packet present(HashMap<String, Object> args) {
    return new MessageProcessor.Packet("");
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
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Imo.")
  @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "msg.jovp", desc = "Any messages that the JOVP sent back.")
  public MessageProcessor.Packet close() {
    setIsInitialised(false);
    return new MessageProcessor.Packet(true, "");
  }

}