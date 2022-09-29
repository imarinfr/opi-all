package org.lei.opi.jovp;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.lei.opi.jovp.Settings.Machine;

/**
 *
 * Unitary tests for connection in series from client to OPI JOVP
 *
 * @since 0.0.1
 */
public class RToMonitorToJovp {

  /** JOVP server port */
  private static final int JOVP_PORT = 51234;
  /** JOVP monitor port */
  private static final int MONITOR_PORT = 50001;

  /** The JOVP server */
  private JovpServer server;
  /** The OPI monitor */
  private Monitor monitor;
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
   * Monitor controlling Display on monoscopic view
   *
   * @since 0.0.1
   */
  @Test
  public void monitorDisplayMono() {
    setupConnections(Machine.DISPLAY_MONO);
    setupCommands(Machine.DISPLAY_MONO);
    clientDriver();
    server.start();
    closeConnections();
  }

  /**
   * Monitor controlling Display on stereoscopic view
   *
   * @since 0.0.1
   */
  @Test
  public void monitorDisplayStereo() {
    setupConnections(Machine.DISPLAY_STEREO);
    setupCommands(Machine.DISPLAY_STEREO);
    clientDriver();
    server.start();
    closeConnections();
  }

  /** setup connections */
  private void setupConnections(Machine machine) {
    try {
      server = new JovpServer(machine, JOVP_PORT); // first setup JOVP server
      monitor = new Monitor(MONITOR_PORT); // then setup monitor
      r = new RClient(monitor.getIP(), monitor.getPort()); // finally setup R client
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** setup commands */
  private void setupCommands(Machine machine) {
    switch(machine) {
      case DISPLAY_MONO, DISPLAY_STEREO -> {
        chooseJson = "jsons/Display/opiChoose.json";
        initJson = "jsons/Display/opiInit.json";
        setupJson = new String[] {
          "jsons/Display/opiSetup.json"
        };
        presentJson = new String[] {
          "jsons/Display/opiPresentStatic.json",
          //"jsons/Display/opiPresentDynamic.json"
        };
      }
      case IMOVIFA -> {
        chooseJson = "jsons/ImoVifa/opiChoose.json";
        initJson = "jsons/ImoVifa/opiInit.json";
        setupJson = new String[] {
          "jsons/ImoVifa/opiSetup.json"
        };
        presentJson = new String[] {
          "jsons/ImoVifa/opiPresent.json",
          "jsons/ImoVifa/opiPresent2.json",
          "jsons/ImoVifa/opiPresent3.json",
          "jsons/ImoVifa/opiPresent4.json",
          "jsons/ImoVifa/opiPresent5.json"
        };
      }
      case PICOVR -> {
        chooseJson = "jsons/PicoVR/opiChoose.json";
        initJson = "jsons/PicoVR/opiInit.json";
        setupJson = new String[] {
          "jsons/PicoVR/opiSetup.json"
        };
        presentJson = new String[] {
          "jsons/PicoVR/opiPresent.json",
        };
      }
      case PHONEHMD -> {
        chooseJson = "jsons/PhoneHMD/opiChoose.json";
        initJson = "jsons/PhoneHMD/opiInit.json";
        setupJson = new String[] {
          "jsons/PhoneHMD/opiSetup.json"
        };
        presentJson = new String[] {
          "jsons/PhoneHMD/opiPresent.json",
        };
      }
    }
  }

  /** setup connections */
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
    new Thread() {
      public void run() {
        try {
          Thread.sleep(500); // need to wait for PsychoEngine to start
          executeCommands();
          server.close();
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }.start();
  }

  /** server driver with lists of present/query etc*/
  private void executeCommands() throws IOException, InterruptedException {
    sendAndReceive(RClient.loadMessage(chooseJson)); // Choose OPI
    System.out.println("OPI QUERY before OPI INITIALIZE");
    sendAndReceive(RClient.loadMessage("jsons/opiQuery.json")); // Query OPI
    sendAndReceive(RClient.loadMessage(initJson)); // Initialize OPI
    Thread.sleep(2000);
    System.out.println("OPI QUERY after OPI INITIALIZE");
    sendAndReceive(RClient.loadMessage("jsons/opiQuery.json")); // Query OPI
    for (String s : setupJson) {
      sendAndReceive(RClient.loadMessage(s)); // Setup OPI
      Thread.sleep(2000);
    }
    for (String s : presentJson) sendAndReceive(RClient.loadMessage(s)); // Present OPI
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
