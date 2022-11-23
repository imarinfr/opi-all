package org.lei.opi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import javafx.collections.ObservableList;

/**
 *
 * Sender and Receiver on socket with Writer thread.
 *
 * @since 0.0.1
 */
public class CSWriter {

  /** Charset is {@value CHARSET_NAME} */
  private static final String CHARSET_NAME = "UTF8";
  /** {@value OPEN_FAILED} */
  private static final String OPEN_FAILED = "Cannot open the socket.";
  /** {@value CHECK_FAILED} */
  private static final String CHECK_FAILED = "Cannot check if socket is empty.";
  /** {@value RECEIVE_FAILED} */
  private static final String RECEIVE_FAILED = "Cannot write receive() message to receiveWriter in CSListener.";
  /** {@value SEND_FAILED} */
  private static final String SEND_FAILED = "Cannot write send() message to sendWriter in CSListener.";
  /** {@value CLOSE_FAILED} */
  private static final String CLOSE_FAILED = "Cannot close the socket.";
  /** {@value CANNOT_OBTAIN_ADDRESS} */
  private static final String CANNOT_OBTAIN_ADDRESS = "Cannot obtain public address.";

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

  /**  A 'human readable' record of incoming and outgoing that might
   * be displayed in monitor, printed, etc.
   */
  ObservableList<String> messageRecord;

  /**
   * Constructs a CSWriter for a host server
   * 
   * @param ip Server IP address
   * @param port Server port
   * 
   * @since 0.1.0
   */
  public CSWriter(String ip, int port) throws RuntimeException {
    try {
      if (ip.equalsIgnoreCase("localhost") || ip.equals("127.0.0.1"))
        this.address = obtainPublicAddress();
      else
        this.address = InetAddress.getByName(ip);
      this.port = port;
      client = new Socket(address, port);
      outgoing = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), CHARSET_NAME));
      incoming = new BufferedReader(new InputStreamReader(client.getInputStream(), CHARSET_NAME));
    } catch (IOException e) {
      System.out.println("Couldn' open connection to " + ip + ":" + port);
      e.printStackTrace();
      throw new RuntimeException(OPEN_FAILED, e);
    }
  }

  /**
   * Close connection to server
   *
   * @since 0.0.1
   */
  public void close() {
    try {
      client.close();
    } catch (IOException e) {
      throw new RuntimeException(CLOSE_FAILED, e);
    }
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
      messageRecord.add(message);
    } catch (IOException e) {
      System.err.println(SEND_FAILED);
      throw new RuntimeException(SEND_FAILED, e);
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
      System.err.println(CHECK_FAILED);
      throw new RuntimeException(CHECK_FAILED, e);
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
        message.append(line + (incoming.ready() ? "\n" : ""));
      }
    } catch (IOException e) {
      System.err.println(RECEIVE_FAILED);
      throw new RuntimeException(RECEIVE_FAILED, e);
    }
    messageRecord.add(message.toString());
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

  /** get network address for public access */
  private InetAddress obtainPublicAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
        NetworkInterface networkInterface = en.nextElement();
        for (Enumeration<InetAddress> address = networkInterface.getInetAddresses(); address.hasMoreElements();) {
          InetAddress inetAddress = address.nextElement();
          if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
            return inetAddress;
          }
        }
      }
    } catch (SocketException e) {
      throw new RuntimeException(CANNOT_OBTAIN_ADDRESS, e);
    }
    return null;
  }

}