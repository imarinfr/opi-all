package org.lei.opi.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Unitary tests for failures from R OPI protocol
 *
 * @since 0.0.1
 */
public class ClientToMonitor {

  /**
   * Utilities and helpers for client in JSON unitary tests
   *
   * @since 0.0.1
   */
  class Client {

    private static final int PORT = 50008;

    private static Socket client;
    private static CSListener opi;

    /** init OpiManager and connect to it to server */
    Client() throws IOException {
      opi = new CSListener(PORT, new OpiManager());
      client = new Socket(opi.getAddress(), opi.getPort());
    }

    /** load JSON message */
    String loadMessage(String file) throws IOException {
      InputStream inputStream = ConnectionTests.class.getResourceAsStream(file);
      assert inputStream != null;
      return IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));
    }

    /** send JSON message to server */
    static void send(String message) {
      opi.send(message);
    }

    /** receive JSON message from server */
    static String receive() throws IOException {
      while (opi.empty()) Thread.onSpinWait();
      return opi.receive();
    }

    /** close client connection to server */
    void close() throws IOException {
      client.close();
      opi.close();
    }

  }

  /**
   * Drive Display perimeter
   *
   * @since 0.0.1
   */
  @Test
  public void Display() {
    String[] ss = {
        "jsons/Display/opiSetup.json"
    };
    String[] ps = {
        "jsons/Display/opiPresentStatic.json",
        "jsons/ImoVifa/opiPresentDynamic.json"
    };
    serverDriver("jsons/Display/opiChoose.json",
        "jsons/Display/opiInit.json",
        ss, 
        ps);
  }

  /**
   * Drive IMO perimeter
   *
   * @since 0.0.1
   */
  @Test
  public void ImoVifa() {
    String[] ss = {
        "jsons/ImoVifa/opiSetup.json"
    };
    String[] ps = {
        "jsons/ImoVifa/opiPresent.json",
        "jsons/ImoVifa/opiPresent2.json",
        "jsons/ImoVifa/opiPresent3.json",
        "jsons/ImoVifa/opiPresent4.json",
        "jsons/ImoVifa/opiPresent5.json"
    };
    serverDriver("jsons/ImoVifa/opiChoose.json",
        "jsons/ImoVifa/opiInit.json",
        ss, 
        ps);
  }

  /**
   * Drive PhoneHMD perimeter
   *
   * @since 0.0.1
   */
  @Test
  public void phoneHMD() {
    serverDriver("jsons/PhoneHMD/opiChoose.json",
        "jsons/PhoneHMD/opiInit.json",
        "jsons/PhoneHMD/opiSetup.json",
        "jsons/PhoneHMD/opiPresent.json");
  }

  /**
   * Drive PicoVR perimeter
   *
   * @since 0.0.1
   */
  @Test
  public void PicoVR() {
    serverDriver("jsons/PicoVR/opiChoose.json",
        "jsons/PicoVR/opiInit.json",
        "jsons/PicoVR/opiSetup.json",
        "jsons/PicoVR/opiPresent.json");
  }

  /**
   * Drive O900 perimeter
   *
   * @since 0.0.1
   */
  @Test
  public void O900() {
    serverDriver("jsons/O900/opiChoose.json",
        "jsons/O900/opiInit.json",
        "jsons/O900/opiSetup.json",
        "jsons/O900/opiPresent.json");
  }

  /**
   * Drive Compass perimeter
   *
   * @since 0.0.1
   */
  @Test
  public void Compass() {
    serverDriver("jsons/Compass/opiChoose.json",
        "jsons/Compass/opiInit.json",
        "jsons/Compass/opiSetup.json",
        "jsons/Compass/opiPresent.json");
  }

  /** server driver */
  private void serverDriver(String chooseJson, String initJson, String setupJson, String presentJson) {
    try {
      Client client = new Client();
      sendAndReceive(client.loadMessage(chooseJson)); // Choose OPI
      sendAndReceive(client.loadMessage(initJson)); // Initialize OPI
      sendAndReceive(client.loadMessage("jsons/opiQuery.json")); // Query OPI
      sendAndReceive(client.loadMessage(setupJson)); // Setup OPI
      sendAndReceive(client.loadMessage(presentJson)); // Present OPI
      sendAndReceive(client.loadMessage("jsons/opiClose.json")); // Close OPI
      client.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** server driver with lists of present/query etc*/
  private void serverDriver(String chooseJson, String initJson, String[] setupJson, String[] presentJson) {
    try {
      Client client = new Client();
      sendAndReceive(client.loadMessage(chooseJson)); // Choose OPI
      sendAndReceive(client.loadMessage("jsons/opiQuery.json")); // Query OPI
      sendAndReceive(client.loadMessage(initJson)); // Initialize OPI
      for (String s : setupJson) 
        sendAndReceive(client.loadMessage(s)); // Setup OPI
      for (String s : presentJson) 
        sendAndReceive(client.loadMessage(s)); // Present OPI
      sendAndReceive(client.loadMessage("jsons/opiClose.json")); // Close OPI
      client.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** send to and receive from server */
  private void sendAndReceive(String message) throws IOException {
    System.out.println("SENT\n" + message);
    Client.send(message);
    System.out.println("RECEIVED\t" + Client.receive() + "\n");
  }
}
