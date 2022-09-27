package org.lei.opi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

import org.lei.opi.core.definitions.MessageProcessor;

/**
 *
 * Sender and Receiver on socket with Listener thread.
 *
 * @since 0.0.1
 */
public class CSListener extends Thread {

  /** {@value CANNOT_CHECK_EMPTY} */
  private static final String CANNOT_CHECK_EMPTY = "Cannot check if socket is empty";
  /** {@value CANNOT_RECEIVE} */
  private static final String CANNOT_RECEIVE = "Cannot write receive() message to receiveWriter in CSListener";
  /** {@value CANNOT_SEND} */
  private static final String CANNOT_SEND = "Cannot write send() message to sendWriter in CSListener";

  /** listen backlog */
  private static final int BACKLOG = 1;

  /** Local connection address */
  private InetAddress address;
  /** Local connection port */
  private int port;
  /** Socket server */
  private ServerSocket server;
  /** Reader for incoming messages from client */
  BufferedReader incoming;
  /** Writer for outgoing feedback messages to client */
  BufferedWriter outgoing;
  /** If not null, add the messages processed by {@link send} to {@link sendWriter} */
  Writer sendWriter = null;
  /** If not null, add the messages procesed by {@link receive} to {@link receiveBuffer} */
  Writer receiveWriter = null;
  /** Message processor */
  private MessageProcessor msgProcessor;
  /** Whether it is listening */
  private boolean listen = true;

  /**
   * Constructs a CSListener for a local port
   * 
   * @param port         Local port
   * @param msgProcessor Processor object
   * 
   * @since 0.1.0
   */
  public CSListener(int port, MessageProcessor msgProcessor) {
    this.msgProcessor = msgProcessor;
    this.port = port;
    this.address = obtainPublicAddress();
    this.start();
    // wait for server to be ready
    while (this.server == null) Thread.onSpinWait();
  }

  /**
   * Constructs a CSListener for a local port and already created receive and send
   * writers
   * 
   * @param port          Local port
   * @param msgProcessor  Processor object
   * @param receiveWriter If this is not null, messages via {@link receive} will
   *                      be println to this.
   * @param sendWriter    If this is not null, messages via {@link send} will be
   *                      println to this.
   * 
   * @since 0.1.0
   */
  public CSListener(int port, MessageProcessor msgProcessor, Writer receiveWriter, Writer sendWriter) {
    this(port, msgProcessor);
    this.receiveWriter = receiveWriter;
    this.sendWriter = sendWriter;
  }

  /** run listener on a different thread */
  public void run() throws RuntimeException {
    Socket socket;
    try {
      server = new ServerSocket(port, BACKLOG, address);
      server.setSoTimeout(100);
      while (listen) {
        try {
          socket = server.accept();
          incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          outgoing = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
          while (listen) {
            if (incoming.ready()) {
              MessageProcessor.Packet pack = msgProcessor.process(receive());
              send(pack.msg);
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
   *
   * Receive message
   *
   * @return Message received
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
      if (receiveWriter != null) receiveWriter.write(message.toString());
    } catch (IOException e) {
      System.err.println(CANNOT_RECEIVE);
      throw new RuntimeException(e);
    }
    return message.toString();
  }

  /**
   *
   * Send message
   *
   * @param message  The message to deliver
   *
   * @since 0.0.1
   */
  public void send(String message) {
    try {
      outgoing.write(message);
      outgoing.newLine();
      outgoing.flush();
      if (sendWriter != null) sendWriter.write(message);
    } catch (IOException e) {
      System.err.println(CANNOT_SEND);
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
    return "Local socket connection at " + getIP() + ":" + getPort();
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
      throw new RuntimeException(e);
    }
    return null;
  }

}