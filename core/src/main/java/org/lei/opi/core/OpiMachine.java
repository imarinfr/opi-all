package org.lei.opi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.Parameters;
import org.reflections.Reflections;

/**
 * The OPI standard for communication with perimeters
 *
 * @since 0.0.1
 */
public abstract class OpiMachine {

  /** {@value BAD_CHOOSE} */
  static final String BAD_CHOOSE = "JSON object does not contain 'command:choose'.";
  /** {@value BAD_MACHINE} */
  static final String BAD_MACHINE = "JSON choos object does not contain the name 'machine'";
  /** {@value UNKNOWN_MACHINE} */
  static final String UNKNOWN_MACHINE = "machine:%s in JSON choose object is unknown.";
  /** {@value BAD_MACHINE_CONSTRUCT} */
  static final String BAD_MACHINE_CONSTRUCT = "Cannot construct machine %s in JSON choose object.\n";

  static final String INCORRECT_FORMAT_IP_PORT = "IP and port have the wrong format";
  static final String COULD_NOT_CONNECT = "Could not connect to JOVP machine at ";
  static final String NOT_INITIALIZED = "JOVP machine has not yet been initialized";
  static final String COULD_NOT_QUERY = "Could not query the JOVP machine";
  static final String COULD_NOT_SETUP = "Could not setup the background and fixation target on the JOVP machine";
  static final String COULD_NOT_PRESENT = "Could not present on the JOVP machine";
  static final String COULD_NOT_DISCONNECT = "Could not disconnect from JOVP machine";


  /**
   * Class to hold information of the 5 key OPI methods ready for use.
   * Should be set in the constructor.
   */
  protected class MethodData {
    Method method;
    Parameters parameters; // The names expected in the JSON string that is a parameter of the 5 key OPI
                           // methods.
  };

  protected HashMap<String, MethodData> opiMethods;
  protected HashMap<String, List<String>> enums; // Enum class : enum values defined in the package.

  /** Connection to perimeter */
  private Socket perimeter;
  /** Outgoing messages to perimeter */
  private BufferedWriter outgoing;
  /** Incoming messages from perimeter */
  private BufferedReader incoming;
  /** Whether perimeter is initialized */
  private boolean initialized;

  /**
   * Set the information about the 5 OPI methods in opiMethods.
   * It is assumed this is called by a subclass.
   */
  public OpiMachine() {
    initialized = false;

    opiMethods = new HashMap<String, MethodData>();
    for (Method method : this.getClass().getMethods()) {
      MethodData data = new MethodData();
      data.method = method;
      data.parameters = method.getAnnotation(Parameters.class);

      // Reflections reflections = new Reflections(this.getClass().getPackageName() +
      // ".opi-core");
      Reflections reflections = new Reflections(this.getClass().getPackageName());
      enums = new HashMap<String, List<String>>();
      for (Class<?> e : reflections.getSubTypesOf(Enum.class))
        enums.put(e.getName(), Stream.of(e.getEnumConstants()).map(Enum.class::cast).map(c -> c.name().toLowerCase()).toList());
        //enums.put(e.getName(), Stream.of(e.getEnumConstants()).map((Enum c) -> c.name().toLowerCase()).toList());//

      // key is method name, value is array of annotations on that method

      opiMethods.put(method.getName(), data);
    }
  }

  /**
   * Map the 'command' to a function, check it has the right parameters, and then
   * call it.
   *
   * @param nameValuePairs A list of name:value pairs with at least the name
   *                       "command".
   * 
   * @return Json object like OpiManger.ok() or OpiManager.error()
   */
  public MessageProcessor.Packet process(HashMap<String, Object> nameValuePairs) {
    // (1) Find the function which is the value of the JSON name "command"
    // (2) Check that the params for the function are in the JSON (via the Params
    // Annotation)
    // (3) Then execute the function.
    String funcName = (String) nameValuePairs.get("command");
    MethodData methodData = opiMethods.get(funcName);

    if (methodData == null)
      return OpiManager.error(String.format("cannot find function %s in %s.",
          funcName, this.getClass()));

    // (2)
    if (methodData.parameters != null)
      for (Parameter param : methodData.parameters.value()) {
        if (!nameValuePairs.containsKey(param.name()) && !param.optional())
          return OpiManager.error(
              String.format("Parameter %s is missing for function %s in %s.", param.name(), funcName, this.getClass()));

        if (!nameValuePairs.containsKey(param.name()) && param.optional())
          continue;

        Object valueObj = nameValuePairs.get(param.name());

        if (param.isList() && (!(valueObj instanceof ArrayList) || ((ArrayList<?>) valueObj).size() == 0))
          return OpiManager
              .error(String.format("Parameter %s should be a non-empty list but is not for function %s in %s.",
                  param.name(), funcName, this.getClass()));
        if (!param.isList() && (valueObj instanceof ArrayList))
          return OpiManager.error(String.format("Parameter %s should not be a list but is not for function %s in %s.",
              param.name(), funcName, this.getClass()));

        List<Object> pList = !param.isList() ? Arrays.asList(valueObj) : ((ArrayList<?>) valueObj).stream().map(Object.class::cast).toList();

        if (enums.containsKey(param.className().getName())) {
          List<String> enumVals = enums.get(param.className().getName());
          System.out.println(param.name());

          Optional<Object> result = pList.stream()
              .filter(p -> !(p instanceof String)
                  || !enumVals.stream().anyMatch(ss -> ss.contains(((String) p).toLowerCase())))
              .findAny();

          if (result.isPresent())
            return OpiManager
                .error(String.format("I cannot find %s in enum type %s for parameter %s in function %s in %s.",
                    result.get(), param.className(), param.name(), funcName, this.getClass()));
        } else if (param.className().getSimpleName().equals("Double")) {
          try {
            Optional<Double> result = pList.stream()
                .map((Object o) -> (Double) o)
                .filter(
                    (Object v) -> ((Double) v).doubleValue() < param.min() || ((Double) v).doubleValue() > param.max())
                .findAny();

            if (result.isPresent())
              return OpiManager.error(String.format(
                  "Parameter %s in function %s of %s is either not a double or not in range [%s,%s]. It is %s.",
                  param.name(), funcName, this.getClass(), param.min(), param.max(), result.get()));
          } catch (ClassCastException e) {
            return OpiManager.error(String.format("A parameter in %s in function %s of %s is not a double.",
                param.name(), funcName, this.getClass()));
          }
        } else { // assuming param is a String
          Optional<Object> result = pList.stream()
              .filter(v -> !(v instanceof String))
              .findAny();
          if (result.isPresent())
            return OpiManager.error(String.format("Parameter %s in function %s of %s should be a String.",
                param.name(), funcName, this.getClass()));
        }
      }
    // (3)
    MessageProcessor.Packet result;
    try {
      result = methodData.parameters == null ? (MessageProcessor.Packet) methodData.method.invoke(this)
          : (MessageProcessor.Packet) methodData.method.invoke(this, nameValuePairs);
    } catch (IllegalAccessException | InvocationTargetException e) {
      return new MessageProcessor.Packet(true, false,
          String.format("cannot execute %s in %s. %s", funcName, this.getClass(), e));
    }
    return result;
  };

