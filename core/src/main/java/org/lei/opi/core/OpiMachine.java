package org.lei.opi.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Consumer;

import java.lang.annotation.Annotation;

import org.lei.opi.core.OpiListener.Command;
import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;
import org.lei.opi.core.definitions.VFCanvas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

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
    /** {@value BAD_DEFAULT} */
    static final String BAD_DEFAULT = "Default value for '%s' in %s is not valid JSON for the parameter type.";
    /** {@value BAD_TYPE} */
    static final String BAD_TYPE = "Type for parameter '%s' is %s in method %s of class %s which is not a class I can find.";
    /** {@value BAD_TYPE2} */
    static final String BAD_TYPE2 = "I cannot convert the value for parameter '%s' in %s to the class %s in %s.";
    /** {@value NOT_LIST} */
    static final String NOT_LIST = "Parameter '%s' should be a non-empty list but it is not for function '%s' in %s.";
    /** {@value NOT_LISTLIST} */
    static final String NOT_LISTLIST = "Parameter '%s' should be a non-empty list of non-empty lists but it is not for function '%s' in %s.";
    /** {@value YES_LIST} */
    static final String YES_LIST = "Parameter '%s' should not be a list but it is for function '%s' in %s.";
    /** {@value NOT_IN_ENUM} */
    static final String NOT_IN_ENUM = "I cannot find the value you gave for '%s' in enum type '%s' in function '%s' in %s.";
    /** {@value NOT_DOUBLE} */
    static final String NOT_A_DOUBLE = "Parameter '%s' in function '%s' of '%s' is not double.";
    /** {@value NOT_AN_INTEGER} */
    static final String NOT_AN_INTEGER = "Parameter '%s' in function '%s' of '%s' is not integer.";
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
    static final String CONNECTED_TO_HOST = "Connected to host at %s:%s";
    /** {@value DISCONNECTED_FROM_HOST} */
    static final String DISCONNECTED_FROM_HOST = "Disconnected from Machine";
  
    /** {@value SETTINGS_FILE} located in System.getProperty("user.dir") */
    static final String SETTINGS_FILE = "opi_settings.json";
  
    /** {@value MACHINES} */
    public static final String[] MACHINES = {"Echo", "Compass", "Maia", "O900", "ImoVifa", "Display", "PhoneHMD", "PicoVR"};
    /** {@value GUI_MACHINE_NAME} */
    public static final String GUI_MACHINE_NAME = "this";
  
    private static Gson gson = new Gson();

    /** Scene to which we will return when this object is junked */
    protected Scene parentScene;  // return here when btnClose is clicked on our GUI

    /** Set by constructor depending on viewMode and tracking settings. 
     *  Do not include full pathname, just simple name with .fxml extension*/
    public String fxmlFileName;

        // Set by call to setVFCanvas. Controls number of eyes shown in GUI
    public boolean viewModeIsMono;
        // Set by call to setVFCanvas. Controls whether tracking images shown in GUI
    public boolean trackingOn;

    /** Connection to the real machine */
    protected Socket socket;
    protected DataInputStream incoming;
    protected PrintWriter outgoing;

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

    public static final Gson settings_gson = new GsonBuilder().setPrettyPrinting().create();

  /**
   * Read in whole settings Json to a HashMap keyed by machine name.
   *
   * @return HashMap keyed by machine name with Settings Objects as values.
   */
    public static HashMap<String, Object> readSettingsFile() throws FileNotFoundException {
        String fp = System.getProperty("user.dir") + File.separator + SETTINGS_FILE;
        try {
            // Get default settings
            String s = Files.readString(Path.of(fp), StandardCharsets.UTF_8);
            return settings_gson.fromJson(s, new TypeToken<HashMap<String, Object>>() {}.getType());
        } catch (IOException | AssertionError e) {
            System.out.println("Could not read settings file " + fp);
        }
        return new HashMap<String, Object>();  // empty HashMap on error
    }

    /**
     * Write the HashMap keyed by machine name as Json to {@link SETTINGS_FILE}.
     *
     * @param map HashMap keyed by machine name with Settings Objects as values.
     */
      public static void writeSettingsFile( HashMap<String, Object> map) {
          String fp = System.getProperty("user.dir") + "/" + SETTINGS_FILE;
          try {
              FileWriter fw = new FileWriter(fp);
              fw.write(settings_gson.toJson(map));
              fw.close();
          } catch (IOException e) {
              System.out.println("Could not write settings file.");
              e.printStackTrace();
          }
      }
  
    /**
     * Create a {@link Settings} class cls with values from the {@value SETTINGS_FILE}.
     * 
     * @param machineName Short name of an OpiMachine subclass which has a {@link Settings} inner class
     * @return A {@link Settings} object of type cls with values from {@value SETTINGS_FILE}.
      Class<? extends Settings> cls) {
     */
    public static Object fillSettings(String machineName) {
        HashMap<String, Object> settingsMap = null;
        try {
          settingsMap = OpiMachine.readSettingsFile();
        } catch (FileNotFoundException e) {
          System.out.println("Cannot open settings file");
          return null;
        }
        if (settingsMap.containsKey(machineName)) {
            String j = settings_gson.toJson(settingsMap.get(machineName));
            try {
                Class<?> cls = Class.forName("org.lei.opi.core."  + machineName + "$Settings");
                return settings_gson.fromJson(j, cls);
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
     * @param method An invokable method for one of the 5 OPI commands
     * @param parameters The @Parameter notations for that method (including all superclasses)
     */
    public record MethodData(Method method, HashSet<Parameter> parameters, HashSet<ReturnMsg> returnMsgs) {};
        /** The methods of the OpiMachine */
    public HashMap<String, MethodData> opiMethods;

    /**
     * Return all @Parameter or @ReturnMsg annotations for method `method` in the 
     * chain of classes from c, c.super(), c.super.super.... up to Object.
     * I think...elements in the returned set will not be unique by parameter.name.
     *    (So subclass @Parameters do not overwrite super class @Parameters).
     * 
     * @param c Class at which to begin looking for @Parameter annotations on method
     * @param method Method to look for in c and all superclasses of c
     * @param annotation Type of annotation to get (ParameterType or ReturnMsg)
     * @return HashSet of all @Parameter and @ReturnMsg annotations (unique by name)
     */
    public static HashSet<? extends Annotation> getAllAnnotations(Class<?> c, final Method method, final Class<? extends Annotation> annotation) {
        HashSet<Annotation> annotations = new HashSet<Annotation>();
        while (c != null) {
            for (Method m : c.getMethods()) {
                if (m.getName() == method.getName()) {
                    for (Annotation p : m.getAnnotationsByType(annotation))
                        annotations.add(p);
                }
            }
            c = c.getSuperclass();  // go up to parent
        }
        return(annotations);
    }   

        /** Enum class : enum values defined in the implementing class */
    public HashMap<String, List<String>> enums; 
  
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
      
        // Get OpiMachine and machine-dependent parameters through annotations
        opiMethods = new HashMap<String, MethodData>();
        for (Method method : methods) {
            HashSet<Parameter> ps = (HashSet<Parameter>)getAllAnnotations(this.getClass(), method, Parameter.class);
            HashSet<ReturnMsg> rms = (HashSet<ReturnMsg>)getAllAnnotations(this.getClass(), method, ReturnMsg.class);

            //HashSet<Parameter> params = getAllParameterAnnotations(this.getClass(), method, Parameter.getAnnotationsByType(Parameter.class));
            opiMethods.put(method.getName(), new MethodData(method, ps, rms));
        }

            // gather all the ENUMS used in Parameter annotations for all methods in this class
        enums = new HashMap<String, List<String>>();    
        HashSet<Class<?>> enumClasses = new HashSet<Class<?>>();
        for (String methodName : opiMethods.keySet())
            for (Parameter p : opiMethods.get(methodName).parameters())
                if (p.className().isEnum())
                    enumClasses.add(p.className());

        for (Class<?> e : enumClasses)
            enums.put(e.getName(), Stream.of(e.getEnumConstants()).map(Enum.class::cast).map(c -> c.name().toLowerCase()).toList());
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
            this.incoming = new DataInputStream(socket.getInputStream());
            this.outgoing = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
            return false;
        }
        return true;
    }
      
    /**
     * Send JSON message to socket
     * Strip any internal \n in the string as a \n terminates the message.
     *
     * @param message The message to send
     * @throws IOException If socket cannot be accessed
     * @since 0.2.0
     */
    void send(String message) throws IOException {
        String m = message.replace("\n", "") + "\n";
        outgoing.write(m);
        outgoing.flush();
    }
     
    /**
    * Receive Packet as a UTF-8 \n terminated JSON string from server
    * @return The message received in a Packet
    * @throws IOException If socket cannot be accessed
    * @since 0.2.0
    */
    Packet receive() throws IOException {
        String str = this.readline();
        return OpiListener.gson.fromJson(str, Packet.class);
    }

    /**
    * Receive a UTF-8 \n terminated string from server (Not the modified UTF-8 of DataInput/Output)
    * @return The message received in a Packet
    * @throws IOException If socket cannot be accessed
    * @since 3.0.0
    */
    String readline() throws IOException {
        byte bs[] = new byte[4];
        StringBuffer str = new StringBuffer(128);
        bs[0] = incoming.readByte();
        while (bs[0] != '\n') {
            if (bs[0] < 128)
                str.append((char)bs[0]);
            else if (bs[0] < 2048) {
                bs[1] = incoming.readByte();
                str.append(new String(Arrays.copyOfRange(bs, 0, 2), "UTF-8"));
            } else if (bs[0] < 65535) {
                bs[1] = incoming.readByte();
                bs[2] = incoming.readByte();
                str.append(new String(Arrays.copyOfRange(bs, 0, 3), "UTF-8"));
            } else {
                bs[1] = incoming.readByte();
                bs[2] = incoming.readByte();
                bs[3] = incoming.readByte();
                str.append(new String(Arrays.copyOfRange(bs, 0, 4), "UTF-8"));
            }
            bs[0] = incoming.readByte();
        }
        return(str.toString());
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
  
    /*
     * Return the GUI to the parent scene recorded when this 
     * instance became the scene controller for a new scene.
     * 
     * @param currentSource is a Node in current scene
     */
    protected void returnToParentScene(Node currentSource) {
        if (this.parentScene == null)
            return;

        Platform.runLater(new Runnable() {
            public void run() {
                final Stage stage = (Stage) currentSource.getScene().getWindow();

                stage.setScene(parentScene);
                stage.show();
            }
        });
    }

    /**
     * Map the 'command' to a function, check it has the right parameters, the call it.
     *
     * @param pairs A list of name:value pairs with at least the name "command"
     * 
     * @return Json object like OpiManger.ok() or OpiManager.error()
     * 
     * @since 0.0.1
     */
    public Packet processPairs(HashMap<String, Object> pairs) {
        /*
         * Processing consist of the following four steps:
         *    (1) Find the function which is the value of the JSON name "command"
         *    (2) Check that the params for the function are in the JSON (via the @Parameter Annotation)
         *    (3) Then execute corresponding method
         */

        // (1) find the command function
        String funcName = (String) pairs.get("command");
        MethodData methodData = opiMethods.get(funcName);
        if (methodData == null)
            return Packet.error(String.format(BAD_COMMAND, funcName, this.getClass()));

        // (2) Check and add optional-default params
        if (methodData.parameters != null) {
            Packet p = validateArgs(pairs, methodData.parameters(), funcName);
            if (!p.getError())
                pairs = (HashMap<String, Object>)gson.fromJson(p.getMsg(), new TypeToken<HashMap<String, Object>>() {}.getType());
            else    
                return(p);
        }

        // (3) execute method
        try {
            Packet result = methodData.parameters().size() == 0
              ? (Packet) methodData.method.invoke(this)
              : (Packet) methodData.method.invoke(this, pairs);
            return result;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return Packet.error(String.format(INVOCATION_FAILED, funcName, this.getClass()), e);
        }
    }
    
    /**
    * Build a java object from the default JSON string param
    * If the object is a list and it is shorter than `length`, add element 0 to the end until it is long enough.
    *
    * @param param Parameter for which to get the default
    * @param length The length of a List or ListList that should be created, replicating the single default if necessary.
    */
    public static Object buildDefault(Parameter param, int length) throws ClassNotFoundException {
        Type t = TypeToken.get(param.className()).getType();
        if (param.isList()) {
            t = TypeToken.getParameterized(ArrayList.class, param.className()).getType();
        } else if (param.isListList()) {
            t = TypeToken.getParameterized(ArrayList.class, ArrayList.class, param.className()).getType();
        }

        Object singleValue;
        if (param.className() == String.class && param.defaultValue().length() == 0) // allow empty string as a default
            singleValue = new String();
        else
            singleValue = OpiListener.gson.fromJson(param.defaultValue(), t);

        if (param.isList() || param.isListList()) {
            ArrayList<Object> a = (ArrayList<Object>)singleValue;
            while (a.size() < length) 
                a.add(a.get(0));
            return a;
        } else 
            return singleValue;
    }

    /** 
    * For each parameter p in parameters
    *  (1) If p is !optional and missing, report an error
    *  (2) If p is optional and absent, add the default value to pairs
    *  (3) Otherwise
    *      (3.1) Check p is of the right type (return error if not)
    *      (3.2) Check p is in range (return error if not)
    *
    * @param pairs Hashmap with parameter names as keys and values as Objects. 
    * @param params @Parameter annotations for the method that is using pairs
    * @param funcName Function name for errors
    *
    * @return Either a packet with error=false, msg=updated pairs object, or an error packet
    *
    * @since 0.2.0
    */
    public Packet validateArgs(HashMap<String, Object> pairs, HashSet<Parameter> parameters, String funcName) {
        for (Parameter param : parameters) {
                // mandatory parameter not received
            if (!pairs.containsKey(param.name()) && !param.optional())
              return Packet.error(String.format(MISSING_PARAMETER, param.name(), funcName, this.getClass()));
                // Optional parameter not here, add it in and go to next param
                // (Note stim.length gets turned into a double by fromJSON)
            if (!pairs.containsKey(param.name()) && param.optional()) {
                try {
                    Object defaultVal = OpiMachine.buildDefault(param, 
                        pairs.containsKey("stim.length") ?  (int)Math.round((Double)pairs.get("stim.length")) : 1);
                    pairs.put(param.name(), defaultVal);
                } catch (JsonSyntaxException e){
                    return Packet.error(String.format(BAD_DEFAULT, param.name(), funcName, this.getClass()));
                } catch (ClassNotFoundException e) {
                    return Packet.error(String.format(BAD_TYPE, param.name(), funcName, this.getClass()));
                }
                continue;
            }

                // Ok we've got a value; let's validate it
            Object valueObj = pairs.get(param.name());

                // things are lists when they should be check length of lists and list of lists
            if ((param.isListList() || param.isList()) && (!(valueObj instanceof ArrayList) || ((ArrayList<?>) valueObj).size() == 0))
                return Packet.error(String.format(NOT_LIST, param.name(), funcName, this.getClass()));
            if (!(param.isListList() || param.isList()) && (valueObj instanceof ArrayList))
                return Packet.error(String.format(YES_LIST, param.name(), funcName, this.getClass()));
            if (param.isListList() &&
                ((ArrayList<?>) valueObj).stream().anyMatch(val -> !(val instanceof ArrayList) || ((ArrayList<?>) val).size() == 0))
                    return Packet.error(String.format(NOT_LISTLIST, param.name(), funcName, this.getClass()));

                // For convenience, let's stick all types (list, listlist, not) into a simple list.
                // so we can just iterate over it to check each element.
            List<Object> pList;
            if (param.isListList())
                pList = (((ArrayList<?>) valueObj).stream().map(Object.class::cast)
                                                           .toList()).stream()
                                                                .map(vector -> (ArrayList<?>) vector)
                                                                .flatMap(List::stream)
                                                                .map(Object.class::cast)
                                                                .toList();
            else if (param.isList()) 
                pList = ((ArrayList<?>) valueObj).stream().map(Object.class::cast).toList();
            else
                pList = Arrays.asList(valueObj);

                // if param is an enum type, check all are valid
                // Note added default params are already Enums so just need to check supplied strings
            if (enums.containsKey(param.className().getName())) { // validate enums
                List<String> enumVals = enums.get(param.className().getName());
                Optional<Object> badOnes = pList.stream()
                                           .filter(p -> !(p.getClass().isEnum()))
                                           .filter(p -> !(p instanceof String) || !enumVals.stream().anyMatch(ss -> ss.contains(((String) p).toLowerCase())))
                                           .findAny();
                if (badOnes.isPresent())
                    return Packet.error(String.format(NOT_IN_ENUM, param.name(), param.className(), funcName, this.getClass()));
            } else if (param.className() == Double.class ||  param.className() == Integer.class) { // validate numbers
                try {
                    double minVal = Math.round(1e10 * param.min()) / 1e10; // avoid weird rounding problems
                    double maxVal = Math.round(1e10 * param.max()) / 1e10;
                    Optional<Double> result = pList.stream()
                        .map((Object o) -> (Double)(((Number.class.cast(o)).doubleValue())))
                        .filter((Double v) -> v.doubleValue() < minVal || v.doubleValue() > maxVal)
                        .findAny();
                    if (result.isPresent())
                        return Packet.error(String.format(OUT_OF_RANGE, param.name(), funcName, this.getClass(), minVal, maxVal, result.get()));
                } catch (ClassCastException e) {
                    if (param.className() == Double.class)
                        return Packet.error(String.format(NOT_A_DOUBLE, param.name(), funcName, this.getClass()));
                    else
                        return Packet.error(String.format(NOT_AN_INTEGER, param.name(), funcName, this.getClass()));
                }
            } else { // assume param is a String, then validate
              Optional<Object> result = pList.stream().filter(v -> !(v instanceof String)).findAny();
              if (result.isPresent())
                return Packet.error(String.format(NOT_A_STRING, param.name(), funcName, this.getClass()));
            }
          }
          return new Packet(pairs);
        }
  
    /**
     * opiInitialise: initialize OPI.
     * All machines will need "ip" and "port" (these are referenced in rgen, so perhaps don't change them)
     * as the address of the Monitor to which the R client will connect.
     * 
     * @param args A map of name:value pairs for Params
     * 
     * @return A Packet containing a JSON object
     * 
     * @since 0.0.1
     */
    @Parameter(name = "ip", desc = "IP Address of the OPI Monitor.", defaultValue = "localhost")
    @Parameter(name = "port", className = Integer.class, desc = "TCP port of the OPI Monitor.", min = 0, max = 65535, defaultValue = "50001")
    public abstract Packet initialize(HashMap<String, Object> args);
  
    /**
     * opiQuery: Query device
     * 
     * @return A Packet containing a JSON object
     *
     * @since 0.0.1
     */
    public abstract Packet query();
  
    /**
     * opiSetup: Change device background and overall settings
     * 
     * @param args pairs of argument name and value
     * 
     * @return A Packet containing a JSON object
     *
     * @since 0.0.1
     */
    public abstract Packet setup(HashMap<String, Object> args);
  
    /**
     * opiPresent: Present OPI stimulus in perimeter
     * 
     * @param args pairs of argument name and value
     * 
     * @return A Packet containing a JSON object
     *
     * @since 0.0.1
     */
    @ReturnMsg(name = "seen", className = Integer.class, desc = "'1' if seen, '0' if not.", min = 0, max = 1)
    @ReturnMsg(name = "time", className = Double.class, desc = "Response time from stimulus onset if button pressed (ms).", min = 0)
    public abstract Packet present(HashMap<String, Object> args);
  
    /**
     * opiClose: Send "close" to the real machine and then close the connection to the real machine.
     * 
     * @param args pairs of argument name and value
     *
     * @return A Packet containing a JSON object
     *
     * @since 0.0.1
     */
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
      return "{\"command\":\"" + command.toString() + "\"}";
    }

    public void printMethods() {
        System.out.println("opiMethods:");
        for(String k : opiMethods.keySet())
            System.out.println(k + opiMethods.get(k).parameters.stream().map((Parameter o)->o.name()).collect(Collectors.joining(", ")));
    }

// ----------- Java FX stuff common to all subclasses
// Assumes that Scene will be one of
//    mono_no_tracking.fxml
//    mono_yes_tracking.fxml
//    stereo_no_tracking.fxml
//    stereo_yes_tracking.fxml

    @FXML
    protected Button btnClose;

    @FXML
    protected Canvas canvasVF;            // mono
    protected VFCanvas canvasVFModel;

    @FXML
    protected Canvas canvasVFLeft;        // stereo
    protected VFCanvas canvasVFModelLeft;

    @FXML
    protected Canvas canvasVFRight;
    protected VFCanvas canvasVFModelRight;

    @FXML
    protected ImageView imageView;  // if tracking on, mono

    @FXML
    protected ImageView imageViewLeft;  // if tracking on stereo

    @FXML
    protected ImageView imageViewRight;  // if tracking on stereo

    @FXML
    protected Label labelChosen;

    @FXML
    protected TextArea textAreaCommands;

    protected record CanvasTriple(double x, double y, String label) { ; };

    /** Set of 4 functions indexed by "mono", "left", "right", "both" to 
    * to take a (x, y, label) and update the relevant canvas */
    protected HashMap<String, Consumer<CanvasTriple>> updateCanvas;
   
    protected void setVFCanvas(boolean mono, boolean tracking) {
        this.viewModeIsMono = mono;
        this.trackingOn = tracking;
        this.fxmlFileName = String.format("%s_%s.fxml", 
            viewModeIsMono ? "mono" : "stereo",
            tracking ? "yes_tracking" : "no_tracking");
    }

    /**
     * This should be called from the FXML initialize() method
     * in the subclass to set up updateCanvas and any other
     * things common to all JOVP GUIs.
     * TODO - add some tracking stuff
     */
    protected void setupJavaFX(String chosenLabel) {
        assert btnClose != null : String.format("fx:id=\"btnClose\" was not injected: check your FXML file %s", fxmlFileName);
        assert textAreaCommands != null : String.format("fx:id=\"textAreaCommands\" was not injected: check your FXML file %s", fxmlFileName);
        assert labelChosen != null : String.format("fx:id=\"labelChosen\" was not injected: check your FXML file %s", fxmlFileName);

        labelChosen.setText(String.format("Chosen OPI: %s", chosenLabel));

        textAreaCommands.setFont(new Font("Arial", 10));

        updateCanvas = new HashMap<String, Consumer<CanvasTriple>>();
        updateCanvas.put("mono", 
             (ct) -> {
                 canvasVFModel.updatePoint(ct.x(), ct.y(), ct.label().toString());
                 VFCanvas.draw(canvasVF, canvasVFModel);
             });
        updateCanvas.put("left", 
             (ct) -> {
                 canvasVFModelLeft.updatePoint(ct.x(), ct.y(), ct.label().toString());
                 VFCanvas.draw(canvasVFLeft, canvasVFModelLeft);
             });
        updateCanvas.put("right", 
             (ct) -> {
                canvasVFModelRight.updatePoint(ct.x(), ct.y(), ct.label().toString());
                 VFCanvas.draw(canvasVFRight, canvasVFModelRight);
             });
        updateCanvas.put("both", 
             (ct) -> {
                 canvasVFModelLeft.updatePoint(ct.x(), ct.y(), ct.label().toString());
                 VFCanvas.draw(canvasVFLeft, canvasVFModelLeft);
                 canvasVFModelRight.updatePoint(ct.x(), ct.y(), ct.label().toString());
                 VFCanvas.draw(canvasVFRight, canvasVFModelRight);
             });

             // put up a blank canvas
        if (canvasVF != null) {
            canvasVFModel = new VFCanvas();
            updateCanvas.get("mono").accept(new CanvasTriple(0, 0, ""));
        } else {
            canvasVFModelLeft = new VFCanvas();
            canvasVFModelRight = new VFCanvas();
            updateCanvas.get("both").accept(new CanvasTriple(0, 0, ""));
        }
    }

    /*
     ** Write s to GUI textAreaCommands and to System.out
     ** @param s string to print/write to GUI
     */
    public void output(String s) {
        System.out.println(s);
        Platform.runLater(()-> {
            if (textAreaCommands != null)
                textAreaCommands.appendText(s);
        });
    }
            
    /**
     * Update both the textArea with the present details
     * and the canvas with stimulus value and location.
     *
     * @param args The @Param pairs. Should contain keys x, y, lum, eye.
     */
    protected void updateGUIOnPresent(HashMap<String, Object> args) {
        StringBuffer sb = new StringBuffer();
        sb.append("Present:\n");
        for (String k : args.keySet())
            sb.append(String.format("\t%s = %s\n", k, args.get(k).toString()));
        output(sb.toString());

        if (this.parentScene == null)
            return;

        Platform.runLater(() -> {
            if (!args.containsKey("x") || !args.containsKey("y") || !args.containsKey("lum")) {
                System.out.println("updateGUIOnPresent cannot find one or more of 'x', 'y', or 'lum'. Not showing anything");
                return;
            }

            List<Double> xList, yList, lList;
            List<String> eList;

            if (args.get("x") instanceof ArrayList) {
                xList = (List<Double>)args.get("x");
                yList = (List<Double>)args.get("y");
                lList = (List<Double>)args.get("lum");
            } else {
                xList = Arrays.asList((Double)args.get("x"));
                yList = Arrays.asList((Double)args.get("y"));
                lList = Arrays.asList((Double)args.get("lum"));
            }
            if (args.containsKey("eye"))
                eList = (List<String>)args.get("eye");
            else {
                eList = new ArrayList<String>();
                for (int i = 0 ; i < xList.size() ; i++)
                    eList.add("mono");
            }

            try {
                for (int i = 0 ; i < xList.size(); i++) {
                    CanvasTriple ct = new CanvasTriple(xList.get(i), yList.get(i), Long.toString(Math.round(lList.get(i))));
                    if (this.viewModeIsMono)
                        updateCanvas.get("mono").accept(ct);
                    else
                        updateCanvas.get(eList.get(i).toLowerCase()).accept(ct);
                }
            } catch (Exception e) { 
                System.out.println("Display present() canvas troubles");
                e.printStackTrace();
            }
        });
    }

    @FXML
    void actionBtnClose(ActionEvent event) {
        returnToParentScene((Node)event.getSource());
    }

    @FXML
    abstract void initialize();
}