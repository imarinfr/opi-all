package org.lei.opi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Stream;

import org.lei.opi.core.structures.Command;
import org.lei.opi.core.structures.Parameters;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

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
                if (message.equals(Command.CLOSE.toString())) break;
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
  /** Constant for exception messages: No commmand field */
  private static final String NO_COMMAND_FIELD = "message does not contain field 'command'";
  /** Constant for exception messages: Missing choose command */
  private static final String NO_CHOOSE_COMMAND = "no machine selected yet. First 'command' must be 'choose'";
  /** Constant for exception messages: No machine specified */
  private static final String WRONG_MACHINE_NAME = "the selected machine %s in 'choose' does not exist";
  /** Constant for exception messages: Machine initialized while trying to choose */
  private static final String MACHINE_INITIALIZED = "Machine '%s' initialized. Cannot choose another one, you silly goose";

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
  /** OPI machine to use */
  private OpiMachine machine;
  /** Selected OPI machine's methods */
  private Method[] methods;
  /** Selected OPI machine's methods' names */
  private String[] names;

  /** Selected OPI machine's methods' parameters */
  private Stream<Parameters> parameters;

  /**
   * Whether a connection has been stablished with machine, in which case, chooser
   * is illegal
   */
  private boolean connected = true;

  /**
   *
   * Start the OPI
   *
   * @param port The local port to listen to R OPI
   *
   * @since 0.0.1
   */
  public OpiManager(int port) {
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
   * Process input JSON commands
   *
   * @param json JSON-structured message with at least a name "command"
   * @return a JSON-structured message with feedback OK or ERROR + descriptive
   *         message
   *
   * @since 0.0.1
   */
  public String process(String json) {
    Gson gson = new Gson();
    Map<String, String> pairs;
    // Check that there is a command
    pairs = gson.fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
    if (!pairs.containsKey("command"))
      return error((new JsonSyntaxException(NO_COMMAND_FIELD).toString()));
    if (machine == null) { // First thing to do is to choose a machine
      return chooseMachine(pairs);
    } else if (pairs.get("command").equals("choose")) {
      if (!connected) return chooseMachine(pairs); // If connected, cannot choose a different machine
      else {
        return error(String.format(MACHINE_INITIALIZED, pairs.get("machine")));
      }
    }
    // If all preliminaries are good, then find function name, ...
    String function = pairs.get("command");
    try {
      // construct and validate parameters, and try and run the method with them
      int i = Arrays.asList(names).indexOf(function);
      String feedback = (String) methods[i].invoke(machine, arguments(function, pairs));
      return ok(feedback);
    } catch (ArrayIndexOutOfBoundsException | JsonParseException | IllegalAccessException |
             InvocationTargetException e) {
      return error(String.format("{Cannot execute method '%s' in %s.\n\"%s\"}", function, machine.getClass(), e));
    }
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
   * Choose machine and get its methods and parameters
   *
   * @param pairs instructions from JSON message
   * 
   * @return JSON-formatted message with feedback
   * 
   * @since 0.1.0
   */
  private String chooseMachine(Map<String, String> pairs) {
    if (!pairs.get("command").equals("choose"))
    return error((new JsonSyntaxException(NO_CHOOSE_COMMAND)).toString());
    //if (!pairs.containsKey("machine"))
    //  throw new JsonSyntaxException(NO_MACHINE_SELECTED);
    try {
      machine = OpiMachine.choose(pairs.get("machine"));
      methods = machine.getClass().getMethods();
      names = Stream.of(methods).map(Method::getName).toArray((String[]::new));
      parameters = Stream.of(methods).map((Method m) -> m.getAnnotation(Parameters.class));
    } catch (ClassNotFoundException e) {
        return error((new JsonSyntaxException(String.format(WRONG_MACHINE_NAME, pairs.get("machine"))).toString()));
    }
    return ok();
  }

  /**
   * Construct arguments from JSON fileand validate them. It makes sure that
   * the the parameters received match the device-dependent OPI standard
   *
   * @param pairs    JSON pairs
   * @param function function to invoke on the selected machine
   * 
   * @since 0.1.0
   */
  private Map<String, String> arguments(String function, Map<String, String> pairs) throws JsonParseException {
    return pairs;
  }

   /**
   * Create an OK message in JSON format with attached results
   * 
   * @param feedback feedback from the OPI command or null
   * 
   * @return JSON-formatted ok message
   * 
   * @since 0.1.0
   */
  private String ok() {
    return "{\n  " + OpiMachine.OK + "\n}";
  }

  /**
   * Create an OK message in JSON format with attached results
   * 
   * @param feedback feedback from the OPI command or null
   * 
   * @return JSON-formatted ok message
   * 
   * @since 0.1.0
   */
  private String ok(String feedback) {
    return "{\n  " + OpiMachine.OK + "\n  \"feedback\": \"" + feedback + "\"\n}";
  }

  /**
   * Create an error message in JSON format to send to R OPI
   * 
   * @param description error description
   * 
   * @return JSON-formatted error message
   * 
   * @since 0.1.0
   */
  private String error(String description) {
    return "{\n" + OpiMachine.ERROR + "  \"description\": \"" + description + "\"\n}";
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