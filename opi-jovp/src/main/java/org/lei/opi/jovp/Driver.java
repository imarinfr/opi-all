package org.lei.opi.jovp;

import es.optocom.jovp.*;

/**
 *
 * OPI IMO driver
 *
 * @since 0.0.1
 */
public class Driver {

  /** The PsychoEngine */
  private PsychoEngine psychoEngine;

  /**
   *
   * ready the OPI JOVP driver
   *
   * @since 0.0.1
   */
  public void ready(Configuration.Impl impl) {
    Configuration config = new Configuration(impl);
    psychoEngine = new PsychoEngine(new Logic(), config.distance, config.viewMode, config.input,
                                    Configuration.PARADIGM, Configuration.VALIDATION_LAYERS, Configuration.API_DUMP);
    psychoEngine.setWindowMonitor(config.screen);
    if(config.fullScreen) psychoEngine.setFullScreen();
  }

  /**
   *
   * Start the OPI JOVP driver
   *
   * @since 0.0.1
   */
  public void start() {
    psychoEngine.start();
  }

  /**
   *
   * Close the OPI JOVP driver
   *
   * @since 0.0.1
   */
  public void cleanup() {
    psychoEngine.cleanup();
  }

}
