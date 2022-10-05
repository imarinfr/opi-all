package org.lei.opi.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.lei.opi.core.OpiManager.Command;
import org.lei.opi.core.definitions.MessageProcessor;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;
import org.reflections.Reflections;

import com.google.gson.Gson;

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
  /** {@value INVOCATION_FAILED} */
  static final String INVOCATION_FAILED = "Cannot invoke '%s' in '%s'. Either the annotation with parameters is incorrect or the method failed";
  /** {@value NOT_DOUBLE} */
  static final String OUT_OF_RANGE = "Parameter '%s' in function '%s' of '%s' is not in range [%s, %s]. It is %s.";
  /** {@value NOT_INITIALIZED} */
  static final String NOT_INITIALIZED = "OPI machine has not yet been initialized";
  /** {@value COULD_NOT_QUERY} */
  static final String COULD_NOT_QUERY = "Could not query the OPI machine";
  /** {@value COULD_NOT_SETUP} */
  static final String COULD_NOT_SETUP = "Could not setup the background and fixation target on the OPI machine";
  /** {@value COULD_NOT_PRESENT} */
  static final String COULD_NOT_PRESENT = "Could not present on the OPI machine";
  /** {@value COULD_NOT_DISCONNECT} */
  static final String COULD_NOT_DISCONNECT = "Could not disconnect from OPI machine";
  /** {@value OPI_SETUP_FAILED} */
  static final String OPI_SETUP_FAILED = "Failed to complete opiSetup. ";
  /** {@value OPI_PRESENT_FAILED} */
  static final String OPI_PRESENT_FAILED = "Failed to complete opiPresent. ";
  /** {@value CONNECTED_TO_HOST} */
  static final String CONNECTED_TO_HOST = "\"Connected to host at %s:%s\"";
  /** {@value DISCONNECTED_FROM_HOST} */
  static final String DISCONNECTED_FROM_HOST = "\"Disconnected from OPI machine\"";

  protected class Jovp {
    int screen;
    int[] physicalSize;
    boolean fullScreen;
    int distance;
    String viewMode;
    String input;
    boolean tracking;
    int depth;
    String gammaFile;
  };
  protected class O900 {
    String eyeSuite;
    String gazeFeed;
    boolean bigWheel;
    boolean max10000;
    boolean f310;
  };
  protected class Settings {
    String ip;
    int port;
    Jovp jovp;
    O900 o900;
  };

  /** All machine settings from a JSON file */
  protected static final Settings SETTINGS;
  static {
     try {
      InputStream inputStream = OpiManager.class.getResourceAsStream("settings.json");
      SETTINGS = (new Gson()).fromJson(IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8)),
        Settings.class);
    } catch (IOException | AssertionError e) {
      e.printStackTrace();
      throw new RuntimeException("Could not load 'settings.json' file", e);
    }
  };

  /**
   *
   * Get the settings for a specific machine class
   *
   * @since 0.0.1
   */
  protected static Settings getMachineSettings(String machine) {
    System.out.println(SETTINGS);
    return SETTINGS;
  }

  /**
   * Class to hold information of the 5 key OPI methods ready for use.
   * Should be set in the constructor
   * 
   * @param method All methods of the class to find the machine-dependent implementations of the 5 key OPI commands
   * @param parameters The names expected in the JSON string that is a parameter of the corresponding OPI command
   * 
   * @since 0.0.1
   */
  protected record MethodData(Method method, Parameter[] parameters) {};

  /** The methods of the OpiMachine */
  protected HashMap<String, MethodData> opiMethods;
  /** Enum class : enum values defined in the package */
  protected HashMap<String, List<String>> enums; 
  /**
   * The machine connector to the OPI server.
   * It is setup in the device-dependent implementation of initialize
   * */
  protected CSWriter writer;

  /**
   * Set the information about the 5 OPI methods in opiMethods
   * It is assumed this is called by a subclass
   * 
   * @throws NoSuchMethodException If method cannot be found
   * @throws SecurityException If security has been breached
   *
   * @since 0.0.1
   */
  public OpiMachine() {
    writer = new CSWriter(SETTINGS.ip, SETTINGS.port);
    // Select the OPI commands
    String[] commands = Arrays.stream(OpiManager.Command.values())
      .map(Enum::name).map(String::toLowerCase).toArray(String[]::new);
    Method[] methods = Arrays.stream(this.getClass().getMethods())
      .filter((Method m) -> Arrays.stream(commands).anyMatch(m.getName()::equals)).toArray(Method[]::new);
    opiMethods = new HashMap<String, MethodData>();
    // Get OpiMachine and machine-dependent parameters through anotations
    for (Method method : methods) {
      Method parentMethod = Arrays.stream(OpiMachine.class.getMethods()).filter(m -> m.getName().equals(method.getName())).findFirst().get();
      Parameter[] parameters = ArrayUtils.addAll(parentMethod.getAnnotationsByType(Parameter.class),
                                                 method.getAnnotationsByType(Parameter.class));
      opiMethods.put(method.getName(), new MethodData(method, parameters));
    }    // gather all ENUMS for the method
    enums = new HashMap<String, List<String>>();    
    Reflections reflections = new Reflections(this.getClass().getPackageName());
    for (Class<?> e : reflections.getSubTypesOf(Enum.class))
      enums.put(e.getName(), Stream.of(e.getEnumConstants()).map(Enum.class::cast)
        .map(c -> c.name().toLowerCase()).toList());
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
      for (Parameter param : methodData.parameters) {
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
      result = methodData.parameters.length == 0
          ? (MessageProcessor.Packet) methodData.method.invoke(this)
          : (MessageProcessor.Packet) methodData.method.invoke(this, pairs);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      return OpiManager.error(String.format(INVOCATION_FAILED, funcName, this.getClass()), e);
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
  @ReturnMsg(name = "res.msg", desc = "The success or error message.")
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
  @ReturnMsg(name = "res.msg", desc = "The error message or a structure with the following data.")
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
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "The error message or a structure with the result of QUERY OPI command.")
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
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "Error message or a structure with the following fields.")
  @ReturnMsg(name = "res.msg.seen", className = Double.class, desc = "'1' if seen, '0' if not.", min = 0, max = 1)
  @ReturnMsg(name = "res.msg.time", className = Double.class, desc = "Response time from stimulus onset if button pressed (ms).", min = 0)
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
  @ReturnMsg(name = "res", desc = "JSON Object with all of the other fields described in @ReturnMsg except 'error'.")
  @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
  @ReturnMsg(name = "res.msg", desc = "The error message or additional results from the CLOSE command")
  public abstract MessageProcessor.Packet close();

  /**
   * build a JSON string to send the message to OPI server
   *
   * @param command The OPI command
   *
   * @return A JSON object
   *
   * @since 0.0.1
   */
  String toJson(Command command) {
    return new StringBuilder("{\n  \"command\": ").append(command).append("\n}").toString();
  }

}