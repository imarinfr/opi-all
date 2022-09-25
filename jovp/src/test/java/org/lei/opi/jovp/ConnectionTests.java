package org.lei.opi.jovp;

import org.junit.jupiter.api.Test;

/**
 *
 * Unitary tests for commumication with IMO driver
 *
 * @since 0.0.1
 */
public class ConnectionTests {

  /**
   *
   * Create listener for Display driver, get local ID and port, and cleanup after use
   *
   * @since 0.0.1
   */
  @Test
  public void getIDAndPort() {
    OpiJovp opiJovp = new OpiJovp(Settings.Machine.DISPLAY_MONO);
    opiJovp.open(50001);
    System.out.println("[getIDAndPort] " + opiJovp.listener);
    opiJovp.close();
  }

  /**
   *
   * Open connection, change local port, and close connection to Display driver
   *
   * @since 0.0.1
   */
  @Test
  public void changeLocalPort() {
    OpiJovp opiJovp = new OpiJovp(Settings.Machine.DISPLAY_MONO);
    opiJovp.open(50001);
    System.out.println("[changeLocalPort] Address was at " + opiJovp.listener);
    opiJovp.close();
    opiJovp.open(50001);
    System.out.println("[changeLocalPort] Address is at " + opiJovp.listener);
    opiJovp.close();
  }

}
