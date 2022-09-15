package org.lei.opi.rgen;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.ImoVifa;

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
    Main.makeR(new ImoVifa(), System.out);
  }

  @Test
  public void opiFunctionTest1() {
    OpiFunction f = new OpiFunction("opiInitialise_for_ImoVifa", "initialize", new String[] {}, "list(err = %s)", false);
    f.generateR(new org.lei.opi.core.ImoVifa(), System.out);
  }

  @Test
  public void opiFunctionTest2() {
    OpiFunction f = new OpiFunction("opiInitialise_for_ImoVifa", "initializzzzze", new String[] {}, "list(err = %s)", false);
    f.generateR(new org.lei.opi.core.ImoVifa(), System.out);
  }

}
