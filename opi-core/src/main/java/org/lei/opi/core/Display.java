package org.lei.opi.core;

import java.util.HashMap;

import org.lei.opi.core.structures.Parameter;
import org.lei.opi.core.structures.ReturnMsg;

/**
 * Kowa AP7000 client
 *
 * @since 0.0.1
 */
public class Display extends Jovp {

  /**
   * Display constructor
   *
   * @since 0.0.1
   */
  public Display() { super(); }

  /**
   * opiQuery: Query device
   * 
   * @return settings and state machine state
   *
   * @since 0.0.1
   */
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error message.")
  @ReturnMsg(name = "msg", desc = "Object with all of the other fields.")
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
  @Parameter(name = "ip_Monitor", desc = "IP Address of the OPI JOVP server.", defaultValue = "localhost")
  @Parameter(name = "port_Monitor", desc = "TCP port of the OPI JOVP server.", className = Double.class, min = 0, max = 65535, defaultValue = "50001")
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Display.")
  @ReturnMsg(name = "msg", desc = "Object with all of the other fields.")
  @ReturnMsg(name = "msg.jovp", desc = "Any messages that the JOVP sent back.")
  public MessageProcessor.Packet initialize(HashMap<String, Object> args) {
    setIsInitialised(true);
    super.initialize(args);
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
  @Parameter(name = "bgRed", desc = "Background color for the screen: red channel. (0..255)", className = Double.class, defaultValue = "0")
  @Parameter(name = "bgGreen", desc = "Background color for the screen: green channel (0..255).", className = Double.class, defaultValue = "0")
  @Parameter(name = "bgBlue", desc = "Background color for the screen: blue channel (0..255).", className = Double.class, defaultValue = "0")
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Display.")
  @ReturnMsg(name = "msg", desc = "Object with all of the other fields.")
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
  @Parameter(name = "w", desc = "List of stimuli response windows (ms).", className = Double.class, min = 0, defaultValue = "1500")
  @Parameter(name = "size", desc = "Stimulus size (degrees).", className = Double.class, defaultValue = "1.72")
  @Parameter(name = "colorRed", desc = "List of stimuli colors: red channel (0..1).", className = Double.class, min = 0, max = 1, isList = true, defaultValue = "list(0)")
  @Parameter(name = "colorGreen", desc = "List of stimuli colors: green channel (0..1).", className = Double.class,min = 0, max = 1, isList = true,  defaultValue = "list(0)")
  @Parameter(name = "colorBlue", desc = "List of stimuli colors: blue channel (0..1).", className = Double.class,min = 0, max = 1, isList = true,  defaultValue = "list(0)")
  @Parameter(name = "lum", desc = "List of luminances (cd/m^2).", className = Double.class,min = 0, isList = true,  defaultValue = "31.4")
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Display.")
  @ReturnMsg(name = "msg", desc = "Object with all of the other fields.")
  @ReturnMsg(name = "msg.seen", desc = "true if seen, false if not.", className = Boolean.class)
  @ReturnMsg(name = "msg.time", desc = "Response time from stimulus onset if button pressed, -1 otherwise (ms).", className = Double.class, min = -1)
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
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Display.")
  @ReturnMsg(name = "msg", desc = "Object with all other fields.")
  @ReturnMsg(name = "msg.jovp", desc = "Any messages that the JOVP sent back.")
  public MessageProcessor.Packet close() {
    setIsInitialised(false);
    return new MessageProcessor.Packet(true, "");
  }

}