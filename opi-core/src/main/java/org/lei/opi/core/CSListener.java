package org.lei.opi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

/**
 *
 * Sender and Receiver on socket with Listener thread.
 *
 * @since 0.0.1
 */

public class CSListener extends Thread {

  /** listen backlog */
  private static final int BACKLOG = 1;

  /** Connection address */
  private InetAddress address;
  /** Connection port */
  private int port;
  /** Socket server */
  private ServerSocket server;
  /** Whether it is listening or not */
  private boolean listen = true;

  private MessageProcessor msgProcessor;

  public CSListener(int port, MessageProcessor processObject) {
    this.msgProcessor = processObject;
    this.port = port;
    address = obtainPublicAddress();

    this.start();
    // wait for server to be ready
    while (this.server == null)
      Thread.onSpinWait();
  }

  /** run listener on a different thread */
  public void run() {
    Socket socket;
    try {
      server = new ServerSocket(port, BACKLOG, address);
      server.setSoTimeout(100);
      while (listen) {
        try {
          socket = server.accept();
          BufferedReader incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          BufferedWriter outgoing = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
          while (listen) {
            if (incoming.ready()) {
              MessageProcessor.Packet pack = msgProcessor.process(receive(incoming));
              send(outgoing, pack.msg);
              if (pack.close) break; // if close requested, break loop
            }
          }
          break;
        } catch (SocketTimeoutException ignored) {
        }
      }
      server.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   *
   * Receive message
   *
   * @param incoming Buffered reader for incoming messages
   *
   * @return Message received
   *
   * @since 0.0.1
   */
  public String receive(BufferedReader incoming) {
    StringBuilder message = new StringBuilder();
    try {
      while (incoming.ready()) {
        String line = incoming.readLine();
        message.append(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return message.toString();
  }

  /**
   *
   * Send message
   *
   * @param outgoing Buffered writer for outgoing messages
   * @param message  The message to deliver
   *
   * @since 0.0.1
   */
  public void send(BufferedWriter outgoing, String message) {
    try {
      outgoing.write(message);
      outgoing.newLine();
      outgoing.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Signal stop listening and wait
   *
   * @since 0.0.1
   */
  public void close() {
    this.listen = false;
    synchronized (this) {
      try {
        this.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Info about OPI as a string
   *
   * @return A string with address formatted as IP:port
   *
   * @since 0.0.1
   */
  public String toString() {
    return "Local socket connection at " + address.toString() + ":" + port;
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
      e.printStackTrace();
    }
    return null;
  }
}