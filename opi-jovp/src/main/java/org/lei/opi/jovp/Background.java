package org.lei.opi.jovp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.DoubleStream;

import es.optocom.jovp.structures.ModelType;

/**
 * Background and fixation target settings
 * 
 * @param bgCol background RGBA color where each channel range is from 0 to max luminance
 * @param fixShape fixation type
 * @param fixCol fixation RGBA color where each channel range is from 0 to max luminance
 * @param fixCx x center of the fixation target in degrees of visual angle
 * @param fixCy y center of the fixation target in degrees of visual angle
 * @param fixSx mayor axis size of the fixation target in degrees of visual angle
 * @param fixSy minor axis size of the fixation target in degrees of visual angle
 * @param fixRotation rotation of the fixation target in degrees
 *
 * @since 0.0.1
 */
public record Background(double[] bgCol, ModelType fixShape, double[] fixCol,
                         double fixCx, double fixCy, double fixSx, double fixSy,
                         double fixRotation) {

  private static final String LUMINANCE_TOO_HIGH = "Requested luminance %s cd/m2 is too high. Maximum luminance is %s";
  private static final String COLOR_OUTSIDE_RANGE = "Color values outside range [0, 1]. Color was %s";
  /**
   * Set background record 
   * 
   * @param args pairs of argument name and value
   * 
   * @return a background record
   * 
   * @throws ClassCastException Cast exception
   * @throws IllegalArgumentException If any value is bad
   * 
   * @since 0.0.1
   */
  static Background set(HashMap<String, Object> args, Calibration[] calibration) throws ClassCastException, IllegalArgumentException {
    double bgLum = (double) args.get("bgLum");
    double fixLum = (double) args.get("fixLum");
    double[] bgCol = ((ArrayList<?>) (args.get("bgCol"))).stream().mapToDouble(Double.class::cast).toArray();
    double[] fixCol = ((ArrayList<?>) (args.get("fixCol"))).stream().mapToDouble(Double.class::cast).toArray();
    return new Background(getRGBA(bgLum, bgCol, calibration),
                          ModelType.valueOf(((String) args.get("fixShape")).toUpperCase()),
                          getRGBA(fixLum, fixCol, calibration),
                          (double) args.get("fixCx"), (double) args.get("fixCy"),
                          (double) args.get("fixSx"), (double) args.get("fixSy"),
                          (double) args.get("fixRotation"));
  }

  /**
   * Obtain pixel level from luminance in cd/m2 from gamma function
   *
   * @param lum the luminance in cd/m2
   * @param gamma the gamma function
   * 
   * @return the device-dependent pixel level between 0 and 1
   * 
   * @throws IllegalArgumentException If any value is bad
   * 
   * @since 0.0.1
   */
  private static double[] getRGBA(double lum, double[] color, Calibration[] calibration) throws IllegalArgumentException {
    if (lum > calibration[0].maxLum())
      throw new IllegalArgumentException(String.format(LUMINANCE_TOO_HIGH, lum, calibration[0].maxLum()));
    if (DoubleStream.of(color).anyMatch(val -> val < 0 || val > 1))
      throw new IllegalArgumentException(String.format(COLOR_OUTSIDE_RANGE, Arrays.toString(color)));
    // TODO: make proper use of max luminance and gamma function: stuff below is incorrect.
    return new double[] {
      getRelativeColorValue(lum * color[0], calibration[0].maxLum(), calibration[0].gamma()),
      getRelativeColorValue(lum * color[1], calibration[1].maxLum(), calibration[1].gamma()),
      getRelativeColorValue(lum * color[2], calibration[2].maxLum(), calibration[2].gamma()),
      1}; // alpha channel
  }

  /**
   * Computes the relative color value for a channel 
   *
   * @param lum the luminance for the channel in cd/m2
   * @param maxLum the maximum luminance for the channel in cd/m2
   * @param gamma the gamma function for that channel
   * 
   * @return the relative channel value
   * 
   * @since 0.0.1
   */
  private static double getRelativeColorValue(double lum, double maxLum, double[] gamma) {
    return lum / maxLum;
  }

}
