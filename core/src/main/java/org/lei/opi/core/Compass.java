package org.lei.opi.core;

/**
 * Compass client
 *
 * @since 0.0.1
 */
public class Compass extends Icare {
    private final double MIN_X = -30;
    private final double MAX_X = 30;
    private final double MIN_Y = -30;
    private final double MAX_Y = 30;
    private final double MIN_PRESENTATION_TIME = 200;
    private final double MAX_PRESENTATION_TIME = 200;
    private final double MIN_RESPONSE_WINDOW = 0;
    private final double MAX_RESPONSE_WINDOW = 2680;
    private final double BACKGROUND_LUMINANCE = 31.4 / Math.PI;
    private final double MIN_LUMINANCE = BACKGROUND_LUMINANCE;
    private final double MAX_LUMINANCE = 10000.0 / Math.PI;
    private final boolean TRACKING = true;  

  /**
   * Compass client: fill constants.
   *
   * @since 0.0.1
   */
  public Compass() { super(); }
 
  public Compass(boolean noSocket) { super(noSocket); } 
}