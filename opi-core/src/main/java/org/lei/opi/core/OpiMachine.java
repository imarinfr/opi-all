package org.lei.opi.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * The OPI standard for communication with perimeters
 *
 * @since 0.0.1
 */
class OpiMachine {

  /** Constant for exception messages: Syntax error for machine implementation */
  static final String BAD_MACHINE = "Syntax for name 'machine' in JSON choose object is bad";
  /** Constant for exception messages: Unknown machine */
  static final String UNKNOWN_MACHINE = "machine:%s in JSON choose object is unknown.";
  /** Constant for exception messages: No commmand field */
  static final String OK = "\"result\": \"ok\"\n";
  /** Constant for exception messages: Missing choose command */
  static final String ERROR = "\"result\": \"error\"\n";

    /** IP Address of perimeter {@value IP} */
    public static final String IP = "{type: String, default = localhost}";
    /** TCP Port of perimeter {@value PORT} */
    public static final String PORT = "{type: Integer, min: 1, max: Inf}";

  /**
   * Choose a machine
   *
   * @param machine a class name that is a subclass of OpiMachine
   *
   * @return new OpiMachine
   *
   * @throws ClassNotFoundException 
   *
   * @since 0.0.1
   */
  static OpiMachine choose(String machine) throws ClassNotFoundException {
    try {
      return (OpiMachine) Class.forName(OpiMachine.class.getPackage().getName() + "." + machine)
        .getDeclaredConstructor()
        .newInstance();
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
          throw new ClassNotFoundException(String.format(UNKNOWN_MACHINE, machine));
    }
  }

  /**
   * Query the settings and capabilities of the perimetry device
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON-structured string with query information
   *
   * @since 0.0.1
   */
  public String query(Map<String, Object> args) {
    return "QUERY FEEDBACK";
  };

  /**
   * Initialize the OPI on the corresponding machine
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON-structured string with initialization feedback
   *
   * @since 0.0.1
   */
  public String initialize(Map<String, Object> args) {
    return "INITIALIZED FEEDBACK";
  };

  /**
   * Change device background and overall settings
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON-structured string with setup feedback
   *
   * @since 0.0.1
   */
  public String setup(Map<String, Object> args) {
    return "SETUP FEEDBACK";
  };

  /**
   * Present OPI stimulus in perimeter
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON-structured string with presentation feedback
   *
   * @since 0.0.1
   */
  public String present(Map<String, Object> args) {
    return "PRESENT FEEDBACK";
  };

  /**
   * Close OPI connection
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON-structured string with closing feedback
   *
   * @since 0.0.1
   */
  public String close(Map<String, Object> args) {
    return "CLOSE FEEDBACK";
  };

}
