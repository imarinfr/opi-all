package org.lei.opi.jovp;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Calibration data
 * 
 * @param maxLum maximum luminance in cd/m2
 * @param depth channel depth in bits
 * @param gamma the gamma function from 0 to 1
 *
 * @since 0.0.1
 */
public record Calibration(double[] maxLum, int[] depth, double[][] gamma) {

  /** {@value WRONG_MAX_LUMINANCE} */
  private static final String WRONG_MAX_LUMINANCE = "Maximum luminance for the gamma functions cannot be negative";
  /** {@value WRONG_GAMMA_SIZE} */
  private static final String WRONG_GAMMA_SIZE = "Wrong length for a gamma function. Lengths for R, G, and B where %s, %s, and %s, respectively";
  /** {@value UNSORTED_GAMMA_FUNCTION} */
  private static final String UNSORTED_GAMMA_FUNCTION = "The gamma function needs to be sorted";
  /** {@value WRONG_GAMMA_VALUE} */
  private static final String WRONG_GAMMA_VALUE = "Invalid gamma function. Some values our outside the range [0, 1]";

  /**
   * Calibration data
   * 
   * @param maxLum maximum luminance in cd/m2
   * @param depth channel depth in bits
   * @param gamma the gamma function from 0 to 1
   *
   * @since 0.0.1
   */
  public static Calibration set(double RmaxLum, int Rdepth, double[] Rgamma,
                     double GmaxLum, int Gdepth, double[] Ggamma,
                     double BmaxLum, int Bdepth, double[] Bgamma) {
    if (RmaxLum < 0 || GmaxLum < 0 || BmaxLum < 0)
      throw new IllegalArgumentException(String.format(WRONG_MAX_LUMINANCE));
    if (Rgamma.length != (int) Math.pow(2, Rdepth) ||
        Ggamma.length != (int) Math.pow(2, Gdepth) ||
        Bgamma.length != (int) Math.pow(2, Bdepth))
      throw new IllegalArgumentException(String.format(WRONG_GAMMA_SIZE, Rgamma.length, Ggamma.length, Bgamma.length));
    if (IntStream.range(1, Rgamma.length).anyMatch(i -> Rgamma[i - 1] > Rgamma[i]) ||
        IntStream.range(1, Ggamma.length).anyMatch(i -> Ggamma[i - 1] > Ggamma[i]) ||
        IntStream.range(1, Bgamma.length).anyMatch(i -> Bgamma[i - 1] > Bgamma[i]))
      throw new IllegalArgumentException(UNSORTED_GAMMA_FUNCTION);
    if (Rgamma[0] < 0 || Rgamma[Rgamma.length - 1] > 1 ||
        Ggamma[0] < 0 || Ggamma[Ggamma.length - 1] > 1 ||
        Bgamma[0] < 0 || Bgamma[Bgamma.length - 1] > 1)
      throw new IllegalArgumentException(WRONG_GAMMA_VALUE);
    return new Calibration(new double[] {RmaxLum, GmaxLum, BmaxLum},
                           new int[] {Rdepth, Gdepth, Bdepth}, 
                           new double[][] {Rgamma, Ggamma, Bgamma});
  }

  /**
   * Obtain pixel level from luminance in cd/m^2 from gamma function
   *
   * @param color The RGB color value in cd/m^2
   * 
   * @return the device-dependent pixel level between 0 and 1
   * 
   * @throws IllegalArgumentException If any value is bad
   * 
   * @since 0.0.1
   */
  public double[] colorValues(double[] color) {
    color[0] = color[0] < 0 ? 0 : (color[0] > maxLum[0] ? 1 : color[0] / maxLum[0]);
    color[1] = color[1] < 0 ? 0 : (color[1] > maxLum[1] ? 1 : color[1] / maxLum[1]);
    color[2] = color[2] < 0 ? 0 : (color[2] > maxLum[2] ? 1 : color[2] / maxLum[2]);
    // TODO: make proper use of max luminance and gamma function: stuff below is incorrect.
    return new double[] {color[0], color[1], color[2], 1};
  }

}