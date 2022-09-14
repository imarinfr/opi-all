package org.lei.opi.rgen;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.Imo;

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
    Main.makeR(new Imo(), System.out);
  }

}
