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

  /**
   *
   * Open connection, change local port, and close connection to Display driver
   *
   * @since 0.0.1
   */
  @Test
  public void changeLocalPort() {
    CSListener monitor = new CSListener(50001, new OpiManager());
    System.out.println("[changeLocalPort] Address was at " + monitor);
    monitor.close();
    monitor = new CSListener(50008, new OpiManager());
    System.out.println("[changeLocalPort] Address is at " + monitor);
    monitor.close();
  }

}
