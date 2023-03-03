package org.lei.opi.jovp;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.lei.opi.jovp.Configuration.Machine;
import org.lei.opi.core.OpiListener;

/**
 *
 * Integrated tests for connection in series from client to OPI JOVP
 *
 * @since 0.0.1
 */
public class RToMonitorToOutsideMachine {

  /** JOVP monitor port */
  private static final int MONITOR_PORT = 50001;

  /** The OPI monitor */
  private Core monitor;
  /** The R client */
  private OpiListener r;

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
  public void toImovifa() {
    setupConnections(Machine.IMOVIFA);
    setupCommands(Machine.IMOVIFA);
    executeCommands();
    closeConnections();
  }

  /** setup connections */
  private void setupConnections(Machine machine) {
    try {
      monitor = new Core(MONITOR_PORT); // then setup monitor
      r = new OpiListener(monitor.getPort(), null); // finally setup R client
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** setup commands */
  private void setupCommands(Machine machine) {
    switch(machine) {
      case DISPLAY -> {
        chooseJson = "Display/opiChoose.json";
        initJson = "Display/opiInit.json";
        setupJson = new String[] {
          "Display/opiSetup.json"
        };
        presentJson = new String[] {
          "Display/opiPresentStatic1.json",
          "Display/opiPresentStatic2.json",
          "Display/opiPresentStatic3.json",
          "Display/opiPresentStatic1.json",
          "Display/opiPresentStatic2.json",
          "Display/opiPresentStatic3.json",
          "Display/opiPresentDynamic1.json",
          "Display/opiPresentDynamic2.json"
        };
      }
      case IMOVIFA -> {
        chooseJson = "ImoVifa/opiChoose.json";
        initJson = "ImoVifa/opiInit.json";
        setupJson = new String[] {
          "ImoVifa/opiSetup.json"
        };
        presentJson = new String[] {
          "ImoVifa/opiPresent.json",
          "ImoVifa/opiPresent2.json",
          "ImoVifa/opiPresent3.json",
          "ImoVifa/opiPresent4.json",
          "ImoVifa/opiPresent5.json"
        };
      }
      case PICOVR -> {
        chooseJson = "PicoVR/opiChoose.json";
        initJson = "PicoVR/opiInit.json";
        setupJson = new String[] {
          "PicoVR/opiSetup.json"
        };
        presentJson = new String[] {
          "PicoVR/opiPresent.json",
        };
      }
      case PHONEHMD -> {
        chooseJson = "PhoneHMD/opiChoose.json";
        initJson = "PhoneHMD/opiInit.json";
        setupJson = new String[] {
          "PhoneHMD/opiSetup.json"
        };
        presentJson = new String[] {
          "PhoneHMD/opiPresent.json",
        };
      }
    }
  }

  /** close connections */
  private void closeConnections() {
    try {
      monitor.close();
      r.closeListener();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** server driver with lists of present/query etc*/
  private void executeCommands() {
    try {
      sendAndReceive(chooseJson);
      System.out.println("OPI QUERY before OPI INITIALIZE");
      sendAndReceive("opiQuery.json"); // Query OPI
      sendAndReceive(initJson); // Initialize OPI
      Thread.sleep(2000);
      System.out.println("OPI QUERY after OPI INITIALIZE");
      sendAndReceive("opiQuery.json"); // Query OPI
      for (String s : setupJson) {
        sendAndReceive(s); // Setup OPI
        Thread.sleep(2000);
      }
      for (String s : presentJson) { // Present OPI
        sendAndReceive(s);
        Thread.sleep(500);
      } // Present OPI
      sendAndReceive("opiClose.json"); // Close OPI  
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  /** R sends to and receives from monitor */
  private void sendAndReceive(String message) throws IOException {
    System.out.println("R SENDS\n" + message);
    r.send(message);
    while (r.empty()) Thread.onSpinWait();
    System.out.println("R RECEIVES\n" + r.receive());
  }

}
