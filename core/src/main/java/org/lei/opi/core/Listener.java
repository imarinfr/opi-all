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
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 *
 * Sender and Receiver on socket with Listener thread and a MessageProcessor object to help out.
 *
 * @since 0.2.0
 */
public abstract class Listener extends Thread {

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

    /** Constant for exception messages: {@value BAD_JSON} */
    public static final String BAD_JSON = "String is not a valid Json object.";
    /** name:value pair in JSON output if there is an error */
    public static String ERROR_YES = "\"error\" : 1";
    /** name:value pair in JSON output if there is not an error */
    private static String ERROR_NO = "\"error\" : 0";
   
    /** Connection address */
    private InetAddress address;
    /** Connection port */
    private int port;
    /** Socket server */
    private ServerSocket server;
    /** Reader for incoming messages on the socket */
    BufferedReader incoming;
    /** Writer for outgoing messages to the socket */
    BufferedWriter outgoing;
    /** If not null, add the messages processed by {@link send} to {@link sendWriter} */
    Writer sendWriter = null;
    /** If not null, add the messages procesed by {@link receive} to {@link receiveBuffer} */
    Writer receiveWriter = null;
    /** Whether it is listening */
    protected boolean listening;

    /** to parse JSONs with fromJson method */
    protected static Gson gson = new Gson();
    /** Given a JSON string, return name:value pairs */
    public static HashMap<String, Object> jsonToPairs(String jsonStr) throws JsonSyntaxException {
        return gson.fromJson(jsonStr, new TypeToken<HashMap<String, Object>>() {}.getType());
    }

    public Listener() { 
        this.listening = false; 
    }

    /**
     * Constructs a CSListener for a local port
     * 
     * @param port      port on which to listen
     * @param idAddress ipAddress in which to listen
     * 
     * @since 0.1.0
     */
    public void connect(int port, InetAddress ipAddress) {
        this.port = port;
        this.address = ipAddress; // ();
        this.listening = true;
        this.start();
        // wait for server to be ready
        while (this.server == null) Thread.onSpinWait();
    }

    /**
     * A class to hold string messages with attributes attached.
     */
    public static class Packet {
        /** true if the socket should be closed that receives this message */
        public boolean close;
        /** true if this message packet contains an error msg */
        public boolean error;
        /** Some sort of message - probably Json, but maybe not. */
        public String msg;

        /** Simple constrcutor 0 */
        public Packet() { this.close = false ; this.msg = ""; this.error = false; }
        /** Simple constrcutor 1 */
        public Packet(String msg) { this.close = false ; this.msg = msg; this.error = false; }
        /** Simple constructor 2 */
        public Packet(boolean close, String msg) { this.close = close ; this.msg = msg; this.error = false;}
        /** Simple constructor 3 */
        public Packet(boolean error, boolean close, String msg) { this.close = close ; this.msg = msg; this.error = error;}
    }

    /** 
    * Process incoming strings from the socket.
    * 
    * @param incomingString
    * @return String reply to send back on socket, or null to close socket.
    */
    public abstract Packet process(String incomingString);

    /** 
     * Run a socket server that applies process() to every incoming message, 
     * sending the result back on the same connection.
     * 
     * Will run forever until process() returns a Packet with close == true.
     *
     * Runs in its own thread */
    public void run() {
      Socket socket;
      try {
          server = new ServerSocket(port, 0, address);
          server.setSoTimeout(100);
          while (this.listening) {
            try {
                socket = server.accept();
                incoming = new BufferedReader(new InputStreamReader(socket.getInputStream(), CHARSET_NAME));
                outgoing = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), CHARSET_NAME));
                while (true) {
                    if (!this.listening) break;
                    if (incoming.ready()) {
                        Packet pack = process(receive());
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
    public void closeListener() {
      this.listening = false;
      synchronized (this) {
        try {
          this.join();
        } catch (InterruptedException e) {
          throw new RuntimeException(CLOSE_FAILED, e);
        }
      }
    }
  
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
    public InetAddress getAddress() { return address; }
  
    /**
     * Get local IP address
     *
     * @return the local IP address
     *
     * @since 0.0.1
     */
    public String getIP() { return address.getHostAddress(); }
  
    /**
     * Get local port
     *
     * @return the local port
     *
     * @since 0.0.1
     */
    public int getPort() { return port; }
  
    /** get network address for public access */
    public static InetAddress obtainPublicAddress() {
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
  
    //------------------------ Utilities for Creating Packets -------------------------
    /**
     * Create an OK message in JSON format with attached results
     * 
     * @param message Json object that is feedback from the OPI command or null
     * 
     * @return JSON-formatted ok message
     * 
     * @since 0.1.0
     */
    public static Packet ok(String message) {
      return ok(message, false);
    }
   
    /**
     * Create an OK message in JSON format with attached results
     * 
     * @param message Json object that is feedback from the OPI command or null
     * @param close Whether to close or not
     * 
     * @return JSON-formatted ok message
     * 
     * @since 0.1.0
     */
    public static Packet ok(String message, boolean close) {
      return new Packet(false, close, String.format("{\n  %s, \n  \"msg\": %s\n}", ERROR_NO, message));
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
    public static Packet error(String description) {
      return new Packet(
          String.format("{\n  %s, \n  \"msg\": \"%s\"\n}", ERROR_YES, description));
    }
   
    /**
     * Create an error message in JSON format to send to R OPI
     * 
     * @param description String error description (no quotes) to add to Json return name 'description'
     * @param exception An exception to print to stderr and add to Json return object name 'exception'
     * 
     * @return JSON-formatted error message
     * 
     * @since 0.1.0
     */
    public static Packet error(String description, Exception exception) {
      exception.printStackTrace();
      String eStr = exception.toString().replace("\"", "\\\"");
      eStr = eStr.replace("\0", "0");
      return new Packet(
          String.format("{\n  %s, \n  \"msg\": \"%s\", \"exception\": \"%s\"\n}", ERROR_YES, description, eStr));
    }
}