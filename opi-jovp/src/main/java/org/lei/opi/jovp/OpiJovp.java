package org.lei.opi.jovp;

import java.util.HashMap;

import org.lei.opi.core.MessageProcessor;
import org.lei.opi.core.MessageProcessor.Packet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * OPI JOVP manager
 *
 * @since 0.0.1
 */
public class OpiJovp extends MessageProcessor {

  /** Constant for exception messages: {@value BAD_JSON} */
  private static final String BAD_JSON = "String is not a valid Json object.";
  /** name:value pair in JSON output if there is an error */
  private static String ERROR_YES  = "\"error\" : 1";
  /** name:value pair in JSON output if there is not an error */
  private static String ERROR_NO   = "\"error\" : 0";

  /** driver connection */
  private final Driver driver;

  /**
   * Start the OPI JOVP comms and driver
   *
   * @since 0.0.1
   */
  public OpiJovp() {
    driver = new Driver();
  }

   /**
   * Create an OK message in JSON format with nothing else
   * 
   * @return JSON-formatted ok message
   * 
   * @since 0.1.0
   */
  public static MessageProcessor.Packet ok() {
    return new MessageProcessor.Packet(String.format("{%s}", ERROR_NO));
  }

  /**
   * Create an OK message in JSON format with attached results
   * 
   * @param feedback Json object that is feedback from the OPI command or null
   * 
   * @return JSON-formatted ok message
   * 
   * @since 0.1.0
   */
  public static MessageProcessor.Packet ok(String feedback) {
    return new MessageProcessor.Packet(String.format("{%s, \"feedback\": %s}", ERROR_NO, feedback));
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
      String.format("{%s, \"description\": \"%s\"}", ERROR_YES, description));
  }

  /**
   * Create an error message in JSON format to send to R OPI
   * 
   * @param description String error description (no quotes) to add to Json return name 'description'
   * @param e An exception to print to stderr and add to Json return object name 'exception'
   * 
   * @return JSON-formatted error message
   * 
   * @since 0.1.0
   */
  public static MessageProcessor.Packet error(String description, Exception e) {
    System.err.println(e);
    return new MessageProcessor.Packet(
      String.format("{%s, \"description\": \"%s\", \"exception\": \"%s\"}", ERROR_YES, description, e.toString()));
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
    Gson gson = new Gson();
    HashMap<String, String> pairs;
    try {
      pairs = gson.fromJson(jsonStr, new TypeToken<HashMap<String, String>>() {}.getType());
    } catch (JsonSyntaxException e) {
      return error(BAD_JSON);
    }
    driver.start();
    driver.cleanup();
    //do things with driver
    return null;
  }

  // TODO process messages: query, initialize, setup, present, close

}