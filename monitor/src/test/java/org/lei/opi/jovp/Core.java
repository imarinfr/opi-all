package org.lei.opi.jovp;

import java.io.IOException;

import org.lei.opi.core.OpiListener;

/**
   * The OPI monitor
   *
   * @since 0.0.1
   */
  class Core {

  /** listener for OpiJovp */
  private static OpiListener monitor;

  /**
   * Initialize the monitor
   *
   * @param port The monitor's local port
   *
   * @throws IOException If socket cannot be opened
   *
   * @since 0.0.1
   */
  Core(int port) throws IOException {
    monitor = new OpiListener(port, null);
  }

  /**
   * Get local IP address
   *
   * @return the local IP address
   *
   * @since 0.0.1
   */
  String getIP() {
    return monitor.getIP();
  }

  /**
   * Get local port
   *
   * @return the local port
   *
   * @since 0.0.1
   */
  int getPort() {
    return monitor.getPort();
  }

  /**
   * Close sockets
   *
   * @throws IOException If servers cannot be closed
   *
   * @since 0.0.1
   */
  void close() throws IOException {
    monitor.closeListener();
  }

}