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

  /** Charset is {@value CHARSET_NAME} */
  private static final String CHARSET_NAME = "UTF8";
  /** {@value LISTENER_FAILED} */
  private static final String LISTENER_FAILED = "CSListener failed.";
  /** {@value CHECK_FAILED} */
  private static final String CHECK_FAILED = "Cannot check if socket is empty.";
  /** {@value RECEIVE_FAILED} */
  private static final String RECEIVE_FAILED = "Cannot write receive() message to receiveWriter in CSListener.";
  /** {@value SEND_FAILED} */
  private static final String SEND_FAILED = "Cannot write send() message to sendWriter in CSListener.";
  /** {@value CLOSE_FAILED} */
  private static final String CLOSE_FAILED = "Cannot close the socket.";
  /** {@value CLOSE_FAILED} */
  private static final String CANNOT_OBTAIN_ADDRESS = "Cannot obtain public address.";

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
  public void run() {
    Socket socket;
    try {
      server = new ServerSocket(port, 0, address);
      server.setSoTimeout(100);
      while (listen) {
        try {
          socket = server.accept();
          incoming = new BufferedReader(new InputStreamReader(socket.getInputStream(), CHARSET_NAME));
          outgoing = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), CHARSET_NAME));
          while (true) {
            if (!listen) break;
            if (incoming.ready()) {
              MessageProcessor.Packet pack = msgProcessor.process(receive());
              send(pack.msg);
              if (pack.close) break; // if close requested, break loop
            }
          }
        } catch (SocketTimeoutException ignored) {}
      }
      server.close();
    } catch (IOException e) {
      throw new RuntimeException(LISTENER_FAILED, e);
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
      System.err.println(CHECK_FAILED);
      throw new RuntimeException(CHECK_FAILED, e);
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
        message.append(line + (incoming.ready() ? "\n" : ""));
      }
      if (receiveWriter != null) receiveWriter.write(message.toString());
    } catch (IOException e) {
      System.err.println(RECEIVE_FAILED);
      throw new RuntimeException(RECEIVE_FAILED, e);
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
      outgoing.write(message.replace("\n", ""));
      outgoing.newLine();
      outgoing.flush();
      if (sendWriter != null) sendWriter.write(message);
    } catch (IOException e) {
      System.err.println(SEND_FAILED);
      throw new RuntimeException(SEND_FAILED, e);
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
        throw new RuntimeException(CLOSE_FAILED, e);
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
      throw new RuntimeException(CANNOT_OBTAIN_ADDRESS, e);
    }
    return null;
  }

}