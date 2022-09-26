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
 * Sender and Receiver on socket with Listener thread.
 *
 * @since 0.0.1
 */
public class CSWriter {

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
   * @return Message received
   *
   * @throws IOException If could not send message
   *
   * @since 0.0.1
   */
  public void send(String message) throws IOException {
    outgoing.write(message);
    outgoing.newLine();
    outgoing.flush();
  }

  /**
   * Check whether incoming buffer is empty
   *
   * @return Whether incoming buffer is empty
   * 
   * @throws IOException If could not check incoming state
   *
   * @since 0.0.1
   */
  public boolean empty() throws IOException {
    return !incoming.ready();
  }

  /**
   * Receive message
   *
   * @return The message received
   * 
   * @throws IOException If could not receive message
   *
   * @since 0.0.1
   */
  public String receive() throws IOException {
    StringBuilder message = new StringBuilder();
    while (incoming.ready()) {
      String line = incoming.readLine();
      message.append(line);
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