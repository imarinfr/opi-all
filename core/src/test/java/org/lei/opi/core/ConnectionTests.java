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
    CSListener opi = new CSListener(50001, new OpiManager());
    System.out.println("[getIDAndPort] " + opi);
    opi.close();
  }
}
