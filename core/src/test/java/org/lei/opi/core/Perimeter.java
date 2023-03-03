package org.lei.opi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

/**
 * Simulates the O900 machine
 *
 * @since 0.0.1
 */
class Perimeter extends Thread {

  enum Machine {O900, COMPASS, MAIA};

  /** Charset is {@value CHARSET_NAME} */
  private static final String CHARSET_NAME = "UTF8";
  /** {@value LISTENER_FAILED} */
  private static final String LISTENER_FAILED = "Listener failed.";
  /** {@value RECEIVE_FAILED} */
  private static final String RECEIVE_FAILED = "Cannot write receive() message to receiveWriter in Listener.";
  /** {@value SEND_FAILED} */
  private static final String SEND_FAILED = "Cannot write send() message to sendWriter in Listener.";
  /** {@value CLOSE_FAILED} */
  private static final String CLOSE_FAILED = "Cannot close the socket.";

  /** listen backlog, which is @value BACKLOG */
  private static final int BACKLOG = 1;

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
  public void run() {
    Socket socket;
    try {
      server = new ServerSocket(port, BACKLOG);
      server.setSoTimeout(100);
      while (listen) {
        try {
          socket = server.accept();
          incoming = new BufferedReader(new InputStreamReader(socket.getInputStream(), CHARSET_NAME));
          outgoing = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), CHARSET_NAME));
          while (listen) if (incoming.ready()) process(receive());
          break;
        } catch (SocketTimeoutException ignored) {
        }
      }
      server.close();
    } catch (IOException e) {
      throw new RuntimeException(LISTENER_FAILED, e);
    }
  }

  /** process message */
  private void process(String message) {
    System.out.println("[Perimeter " + machine + "] received instruction: " + message);
    String[] input = message.split(" ");
    switch(machine) {
      case O900 -> processO900(input[0]);
      case COMPASS, MAIA -> processICare(input[0]);
    };
  }

  /** process O900 message */
  private void processO900(String cmd) {
    switch(cmd) {
      case "OPI_INITIALIZE", "OPI_SET_BACKGROUND" -> send("0");
      case "OPI_PRESENT_STATIC", "OPI_PRESENT_STATIC_F310" -> send("null|||1|||798|||0.5|||1");
      case "OPI_PRESENT_KINETIC" -> send("null|||1|||5|||-1.5|||1|||0.5|||1");
      default -> send("WRONG COMMAND");
    };
  }

  /** process Compass or Maia message */
  private void processICare(String cmd) {
    switch(cmd) {
      case "OPI-OPEN" -> send(sendOpenIcare());
      case "OPI-SET-FIXATION", "OPI-SET-TRACKING", "OPI-MOVE-STIMULUS" -> send("0");
      case "OPI-PRESENT-STATIC" -> send("0 1 582 6395477 1498888428 1498889999 2 0 0.212 912 753");
      case "OPI-CLOSE" -> send(sendCloseIcare());
      default -> send("WRONG COMMAND");
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
      System.err.println(RECEIVE_FAILED);
      throw new RuntimeException(RECEIVE_FAILED, e);
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
      System.err.println(SEND_FAILED);
      throw new RuntimeException(SEND_FAILED, e);
    }
  }

  /**
   * Send message
   *
   * @param message The message to send
   *
   * @since 0.0.1
   */
  public void send(ByteBuffer byteBuffer) {
    StringBuilder message = new StringBuilder();
    for (byte value : byteBuffer.array()) message.append(value).append(" ");
    try {
      outgoing.write(message.toString());
      outgoing.newLine();
      outgoing.flush();
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

  /** simulates OPI-OPEN return */
  private ByteBuffer sendOpenIcare() {
    float prlx = 0.05f;
    float prly = 0.08f;
    float onhx = 15.2f;
    float onhy = -2.5f;
    float[] image = new float[] {1, 2, 3, 4,
                                 5, 6, 7, 8,
                                 9, 10, 11, 12,
                                 13, 14, 15, 16};
    int length = 4 * (5 + image.length);
    ByteBuffer byteBuffer = ByteBuffer.allocate(length)
      .putInt(length - 4)
      .putFloat(prlx)
      .putFloat(prly)
      .putFloat(onhx)
      .putFloat(onhy);
    for (float value : image) byteBuffer.putFloat(value);
    return byteBuffer;
  }

  /** simulates OPI-CLOSE return */
  private ByteBuffer sendCloseIcare() {
    int[] times = new int[] {1000001, 1000002, 1000003, 1000004};
    float[] posx = new float[] {0.0f, 0.0f, 1.0f, 1.0f};
    float[] posy = new float[] {0.0f, 1.0f, 0.0f, 1.0f};
    int length = 4 * (1 + 3 * times.length);
    ByteBuffer byteBuffer = ByteBuffer.allocate(length).putInt(length - 4);
    for (int i = 0; i < times.length; i++) byteBuffer.putInt(times[i]).putFloat(posx[i]).putFloat(posy[i]);
    return byteBuffer;
  }

}