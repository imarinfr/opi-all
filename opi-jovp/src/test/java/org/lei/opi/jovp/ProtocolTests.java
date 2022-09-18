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

  /** driver port */
  private static final int PORT = 50008;
  /** The OPI JOVP */
  OpiJovp opiJovp;
  /**
   * Utilities and helpers for client in JSON unitary tests
   *
   * @since 0.0.1
   */
  class Monitor {

    private static Socket monitor;
    private static BufferedReader incoming;
    private static BufferedWriter outgoing;

    /** init OpiManager and connect to it to server */
    Monitor(CSListener driver) throws IOException {
      monitor = new Socket(driver.getAddress(), driver.getPort());
      incoming = new BufferedReader(new InputStreamReader(monitor.getInputStream()));
      outgoing = new BufferedWriter(new OutputStreamWriter(monitor.getOutputStream()));
    }

    /** load JSON message */
    static String loadMessage(String file) throws IOException {
      InputStream inputStream = ProtocolTests.class.getResourceAsStream(file);
      assert inputStream != null;
      return IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));
    }

    /** send JSON message to server */
    static void send(CSListener driver, String message) {
      driver.send(outgoing, message);
    }

    /** receive JSON message from server */
    static String receive(CSListener driver) throws IOException {
      while (!incoming.ready()) Thread.onSpinWait();
      return driver.receive(incoming);
    }

    /** close client connection to server */
    static void close() throws IOException {
      monitor.close();
    }

  }

  /**
   * Monitor IMO perimeter
   *
   * @since 0.0.1
   */
  @Test
  public void monitorDisplay() {
    opiJovp = new OpiJovp(Machine.DISPLAY);
    opiJovp.open(PORT);
    new Thread() {
      public void run() {
        try {
          Thread.sleep(200);
          monitorDriver();
          Thread.sleep(200);
          //signal finish all
          opiJovp.psychoEngine.finish();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }.start();
    opiJovp.start(); // start the psychoEngine
    opiJovp.close(); // start the psychoEngine
  }

  /** server driver with lists of present/query etc*/
  private void monitorDriver() throws InterruptedException {
    String[] ss = {
      "jsons/Display/opiSetup.json"
    };
    String[] ps = {
        "jsons/Display/opiPresent.json"
    };
    monitorDriver("jsons/Display/opiInit.json", ss, ps);
  }

  /** server driver with lists of present/query etc*/
  private void monitorDriver(String initJson, String[] setupJson, String[] presentJson) throws InterruptedException {
    try {
      new Monitor(opiJovp.listener);
      sendAndReceive(Monitor.loadMessage("jsons/opiQuery.json")); // Query OPI
      sendAndReceive(Monitor.loadMessage(initJson)); // Initialize OPI
      Thread.sleep(1000);
      for (String s : setupJson) sendAndReceive(Monitor.loadMessage(s)); // Setup OPI
      Thread.sleep(1000);
      for (String s : presentJson) sendAndReceive(Monitor.loadMessage(s)); // Present OPI
      sendAndReceive(Monitor.loadMessage("jsons/opiClose.json")); // Close OPI
      Monitor.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  /** send to and receive from server */
  private void sendAndReceive(String message) throws IOException {
    System.out.println("SENT\n" + message);
    Monitor.send(opiJovp.listener, message);
    System.out.println("RECEIVED\t" + Monitor.receive(opiJovp.listener) + "\n");
  }

}
