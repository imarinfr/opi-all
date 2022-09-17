package org.lei.opi.jovp;

import java.io.IOException;

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
    Settings settings;
    try {
      settings = Settings.defaultSettings(Settings.Machine.IMOVIFA);
      CSListener driver = new CSListener(50001, new OpiJovp(settings));
      System.out.println("[getIDAndPort] " + driver);
      driver.close();
    } catch (IllegalArgumentException | IOException e) {
      e.printStackTrace();
    }
  }

  /**
   *
   * Open connection, change local port, and close connection to IMO driver
   *
   * @since 0.0.1
   */
  @Test
  public void changeLocalPort() {
    Settings settings;
    try {
      settings = Settings.defaultSettings(Settings.Machine.IMOVIFA);
      CSListener driver = new CSListener(50001, new OpiJovp(settings));
      System.out.println("[changeLocalPort] Address was at " + driver);
      driver.close();
      driver = new CSListener(50009, new OpiJovp(settings));
      System.out.println("[changeLocalPort] Address is at " + driver);
      driver.close();
    } catch (IllegalArgumentException | IOException e) {
      e.printStackTrace();
    }
  }

}
