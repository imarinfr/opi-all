package org.lei.opi.core;

import org.junit.jupiter.api.Test;

/**
 *
 * Unitary tests for socket connections
 *
 * @since 0.0.1
 */
public class ConnectionTests {

  /**
   *
   * Open OPI, get local ID and port, and cleanup after use
   *
   * @since 0.0.1
   */
  @Test
  public void getIDAndPort() {
    OpiManager opi = new OpiManager(50001);
    System.out.println("[getIDAndPort] " + opi);
    opi.close();
  }

  /**
   *
   * Open and close connection to local socket
   *
   * @since 0.0.1
   */
  @Test
  public void changeLocalPort() {
    OpiManager opi = new OpiManager(50001);
    System.out.println("[changeLocalPort] Address was at " + opi);
    opi.setLocalPort(50002);
    System.out.println("[changeLocalPort] Address is at " + opi);
    opi.close();
  }

}
