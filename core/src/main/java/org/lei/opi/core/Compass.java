package org.lei.opi.core;

/**
 * Compass client
 *
 * @since 0.0.1
 */
public class Compass extends Icare {

  /**
   * Compass client: fill constants.
   *
   * @since 0.0.1
   */
  public Compass() {
    super();
    MIN_X = -30;
    MAX_X = 30;
    MIN_Y = -30;
    MAX_Y = 30;
    MIN_PRESENTATION_TIME = 200;
    MAX_PRESENTATION_TIME = 200;
    MIN_RESPONSE_WINDOW = 0;
    MAX_RESPONSE_WINDOW = 2680;
    BACKGROUND_LUMINANCE = 31.4 / Math.PI;
    MIN_LUMINANCE = BACKGROUND_LUMINANCE;
    MAX_LUMINANCE = 10000.0 / Math.PI;
    TRACKING = true;  
  }
 
  public Compass(boolean noSocket) {
    this();
  }
}