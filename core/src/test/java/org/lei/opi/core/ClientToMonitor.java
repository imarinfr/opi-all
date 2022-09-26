package org.lei.opi.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Unitary tests for failures from R OPI protocol
 *
 * @since 0.0.1
 */
public class ClientToMonitor {

  /** Communication port */
  private static final int PORT = 50008;

  /**
   * Utilities and helpers for client in JSON unitary tests
   *
   * @since 0.0.1
   */
  class Client {

    /** CSWriter to act as R client */
    private static CSWriter r;

    /** init socket and connect to to monitor */
    Client(String ip, int port) throws IOException {
      // use CSWriter as support
      r = new CSWriter(ip, port);
    }

    /** load JSON message as client */
    static String loadMessage(String file) throws IOException {
      InputStream inputStream = ClientToMonitor.class.getResourceAsStream(file);
      return IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));
    }

    /** send JSON message to monitor */
    static void send(String message) throws IOException {
      r.send(message);
    }

    /** check if something has been received */
    static boolean empty() throws IOException {
      return r.empty();
    }

    /** receive JSON message from monitor */
    static String receive() throws IOException {
      return r.receive();
    }

    /** close client socket */
    void close() throws IOException {
      r.close();
    }

  }

  /**
   * Drive Display perimeter
   *
   * @since 0.0.1
   */
  @Test
  public void Display() {
    String[] setup = {
        "jsons/Display/opiSetup.json"
    };
    String[] present = {
        "jsons/Display/opiPresentStatic.json",
        "jsons/ImoVifa/opiPresentDynamic.json"
    };
    RClientToMonitor("jsons/Display/opiChoose.json",
        "jsons/Display/opiInit.json", setup, present);
  }

  /**
   * Drive IMO perimeter
   *
   * @since 0.0.1
   */
  @Test
  public void ImoVifa() {
    String[] setup = {
        "jsons/ImoVifa/opiSetup.json"
    };
    String[] present = {
        "jsons/ImoVifa/opiPresent.json",
        "jsons/ImoVifa/opiPresent2.json",
        "jsons/ImoVifa/opiPresent3.json",
        "jsons/ImoVifa/opiPresent4.json",
        "jsons/ImoVifa/opiPresent5.json"
    };
    RClientToMonitor("jsons/ImoVifa/opiChoose.json",
        "jsons/ImoVifa/opiInit.json", setup, present);
  }

  /** server driver with lists of present/query etc*/
  private void RClientToMonitor(String chooseJson, String initJson, String[] setupJson, String[] presentJson) {
    try {
      CSListener monitor = new CSListener(PORT, new OpiManager());
      Client r = new Client(monitor.getIP(), monitor.getPort());
      sendAndReceive(Client.loadMessage(chooseJson)); // Choose OPI
      sendAndReceive(Client.loadMessage(initJson)); // Initialize OPI
      sendAndReceive(Client.loadMessage("jsons/opiQuery.json")); // Query OPI
      for (String s : setupJson) sendAndReceive(Client.loadMessage(s)); // Setup OPI
      for (String s : presentJson) sendAndReceive(Client.loadMessage(s)); // Present OPI
      sendAndReceive(Client.loadMessage("jsons/opiClose.json")); // Close OPI
      r.close();
      monitor.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** send to and receive from server */
  private void sendAndReceive(String message) throws IOException {
    Client.send(message);
    System.out.println("SENT\n" + message);
    while(Client.empty()) Thread.onSpinWait();
    System.out.println("RECEIVED\t" + Client.receive() + "\n");
  }
}
