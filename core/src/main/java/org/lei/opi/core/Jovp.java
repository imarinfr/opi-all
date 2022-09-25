package org.lei.opi.core;

import java.io.IOException;
import java.util.HashMap;

import org.lei.opi.core.OpiManager.Command;
import org.lei.opi.core.definitions.MessageProcessor;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.Present;
import org.lei.opi.core.definitions.ReturnMsg;
import org.lei.opi.core.definitions.Setup;

import es.optocom.jovp.structures.Eye;
import es.optocom.jovp.structures.ModelType;
import es.optocom.jovp.structures.TextureType;

/**
 * JOVP client
 *
 * @since 0.0.1
 */
public class Jovp extends OpiMachine {  

  /**
   * opiQuery: Query device
   * 
   * @return settings and state machine state
   *
   * @since 0.0.1
   */
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "The success or error message.")
  public MessageProcessor.Packet query(HashMap<String, Object> args) {
    if (!initialized) return OpiManager.error(NOT_INITIALIZED);
    try {
      writer.send(Command.QUERY.toString());
      while (writer.empty()) Thread.onSpinWait();
      return new MessageProcessor.Packet(writer.receive());
    } catch (IOException | ClassCastException | IllegalArgumentException e) {
      return OpiManager.error(COULD_NOT_QUERY);
    }

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
  @Parameter(name = "bgLum", desc = "Background luminance for eye.", className = Double.class, defaultValue = "10", min = 0, max = 3183.099)
  @Parameter(name = "bgCol", desc = "Background color for eye.", className = Double[].class, isList = true, defaultValue = "list(1, 1, 1)")
  @Parameter(name = "fixType", desc = "Fixation target type for eye.", className = ModelType.class, defaultValue = "maltese")
  @Parameter(name = "fixLum", desc = "Fixation target luminance for eye.", className = Double.class, defaultValue = "20", min = 0, max = 3183.099)
  @Parameter(name = "fixCol", desc = "Fixation target color for eye.", className = Double[].class, isList = true, defaultValue = "list(0, 1, 0)")
  @Parameter(name = "fixCx", desc = "x-coordinate of fixation target (degrees).", className = Double.class, min = -90, max = 90, defaultValue = "0")
  @Parameter(name = "fixCy", desc = "y-coordinate of fixation target (degrees).", className = Double.class, min = -90, max = 90, defaultValue = "0")
  @Parameter(name = "fixSx", desc = "diameter along major axis of ellipse (degrees).", className = Double.class, min = 0, max = 180, defaultValue = "1")
  @Parameter(name = "fixSy", desc = "diameter along minor axis of ellipse (degrees).", className = Double.class, min = 0, max = 180, defaultValue = "1")
  @Parameter(name = "fixRotation", desc = "Angles of rotation of fixation target (degrees). Only useful if sx != sy specified.", className = Double.class, min = 0, max = 360, defaultValue = "0")
  @Parameter(name = "tracking", desc = "Whether to correct stimulus location based on eye position.", className = Double.class, min = 0, max = 1, defaultValue = "0")
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "The success or error message.")
  public MessageProcessor.Packet setup(HashMap<String, Object> args) {
    if (!initialized) return OpiManager.error(NOT_INITIALIZED);
    try {
      writer.send(Setup.set(args).toJson());
      while (writer.empty()) Thread.onSpinWait();
      return new MessageProcessor.Packet(writer.receive());
    } catch (IOException | ClassCastException | IllegalArgumentException e) {
      return OpiManager.error(COULD_NOT_SETUP);
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
  @Parameter(name = "eye", desc = "Eye to test.", className = Eye.class, isList = true, defaultValue = "list('left')")
  @Parameter(name = "shape", desc = "Stimulus shape.", className = ModelType.class, isList = true, defaultValue = "list('circle')", optional = true)
  @Parameter(name = "type", desc = "Stimulus type.", className = TextureType.class, isList = true, defaultValue = "list('flat')", optional = true)
  @Parameter(name = "x", desc = "List of x co-ordinates of stimuli (degrees).", className = Double.class, min = -90, max = 90, isList = true, defaultValue = "list(0)")
  @Parameter(name = "y", desc = "List of y co-ordinates of stimuli (degrees).", className = Double.class, min = -90, max = 90, isList = true, defaultValue = "list(0)")
  @Parameter(name = "sx", desc = "List of diameters along major axis of ellipse (degrees).", className = Double.class, min = 0, max = 180, isList = true, defaultValue = "list(1.72)")
  @Parameter(name = "sy", desc = "List of diameters along minor axis of ellipse (degrees).", className = Double.class, min = 0, max = 180, isList = true, defaultValue = "list(1.72)")
  @Parameter(name = "lum", desc = "List of stimuli luminances (cd/m^2).", className = Double.class, isList = true, min = 0, max = 3183.099, defaultValue = "list(20)")
  @Parameter(name = "color", desc = "List of stimuli colors.", className = Double[][].class, isList = true, defaultValue = "list(list(1, 1, 1))")
  @Parameter(name = "rotation", desc = "List of angles of rotation of stimuli (degrees). Only useful if sx != sy specified.", className = Double.class, min = 0, max = 360, isList = true, optional = true)
  @Parameter(name = "contrast", desc = "List of stimulus contrasts (from 0 to 1).", className = Double.class, min = 0, max = 1, isList = true, optional = true)
  @Parameter(name = "phase", desc = "List of phases (in degrees) for generation of spatial patterns. Only useful if type != FLAT", className = Double.class, min = 0, max = 100, isList = true, optional = true)
  @Parameter(name = "frequency", desc = "List of frequencies (in cycles per degrees) for generation of spatial patterns. Only useful if type != FLAT", className = Double.class, min = 0, max = 300, isList = true, optional = true)
  @Parameter(name = "defocus", desc = "List of defocus values in Diopters for stimulus post-processing.", className = Double.class, min = 0, max = 1, isList = true, optional = true)
  @Parameter(name = "textRotation", desc = "List of angles of rotation of stimuli (degrees). Only useful if type != FLAT", className = Double.class, min = 0, max = 360, isList = true, optional = true)
  @Parameter(name = "t", desc = "List of stimuli presentation times (ms).", className = Double.class, min = 0, isList = true, defaultValue = "list(200)")
  @Parameter(name = "w", desc = "List of stimuli response windows (ms).", className = Double.class, min = 0, isList = true, defaultValue = "list(1500)")
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "Error message or a structure with the following data.")
  @ReturnMsg(name = "res.msg.seen", desc = "true if seen, false if not.", className = Boolean.class)
  @ReturnMsg(name = "res.msg.time", desc = "Response time from stimulus onset if button pressed, -1 otherwise (ms).", className = Double.class, min = -1)
  @ReturnMsg(name = "res.msg.eyex", desc = "x co-ordinates of pupil at times eyet (degrees).", className = Double.class, isList = true)
  @ReturnMsg(name = "res.msg.eyey", desc = "y co-ordinates of pupil at times eyet (degrees).", className = Double.class, isList = true)
  @ReturnMsg(name = "res.msg.eyed", desc = "Diameter of pupil at times eyet (degrees).", className = Double.class, isList = true)
  @ReturnMsg(name = "res.msg.eyet", desc = "Time of (eyex,eyey) pupil relative to stimulus onset t=0 (ms).", className = Double.class, isList = true)
  public MessageProcessor.Packet present(HashMap<String, Object> args) {
    if (!initialized) return OpiManager.error(NOT_INITIALIZED);
    try {
      writer.send(Present.set(args).toJson());
      while (writer.empty()) Thread.onSpinWait();
      return new MessageProcessor.Packet(writer.receive());
    } catch (IOException | ClassCastException | IllegalArgumentException e) {
      return OpiManager.error(COULD_NOT_PRESENT);
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
  @ReturnMsg(name = "res.msg", desc = "The success or error message")
  public MessageProcessor.Packet close() {
    if (!initialized) return OpiManager.error(NOT_INITIALIZED);
    try {
      writer.send(Command.CLOSE.toString());
      writer.close();
      initialized = false;
      return OpiManager.ok(DISCONNECTED_TO_HOST + writer, true);
    } catch (IOException e) {
      return OpiManager.error(COULD_NOT_DISCONNECT + writer, e);
    }
  }

}