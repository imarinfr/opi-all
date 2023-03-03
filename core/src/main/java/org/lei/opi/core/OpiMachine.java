package org.lei.opi.core;

import org.lei.opi.core.OpiListener.Packet;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.lei.opi.core.OpiListener.Command;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;
import org.reflections.Reflections;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javafx.scene.Scene;

/**
 * The OPI machine standard for communication with perimeters.
 * It implements the process() function of Listener to
 * dispatch the 5 standard methods (Initialize, Setup, Query, Present, Close)
 * to the appropriate abstract method which is then implemented in 
 * a machine specific way by the implementing classes.
 * 
 * This class also contains some common methods to deal with reading
 * and saving the settings file which records settings for each machine.
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
    /** {@value COULD_NOT_INITIALIZE} */
    static final String COULD_NOT_INITIALIZE = "Could not send initialize to the Machine";
    /** {@value COULD_NOT_QUERY} */
    static final String COULD_NOT_QUERY = "Could not query the OPI machine";
    /** {@value COULD_NOT_SETUP} */
    static final String COULD_NOT_SETUP = "Could not setup the background and fixation target on the Machine";
    /** {@value COULD_NOT_PRESENT} */
    static final String COULD_NOT_PRESENT = "Could not present on the Machine";
    /** {@value COULD_NOT_PRESENT} */
    static final String COULD_NOT_CLOSE = "Could not close the Machine";
    /** {@value COULD_NOT_DISCONNECT} */
    static final String COULD_NOT_DISCONNECT = "Could not disconnect from Machine";
    /** {@value OPI_SETUP_FAILED} */
    static final String OPI_SETUP_FAILED = "Failed to complete opiSetup. ";
    /** {@value OPI_PRESENT_FAILED} */
    static final String OPI_PRESENT_FAILED = "Failed to complete opiPresent. ";
    /** {@value CONNECTED_TO_HOST} */
    static final String CONNECTED_TO_HOST = "\"Connected to host at %s:%s\"";
    /** {@value DISCONNECTED_FROM_HOST} */
    static final String DISCONNECTED_FROM_HOST = "\"Disconnected from Machine\"";
  
    /** {@value SETTINGS_FILE} located in System.getProperty("user.dir") */
    static final String SETTINGS_FILE = "opi_settings.json";
  
    /** {@value MACHINES} */
    public static final String[] MACHINES = {"Echo", "Compass", "Maia", "O900", "ImoVifa", "Display", "PhoneHMD", "PicoVR"};
    /** {@value GUI_MACHINE_NAME} */
    public static final String GUI_MACHINE_NAME = "this";
  
    /** Scene to which we will return when this object is junked */
    protected Scene parentScene;  // return here when btnClose is clicked on our GUI

    /** Connection to the real machine */
    protected Socket socket;  // return here when btnClose is clicked on our GUI
    protected BufferedReader incoming;
    protected BufferedWriter outgoing;
  
    /** 
     * The beginnings of machine specific settings. 
     * 
     * All machines have ip and port. Some might add more settings in the 
     * subclass, and so should extend this class there. 
     * Each subclass should also implement the accessor {@link getSettings}
     * to return their extended Settings object.
     * Note that the fields in Settings and its subclasses should be public 
     * so that the Monitor GUI can grab them.
     }
    */
    protected static class Settings {
      public String ip;
      public int port;
    };
    public abstract Settings getSettings();

  /**
   * Read in whole settings Json to a HashMap keyed by machine name.
   *
   * @return HashMap keyed by machine name with Settings Objects as values.
   */
    public static HashMap<String, Object> readSettingsFile() {
        Gson gson = new Gson();
        String fp = System.getProperty("user.dir") + "/" + SETTINGS_FILE;
        try {
            // Get default settings
            InputStream inputStream = new FileInputStream(fp);
            return gson.fromJson(IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8)),
              new TypeToken<HashMap<String, Object>>() {}.getType());
        } catch (IOException | AssertionError e) {
            System.out.println("Could not read settings file.");
            e.printStackTrace();
        }
        return new HashMap<String, Object>();  // empty HashMap on error
    }

    /**
     * Write the HashMap keyed by machine name as Json to {@link SETTINGS_FILE}.
     *
     * @param map HashMap keyed by machine name with Settings Objects as values.
     */
      public static void writeSettingsFile( HashMap<String, Object> map) {
          Gson gson = new Gson();
          String fp = System.getProperty("user.dir") + "/" + SETTINGS_FILE;
          try {
              FileWriter fw = new FileWriter(fp);
              fw.write(gson.toJson(map));
              fw.close();
          } catch (IOException e) {
              System.out.println("Could not write settings file.");
              e.printStackTrace();
          }
      }
  
    /**
     * Create a {@link Settings} class cls with values from the {@value SETTINGS_FILE}.
     * 
     * @param cls A {@link Settings} class to create and return.  TODO delete this
     * @param machineName Short name of an OpiMachine subclass which has a {@link Settings} inner class
     * @return A {@link Settings} object of type cls with values from {@value SETTINGS_FILE}.
      Class<? extends Settings> cls) {
     */
    public static Object fillSettings(String machineName) {
        HashMap<String, Object> settingsMap = OpiMachine.readSettingsFile();
        if (settingsMap.containsKey(machineName)) {
            Gson gson = new Gson();
            String j = gson.toJson(settingsMap.get(machineName));
            try {
                Class<?> cls = Class.forName("org.lei.opi.core."  + machineName + "$Settings");
                return gson.fromJson(j, cls);
            } catch (ClassNotFoundException e) {
                System.out.println("Cannot find class " + "org.lei.opi.core."  + machineName + "$Settings");
                return null;
            }
        } 
        System.out.println("Cannot find " + machineName + " in settings file");
        return null;
    }
  
    /**
     * Class to hold information of the 5 key OPI methods ready for use.
     * Should be set in the constructor
     * 
     * @param method All methods of the class to find the machine-dependent implementations of the 5 key OPI commands
     * @param parameters The names expected in the JSON string that is a parameter of the corresponding OPI command
     */
    protected record MethodData(Method method, Parameter[] parameters) {};
  
        /** The methods of the OpiMachine */
    protected HashMap<String, MethodData> opiMethods;
        /** Enum class : enum values defined in the implementing class */
    protected HashMap<String, List<String>> enums; 
  
    /**
     * Set the information about the 5 OPI methods in opiMethods
     * 
     * @param parentScene the parent Scene to which the GUI will return when this machine is closed.
     *
     * @throws NoSuchMethodException If method cannot be found
     * @throws SecurityException If security has been breached
     *
     * @since 0.0.1
     */
    public OpiMachine(Scene parentScene) {
        this.parentScene = parentScene;
        this.socket = null;
      
        // Select the OPI commands which must also be the method names in the implementing class.
        String[] commands = Arrays.stream(OpiListener.Command.values())
          .map(Enum::name).map(String::toLowerCase).toArray(String[]::new);
      
        Method[] methods = Arrays.stream(this.getClass().getMethods())
          .filter((Method m) -> Arrays.stream(commands).anyMatch(m.getName()::equals)).toArray(Method[]::new);
      
        opiMethods = new HashMap<String, MethodData>();
      
        // Get OpiMachine and machine-dependent parameters through annotations
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

    /*
     * Establish socket connection to ip:port.
     * @param ip String TCP/IP address
     * @param port integer TCP/IP port number
     * @return true if successful false otherwise
     */
    public boolean connect(String ip, int port) {
        try {
            this.socket = new Socket(ip, port);
            this.incoming = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
            this.outgoing = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
            return false;
        }
        return true;
    }
      
    /**
     * Send JSON message to socket
     * @param message The message to send
     * @throws IOException If socket cannot be accessed
     * @since 0.2.0
     */
    void send(String message) throws IOException {
        outgoing.write(message);
    }
     
    /**
    * Receive \n terminated JSON message from monitor
    * @return The message received
    * @throws IOException If socket cannot be accessed
    * @since 0.2.0
    */
    String receive() throws IOException {
        return incoming.readLine();
    }
     
    /**
    * Close socket
    * @throws IOException If client cannot be closed
    * @since 0.2.0
    */
    void closeSocket() throws IOException {
        incoming.close();
        outgoing.close();
        socket.close();
    }
  
    /**
     * Map the 'command' to a function, check it has the right parameters, and the call it
     *
     * @param jsonStr A JSON string of name:value pairs with at least the name "command"
     * 
     * @return Json object like OpiMachine.ok() or OpiMachine.error()
     * 
     * @since 0.2.0
     */
    public Packet process(String jsonStr) {
            // Parse JSON to hashmap with pairs of name:values
        HashMap<String, Object> pairs;
        try {
            pairs = OpiListener.jsonToPairs(jsonStr);
        }  catch (JsonSyntaxException e) {
            return OpiListener.error(OpiListener.BAD_JSON, e);
        }
        return this.processPairs(pairs);
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
    public Packet processPairs(HashMap<String, Object> pairs) {
        /*
         * Processing consist of the following three steps:
         *    (1) Find the function which is the value of the JSON name "command"
         *    (2) Check that the params for the function are in the JSON (via the @Parameter Annotation)
         *    (3) Then execute corresponding method
         */
        // (1) find the function
        String funcName = (String) pairs.get("command");
        MethodData methodData = opiMethods.get(funcName);
        if (methodData == null)
          return OpiListener.error(String.format(BAD_COMMAND, funcName, this.getClass()));
        // (2) Check params
        if (methodData.parameters != null)
          for (Parameter param : methodData.parameters) {
            // mandatory parameter not received
            if (!pairs.containsKey(param.name()) && !param.optional())
              return OpiListener.error(String.format(MISSING_PARAMETER, param.name(), funcName, this.getClass()));
            if (!pairs.containsKey(param.name()) && param.optional()) continue;
            Object valueObj = pairs.get(param.name());
            // check lists and list of lists
            if ((param.isListList() || param.isList()) && (!(valueObj instanceof ArrayList) || ((ArrayList<?>) valueObj).size() == 0))
              return OpiListener.error(String.format(NOT_LIST, param.name(), funcName, this.getClass()));
            if (!(param.isListList() || param.isList()) && (valueObj instanceof ArrayList))
              return OpiListener.error(String.format(YES_LIST, param.name(), funcName, this.getClass()));
            // if list is actually made of lists, need to make them into a single list of values
            if (param.isListList() &&
                ((ArrayList<?>) valueObj).stream().anyMatch(val -> !(val instanceof ArrayList) || ((ArrayList<?>) val).size() == 0))
              return OpiListener.error(String.format(NOT_LISTLIST, param.name(), funcName, this.getClass()));
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
                return OpiListener.error(String.format(NOT_IN_ENUM, result.get(), param.className(), param.name(), funcName, this.getClass()));
            } else if (param.className().getSimpleName().equals("Double")) { // validate doubles
              try {
                double minVal = Math.round(1e10 * param.min()) / 1e10; // avoid weird rounding problems
                double maxVal = Math.round(1e10 * param.max()) / 1e10;
                Optional<Double> result = pList.stream()
                    .map((Object o) -> (Double) o)
                    .filter((Object v) -> ((Double) v).doubleValue() < minVal || ((Double) v).doubleValue() > maxVal)
                    .findAny();
                if (result.isPresent())
                  return OpiListener.error(String.format(OUT_OF_RANGE, param.name(), funcName, this.getClass(), minVal, maxVal, result.get()));
              } catch (ClassCastException e) {
                return OpiListener.error(String.format(NOT_A_DOUBLE, param.name(), funcName, this.getClass()));
              }
            } else { // assume param is a String, then validate
              Optional<Object> result = pList.stream().filter(v -> !(v instanceof String)).findAny();
              if (result.isPresent())
                return OpiListener.error(String.format(NOT_A_STRING, param.name(), funcName, this.getClass()));
            }
          }
        // (3) execute method
        Packet result;
        try {
          result = methodData.parameters.length == 0
              ? (Packet) methodData.method.invoke(this)
              : (Packet) methodData.method.invoke(this, pairs);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          return OpiListener.error(String.format(INVOCATION_FAILED, funcName, this.getClass()), e);
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
    @ReturnMsg(name = "res", desc = "List with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "res.error", desc = "Error code '0' if all good, '1' something wrong.")
    @ReturnMsg(name = "res.msg", desc = "The success or error message.")
    public abstract Packet initialize(HashMap<String, Object> args);
  
    /**
     * opiQuery: Query device
     * 
     * @return settings and state machine state
     *
     * @since 0.0.1
     */
    @ReturnMsg(name = "res", desc = "List with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
    @ReturnMsg(name = "res.msg", desc = "The error message or a structure with the following data.")
    public abstract Packet query();
  
    /**
     * opiSetup: Change device background and overall settings
     * 
     * @param args pairs of argument name and value
     * 
     * @return A JSON object with return messages
     *
     * @since 0.0.1
     */
    @ReturnMsg(name = "res", desc = "List with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
    @ReturnMsg(name = "res.msg", desc = "The error message or a structure with the result of QUERY OPI command.")
    public abstract Packet setup(HashMap<String, Object> args);
  
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * 
     * @param args pairs of argument name and value
     * 
     * @return A JSON object with return messages
     *
     * @since 0.0.1
     */
    @ReturnMsg(name = "res", desc = "List with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
    @ReturnMsg(name = "res.msg", desc = "Error message or a structure with the following fields.")
    @ReturnMsg(name = "res.msg.seen", className = Double.class, desc = "'1' if seen, '0' if not.", min = 0, max = 1)
    @ReturnMsg(name = "res.msg.time", className = Double.class, desc = "Response time from stimulus onset if button pressed (ms).", min = 0)
    public abstract Packet present(HashMap<String, Object> args);
  
    /**
     * opiClose: Close OPI connection
     * 
     * @param args pairs of argument name and value
     *
     * @return A JSON object with return messages
     *
     * @since 0.0.1
     */
    @ReturnMsg(name = "res", desc = "List with all of the other fields described in @ReturnMsg except 'error'.")
    @ReturnMsg(name = "res.error", desc = "'0' if success, '1' if error.")
    @ReturnMsg(name = "res.msg", desc = "The error message or additional results from the CLOSE command")
    public abstract Packet close();
  
    /**
     * build a JSON string to send the message to OPI server
     *
     * @param command The OPI command
     *
     * @return A JSON object
     *
     * @since 0.0.1
     */
    protected String toJson(Command command) {
      return new StringBuilder("{\n  \"command\": ").append(command).append("\n}").toString();
    }
}