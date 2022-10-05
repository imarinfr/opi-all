package org.lei.opi.core;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 *
 * Unitary tests for socket connections
 *
 * @since 0.0.1
 */
public class MonitorTests {

  /** Monitor port */
  private static final int PORT = 51234;

  /** Monitor */
  CSListener monitor;
  /** RClient: initiator */
  RClient r;

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

  /**
   *
   * Open monitor, get local ID and port, and close it
   *
   * @since 0.0.1
   */
  @Test
  public void machineSettings() {
    try {
      monitor = new CSListener(PORT, new OpiManager());
      r = new RClient(monitor.getIP(), monitor.getPort());
      sendAndReceive("Jovp"); // Settings Jovp
      sendAndReceive("O900"); // Settings O900
      sendAndReceive("Compass"); // Settings for Compass
      sendAndReceive("Maia"); // Settings for Maia
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    monitor.close();
  }

  /** R sends to and receives from monitor */
  private void sendAndReceive(String machine) throws IOException {
    System.out.println("Settings for " + machine + ":\n");
    r.send(buildChooseJson(machine));
    while (r.empty()) Thread.onSpinWait();
    System.out.println("R RECEIVES\n" + r.receive());
  }

  /** Builds a json to choose a machine that will be sent to monitor */
  private String buildChooseJson(String machine) {
    return new StringBuilder("{\n  \"command\" : \"CHOOSE\",\n  \"machine\" : \"")
      .append(machine).append("\"\n}").toString();
  }

}
