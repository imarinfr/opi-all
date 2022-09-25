package org.lei.opi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
  BufferedWriter outgoingMsg;
  /** Reader for incoming feedback messages from server */
  BufferedReader incomingMsg;

  /**
   * Constructs a CSWriter for a host server
   * 
   * @param ip Server IP address
   * @param port Server port
   * @param msgProcessor Processor object
   *
   * @throws UnknownHostException
   * 
   * @since 0.1.0
   */
  public CSWriter(String ip, int port) throws UnknownHostException {
    this.address = InetAddress.getByName(ip);
    this.port = port;
  }

  /**
   * Open connection to server
   *
   * @throws IOException
   *
   * @since 0.0.1
   */
  public void open() throws IOException {
    client = new Socket(address, port);
    // for sending strings
    outgoingMsg = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
    incomingMsg = new BufferedReader(new InputStreamReader(client.getInputStream()));
  }

  /**
   * Close connection to server
   *
   * @throws IOException
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
   * @throws IOException
   *
   * @since 0.0.1
   */
  public void send(String message) throws IOException {
    outgoingMsg.write(message);
    outgoingMsg.newLine();
    outgoingMsg.flush();
  }

  /**
   * Check whether incoming buffer is empty
   *
   * @return Whether incoming buffer is empty
   *
   * @since 0.0.1
   */
  public boolean empty() throws IOException {
    return !incomingMsg.ready();
  }

  /**
   * Receive message
   *
   * @return The message received
   * 
   * @throws IOException
   *
   * @since 0.0.1
   */
  public String receive() throws IOException {
    StringBuilder message = new StringBuilder();
    while (incomingMsg.ready()) {
      String line = incomingMsg.readLine();
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
    return "Server socket connection at " + address.toString() + ":" + port;
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
   * Get local address
   *
   * @return the listener port
   *
   * @since 0.0.1
   */
  public int getPort() {
    return port;
  }

}