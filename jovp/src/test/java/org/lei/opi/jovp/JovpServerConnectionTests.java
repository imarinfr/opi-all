package org.lei.opi.jovp;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.CSListener;

import es.optocom.jovp.Controller;

/**
 *
 * Unitary tests for commumication with IMO driver
 *
 * @since 0.0.1
 */
public class JovpServerConnectionTests {

  /**
   *
   * Open monitor, get local ID and port, and close it
   *
   * @since 0.0.1
   */
  @Test
  public void getIDAndPort() {
    CSListener server = new CSListener(50001, new OpiJovp());
    System.out.println("[getIDAndPort] " + server);
     server.close();
  }

  /**
   *
   * Open connection, change local port, and close connection to Display driver
   *
   * @since 0.0.1
   */
  @Test
  public void changeLocalPort() {
    CSListener monitor = new CSListener(50001, new OpiJovp());
    System.out.println("[changeLocalPort] Address was at " + monitor);
    monitor.close();
    monitor = new CSListener(50008, new OpiJovp());
    System.out.println("[changeLocalPort] Address is at " + monitor);
    monitor.close();
  }

  /**
   *
   * List of suitable USB serial controllers attached to the computer
   *
   * @since 0.0.1
   */
  @Test
  public void listUsbControllers() {
    System.out.println(Arrays.toString(Controller.getSuitableControllers()));
  }

}
