package org.lei.opi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * Simulates the O900 machine
 *
 * @since 0.0.1
 */
class Perimeter extends Thread {

  enum Machine {O900, COMPASS, MAIA};

  /** listen backlog, which is @value BACKLOG */
  private static final int BACKLOG = 1;
  /** {Charset is @value CHARSET_NAME} */
  private static final String CHARSET_NAME = "UTF8";
  /** {@value CANNOT_SEND} */
  private static final String CANNOT_SEND = "Cannot send message in CSWriter";
  /** {@value CANNOT_RECEIVE} */
  private static final String CANNOT_RECEIVE = "Cannot receive message in CSWriter";

  /** The listener for the Perimeter */
  private Machine machine;
  /** The listener for the Perimeter */
  private ServerSocket server;
  /** Local connection port */
  private int port;
  /** Reader for incoming feedback messages from server */
  BufferedReader incoming;
  /** Writer for outgoing command messages to server */
  BufferedWriter outgoing;
  /** Whether it is listening */
  private boolean listen = true;

  /**
   * Initialize the Perimeter
   *
   * @param port Perimeter's local port
   *
   * @throws IOException If server cannot be opened
   *
   * @since 0.0.1
   */
  Perimeter(Machine machine, int port) throws IOException {
    this.machine = machine;
    this.port = port;
    this.start();
    // wait for server to be ready
    while (this.server == null) Thread.onSpinWait();
  }

  /** run listener on a different thread */
  public void run() throws RuntimeException {
    Socket socket;
    try {
      server = new ServerSocket(port, BACKLOG);
      server.setSoTimeout(100);
      while (listen) {
        try {
          socket = server.accept();
          incoming = new BufferedReader(new InputStreamReader(socket.getInputStream(), CHARSET_NAME));
          outgoing = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), CHARSET_NAME));
          while (listen) if (incoming.ready()) send(process(receive()));
          break;
        } catch (SocketTimeoutException ignored) {
        }
      }
      server.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** process message */
  private String process(String message) {
    String[] input = message.split(" ");
    switch(machine) {
      case O900 -> processO900(input[0], Arrays.copyOfRange(input, 1, input.length));
      case COMPASS, MAIA -> processICare(input[0], Arrays.copyOfRange(input, 1, input.length));
    }
    return message;
  }

  /** process O900 message */
  private String processO900(String cmd, String[] pars) {
    return switch(cmd) {
      case "OPI_INITIALIZE" -> "OPI_INITIALIZE with params " + Arrays.toString(pars);
      case "OPI_SET_BACKGROUND" -> "OPI_SET_BACKGROUND with params " + Arrays.toString(pars);
      case "OPI_PRESENT_STATIC" -> "OPI_PRESENT_STATIC with params " + Arrays.toString(pars);
      case "OPI_PRESENT_STATIC_F310" -> "OPI_PRESENT_STATIC_F310 with params " + Arrays.toString(pars);
      case "OPI_PRESENT_KINETIC" -> "OPI_PRESENT_KINETIC with params " + Arrays.toString(pars);
      case "OPI_CLOSE" -> "OPI_CLOSE with params " + Arrays.toString(pars);
      default -> "WRONG COMMAND";
    };
  }

  /** process Compass or Maia message */
  private String processICare(String cmd, String[] pars) {
    return switch(cmd) {
      case "OPI-OPEN" -> "OPI-OPEN with params " + Arrays.toString(pars);
      case "OPI-SET-FIXATION" -> "OPI-SET-FIXATION with params " + Arrays.toString(pars);
      case "OPI-SET-TRACKING" -> "OPI-SET-TRACKING with params " + Arrays.toString(pars);
      case "OPI-PRESENT-STATIC" -> "OPI-PRESENT-STATIC with params " + Arrays.toString(pars);
      case "OPI-CLOSE" -> "OPI-CLOSE with params " + Arrays.toString(pars);
      default -> "WRONG COMMAND";
    };
  }

  /**
   * Get local port
   *
   * @return the local port
   *
   * @since 0.0.1
   */
  int getPort() {
    return port;
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
      System.err.println(CANNOT_RECEIVE);
      throw new RuntimeException(e);
    }
    return message.toString();
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

}