package org.lei.opi.jovp;

import java.io.IOException;
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
    try {
      CSListener server = new CSListener(50001, new OpiJovp(Settings.Machine.DISPLAY_MONO));
      System.out.println("[getIDAndPort] " + server);
      server.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   *
   * Open connection, change local port, and close connection to Display driver
   *
   * @since 0.0.1
   */
  @Test
  public void changeLocalPort() {
    try {
      CSListener monitor = new CSListener(50001, new OpiJovp(Settings.Machine.DISPLAY_MONO));
      System.out.println("[changeLocalPort] Address was at " + monitor);
      monitor.close();
      monitor = new CSListener(50008, new OpiJovp(Settings.Machine.DISPLAY_MONO));
      System.out.println("[changeLocalPort] Address is at " + monitor);
      monitor.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   *
   * Load default configuration files
   *
   * @since 0.0.1
   */
  @Test
  public void defaultConfigurations() {
    Settings settings;
    try {
      settings = Settings.defaultSettings(Settings.Machine.IMOVIFA);
      System.out.println(settings);
      settings = Settings.defaultSettings(Settings.Machine.PICOVR);
      System.out.println(settings);
      settings = Settings.defaultSettings(Settings.Machine.PHONEHMD);
      System.out.println(settings);
      settings = Settings.defaultSettings(Settings.Machine.DISPLAY_MONO);
      System.out.println(settings);
      settings = Settings.defaultSettings(Settings.Machine.DISPLAY_STEREO);
      System.out.println(settings);
    } catch (IllegalArgumentException | IOException e) {
      throw new RuntimeException(e);
    }
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
