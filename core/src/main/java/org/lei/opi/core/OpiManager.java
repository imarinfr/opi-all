package org.lei.opi.core;

import java.util.HashMap;
import java.util.stream.Stream;

import org.lei.opi.core.definitions.MessageProcessor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 *
 * OPI manager
 *
 * @since 0.0.1
 */
public class OpiManager extends MessageProcessor {

/**
 * Command. In JSON files they will appear as:
 *    name:value pair where name == "command"
 *
 * @since 0.0.1
 */
public enum Command {
  /** Choose OPI implementation */
  CHOOSE,
  /** Query device constants */
  QUERY,
  /** Setup OPI */
  SETUP,
  /** Initialize OPI connection */
  INITIALIZE,
  /** Present OPI static, kinetic, or temporal stimulus */
  PRESENT,
  /** Close OPI connection */
  CLOSE
}

  /** Constant for exception messages: {@value BAD_JSON} */
  public static final String BAD_JSON = "String is not a valid Json object.";
  /** Constant for exception messages: {@value NO_COMMAND_FIELD} */
  public static final String NO_COMMAND_FIELD = "Json message does not contain field 'command'.";
  /** Constant for exception messages: {@value BAD_COMMAND_FIELD} {@link Command} */
  public static final String BAD_COMMAND_FIELD = "value of 'command' name in Json message is not one of Command'.";
  /** Constant for exception messages: {@value NO_CHOOSE_COMMAND} */
  private static final String NO_CHOOSE_COMMAND = "No machine selected yet. First 'command' must be " + Command.CHOOSE.name();
  /** Constant for exception messages: {@value MACHINE_NEEDS_CLOSING} */
  private static final String MACHINE_NEEDS_CLOSING = "Close the previous machine before choosing another.";
  /** Constant for exception messages: {@value WRONG_MACHINE_NAME} */
  private static final String WRONG_MACHINE_NAME = "Cannot create the selected machine %s in 'command:'" + Command.CHOOSE.name() + "' as it does not exist.";

  /** name:value pair in JSON output if there is an error */
  public static String ERROR_YES = "\"error\" : 1";
  /** name:value pair in JSON output if there is not an error */
  private static String ERROR_NO = "\"error\" : 0";

  /** The OpiMachine object that is instantiated when a Command.CHOOSE is received */
  private OpiMachine machine;

  private static Gson gson = new Gson(); // for fromJson method

  /**
   *
   * Start the OPI manager
   *
   * @since 0.0.1
   */
  public OpiManager() {
    this.machine = null; // no opi machine chosen yet
  }

  /**
   * Create an OK message in JSON format with attached results
   * 
   * @param message Json object that is feedback from the OPI command or null
   * 
   * @return JSON-formatted ok message
   * 
   * @since 0.1.0
   */
  public static MessageProcessor.Packet ok(String message) {
    return ok(message, false);
  }

  /**
   * Create an OK message in JSON format with attached results
   * 
   * @param message Json object that is feedback from the OPI command or null
   * @param close Whether to close or not
   * 
   * @return JSON-formatted ok message
   * 
   * @since 0.1.0
   */
  public static MessageProcessor.Packet ok(String message, boolean close) {
    return new MessageProcessor.Packet(false, close, String.format("{%s, \"msg\": %s}", ERROR_NO, message));
  }

  /**
   * Create an error message in JSON format to send to R OPI
   * 
   * @param description String error description (no quotes)
   * 
   * @return JSON-formatted error message
   * 
   * @since 0.1.0
   */
  public static MessageProcessor.Packet error(String description) {
    return new MessageProcessor.Packet(
        String.format("{%s, \"msg\": \"%s\"}", ERROR_YES, description));
  }

  /**
   * Create an error message in JSON format to send to R OPI
   * 
   * @param description String error description (no quotes) to add to Json return name 'description'
   * @param exception An exception to print to stderr and add to Json return object name 'exception'
   * 
   * @return JSON-formatted error message
   * 
   * @since 0.1.0
   */
  public static MessageProcessor.Packet error(String description, Exception exception) {
    System.err.println(exception);
    return new MessageProcessor.Packet(
        String.format("{%s, \"msg\": \"%s\", \"exception\": \"%s\"}", ERROR_YES, description, exception.toString()));
  }

  /**
   * Process incoming Json commands. If it is a 'choose' command, then
   * set the private field machine to a new instance of that machine.
   * If it is another command, then process it using the machine object.
   *
   * @param jsonStr A JSON object that at least contains the name 'command'.
   * 
   * @return JSON-formatted message with feedback
   * 
   * @since 0.1.0
   */
  public MessageProcessor.Packet process(String jsonStr) {
    HashMap<String, Object> pairs;
    // Parse JSON to hashmap with pairs of name:values
    try {
      pairs = gson.fromJson(jsonStr, new TypeToken<HashMap<String, Object>>() {}.getType());
    } catch (JsonSyntaxException e) {
      return error(BAD_JSON);
    }
    // Get command
    if (!pairs.containsKey("command")) // needs a command
      return error(NO_COMMAND_FIELD);
    String cmd;
    try {
      cmd = (String) pairs.get("command");
    } catch (ClassCastException e) {
      return error(BAD_COMMAND_FIELD);
    }
    // Check it is a valid command
    if (!Stream.of(Command.values()).anyMatch((e) -> e.name().equalsIgnoreCase(cmd)))
      return error(BAD_COMMAND_FIELD);
    // If it is a CHOOSE command, then let's fire up the chosen machine (unless one already open)
    if (cmd.equalsIgnoreCase(Command.CHOOSE.name())) {
      if (this.machine != null && machine.initialized)
        return error(MACHINE_NEEDS_CLOSING);
      String className = OpiMachine.class.getPackage().getName() + "." + pairs.get("machine");
      try {
        machine = (OpiMachine) Class.forName(className).getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        return error(String.format(WRONG_MACHINE_NAME, className), e);
      }
      return ok("", false);
    } else { // If it is not a CHOOSE command and there is no machine open, give up else try it out
      if (this.machine == null)
        return error(NO_CHOOSE_COMMAND);
      return this.machine.process(pairs);
    }
  }

}