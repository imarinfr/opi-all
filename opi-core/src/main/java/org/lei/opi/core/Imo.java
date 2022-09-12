package org.lei.opi.core;

import org.lei.opi.core.structures.Parameter;
import org.lei.opi.core.structures.Eye;

import java.util.HashMap;

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
  public String query(HashMap<String, String> args) {
    String jovp = super.query(args);
    return String.format("{\"result\": \"Imo Queried\", \"jovp\": %s}", jovp);
  }

  /** opiInitialise
  * @param args A map of name:value pairs for Params.
  * @return A JSON object with machine specific initialise information
  */
  @Parameter(name = "ip", desc = "IP Address of the perimeter.")
  @Parameter(name = "port", desc = "TCP port of the perimeter.")
  public String initialize(HashMap<String, String> args) {
    String jovp = super.initialize(args);
    setIsInitialised(true);
    return String.format("{jvop: %s}", jovp);
  }

  /** opiSetBackground 
  * @param args A map of name:value pairs for Params.
  * @return A JSON object with machine specific setup information
  */
  @Parameter(name = "eye", desc = "Eye to set.", type = Eye.class)
  //@Param(name = "color", desc = "Background color for eye.", type = COLOR.class)
  //@Param(name = "fix", desc = "Fixation type for eye.", type = FIXATION.class)
  public String setup(HashMap<String, String> args) {
    String jovp = super.setup(args);
    return String.format("{jvop: %s}", jovp);
  }

  /** opiPresent 
  * @param args A map of name:value pairs for Params.
  * @return A JSON object with machine specific presentation information
  */
  @Parameter(name = "x", desc = "x co-ordinate of stimuli (can be a list).", type = Double.class, min = -80, max = 80)
  @Parameter(name = "y", desc = "y co-ordinate of stimuli (can be a list).", type = Double.class, min = -80, max = 80)
  @Parameter(name = "eye" , desc = "Eye to test.", type = Eye.class)
  //@Param(name = "color", desc = "Background color for eye.<br>", type = COLOR.class)
  public String present(HashMap<String, String> args) {
    String jovp = super.present(args);
    return String.format("{\"x\": %s, \"y\": %s, \"eye\": %s, \"seen\": 1, \"time\": 666, \"jovp\": %s}",
      args.get("x"), args.get("y"), args.get("eye"), jovp);
  }

  /** opiClose 
  * @param args A map of name:value pairs for Params.
  * @return A JSON object with machine specific query information
  */
  public String close(HashMap<String, String> args) {
    String jovp = super.close(args);
    setIsInitialised(false);
    return String.format("{jvop: %s}", jovp);
  }
}