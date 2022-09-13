package org.lei.opi.jovp;

import es.optocom.jovp.*;
import es.optocom.jovp.structures.Paradigm;

/**
 *
 * OPI IMO driver
 *
 * @since 0.0.1
 */
public class Driver {

  /** Paradigm is a click response for seen or not seen */
  private static final Paradigm PARADIGM = Paradigm.CLICKER;
  /** Debugging parameter: whether to enable Vulkan validation layers */
  private static final boolean VALIDATION_LAYERS = PsychoEngine.VALIDATION_LAYERS;
  /** Debugging parameter: whether to dump Vulkan API feedback */
  private static final boolean API_DUMP = false;

  /** The PsychoEngine */
  private PsychoEngine psychoEngine;

  /**
   *
   * ready the OPI JOVP driver
   *
   * @since 0.0.1
   */
  public void ready(Configuration.Impl impl) {
    Configuration config = new Configuration();
    psychoEngine = new PsychoEngine(new Logic(), config.distance, config.viewMode, config.input,
                                    PARADIGM, VALIDATION_LAYERS, API_DUMP);
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
