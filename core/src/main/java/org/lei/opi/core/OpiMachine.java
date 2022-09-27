package org.lei.opi.core;

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

  /** {@value BAD_COMMAND} */
  static final String BAD_COMMAND = "Cannot find command '%s' in %s.";
  /** {@value MISSING_PARAMETER} */
  static final String MISSING_PARAMETER = "Parameter '%s' is missing for function '%s' in %s.";
  /** {@value NOT_LIST} */
  static final String NOT_LIST = "Parameter '%s' should be a non-empty list but it is not for function '%s' in %s.";
  /** {@value NOT_LISTLIST} */
  static final String NOT_LISTLIST = "Parameter '%s' should be a non-empty list of non-empty lists but it is not for function '%s' in %s.";
  /** {@value YES_LIST} */
  static final String YES_LIST = "Parameter '%s' should not be a list but it is for function '%s' in %s.";
  /** {@value NOT_IN_ENUM} */
  static final String NOT_IN_ENUM = "I cannot find '%s' in enum type '%s' for parameter '%s' in function '%s' in %s.";
  /** {@value NOT_DOUBLE} */
  static final String NOT_A_DOUBLE = "Parameter '%s' in function '%s' of '%s' is not double.";
  /** {@value NOT_A_STRING} */
  static final String NOT_A_STRING = "Parameter '%s' in function '%s' of '%s' should be a String.";
  /** {@value NOT_DOUBLE} */
  static final String OUT_OF_RANGE = "Parameter '%s' in function '%s' of '%s' is not in range [%s, %s]. It is %s.";
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
      return OpiManager.error(String.format(BAD_COMMAND, funcName, this.getClass()));
    // (2) Check params
    if (methodData.parameters != null)
      for (Parameter param : methodData.parameters.value()) {
        // mandatory parameter not received
        if (!pairs.containsKey(param.name()) && !param.optional())
          return OpiManager.error(String.format(MISSING_PARAMETER, param.name(), funcName, this.getClass()));
        if (!pairs.containsKey(param.name()) && param.optional()) continue;
        Object valueObj = pairs.get(param.name());
        // check lists and list of lists
        if ((param.isListList() || param.isList()) && (!(valueObj instanceof ArrayList) || ((ArrayList<?>) valueObj).size() == 0))
          return OpiManager.error(String.format(NOT_LIST, param.name(), funcName, this.getClass()));
        if (!(param.isListList() || param.isList()) && (valueObj instanceof ArrayList))
          return OpiManager.error(String.format(YES_LIST, param.name(), funcName, this.getClass()));
        // if list is actually made of lists, need to make them into a single list of values
        if (param.isListList() &&
            ((ArrayList<?>) valueObj).stream().anyMatch(val -> !(val instanceof ArrayList) || ((ArrayList<?>) val).size() == 0))
          return OpiManager.error(String.format(NOT_LISTLIST, param.name(), funcName, this.getClass()));
        // recast
        List<Object> pList;
        if (param.isListList())
          pList = (((ArrayList<?>) valueObj).stream().map(Object.class::cast).toList()).stream()
            .map(vector -> (ArrayList<?>) vector).flatMap(List::stream).map(Object.class::cast).toList();
        else if (param.isList()) 
          pList = ((ArrayList<?>) valueObj).stream().map(Object.class::cast).toList();
        else
          pList = Arrays.asList(valueObj);
        if (enums.containsKey(param.className().getName())) { // validate enums
          List<String> enumVals = enums.get(param.className().getName());
          Optional<Object> result = pList.stream()
              .filter(p -> !(p instanceof String) || !enumVals.stream().anyMatch(ss -> ss.contains(((String) p).toLowerCase())))
              .findAny();
          if (result.isPresent())
            return OpiManager.error(String.format(NOT_IN_ENUM, result.get(), param.className(), param.name(), funcName, this.getClass()));
        } else if (param.className().getSimpleName().equals("Double")) { // validate doubles
          try {
            double minVal = Math.round(1e10 * param.min()) / 1e10; // avoid weird rounding problems
            double maxVal = Math.round(1e10 * param.max()) / 1e10;
            Optional<Double> result = pList.stream()
                .map((Object o) -> (Double) o)
                .filter((Object v) -> ((Double) v).doubleValue() < minVal || ((Double) v).doubleValue() > maxVal)
                .findAny();
            if (result.isPresent())
              return OpiManager.error(String.format(OUT_OF_RANGE, param.name(), funcName, this.getClass(), minVal, maxVal, result.get()));
          } catch (ClassCastException e) {
            return OpiManager.error(String.format(NOT_A_DOUBLE, param.name(), funcName, this.getClass()));
          }
        } else { // assume param is a String, then validate
          Optional<Object> result = pList.stream().filter(v -> !(v instanceof String)).findAny();
          if (result.isPresent())
            return OpiManager.error(String.format(NOT_A_STRING, param.name(), funcName, this.getClass()));
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
  @Parameter(name = "ip", desc = "IP Address of the OPI machine.", defaultValue = "localhost")
  @Parameter(name = "port", className = Double.class, desc = "TCP port of the OPI machine.", min = 0, max = 65535, defaultValue = "50001")
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "Error code '0' if all good, '1' something wrong.")
  @ReturnMsg(name = "res.msg", desc = "The error message if an error occured or empty otherwise.")
  public abstract MessageProcessor.Packet initialize(HashMap<String, Object> args);

  /**
   * opiQuery: Query device
   * 
   * @return settings and state machine state
   *
   * @since 0.0.1
   */
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "Error message or a structure with QUERY data.")
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