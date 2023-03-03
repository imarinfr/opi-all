package org.lei.opi.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.net.Socket;

import org.apache.commons.io.IOUtils;

/**
   * The R client
   *
   * @since 0.0.1
   */
  public class RClient {
    private Socket socket;
    private BufferedReader incoming;
    private BufferedWriter outgoing;

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
      socket = new Socket(ip, port);

      incoming = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
      outgoing = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
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
     * @since 0.2.0
     */
    void send(String message) throws IOException {
      outgoing.write(message);
    }
   
    /**
     * Receive \n terminated JSON message from monitor
     *
     * @return The message received
     *
     * @throws IOException If socket cannot be accessed
     *
     * @since 0.2.0
     */
    String receive() throws IOException {
      return incoming.readLine();
    }
   
    /**
     * Close socket
     *
     * @throws IOException If client cannot be closed
     *
     * @since 0.0.1
     */
    void close() throws IOException {
      incoming.close();
      outgoing.close();
      socket.close();
    }
}  