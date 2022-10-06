package org.lei.opi.core;

/**
 * Maia client
 *
 * @since 0.0.1
 */
public class Maia extends Icare {
    /**
     * Maia client: fill constants.
     *
     * @since 0.0.1
     */
    public Maia() {
        super();
        MIN_X = -20;
        MAX_X = 20;
        MIN_Y = -20;
        MAX_Y = 20;
        MIN_PRESENTATION_TIME = 200;
        MAX_PRESENTATION_TIME = 200;
        MIN_RESPONSE_WINDOW = 0;
        MAX_RESPONSE_WINDOW = 2680;
        BACKGROUND_LUMINANCE = 4.0 / Math.PI;
        MIN_LUMINANCE = BACKGROUND_LUMINANCE;
        MAX_LUMINANCE = 1000.0 / Math.PI;
        TRACKING = true;
    }

    public Maia(boolean noSocket) {
        this();
    }
}