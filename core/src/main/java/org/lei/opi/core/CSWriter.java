package org.lei.opi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * Sender and Receiver on socket with Writer thread.
 *
 * @since 0.0.1
 */
public class CSWriter {

  /** {@value CANNOT_SEND} */
  private static final String CANNOT_SEND = "Cannot send message in CSWriter";
  /** {@value CANNOT_CHECK_EMPTY} */
  private static final String CANNOT_CHECK_EMPTY = "Cannot check if socket is empty";
  /** {@value CANNOT_RECEIVE} */
  private static final String CANNOT_RECEIVE = "Cannot receive message in CSWriter";

  /** Server onnection address */
  private InetAddress address;
  /** Server connection port */
  private int port;
  /** Socket client */
  private Socket client;
  /** Writer for outgoing command messages to server */
  BufferedWriter outgoing;
  /** Reader for incoming feedback messages from server */
  BufferedReader incoming;

  /**
   * Constructs a CSWriter for a host server
   * 
   * @param ip Server IP address
   * @param port Server port
   *
   * @throws IOException If could not initialize connection
   * 
   * @since 0.1.0
   */
  public CSWriter(String ip, int port) throws IOException {
    if (ip.equalsIgnoreCase("localhost") | ip.equals("127.0.0.1"))
      this.address = InetAddress.getLocalHost();
    else
      this.address = InetAddress.getByName(ip);
    this.port = port;
    client = new Socket(address, port);
    outgoing = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
    incoming = new BufferedReader(new InputStreamReader(client.getInputStream()));
  }

  /**
   * Close connection to server
   *
   * @throws IOException If could not close connection
   *
   * @since 0.0.1
   */
  public void close() throws IOException {
    client.close();
  }

  /**
   * Send message
   *
   * @param message The message to send
   *
   * @since 0.0.1
   */
  public void send(String message) {
    try {
      outgoing.write(message);
      outgoing.newLine();
      outgoing.flush();
    } catch (IOException e) {
      System.err.println(CANNOT_SEND);
      throw new RuntimeException(e);
    }
  }

  /**
   * Check whether incoming buffer is empty
   *
   * @return Whether incoming buffer is empty
   *
   * @since 0.0.1
   */
  public boolean empty() {
    try {
      return !incoming.ready();
    } catch (IOException e) {
      System.err.println(CANNOT_CHECK_EMPTY);
      throw new RuntimeException(e);
    }
  }

  /**
   * Receive message
   *
   * @return The message received
   *
   * @since 0.0.1
   */
  public String receive() {
    StringBuilder message = new StringBuilder();
    try {
      while (incoming.ready()) {
        String line = incoming.readLine();
        message.append(line);
      }
    } catch (IOException e) {
      System.err.println(CANNOT_RECEIVE);
      throw new RuntimeException(e);
    }
    return message.toString();
  }

  /**
   * Info about OPI as a string
   *
   * @return A string with address formatted as IP:port
   *
   * @since 0.0.1
   */
  public String toString() {
    return "Server socket connection at " + getIP() + ":" + getPort();
  }

  /**
   * Get local address
   *
   * @return the local address
   *
   * @since 0.0.1
   */
  public InetAddress getAddress() {
    return address;
  }

  /**
   * Get local IP address
   *
   * @return the local IP address
   *
   * @since 0.0.1
   */
  public String getIP() {
    return address.getHostAddress();
  }

  /**
   * Get local port
   *
   * @return the local port
   *
   * @since 0.0.1
   */
  public int getPort() {
    return port;
  }

}