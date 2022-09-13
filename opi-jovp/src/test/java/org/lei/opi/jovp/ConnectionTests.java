package org.lei.opi.jovp;

import org.junit.jupiter.api.Test;

import org.lei.opi.core.*;;

/**
 *
 * Unitary tests for commumication with IMO driver
 *
 * @since 0.0.1
 */
public class ConnectionTests {

  /**
   *
   * Create listener for IMO driver, get local ID and port, and cleanup after use
   *
   * @since 0.0.1
   */
  @Test
  public void getIDAndPort() {
    CSListener driver = new CSListener(50001, new OpiJovp());
    System.out.println("[getIDAndPort] " + driver);
    driver.close();
  }

  /**
   *
   * Open connection, change local port, and close connection to IMO driver
   *
   * @since 0.0.1
   */
  @Test
  public void changeLocalPort() {
    CSListener driver = new CSListener(50001, new OpiJovp());
    System.out.println("[changeLocalPort] Address was at " + driver);
    driver.close();
    driver = new CSListener(50001, new OpiJovp());
    System.out.println("[changeLocalPort] Address is at " + driver);
    driver.close();
  }

}
