package org.lei.opi.jovp;

import es.optocom.jovp.Monitor;
import es.optocom.jovp.definitions.ViewMode;

/**
 * Device settings to return upon query from OPI monitor
 *
 * @param distance viewing distance
 * @param fov current horizontal and vertical field of view in degrees of visual angle
 * @param viewMode viewing mode: MONO or STEREO
 * @param input Either 'mouse', 'keypad', or the name of a suitable USB controller
 * @param pseudoGray whether JOVP is using a bit-stealing algorithm
 * @param fullScreen whether JOVP machine is running on full screen mode
 * @param tracking  whether JOVP machine is able to do eye tracking
 * @param maxLum maximum luminance on the R, G, and B channels (cd/m^2)
 * @param maxPixel maximum pixel (eg 255, 1024)
 * @param lumPrecision Number of decimal places for luminance in cd/m^2 in inversegamma function
 * @param invGammaFile the path of the inverse gamma file
 * @param monitor display of the OPI JOVP machine
 * @param webcam String representation of WebCamConfiguration
 * @param leftEyex x coordinate of left eye (degrees from image centre)
 * @param leftEyey y coordinate of left eye (degrees from image centre)
 * @param leftEyed diameter of left eye (mm)
 * @param rightEyex x coordinate of right eye (degrees from image centre)
 * @param rightEyey y coordinate of right eye (degrees from image centre)
 * @param rightEyed diameter of right eye (mm)
 * @since 0.0.1
 */
public record Query(int distance, float[] fov, ViewMode viewMode, String input, boolean pseudoGray,
                    boolean fullScreen, boolean tracking, double maxLum, int maxPixel, double lumPrecision, String invGammaFile, Monitor monitor,
                    String webcam,
                    double leftEyex, double leftEyey, double leftEyed, double rightEyex, double rightEyey, double rightEyed) {

}