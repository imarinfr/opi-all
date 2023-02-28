package org.lei.opi.jovp;

import java.io.IOException;

import org.lei.opi.core.Display;

/**
 * The Jovp server
 *
 * @since 0.0.1
 */
class JovpServer {

  /** The OpiJovp */
  private static OpiJovp opiJovp;
  /** The listener for OpiJovp */
  private static Display listener;

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
    opiJovp = new OpiJovp();
    opiJovp.connect(50005, Display.obtainPublicAddress());  // start on localhost
    System.out.println("Machine address is " + opiJovp.getIP() + ":" + opiJovp.getPort());
           
    opiJovp.startPsychoEngine(); // TODO I suspect that this will take over and no messages will be processed, but let's see
           
    listener = new Display(null);

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
    return listener.getIP();
  }

  /**
   * Get local port
   *
   * @return the local port
   *
   * @since 0.0.1
   */
  int getPort() {
    return listener.getPort();
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
    listener.closeListener();
  }

}