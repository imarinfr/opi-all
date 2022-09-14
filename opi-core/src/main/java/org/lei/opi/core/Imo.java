package org.lei.opi.core;

import org.lei.opi.core.structures.Parameter;
import org.lei.opi.core.structures.ReturnMsg;
import org.lei.opi.core.structures.Eye;

import java.util.HashMap;

/**
 * ImoVifa client
 *
 * @since 0.0.1
 */
public class Imo extends Jovp {
    public Imo() { 
        super(); 
    }

    /** opiQuery
     * @param args A map of name:value pairs for Params.
    * @return A JSON object with machine specific query information
    */
    @ReturnMsg(name = "error", desc = "Empty string for all good, else error message.")
    @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "jovp", desc = "Any messages that the JOVP sent back.")
    public MessageProcessor.Packet query(HashMap<String, String> args) {
        String jovp = super.query(args).msg;

        return OpiManager.ok(String.format("{\"result\": \"Imo Queried\", \"jovp\": %s}", jovp), false);
    }

    /** opiInitialise
     * @param args A map of name:value pairs for Params.
    * @return A JSON object with machine specific initialise information
    */
    @Parameter(name = "ip", desc = "IP Address of the perimeter.")
    @Parameter(name = "port", desc = "TCP port of the perimeter.", className = Double.class, min = 0, max = 65535)
    @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Imo.")
    @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "jovp", desc = "Any messages that the JOVP sent back.")
    public MessageProcessor.Packet initialize(HashMap<String, String> args) {
        MessageProcessor.Packet jovp = super.initialize(args);
        if (jovp.error) {
            setIsInitialised(false);
            return OpiManager.error(String.format("{jvop: %s}", jovp.msg));
        } else {
            setIsInitialised(true);
            return OpiManager.ok(String.format("{jvop: %s}", jovp.msg), false);
        }
    }

    /** opiSetBackground 
     * @param args A map of name:value pairs for Params.
    * @return A JSON object with machine specific setup information
    */
    @Parameter(name = "eye", desc = "Eye to set.", className = Eye.class)
    //@Param(name = "color", desc = "Background color for eye.", className = COLOR.class)
    //@Param(name = "fix", desc = "Fixation className for eye.", className = FIXATION.class)
    @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Imo.")
    @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "jovp", desc = "Any messages that the JOVP sent back.")
    public MessageProcessor.Packet setup(HashMap<String, String> args) {
        String jovp = super.setup(args).msg;

        return OpiManager.ok(String.format("{jvop: %s}", jovp), false);
    }

    /** opiPresent 
     * @param args A map of name:value pairs for Params.
    * @return A JSON object with machine specific presentation information
    */
    @Parameter(name = "eye" , desc = "Eye to test.", className = Eye.class)
    @Parameter(name = "x", desc = "List of x co-ordinates of stimuli (degrees).", className = Double.class, min = -80, max = 80, isList = true)
    @Parameter(name = "y", desc = "List of y co-ordinates of stimuli (degrees).", className = Double.class, min = -80, max = 80, isList = true)
    @Parameter(name = "t", desc = "List of stimuli presentation times (ms).", className = Double.class, min = 0, isList = true)
    @Parameter(name = "w", desc = "List of stimuli response windows (ms).", className = Double.class, min = 0, isList = true)
    @Parameter(name = "color", desc = "List of stimuli colors.", className = Jovp.Color.class, isList = true)
    @Parameter(name = "sx", desc = "List of diameters along major axis of ellipse (degrees).", className = Double.class, min = 0, max = 180, isList = true, optional = true)
    @Parameter(name = "sy", desc = "List of diameters along minor axis of ellipse (degrees).", className = Double.class, min = 0, max = 180, isList = true, optional = true)
    @Parameter(name = "rotation", desc = "List of angles of rotaion of stimuli (degrees). Only useful if sx != sy specified.", className = Double.class, min = -360, max = 360, isList = true, optional = true)
    @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Imo.")
    @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "seen", desc = "true if seen, false if not.", className = Boolean.class)
    @ReturnMsg(name = "time", desc = "Response time from stimulus onset if button pressed, -1 otherwise (ms).", className = Double.class, min = -1)
    @ReturnMsg(name = "eyex", desc = "x co-ordinates of pupil at times eyet (degrees).", className = Double.class, isList = true)
    @ReturnMsg(name = "eyey", desc = "y co-ordinates of pupil at times eyet (degrees).", className = Double.class, isList = true)
    @ReturnMsg(name = "eyed", desc = "Diameter of pupil at times eyet (degrees).", className = Double.class, isList = true)
    @ReturnMsg(name = "eyet", desc = "Time of (eyex,eyey) pupil relative to stimulus onset t=0 (ms).", className = Double.class, isList = true)
    @ReturnMsg(name = "jovp", desc = "Any JOVP-specific messages that the JOVP sent back.")
    public MessageProcessor.Packet present(HashMap<String, String> args) {
        String jovp = super.present(args).msg;

        return OpiManager.ok(
            String.format("{\"x\": %s, \"y\": %s, \"eye\": %s, \"seen\": 1, \"time\": 666, \"jovp\": %s}",
                args.get("x"), args.get("y"), args.get("eye"), jovp), false);
    }

    /** opiClose 
     * @param args A map of name:value pairs for Params.
    * @return A JSON object with machine specific query information
    */
    @ReturnMsg(name = "error", desc = "Empty string for all good, else error messages from Imo.")
    @ReturnMsg(name = "msg", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "jovp", desc = "Any messages that the JOVP sent back.")
    public MessageProcessor.Packet close(HashMap<String, String> args) {
        String jovp = super.close(args).msg;

        setIsInitialised(false);

        return OpiManager.ok(String.format("{jvop: %s}", jovp), true);
    }
}