  /**
   * opiInitialise: initialize OPI
   * 
   * @param args A map of name:value pairs for Params
   * 
   * @return A JSON object with machine specific initialise information
   * 
   * @since 0.0.1
   */
  public abstract MessageProcessor.Packet initialize(HashMap<String, Object> args);

  /**
   * opiInitialise: initialize OPI
   * 
   * @param args A map of name:value pairs for Params
   * 
   * @return A JSON object with return messages
   * 
   * @since 0.0.1
   */
  public MessageProcessor.Packet initialise(HashMap<String, Object> args) {
    return this.initialize(args);
  }

  /**
   * opiQuery: Query device
   * 
   * @return settings and state machine state
   *
   * @since 0.0.1
   */
  public abstract MessageProcessor.Packet query();

  /**
   * opiSetup: Change device background and overall settings
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  public abstract MessageProcessor.Packet setup(HashMap<String, Object> args);

  /**
   * opiPresent: Present OPI stimulus in perimeter
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  public abstract MessageProcessor.Packet present(HashMap<String, Object> args);

  /**
   * opiClose: Close OPI connection
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  public abstract MessageProcessor.Packet close();

  /**
   * Get whether perimeter is initialized
   * 
   * @return initialized whether initrialized or not
   *
   * @since 0.0.1
   */
  public boolean getInitialised() {
    return initialized;
  }

  /**
   * Send opiInitialize command to perimeter
   * 
   * @param jsonStr
   *
   * @throws IOException if something went wrong making the socket connection
   *
   * @since 0.0.1
   */
  public MessageProcessor.Packet sendInitCommand(String ip, int port, String jsonStr) {
    try {
      openConnection(ip, port);
      outgoing.write(jsonStr);
      // TODO get feedback
      String msg = "FEEDBACK";
      return OpiManager.ok(msg, false);
    } catch (IOException e) {
      return OpiManager.error(COULD_NOT_CONNECT + ip + ":" + port);
    }
  }

  /**
   * Send opiQuery command to perimeter
   *
   * @throws IOException if something went wrong making the socket connection
   *
   * @since 0.0.1
   */
  public MessageProcessor.Packet sendQueryCommand() {
    // TODO CONSTRUCT QUERY COMMAND
    String jsonStr = "QUERY COMMAND";
    try {
      outgoing.write(jsonStr);
      // TODO get feedback
      String msg = "FEEDBACK";
      return OpiManager.ok(msg, false);
    } catch (IOException e) {
      return OpiManager.ok(COULD_NOT_QUERY, false);
    }
  }

  /**
   * Send opiClose command to perimeter
   * 
   * @param jsonStr
   *
   * @throws IOException if something went wrong making the socket connection
   *
   * @since 0.0.1
   */
  public MessageProcessor.Packet sendCloseCommand(String jsonStr) {
    try {
      outgoing.write(jsonStr);
      // TODO get feedback
      String msg = "FEEDBACK";
      closeConnection();
      return OpiManager.ok(msg, false);
    } catch (IOException e) {
      return OpiManager.ok(COULD_NOT_DISCONNECT, false);
    }
  }

  /**
   * Open socket connection
   * 
   * @param ip perimeter IP address
   * @param port perimeter port
   *
   * @throws IOException if something went wrong making the socket connection
   *
   * @since 0.0.1
   */
  private void openConnection(String ip, int port) throws IOException {
    perimeter = new Socket(ip, port);
    outgoing = new BufferedWriter(new OutputStreamWriter(perimeter.getOutputStream()));
    incoming = new BufferedReader(new InputStreamReader(perimeter.getInputStream()));
    initialized = true;
  }

  /**
   * Close socket connection
   *
   * @throws IOException if something went wrong making the socket connection
   *
   * @since 0.0.1
   */
  private void closeConnection() throws IOException {
    perimeter.close();
    outgoing.close();
    incoming.close();
    initialized = false;
  }

}