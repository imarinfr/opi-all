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
  Listener monitor;
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
    Listener monitor = new OpiClient(50001);
    System.out.println("[getIDAndPort] " + monitor);
    monitor.closeListener();
  }

  /**
   *
   * Open connection, change local port, and close connection to Display driver
   *
   * @since 0.0.1
   */
  @Test
  public void changeLocalPort() {
    Listener monitor = new OpiClient(50001);
    System.out.println("[changeLocalPort] Address was at " + monitor);
    monitor.closeListener();
    monitor = new OpiClient(50008);
    System.out.println("[changeLocalPort] Address is at " + monitor);
    monitor.closeListener();
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
      monitor = new OpiClient(PORT);
      r = new RClient(monitor.getIP(), monitor.getPort());
      sendAndReceive("Jovp"); // Settings Jovp
      sendAndReceive("O900"); // Settings O900
      sendAndReceive("Compass"); // Settings for Compass
      sendAndReceive("Maia"); // Settings for Maia
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    monitor.closeListener();
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
