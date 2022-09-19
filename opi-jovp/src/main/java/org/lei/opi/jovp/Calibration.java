package org.lei.opi.jovp;

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
public record Calibration(double maxLum, int depth, double[] gamma) {

  /** Error message: wrong maximum luminance */
  private static final String WRONG_MAX_LUMINANCE = "Wrong maximum luminance for the gamma function. It is %s";
  /** Error message: wrong gamma size */
  private static final String WRONG_GAMMA_SIZE = "Wrong size for the gamma function. Size is %s";
  /** Error message: unsorted gamma function */
  private static final String UNSORTED_GAMMA_FUNCTION = "The gamma function needs to be sorted";
  /** Error message: wrong gamma value */
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
  public Calibration(double maxLum, int depth, double[] gamma) {
    if (maxLum < 0)
      throw new IllegalArgumentException(String.format(WRONG_MAX_LUMINANCE, maxLum));
    if (gamma.length != (int) Math.pow(2, depth))
      throw new IllegalArgumentException(String.format(WRONG_GAMMA_SIZE, gamma.length));
    //if ()
    if (IntStream.range(1, gamma.length).anyMatch(i -> gamma[i - 1] > gamma[i]))
    throw new IllegalArgumentException(UNSORTED_GAMMA_FUNCTION);
    if (gamma[0] < 0 || gamma[gamma.length] > 1)
      throw new IllegalArgumentException(WRONG_GAMMA_VALUE);
    this.maxLum = maxLum;
    this.depth = depth;
    this.gamma = gamma;
  }
}
