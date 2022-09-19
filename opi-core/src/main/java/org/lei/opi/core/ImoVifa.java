package org.lei.opi.core;

import org.lei.opi.core.structures.Parameter;
import org.lei.opi.core.structures.ReturnMsg;

import java.util.HashMap;

/**
 * ImoVifa client
 *
 * @since 0.0.1
 */
public class ImoVifa extends Jovp {

    /**
     * ImoVifa constructor
     *
     * @since 0.0.1
     */
    public ImoVifa() { 
        super(); 
    }

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
    @ReturnMsg(name = "msg.isTracking", desc = "0 eye tracking is off, any other value it is on.", className = Double.class, min = 0)
    @ReturnMsg(name = "msg.isCalibrated", desc = "0 eye tracking has not been calibrated, any other value it has.", className = Double.class, min = 0)
    public MessageProcessor.Packet query() {
        String jovp = super.query().msg;

        return OpiManager.ok(String.format("{\"result\": \"Imo Queried\", \"jovp\": %s}", jovp), false);
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
    @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Imo.")
    @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "msg.jovp", desc = "Any messages that the JOVP sent back.")
    public MessageProcessor.Packet initialize(HashMap<String, Object> args) {
        MessageProcessor.Packet jovp = super.initialize(args);
        if (jovp.error) {
            setIsInitialised(false);
            return OpiManager.error(String.format("{jvop: %s}", jovp.msg));
        } else {
            setIsInitialised(true);
            return OpiManager.ok(String.format("{jvop: %s}", jovp.msg), false);
        }
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
    @Parameter(name = "eye", desc = "Eye to set.", className = Eye.class, defaultValue = "both")
    @Parameter(name = "bgLum", desc = "Background luminance for eye.", className = Double.class, defaultValue = "10", min = 0, max = 3183.099)
    @Parameter(name = "bgCol", desc = "Background color for eye.", className = Double[].class, isList = true, defaultValue = "list(1, 1, 1)")
    @Parameter(name = "fixType", desc = "Fixation target type for eye.", className = ShapeType.class, defaultValue = "maltese")
    @Parameter(name = "fixLum", desc = "Fixation target luminance for eye.", className = Double.class, defaultValue = "20", min = 0, max = 3183.099)
    @Parameter(name = "fixCol", desc = "Fixation target color for eye.", className = Double[].class, isList = true, defaultValue = "list(0, 1, 0)")
    @Parameter(name = "fixCx", desc = "x-coordinate of fixation target (degrees).", className = Double.class, min = -90, max = 90, defaultValue = "0")
    @Parameter(name = "fixCy", desc = "y-coordinate of fixation target (degrees).", className = Double.class, min = -90, max = 90, defaultValue = "0")
    @Parameter(name = "fixSx", desc = "diameter along major axis of ellipse (degrees).", className = Double.class, min = 0, max = 180, defaultValue = "1")
    @Parameter(name = "fixSy", desc = "diameter along minor axis of ellipse (degrees).", className = Double.class, min = 0, max = 180, defaultValue = "1")
    @Parameter(name = "fixRotation", desc = "Angles of rotation of fixation target (degrees). Only useful if sx != sy specified.", className = Double.class, min = 0, max = 360, defaultValue = "0")
    @Parameter(name = "tracking", desc = "Whether to correct stimulus location based on eye position.", className = Double.class, min = 0, max = 1, defaultValue = "0")
    @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from ImoVifa.")
    @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "msg.jovp", desc = "Any messages that the JOVP sent back.")
    public MessageProcessor.Packet setup(HashMap<String, Object> args) {
        String jovp = super.setup(args).msg;

        return OpiManager.ok(String.format("{jvop: %s}", jovp), false);
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
    @Parameter(name = "eye" , desc = "Eye to test.", className = Eye.class, isList = true, defaultValue = "list('left')")
    @Parameter(name = "type" , desc = "Stimulus type.", className = ShapeType.class, isList = true, defaultValue = "list('circle')")
    @Parameter(name = "x", desc = "List of x co-ordinates of stimuli (degrees).", className = Double.class, min = -90, max = 90, isList = true, defaultValue = "list(0)")
    @Parameter(name = "y", desc = "List of y co-ordinates of stimuli (degrees).", className = Double.class, min = -90, max = 90, isList = true, defaultValue = "list(0)")
    @Parameter(name = "t", desc = "List of stimuli presentation times (ms).", className = Double.class, min = 0, isList = true, defaultValue = "list(200)")
    @Parameter(name = "w", desc = "List of stimuli response windows (ms).", className = Double.class, min = 0, isList = true, defaultValue = "list(1500)")
    @Parameter(name = "lum", desc = "List of stimuli luminances (cd/m^2).", className = Double.class, isList = true, min = 0, max = 3183.099, defaultValue = "list(20)")
    @Parameter(name = "color", desc = "List of stimuli colors.", className = Double[][].class, isList = true, defaultValue = "list(list(1, 1, 1))")
    @Parameter(name = "sx", desc = "List of diameters along major axis of ellipse (degrees).", className = Double.class, min = 0, max = 180, isList = true, defaultValue = "list(1.72)")
    @Parameter(name = "sy", desc = "List of diameters along minor axis of ellipse (degrees).", className = Double.class, min = 0, max = 180, isList = true, defaultValue = "list(1.72)")
    @Parameter(name = "rotation", desc = "List of angles of rotaion of stimuli (degrees). Only useful if sx != sy specified.", className = Double.class, min = -360, max = 360, isList = true, optional = true)
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
        String jovp = super.present(args).msg;

        return OpiManager.ok(
            String.format("{\"x\": %s, \"y\": %s, \"eye\": %s, \"seen\": 1, \"time\": 666, \"jovp\": %s}",
                args.get("x"), args.get("y"), args.get("eye"), jovp), false);
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
    public MessageProcessor.Packet close(HashMap<String, Object> args) {
        String jovp = super.close().msg;

        setIsInitialised(false);

        return OpiManager.ok(String.format("{jvop: %s}", jovp), true);
    }

}