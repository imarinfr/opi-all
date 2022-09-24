package org.lei.opi.core;

import java.util.HashMap;

import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;

/**
 * Kowa AP7000 client
 *
 * @since 0.0.1
 */
public class Kowa extends OpiMachine {

  private enum BgLum {BG_10, BG_100};
  private enum BgCol {WHITE, YELLOW};
  private enum ShapeType {CENTER, AUX, MACULA, AUX_LEFT};
  private enum Size {GI, GII, GIII, GIV, GV};
  private enum Color {WHITE, RED, GREEN, BLUE};

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
  @Parameter(name = "fixType", desc = "Fixation target type for eye.", className = ShapeType.class, defaultValue = "center")
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from ImoVifa.")
  @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "msg.kowa", desc = "Kowa-specific messages.")
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
  @Parameter(name = "size", desc = "Stimulus size (degrees).", className = Size.class, defaultValue = "GIII")
  @Parameter(name = "color", desc = "List of stimuli colors.", className = Color.class, defaultValue = "white")
  @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from ImoVifa.")
  @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "msg.seen", desc = "true if seen, false if not.", className = Boolean.class)
  @ReturnMsg(name = "msg.time", desc = "Response time from stimulus onset if button pressed, -1 otherwise (ms).", className = Double.class, min = -1)
  @ReturnMsg(name = "msg.eyex", desc = "x co-ordinates of pupil at times eyet (degrees).", className = Double.class, isList = true)
  @ReturnMsg(name = "msg.eyey", desc = "y co-ordinates of pupil at times eyet (degrees).", className = Double.class, isList = true)
  @ReturnMsg(name = "msg.eyed", desc = "Diameter of pupil at times eyet (degrees).", className = Double.class, isList = true)
  @ReturnMsg(name = "msg.eyet", desc = "Time of (eyex,eyey) pupil relative to stimulus onset t=0 (ms).", className = Double.class, isList = true)
  @ReturnMsg(name = "msg.kowa", desc = "Kowa-specific messages.")
  public MessageProcessor.Packet present(HashMap<String, Object> args) {
    return new MessageProcessor.Packet("");
  }

}