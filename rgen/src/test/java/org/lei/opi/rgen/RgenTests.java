package org.lei.opi.rgen;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.Jovp;

/**
 *
 * Unitary tests for socket connections
 *
 * @since 0.0.1
 */
public class RgenTests {
  
  /**
   *
   * DUMMY description
   *
   * @since 0.0.1
   */
  @Test
  public void mainTest() {
    Main.makeR(new Jovp(null), System.out);
  }

  @Test
  public void opiFunctionTest1() {
    OpiFunction f = new OpiFunction(new Jovp(null), "opiInitialise", "initialize", "", "list(err = %s)", true);
    f.generateR(System.out);
  }

  @Test
  public void opiFunctionTest2() {
    OpiFunction f = new OpiFunction(new Jovp(null), "opiInitializzze", "initialize", "", "list(err = %s)", true);
    f.generateR(System.out);
  }

}
