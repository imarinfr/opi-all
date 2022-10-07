package org.lei.opi.core;

/**
 * Maia client
 *
 * @since 0.0.1
 */
public class Maia extends Icare {
    final double MIN_X = -20;
    final double MAX_X = 20;
    final double MIN_Y = -20;
    final double MAX_Y = 20;
    final double MIN_PRESENTATION_TIME = 200;
    final double MAX_PRESENTATION_TIME = 200;
    final double MIN_RESPONSE_WINDOW = 0;
    final double MAX_RESPONSE_WINDOW = 2680;
    final double BACKGROUND_LUMINANCE = 4.0 / Math.PI;
    final double MIN_LUMINANCE = BACKGROUND_LUMINANCE;
    final double MAX_LUMINANCE = 1000.0 / Math.PI;
    final boolean TRACKING = true;
    /**
     * Maia client: fill constants.
     *
     * @since 0.0.1
     */
    public Maia() { super(); }

    public Maia(boolean noSocket) { super(noSocket); }
}