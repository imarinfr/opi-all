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
import java.util.HashMap;
import java.util.stream.Stream;

import org.lei.opi.core.structures.Command;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 *
 * OPI manager
 *
 * @since 0.0.1
 */
public class OpiManager {

  /** listen thread */
  private class Listener extends Thread {

    ServerSocket server;
    boolean listen = true;

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
                String message = process(receive(incoming));
                if (message.equals(Command.CLOSE.name())) 
                  break;
                send(outgoing, message);
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

  }

  /** listen backlog */
  private static final int BACKLOG = 1;
  /** Constant for exception messages: {@value BAD_JSON} */
  private static final String BAD_JSON = "String is not a valid Json object.";
  /** Constant for exception messages: {@value NO_COMMAND_FIELD} */
  private static final String NO_COMMAND_FIELD = "Json message does not contain field 'command'.";
  /** Constant for exception messages: {@value BAD_COMMAND_FIELD} {@link Command} */
  private static final String BAD_COMMAND_FIELD = "value of 'command' name in Json message is not one of Command'.";
  /** Constant for exception messages: {@value NO_CHOOSE_COMMAND} */
  private static final String NO_CHOOSE_COMMAND = "No machine selected yet. First 'command' must be " + Command.CHOOSE.name();
  /** Constant for exception messages: {@value MACHINE_NEEDS_CLOSING} */
  private static final String MACHINE_NEEDS_CLOSING = "Close the previous machine before choosing another.";
  /** Constant for exception messages: {@value WRONG_MACHINE_NAME} */
  private static final String WRONG_MACHINE_NAME = "Cannot create the selected machine %s in 'command:'" + Command.CHOOSE.name() + "' as it does not exist.";
  /** Constant for exception messages: {@value WRONG_MACHINE_SUPER} */
  private static final String WRONG_MACHINE_SUPER = "You cannot create a machine from OpiMachine. Use a subclass of OpiMachine for the choose command.";

  /** name:value pair in JSON output if there is an error */
  private static String ERROR_YES  = "\"error\" : 1";
  /** name:value pair in JSON output if there is not an error */
  private static String ERROR_NO   = "\"error\" : 0";

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
  public static String receive(BufferedReader incoming) {
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
  public static void send(BufferedWriter outgoing, String message) {
    try {
      outgoing.write(message);
      outgoing.newLine();
      outgoing.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Connection address */
  protected InetAddress address;
  /** Connection port */
  protected int port;
  /** server listener */
  private Listener listener;
  /** OPI machine to use. */
  private OpiMachine machine;

  /**
   *
   * Start the OPI
   *
   * @param port The local port to listen to R OPI
   *
   * @since 0.0.1
   */
  public OpiManager(int port) {
    this.machine = null;  // no opi machine chosen yet

    this.port = port;
    address = obtainPublicAddress();
    listener = new Listener();
    listener.start();
    // wait for server to be ready
    while (listener.server == null)
      Thread.onSpinWait();
  }

  /**
   * Signal stop listening and wait
   *
   * @since 0.0.1
   */
  public void close() {
    listener.listen = false;
    synchronized (listener) {
      try {
        listener.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   *
   * Set local port. That is, close and cleanup connection,
   * and create a new connection with a different port
   *
   * @param port The local port to listen to R OPI
   *
   * @since 0.0.1
   */
  public void setLocalPort(int port) {
    close();
    this.port = port;
    listener = new Listener();
  }

  /**
   *
   * Return info about OPI as a string
   *
   * @return A string with address formatted as IP:port
   *
   * @since 0.0.1
   */
  public String toString() {
    return "Local socket connection at " + address.toString() + ":" + port;
  }

   /**
   * Create an OK message in JSON format with nothing else
   * 
   * @return JSON-formatted ok message
   * 
   * @since 0.1.0
   */
  public static String ok() {
    return String.format("{%s}", ERROR_NO);
  }

  /**
   * Create an OK message in JSON format with attached results
   * 
   * @param feedback Json object that is feedback from the OPI command or null
   * 
   * @return JSON-formatted ok message
   * 
   * @since 0.1.0
   */
  public static String ok(String feedback) {
    return String.format("{%s, \"feedback\": %s}", ERROR_NO, feedback);
  }

  /**
   * Create an error message in JSON format to send to R OPI
   * 
   * @param description String error description (no quotes)
   * 
   * @return JSON-formatted error message
   * 
   * @since 0.1.0
   */
  public static String error(String description) {
    return String.format("{%s, \"description\": \"%s\"}", ERROR_YES, description);
  }

  /**
   * Create an error message in JSON format to send to R OPI
   * 
   * @param description String error description (no quotes) to add to Json return name 'description'
   * @param e An exception to print to stderr and add to Json return object name 'exception'
   * 
   * @return JSON-formatted error message
   * 
   * @since 0.1.0
   */
  public static String error(String description, Exception e) {
    System.err.println(e);
    return String.format("{%s, \"description\": \"%s\", \"exception\": \"%s\"}", ERROR_YES, description, e.toString());
  }

  /**
   * Process incoming Json commands. If it is a 'choose' command, then 
   * set the private field machine to a new instance of that machine.
   * If it is another command, then process it using the machine object.
   *
   * @param jsonStr A JSON object that at least contains the name 'command'.
   * 
   * @return JSON-formatted message with feedback
   * 
   * @since 0.1.0
   */
  private String process(String jsonStr) {
    Gson gson = new Gson();

    HashMap<String, String> pairs;
    try {
      pairs = gson.fromJson(jsonStr, HashMap.class);
    } catch (JsonSyntaxException e) {
      return error(BAD_JSON);
    }
        
    if (!pairs.containsKey("command")) // needs a command
      return error(NO_COMMAND_FIELD);

      // check it is a valid command from Command.*
    if (!Stream.of(Command.values()).anyMatch((e) -> e.name().equalsIgnoreCase(pairs.get("command"))))
      return error(BAD_COMMAND_FIELD);

      // If it is a CHOOSE command, then let's fire up the chosen machine (unless one already open)
    if (pairs.get("command").equalsIgnoreCase(Command.CHOOSE.name())) {
      if (this.machine != null && machine.getIsInitialised())
        return error(MACHINE_NEEDS_CLOSING);

      String className = OpiMachine.class.getPackage().getName() + "." + pairs.get("machine");
      try {
        //if (pairs.get("machine") == machine.getClass().getName())  // should be called on a subclass
        //  return error(WRONG_MACHINE_SUPER);
        machine = (OpiMachine) Class.forName(className)
          .getDeclaredConstructor()
          .newInstance();
      } catch (Exception e) {
          return error(String.format(WRONG_MACHINE_NAME, className), e);
      }
 
      return ok();
    } else { // If it is not a CHOOSE command and there is no machine open, give up else try it out
      if (this.machine == null)
        return error(NO_CHOOSE_COMMAND);

      return this.machine.process(pairs);
    }
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