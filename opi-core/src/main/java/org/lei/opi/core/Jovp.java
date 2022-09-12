package org.lei.opi.core;

import java.util.HashMap;

/**
 * JOVP client
 *
 * @since 0.0.1
 */
public class Jovp extends OpiMachine {
  public Jovp() { super(); }

  /* (non-Javadoc)
  * @see org.lei.opi.core.OpiMachine#query()
  */
  public String query(HashMap<String, String> args) {
    // any generic JOVP query stuff here
    return "null";
  }

  public String initialize(HashMap<String, String> args) {
    // any generic JOVP initialise stuff here
    return "null";
  }

  /* (non-Javadoc)
   * @see org.lei.opi.core.OpiMachine#setup()
   */
  public String setup(HashMap<String, String> args) {
    // any generic JOVP setup stuff here
    return "null";
  }

  /* (non-Javadoc)
   * @see org.lei.opi.core.OpiMachine#present()
   */
  public String present(HashMap<String, String> args) {
    // any generic JOVP present stuff here
    return "null";
  }

  /* (non-Javadoc)
   * @see org.lei.opi.core.OpiMachine#close()
   */
  public String close(HashMap<String, String> args) {
    // any generic JOVP close stuff here
    return "null";
  }
}