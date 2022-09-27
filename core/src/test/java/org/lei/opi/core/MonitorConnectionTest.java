package org.lei.opi.core;

import org.junit.jupiter.api.Test;

/**
 *
 * Unitary tests for socket connections
 *
 * @since 0.0.1
 */
public class MonitorConnectionTest {

  /**
   *
   * Open monitor, get local ID and port, and close it
   *
   * @since 0.0.1
   */
  @Test
  public void getIDAndPort() {
    CSListener monitor = new CSListener(50001, new OpiManager());
    System.out.println("[getIDAndPort] " + monitor);
    monitor.close();
  }
}
