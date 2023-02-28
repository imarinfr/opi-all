package org.lei.opi.core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.stream.Stream;
import com.google.gson.JsonSyntaxException;

/**
 *
 * OPI manager
 *
 * @since 0.0.1
 */
public class OpiClient extends Listener {

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
  
  /** Constant for exception messages: {@value NO_COMMAND_FIELD} */
  public static final String NO_COMMAND_FIELD = "Json message does not contain field 'command'.";
  /** Constant for exception messages: {@value BAD_COMMAND_FIELD} {@link Command} */
  public static final String BAD_COMMAND_FIELD = "value of 'command' name in Json message is not one of Command'.";
  /** Constant for exception messages: {@value NO_CHOOSE_COMMAND} */
  private static final String NO_CHOOSE_COMMAND = "No machine selected yet. First 'command' must be " + Command.CHOOSE.name();
  /** Constant for exception messages: {@value WRONG_MACHINE_NAME} */
  private static final String WRONG_MACHINE_NAME = "Cannot create the selected machine %s in 'command:'" + Command.CHOOSE.name() + "' as it does not exist.";
  /** Constant for exception messages: {@value MACHINE_SELECTED} */
  private static final String MACHINE_SELECTED = "\"Machine %s selected\"";

  /** The OpiMachine object that is instantiated when a Command.CHOOSE is received */
  private OpiMachine machine;

  /**
   *
   * Start the OPI manager
   *
   * @since 0.0.1
   */
  public OpiClient(int port) {
    this.connect(port, Listener.obtainPublicAddress());  // run on localhost
    this.machine = null; // no opi machine chosen yet
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
    public Packet process(String jsonStr) {
            // Parse JSON to hashmap with pairs of name:values
        HashMap<String, Object> pairs;
        try {
            pairs = Listener.jsonToPairs(jsonStr);
        }  catch (JsonSyntaxException e) {
            return error(BAD_JSON, e);
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
            if (this.machine != null)
                this.machine.close();

            String className = OpiMachine.class.getPackage().getName() + "." + pairs.get("machine");
            try {
                machine = (OpiMachine) Class.forName(className).getDeclaredConstructor().newInstance();
                return ok(String.format(MACHINE_SELECTED, pairs.get("machine")), false);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                   InvocationTargetException | NoSuchMethodException | SecurityException e) {
                return error(String.format(WRONG_MACHINE_NAME, pairs.get("machine")), e);
            }
        } else { // If it is not a CHOOSE command and there is no machine open, give up else try it out
            if (this.machine == null)
                return error(NO_CHOOSE_COMMAND);
            return this.machine.processPairs(pairs);
        }
    }
}