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
  public MessageProcessor.Packet query() {
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
  @Parameter(name = "bgLum", className = Double.class, desc = "Background luminance for eye.", min = 0, max = 3183.099, defaultValue = "10")
  @Parameter(name = "bgCol", className = Double.class, desc = "Background color for eye.", isList = true, min = 0, max = 1, defaultValue = "list(1, 1, 1)")
  @Parameter(name = "fixType", className = ModelType.class, desc = "Fixation target type for eye.", defaultValue = "maltese")
  @Parameter(name = "fixLum", className = Double.class, desc = "Fixation target luminance for eye.", min = 0, max = 3183.099, defaultValue = "20")
  @Parameter(name = "fixCol", className = Double.class, desc = "Fixation target color for eye.", isList = true, defaultValue = "list(0, 1, 0)")
  @Parameter(name = "fixCx", className = Double.class, desc = "x-coordinate of fixation target (degrees).", min = -90, max = 90, defaultValue = "0")
  @Parameter(name = "fixCy", className = Double.class, desc = "y-coordinate of fixation target (degrees).", min = -90, max = 90, defaultValue = "0")
  @Parameter(name = "fixSx", className = Double.class, desc = "diameter along major axis of ellipse (degrees).", min = 0, defaultValue = "1")
  @Parameter(name = "fixSy", className = Double.class, desc = "diameter along minor axis of ellipse (degrees). If not received, then sy = sx.", optional = true, min = 0, defaultValue = "1")
  @Parameter(name = "fixRotation", className = Double.class, desc = "Angles of rotation of fixation target (degrees). Only useful if sx != sy specified.", optional = true, min = 0, max = 360, defaultValue = "0")
  @Parameter(name = "tracking", className = Double.class, desc = "Whether to correct stimulus location based on eye position.", optional = true, min = 0, max = 1, defaultValue = "0")
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
  @Parameter(name = "eye", className = Eye.class, desc = "Eye to test.", isList = true, defaultValue = "list('left')")
  @Parameter(name = "shape", className = ModelType.class, desc = "Stimulus shape.", isList = true, optional = true, defaultValue = "list('circle')")
  @Parameter(name = "type", className = TextureType.class, desc = "Stimulus type.", isList = true, optional = true, defaultValue = "list('flat')")
  @Parameter(name = "x", className = Double.class, desc = "List of x co-ordinates of stimuli (degrees).", isList = true, min = -90, max = 90, defaultValue = "list(0)")
  @Parameter(name = "y", className = Double.class, desc = "List of y co-ordinates of stimuli (degrees).", isList = true, min = -90, max = 90, defaultValue = "list(0)")
  @Parameter(name = "sx", className = Double.class, desc = "List of diameters along major axis of ellipse (degrees).", isList = true, min = 0, max = 180, defaultValue = "list(1.72)")
  @Parameter(name = "sy", className = Double.class, desc = "List of diameters along minor axis of ellipse (degrees). If not received, then sy = sx", isList = true, optional = true, min = 0, max = 180, defaultValue = "list(1.72)")
  @Parameter(name = "lum", className = Double.class, desc = "List of stimuli luminances (cd/m^2).", isList = true, min = 0, max = 3183.099, defaultValue = "list(20)")
  @Parameter(name = "color", className = Double.class, desc = "List of stimuli colors.", isListList = true, min = 0, max = 1, defaultValue = "list(list(1, 1, 1))")
  @Parameter(name = "rotation", className = Double.class, desc = "List of angles of rotation of stimuli (degrees). Only useful if sx != sy specified.", isList = true, optional = true, min = 0, max = 360, defaultValue = "list(0)")
  @Parameter(name = "contrast", className = Double.class, desc = "List of stimulus contrasts (from 0 to 1). Only useful if type != FLAT.", isList = true, optional = true, min = 0, max = 1, defaultValue = "list(1)")
  @Parameter(name = "phase", className = Double.class, desc = "List of phases (in degrees) for generation of spatial patterns. Only useful if type != FLAT", isList = true, optional = true, min = 0, defaultValue = "list(0)")
  @Parameter(name = "frequency", className = Double.class, desc = "List of frequencies (in cycles per degrees) for generation of spatial patterns. Only useful if type != FLAT", isList = true, optional = true, min = 0, max = 300, defaultValue = "list(0)")
  @Parameter(name = "defocus", className = Double.class, desc = "List of defocus values in Diopters for stimulus post-processing.", isList = true, optional = true, min = 0, defaultValue = "list(0)")
  @Parameter(name = "textRotation", className = Double.class, desc = "List of angles of rotation of stimuli (degrees). Only useful if type != FLAT", isList = true, optional = true, min = 0, max = 360, defaultValue = "list(0)")
  @Parameter(name = "t", className = Double.class, desc = "List of stimuli presentation times (ms).", isList = true, min = 0, defaultValue = "list(200)")
  @Parameter(name = "w", className = Double.class, desc = "List of stimuli response windows (ms).", isList = true, min = 0, defaultValue = "list(1500)")
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "Error message or a structure with the following data.")
  @ReturnMsg(name = "res.msg.seen", className = Double.class, desc = "'1' if seen, '0' if not.", min = 0, max = 1)
  @ReturnMsg(name = "res.msg.time", className = Double.class, desc = "Response time from stimulus onset if button pressed.", min = 0)
  @ReturnMsg(name = "res.msg.eyex", className = Double.class, desc = "x co-ordinates of pupil at times eyet (degrees).")
  @ReturnMsg(name = "res.msg.eyey", className = Double.class, desc = "y co-ordinates of pupil at times eyet (degrees).")
  @ReturnMsg(name = "res.msg.eyed", className = Double.class, desc = "Diameter of pupil at times eyet (mm).")
  @ReturnMsg(name = "res.msg.eyet", className = Double.class, desc = "Time of (eyex, eyey) pupil from stimulus onset (ms).", min = 0)
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