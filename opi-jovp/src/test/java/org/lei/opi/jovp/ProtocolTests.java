package org.lei.opi.jovp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.lei.opi.core.CSListener;
import org.lei.opi.jovp.Settings.Machine;

/**
 *
 * Unitary tests for protocol thingies
 *
 * @since 0.0.1
 */
public class ProtocolTests {

    /**
   * Utilities and helpers for client in JSON unitary tests
   *
   * @since 0.0.1
   */
  class Monitor {

    private static final int PORT = 50008;

    private static Socket monitor;
    private static BufferedReader incoming;
    private static BufferedWriter outgoing;

    private static CSListener driver;

    /** init OpiManager and connect to it to server */
    Monitor() throws IOException {
      driver = new CSListener(PORT, new OpiJovp(Machine.DISPLAY));
      monitor = new Socket(driver.getAddress(), driver.getPort());
      incoming = new BufferedReader(new InputStreamReader(monitor.getInputStream()));
      outgoing = new BufferedWriter(new OutputStreamWriter(monitor.getOutputStream()));
    }

    /** load JSON message */
    String loadMessage(String file) throws IOException {
      InputStream inputStream = ProtocolTests.class.getResourceAsStream(file);
      assert inputStream != null;
      return IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));
    }

    /** send JSON message to server */
    static void send(String message) {
      driver.send(outgoing, message);
    }

    /** receive JSON message from server */
    static String receive() throws IOException {
      while (!incoming.ready()) Thread.onSpinWait();
      return driver.receive(incoming);
    }

    /** close client connection to server */
    void close() throws IOException {
      monitor.close();
      driver.close();
    }

  }

  /**
   * Monitor IMO perimeter
   *
   * @since 0.0.1
   */
  @Test
  public void monitorImoVifa() {
    String[] ss = {
      "jsons/ImoVifa/opiSetup.json"
  };
    String[] ps = {
        "jsons/ImoVifa/opiPresent.json"
    };
    serverDriver("jsons/ImoVifa/opiInit.json", ss, ps);
  }

    /** server driver with lists of present/query etc*/
    private void serverDriver(String initJson, String[] setupJson, String[] presentJson) {
      try {
        Monitor monitor = new Monitor();
        sendAndReceive(monitor.loadMessage("jsons/opiQuery.json")); // Query OPI
        sendAndReceive(monitor.loadMessage(initJson)); // Initialize OPI
        for (String s : setupJson) sendAndReceive(monitor.loadMessage(s)); // Setup OPI
        for (String s : presentJson) sendAndReceive(monitor.loadMessage(s)); // Present OPI
        sendAndReceive(monitor.loadMessage("jsons/opiClose.json")); // Close OPI
        monitor.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  
    /** send to and receive from server */
    private void sendAndReceive(String message) throws IOException {
      System.out.println("SENT\n" + message);
      Monitor.send(message);
      System.out.println("RECEIVED\t" + Monitor.receive() + "\n");
    }

}
