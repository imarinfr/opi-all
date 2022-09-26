package org.lei.opi.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.lei.opi.core.definitions.MessageProcessor;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.Parameters;
import org.lei.opi.core.definitions.ReturnMsg;
import org.reflections.Reflections;

/**
 * The OPI machine standard for communication with perimeters
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
  /** {@value INCORRECT_FORMAT_IP_PORT} */
  static final String INCORRECT_FORMAT_IP_PORT = "IP and port have the wrong format";
  /** {@value UNKNOWN_HOST} */
  static final String SERVER_NOT_READY = "Server at %s is unknown or does not exist";
  /** {@value CONNECTED_TO_HOST} */
  static final String CONNECTED_TO_HOST = "Connected to host at ";
  /** {@value NOT_INITIALIZED} */
  static final String NOT_INITIALIZED = "OPI machine has not yet been initialized";
  /** {@value COULD_NOT_QUERY} */
  static final String COULD_NOT_QUERY = "Could not query the OPI machine";
  /** {@value COULD_NOT_SETUP} */
  static final String COULD_NOT_SETUP = "Could not setup the background and fixation target on the OPI machine";
  /** {@value COULD_NOT_PRESENT} */
  static final String COULD_NOT_PRESENT = "Could not present on the OPI machine";
  /** {@value COULD_NOT_DISCONNECT} */
  static final String COULD_NOT_DISCONNECT = "Could not disconnect from OPI machine at";
  /** {@value DISCONNECTED_TO_HOST} */
  static final String DISCONNECTED_TO_HOST = "Connected from host at ";
  /** {@value OPI_SETUP_FAILED} */
  static final String OPI_SETUP_FAILED = "Failed to complete opiSetup. ";
  /** {@value OPI_PRESENT_FAILED} */
  static final String OPI_PRESENT_FAILED = "Failed to complete opiPresent. ";

  /**
   * Class to hold information of the 5 key OPI methods ready for use.
   * Should be set in the constructor
   * 
   * @since 0.0.1
   */
  protected class MethodData {
    // All methods of the class to find the achine-dependent implementations of the 5 key OPI commands
    Method method;
    // The names expected in the JSON string that is a parameter of the 5 key OPI methods
    Parameters parameters;
  };

  /** The methods of the OpiMachine */
  protected HashMap<String, MethodData> opiMethods;
  /** Enum class : enum values defined in the package */
  protected HashMap<String, List<String>> enums; 
  /**
   * The machine connector to the OPI server.
   * It is setup in the device-dependent implementation of initialize
   * */
  protected CSWriter writer;
  /** Whether perimeter is initialized */
  protected boolean initialized;

  /**
   * Set the information about the 5 OPI methods in opiMethods
   * It is assumed this is called by a subclass
   * 
   * @since 0.0.1
   */
  public OpiMachine() {
    initialized = false;

    opiMethods = new HashMap<String, MethodData>();
    for (Method method : this.getClass().getMethods()) {
      MethodData data = new MethodData();
      data.method = method;
      data.parameters = method.getAnnotation(Parameters.class);

      // key is method name, value is array of annotations on that method
      Reflections reflections = new Reflections(this.getClass().getPackageName());
      enums = new HashMap<String, List<String>>();
      for (Class<?> e : reflections.getSubTypesOf(Enum.class))
        enums.put(e.getName(), Stream.of(e.getEnumConstants()).map(Enum.class::cast).map(c -> c.name().toLowerCase()).toList());
      opiMethods.put(method.getName(), data);
    }
  }

  /**
   * Map the 'command' to a function, check it has the right parameters, and the call it
   *
   * @param pairs A list of name:value pairs with at least the name "command"
   * 
   * @return Json object like OpiManger.ok() or OpiManager.error()
   * 
   * @since 0.0.1
   */
  public MessageProcessor.Packet process(HashMap<String, Object> pairs) {
    /*
     * Processing consist of the following three steps:
     *    (1) Find the function which is the value of the JSON name "command"
     *    (2) Check that the params for the function are in the JSON (via the Params Annotation)
     *    (3) Then execute corresponding method
     */
    // (1) find the function
    String funcName = (String) pairs.get("command");
    MethodData methodData = opiMethods.get(funcName);
    if (methodData == null)
      return OpiManager.error(String.format("cannot find function %s in %s.",
          funcName, this.getClass()));
    // (2) Check params
    if (methodData.parameters != null)
      for (Parameter param : methodData.parameters.value()) {
        if (!pairs.containsKey(param.name()) && !param.optional())
          return OpiManager.error(
              String.format("Parameter %s is missing for function %s in %s.", param.name(), funcName, this.getClass()));
        if (!pairs.containsKey(param.name()) && param.optional())
          continue;
        Object valueObj = pairs.get(param.name());
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
    // (3) execute method
    MessageProcessor.Packet result;
    try {
      result = methodData.parameters == null
          ? (MessageProcessor.Packet) methodData.method.invoke(this)
          : (MessageProcessor.Packet) methodData.method.invoke(this, pairs);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      return OpiManager.error(String.format("Cannot execute %s in %s.", funcName, this.getClass()), e);
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
  @Parameter(name = "ip", desc = "IP Address of the OPI machine.")
  @Parameter(name = "port", desc = "TCP port of the OPI machine.", className = Double.class, min = 0, max = 65535, defaultValue = "50001")
  @Parameter(name = "ipMonitor", desc = "IP Address of the OPI monitor.")
  @Parameter(name = "portMonitor", desc = "TCP port of the OPI monitor.", className = Double.class, min = 0, max = 65535, defaultValue = "50008")
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "Error code '0' if all good, '1' something wrong.")
  @ReturnMsg(name = "res.msg", desc = "The error message if an error occured or empty otherwise.")
  public MessageProcessor.Packet initialize(HashMap<String, Object> args) {
    try {
      writer = new CSWriter((String) args.get("ip"), (int) ((double) args.get("port")));
      initialized = true;
      return OpiManager.ok(CONNECTED_TO_HOST + args.get("ip") + ":" + (int) ((double) args.get("port")));
    } catch (ClassCastException e) {
      return OpiManager.error(INCORRECT_FORMAT_IP_PORT);
    } catch (IOException e) {
      return OpiManager.error(String.format(SERVER_NOT_READY, args.get("ip") + ":" + (int) ((double) args.get("port"))));
    }
  };

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

}