package org.lei.opi.jovp;

import java.io.IOException;

import org.lei.opi.core.CSListener;

import es.optocom.jovp.PsychoEngine;

/**
 * OPI JOVP manager
 *
 * @since 0.0.1
 */
public class OpiJovp {

  /** OpiJovp settings */
  Settings settings;
  /** The driver */
  OpiDriver driver;
  /** The listener */
  CSListener listener;
  /** The PsychoEngine */
  PsychoEngine psychoEngine;

  /**
   * Start the OPI JOVP driver with default settings settings
   *
   * @param machine the OPI JOVP machine
   *
   * @throws IOException
   * 
   * @since 0.0.1
   */
  OpiJovp(Settings.Machine machine) {
    try {
      settings = Settings.defaultSettings(machine);
      driver = new OpiDriver(settings);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Start the OPI JOVP comms and driver from a record of settings
   * 
   * @param machine the OPI JOVP machine
   * @param file the file path and name
   *
   * @since 0.0.1
   */
  OpiJovp(Settings.Machine machine, String file) {
    try {
      settings = Settings.load(machine, file);
      driver = new OpiDriver(settings);
    } catch (IOException | IllegalArgumentException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Open listener
   * 
   * @param port listening port
   *
   * @since 0.0.1
   */
  public CSListener open(int port) {
    listener = new CSListener(port, driver);
    return listener;
  }

  /**
   * Close listener
   *
   * @since 0.0.1
   */
  public void close() {
    listener.close();
    listener = null;
  }

  /**
   * Initialize the psychoEngine
   *
   * @since 0.1.0
   */
  public void start() {
    psychoEngine = new PsychoEngine(new OpiLogic(driver), settings.distance(), settings.viewMode(), settings.input(),
                                    Settings.PARADIGM, Settings.VALIDATION_LAYERS, Settings.API_DUMP);
    psychoEngine.setWindowMonitor(settings.screen());
    if (settings.fullScreen()) psychoEngine.setFullScreen();
    psychoEngine.hide();
    psychoEngine.start();
    psychoEngine.cleanup();
    psychoEngine = null;
  }

}