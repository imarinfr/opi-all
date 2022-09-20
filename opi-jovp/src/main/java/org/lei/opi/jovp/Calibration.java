package org.lei.opi.jovp;

import java.util.Arrays;
import java.util.stream.DoubleStream;
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

  /** Error message: wrong maximum luminance */
  private static final String WRONG_MAX_LUMINANCE = "Maximum luminance for the gamma functions cannot be negative";
  /** Error message: wrong gamma size */
  private static final String WRONG_GAMMA_SIZE = "Wrong length for a gamma function. Lengths for R, G, and B where %s, %s, and %s, respectively";
  /** Error message: unsorted gamma function */
  private static final String UNSORTED_GAMMA_FUNCTION = "The gamma function needs to be sorted";
  /** Error message: wrong gamma value */
  private static final String WRONG_GAMMA_VALUE = "Invalid gamma function. Some values our outside the range [0, 1]";
  /** Error message: color outside range */
  private static final String COLOR_OUTSIDE_RANGE = "Color values outside range [0, 1]. Color was %s";
  /** Error message: color luminance too high */
  private static final String LUMINANCES_OUTSIDE_RANGE = "Requested RGB luminances %s cd/m^2 are too high. Maximum luminances are %s cd/m^2";

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
  public double[] colorValues(double lum, double[] color) throws IllegalArgumentException {
    if (DoubleStream.of(color).anyMatch(val -> val < 0 || val > 1))
      throw new IllegalArgumentException(String.format(COLOR_OUTSIDE_RANGE, Arrays.toString(color)));
    color[0] = lum * color[0];
    color[1] = lum * color[1];
    color[2] = lum * color[2];
    if (color[0] > maxLum[0] || color[1] > maxLum[1] || color[2] > maxLum[2])
      throw new IllegalArgumentException(String.format(LUMINANCES_OUTSIDE_RANGE, Arrays.toString(color), Arrays.toString(maxLum)));
    // TODO: make proper use of max luminance and gamma function: stuff below is incorrect.
    return new double[] {color[0] / maxLum[0], color[1] / maxLum[1], color[2] / maxLum[2], 1};
  }

}