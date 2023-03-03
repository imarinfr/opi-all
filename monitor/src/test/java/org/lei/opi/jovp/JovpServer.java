package org.lei.opi.jovp;

import java.io.IOException;

import org.lei.opi.core.OpiListener;

/**
 * The Jovp server
 *
 * @since 0.0.1
 */
class JovpServer {

  /** The OpiJovp */
  private static OpiJovp opiJovp;

  /**
   * Initialize the Jovp server
   *
   * @param machine The server's machine with default settings
   * @param port Server's port
   *
   * @throws IOException If server cannot be opened
   *
   * @since 0.0.1
   */
  JovpServer(Configuration.Machine machine, int port) throws IOException {
    opiJovp = new OpiJovp(port);
    System.out.println("Machine address is " + opiJovp.getIP() + ":" + opiJovp.getPort());
           
    opiJovp.startPsychoEngine(); // TODO I suspect that this will take over and no messages will be processed, but let's see
           
    while (true) Thread.onSpinWait();  // not sure why, but there you go.
  }

  /**
   * Get local IP address
   *
   * @return the local IP address
   *
   * @since 0.0.1
   */
  String getIP() {
    return opiJovp.getIP();
  }

  /**
   * Get local port
   *
   * @return the local port
   *
   * @since 0.0.1
   */
  int getPort() {
    return opiJovp.getPort();
  }

  /**
   * Start the PsychoEngine in the JOVP server
   *
   * @since 0.0.1
   */
  void start() {
    opiJovp.start();
  }

  /**
   * Finish the JOVP run and close socket
   *
   * @throws IOException If server cannot be closed
   *
   * @since 0.0.1
   */
  void close() throws IOException {
    opiJovp.finish();
    opiJovp.closeListener();
  }

}