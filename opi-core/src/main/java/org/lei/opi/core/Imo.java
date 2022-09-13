package org.lei.opi.core;

import org.lei.opi.core.structures.Parameter;
import org.lei.opi.core.structures.Eye;

import java.util.HashMap;
import java.util.List;

/**
 * JOVP client
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
  public MessageProcessor.Packet query(HashMap<String, String> args) {
    String jovp = super.query(args).msg;
    return new MessageProcessor.Packet(
      String.format("{\"result\": \"Imo Queried\", \"jovp\": %s}", jovp));
  }

  /** opiInitialise
  * @param args A map of name:value pairs for Params.
  * @return A JSON object with machine specific initialise information
  */
  @Parameter(name = "ip", desc = "IP Address of the perimeter.")
  @Parameter(name = "port", desc = "TCP port of the perimeter.")
  public MessageProcessor.Packet initialize(HashMap<String, String> args) {
    String jovp = super.initialize(args).msg;
    setIsInitialised(true);
    return new MessageProcessor.Packet(
      String.format("{jvop: %s}", jovp));
  }

  /** opiSetBackground 
  * @param args A map of name:value pairs for Params.
  * @return A JSON object with machine specific setup information
  */
  @Parameter(name = "eye", desc = "Eye to set.", className = Eye.class)
  //@Param(name = "color", desc = "Background color for eye.", className = COLOR.class)
  //@Param(name = "fix", desc = "Fixation className for eye.", className = FIXATION.class)
  public MessageProcessor.Packet setup(HashMap<String, String> args) {
    String jovp = super.setup(args).msg;
    return new MessageProcessor.Packet(
      String.format("{jvop: %s}", jovp));
  }

  /** opiPresent 
  * @param args A map of name:value pairs for Params.
  * @return A JSON object with machine specific presentation information
  */
  @Parameter(name = "eye" , desc = "Eye to test.", className = Eye.class)
  @Parameter(name = "x", desc = "List of x co-ordinates of stimuli (degrees).", className = Double.class, min = -80, max = 80)
  @Parameter(name = "y", desc = "List of y co-ordinates of stimuli (degrees).", className = Double.class, min = -80, max = 80)
  @Parameter(name = "t", desc = "List of stimuli presentation times (ms).", className = Double.class, min = 0, max = Double.MAX_VALUE)
  @Parameter(name = "w", desc = "List of stimuli response windows (ms).", className = Double.class, min = 0, max = Double.MAX_VALUE)
  @Parameter(name = "color", desc = "List of stimuli colors.", className = Jovp.Color.class)
  //@Param(name = "color", desc = "Background color for eye.<br>", className = COLOR.class)
  public MessageProcessor.Packet present(HashMap<String, String> args) {
    String jovp = super.present(args).msg;
    return new MessageProcessor.Packet(
      String.format("{\"x\": %s, \"y\": %s, \"eye\": %s, \"seen\": 1, \"time\": 666, \"jovp\": %s}",
      args.get("x"), args.get("y"), args.get("eye"), jovp));
  }

  /** opiClose 
  * @param args A map of name:value pairs for Params.
  * @return A JSON object with machine specific query information
  */
  public MessageProcessor.Packet close(HashMap<String, String> args) {
    String jovp = super.close(args).msg;
    setIsInitialised(false);

    return new MessageProcessor.Packet(
      true,
      String.format("{jvop: %s}", jovp));
  }
}