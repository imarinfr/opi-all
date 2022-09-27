package org.lei.opi.jovp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.lei.opi.core.CSWriter;

/**
   * The R client
   *
   * @since 0.0.1
   */
  public class RClient {

  /** CSWriter to act as R client */
  private static CSWriter r;

  /**
   * Initialize the R client
   *
   * @param ip The IP of the monitor
   * @param port The port of the Monitor
   *
   * @throws IOException If socket cannot be opened
   *
   * @since 0.0.1
   */
  RClient(String ip, int port) throws IOException {
    r = new CSWriter(ip, port);
  }

  /**
   * Load file with JSON message to send
   *
   * @param file The JSON file to load
   *
   * @throws IOException If file cannot be loaded
   *
   * @since 0.0.1
   */
  static String loadMessage(String file) throws IOException {
    InputStream inputStream = RClient.class.getResourceAsStream(file);
    return IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));
  }

  /**
   * Send JSON message to monitor
   *
   * @param message The message to send
   *
   * @throws IOException If socket cannot be accessed
   *
   * @since 0.0.1
   */
  void send(String message) throws IOException {
    r.send(message);
  }

  /**
   * Check if something has been received from monitor
   *
   * @return Whether socket is empty
   *
   * @throws IOException If socket cannot be accessed
   *
   * @since 0.0.1
   */
  boolean empty() throws IOException {
    return r.empty();
  }

  /**
   * Receive JSON message from monitor
   *
   * @return The message received
   *
   * @throws IOException If socket cannot be accessed
   *
   * @since 0.0.1
   */
  String receive() throws IOException {
    return r.receive();
  }

  /**
   * Close socket
   *
   * @throws IOException If server cannot be closed
   *
   * @since 0.0.1
   */
  void close() throws IOException {
    r.close();
  }

}
