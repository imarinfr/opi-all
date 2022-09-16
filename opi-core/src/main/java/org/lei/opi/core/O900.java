package org.lei.opi.core;

import java.util.HashMap;

import org.lei.opi.core.structures.Parameter;
import org.lei.opi.core.structures.ReturnMsg;

/**
 * Octopus O900 client
 *
 * @since 0.0.1
 */
public class O900 extends OpiMachine {

  private enum Eye {LEFT, RIGHT};
  private enum BgLum {BG_OFF, BG_1, BG_10, BG_100};
  private enum BgCol {WHITE, YELLOW};
  private enum FixType {CENTER, CROSS, RING};
  private enum StCol {WHITE, RED, BLUE};
  private enum StSize {GI, GII, GIII, GIV, GV, GVI};

  /**
   * Octopus O900 constructor
   *
   * @since 0.0.1
   */
  public O900() { super(); }

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
  @Parameter(name = "ip_Monitor", desc = "IP Address of the O900 server.", defaultValue = "localhost")
  @Parameter(name = "port_Monitor", desc = "TCP port of the O900 server.", className = Double.class, min = 0, max = 65535, defaultValue = "50001")
  @Parameter(name = "eye", desc = "Eye to set.", className = Eye.class, defaultValue = "left")
  @Parameter(name = "eyeSuite", desc = "Path to EyeSuite.", className = String.class, defaultValue = "C:/XXX/eyeSuite/")
  @Parameter(name = "gazeFeed", desc = "Path where to save gaze feed. Directory must exists", className = String.class, defaultValue = "C:/XXX/gazeFeed/")
  @Parameter(name = "bigWheel", desc = "Whether O900 has a big wheel for displaying Goldmann Size VI stimuli.", className = Double.class, min = 0, max = 1, defaultValue = "0")
  @Parameter(name = "pres", desc = "Volume for auditory feedback when a stimulus is presented: 0 means no buzzer.", className = Double.class, min = 0, max = 3, defaultValue = "0")
  @Parameter(name = "resp", desc = "Volume for auditory feedback when observer presses the clicker: 0 means no buzzer.", className = Double.class, min = 0, max = 3, defaultValue = "0")
  @Parameter(name = "max10000", desc = "Whether O900 can handle a maximum luminance of 10000 apostilbs instead of 4000. Check the settings in EyeSuite", className = Double.class, min = 0, max = 1, defaultValue = "0")
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
  @Parameter(name = "bgLum", desc = "Background luminance for eye.", className = BgLum.class, defaultValue = "white")
  @Parameter(name = "bgCol", desc = "Background color for eye.", className = BgCol.class, defaultValue = "white")
  @Parameter(name = "fixType", desc = "Fixation target type for eye.", className = FixType.class, defaultValue = "center")
  @Parameter(name = "fixLum", desc = "Fixation luminance color for eye (from 0% to 100%).", className = Double.class, defaultValue = "50", min = 0, max = 100)
  @Parameter(name = "f310", desc = "Whether to use Logitech's F310 controlles", className = Double.class, defaultValue = "0", min = 0, max = 1)
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
  @Parameter(name = "w", desc = "List of stimuli response windows (ms).", className = Double.class, min = 0, defaultValue = "1500")
  @Parameter(name = "lum", desc = "List of stimuli luminances (cd/m^2).", className = Double.class, min = 0, max = 3183.099, defaultValue = "20")
  @Parameter(name = "size", desc = "Stimulus size (degrees). Can be Goldmann Size I to V (or VI if device has a big wheel)", className = StSize.class, defaultValue = "GV")
  @Parameter(name = "color", desc = "List of stimuli colors.", className = StCol.class, defaultValue = "white")
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