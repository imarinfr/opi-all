package org.lei.opi.core;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 *
 * Unitary tests for socket connections
 *
 * @since 0.0.1
 */
public class PerimeterTest {

  /** JOVP server port */
  private static final int PERIMETER_PORT = 51234;
  /** JOVP monitor port */
  private static final int MONITOR_PORT = 50001;

  /** The JOVP server */
  private Perimeter server;
  /** The OPI monitor */
  private Core monitor;
  /** The R client */
  private RClient r;

  /** OPI_CHOOSE command */
  private String chooseJson;
  /** OPI_INITIALIZE command */
  private String initJson;
  /** OPI_SETUP command */
  private String[] setupJson;
  /** OPI_PRESENT command */
  private String[] presentJson;

  /**
   *
   * Test O900
   *
   * @since 0.0.1
   */
  @Test
  public void o900() {
    setupConnections(Perimeter.Machine.O900);
    setupCommands(Perimeter.Machine.O900);
    clientDriver();
    closeConnections();
  }

  /**
   *
   * Test Compass
   *
   * @since 0.0.1
   */
  @Test
  public void Compass() {
    setupConnections(Perimeter.Machine.COMPASS);
    setupCommands(Perimeter.Machine.COMPASS);
    clientDriver();
    closeConnections();
  }

    /**
   *
   * Test Maia
   *
   * @since 0.0.1
   */
  @Test
  public void Maia() {
    setupConnections(Perimeter.Machine.MAIA);
    setupCommands(Perimeter.Machine.MAIA);
    clientDriver();
    closeConnections();
  }

  /** setup connections */
  private void setupConnections(Perimeter.Machine machine) {
    try {
      server = new Perimeter(machine, PERIMETER_PORT); // first setup JOVP server
      monitor = new Core(MONITOR_PORT); // then setup monitor
      r = new RClient(monitor.getIP(), monitor.getPort()); // finally setup R client
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** setup commands */
  private void setupCommands(Perimeter.Machine machine) {
    switch(machine) {
      case O900 -> {
        chooseJson = "jsons/O900/opiChoose.json";
        initJson = "jsons/O900/opiInit.json";
        setupJson = new String[] {
          "jsons/O900/opiSetup.json"
        };
        presentJson = new String[] {
          "jsons/O900/opiPresentStatic.json",
          "jsons/O900/opiPresentKinetic.json"
        };
      }
      case COMPASS -> {
        chooseJson = "jsons/Compass/opiChoose.json";
        initJson = "jsons/Compass/opiInit.json";
        setupJson = new String[] {
          "jsons/Compass/opiSetup.json"
        };
        presentJson = new String[] {
          "jsons/Compass/opiPresent.json",
        };
      }
      case MAIA -> {
        chooseJson = "jsons/Maia/opiChoose.json";
        initJson = "jsons/Maia/opiInit.json";
        setupJson = new String[] {
          "jsons/Maia/opiSetup.json"
        };
        presentJson = new String[] {
          "jsons/Maia/opiPresent.json",
        };
      }
    }
  }

  /** close connections */
  private void closeConnections() {
    try {
      monitor.close();
      r.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** execute commands from driver */
  private void clientDriver() {
    try {
      executeCommands();
      server.close();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }  
  }

  /** server driver with lists of present/query etc*/
  private void executeCommands() throws IOException, InterruptedException {
    sendAndReceive(RClient.loadMessage(chooseJson)); // Choose OPI
    System.out.println("OPI QUERY before OPI INITIALIZE");
    sendAndReceive(RClient.loadMessage("jsons/opiQuery.json")); // Query OPI
    sendAndReceive(RClient.loadMessage(initJson)); // Initialize OPI
    System.out.println("OPI QUERY after OPI INITIALIZE");
    sendAndReceive(RClient.loadMessage("jsons/opiQuery.json")); // Query OPI
    for (String s : setupJson) {
      sendAndReceive(RClient.loadMessage(s)); // Setup OPI
    }
    for (String s : presentJson) { // Present OPI
      sendAndReceive(RClient.loadMessage(s));
    } // Present OPI
    sendAndReceive(RClient.loadMessage("jsons/opiClose.json")); // Close OPI  
  }

  /** R sends to and receives from monitor */
  private void sendAndReceive(String message) throws IOException {
    System.out.println("R SENDS\n" + message);
    r.send(message);
    while (r.empty()) Thread.onSpinWait();
    System.out.println("R RECEIVES\n" + r.receive());
  }

}