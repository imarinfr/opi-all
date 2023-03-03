package org.lei.opi.jovp;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.Jovp;

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
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[getIDAndPort] " + server);
  }

  /**
   *
   * Open connection, change local port, and close connection to Display driver
   *
   * @since 0.0.1
   */
  @Test
  public void changeLocalPort() {
    OpiJovp server = new OpiJovp(50003);
    System.out.println("[changeLocalPort] Address was at " + server);
    server = new OpiJovp(50008);
    System.out.println("[changeLocalPort] Address is at " + server);
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
