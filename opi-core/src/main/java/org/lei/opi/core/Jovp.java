package org.lei.opi.core;

import java.util.HashMap;

/**
 * JOVP client
 *
 * @since 0.0.1
 */
public class Jovp extends OpiMachine {
  public enum Color { WHITE, RED, GREEN, BLUE };

  public Jovp() { super(); }

  /* (non-Javadoc)
  * @see org.lei.opi.core.OpiMachine#query()
  */
  public MessageProcessor.Packet query(HashMap<String, String> args) {
    // any generic JOVP query stuff here
    return new MessageProcessor.Packet("null");
  }

  public MessageProcessor.Packet initialize(HashMap<String, String> args) {
    // any generic JOVP initialise stuff here
    return new MessageProcessor.Packet("null");
  }

  /* (non-Javadoc)
   * @see org.lei.opi.core.OpiMachine#setup()
   */
  public MessageProcessor.Packet setup(HashMap<String, String> args) {
    // any generic JOVP setup stuff here
    return new MessageProcessor.Packet("null");
  }

  /* (non-Javadoc)
   * @see org.lei.opi.core.OpiMachine#present()
   */
  public MessageProcessor.Packet present(HashMap<String, String> args) {
    // any generic JOVP present stuff here
    return new MessageProcessor.Packet("null");
  }

  /* (non-Javadoc)
   * @see org.lei.opi.core.OpiMachine#close()
   */
  public MessageProcessor.Packet close(HashMap<String, String> args) {
    // any generic JOVP close stuff here
    return new MessageProcessor.Packet("null");
  }
}