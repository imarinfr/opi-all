package org.lei.opi.jovp;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.lei.opi.jovp.Settings.Machine;

/**
 *
 * Integrated tests for connection in series from client to OPI JOVP
 *
 * @since 0.0.1
 */
public class MonitorToJovpTests {

  /** JOVP server port */
  private static final int JOVP_PORT = 51234;
  /** JOVP monitor port */
  private static final int MONITOR_PORT = 50001;

  /** The JOVP server */
  private JovpServer server;
  /** The OPI monitor */
  private Core monitor;

  /**
   * Monitor controlling Display on monoscopic view
   *
   * @since 0.0.1
   */
  @Test
  public void monitorDisplayMono() {
    setupConnections(Machine.DISPLAY_MONO);
    server.run();
    closeConnections();
  }

  /**
   * Monitor controlling Display on stereoscopic view
   *
   * @since 0.0.1
   */
  @Test
  public void monitorDisplayStereo() {
    setupConnections(Machine.DISPLAY_STEREO);
    server.run();
    closeConnections();
  }

  /** setup connections */
  private void setupConnections(Machine machine) {
    try {
      server = new JovpServer(machine, JOVP_PORT); // first setup JOVP server
      monitor = new Core(MONITOR_PORT); // then setup monitor
      System.out.println("Listening...");
      System.out.println("Server: " + server.getIP() + ":" + server.getPort());
      System.out.println("Monitor: " + monitor.getIP() + ":" + monitor.getPort());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** close connections */
  private void closeConnections() {
    try {
      monitor.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
