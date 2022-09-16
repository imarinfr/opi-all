package org.lei.opi.core;

import java.util.HashMap;

/**
 * JOVP client
 *
 * @since 0.0.1
 */
public class Jovp extends OpiMachine {

  public enum Eye {LEFT, RIGHT, BOTH};
  public enum Color {WHITE, RED, GREEN, BLUE};
  public enum ShapeType {CIRCLE, SQUARE, TRIANGLE, ANNULUS, CROSS, MALTESE}; // for stimulus presentation and fixation target

  /**
   * JOVP constructor
   *
   * @since 0.0.1
   */
  public Jovp() { super(); }

  /**
   * opiQuery: Query device
   * 
   * @return settings and state machine state
   *
   * @since 0.0.1
   */
  public MessageProcessor.Packet query() {
    // any generic JOVP query stuff here
    return new MessageProcessor.Packet("null");
  }

  /**
   * opiInitialise: initialize OPI
   * 
   * @param args A map of name:value pairs for Params
   * 
   * @return A JSON object with return messages
   * 
   * @since 0.0.1
   */
  public MessageProcessor.Packet initialize(HashMap<String, Object> args) {
    // any generic JOVP initialise stuff here
    return new MessageProcessor.Packet("null");
  }

  /**
   * opiSetup: Change device background and overall settings
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  public MessageProcessor.Packet setup(HashMap<String, Object> args) {
    // any generic JOVP setup stuff here
    return new MessageProcessor.Packet("null");
  }

  /**
   * opiPresent: Present OPI stimulus in perimeter
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  public MessageProcessor.Packet present(HashMap<String, Object> args) {
    // any generic JOVP present stuff here
    return new MessageProcessor.Packet("null");
  }

  /**
   * opiClose: Close OPI connection
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  public MessageProcessor.Packet close() {
    // any generic JOVP close stuff here
    return new MessageProcessor.Packet("null");
  }

}