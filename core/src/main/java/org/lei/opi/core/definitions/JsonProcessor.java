package org.lei.opi.core.definitions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Routines to convert from list to arrays
 * 
 * @since 0.0.1
 */
public class JsonProcessor {

  /** {@value COLOR_OUTSIDE_RANGE} */
  private static final String COLOR_OUTSIDE_RANGE = "Color values outside range [0, 1]. Color was %s";

  /**
   * Get an array of integer values from a JSON list
   * 
   * @param list list of integers from JSON
   * 
   * @return an array of integers
   * 
   * @throws ClassCastException Cast exception
   * 
   * @since 0.0.1
   */
  public static int[] toIntArray(Object list) throws ClassCastException {
    return ((ArrayList<?>) list).stream().mapToDouble(Double.class::cast).mapToInt(d -> (int) d).toArray();  
  }


  /**
   * Get an array of String values from a JSON list
   * 
   * @param list list of Strings from JSON
   * 
   * @return an array of Strings
   * 
   * @throws ClassCastException Cast exception
   * 
   * @since 0.0.1
   */
  public static String[] toStringArray(Object list) throws ClassCastException {
    return ((ArrayList<?>) list).stream().map(String.class::cast).map(String::toUpperCase).toArray(String[]::new);
  }

  /**
   * Get an array of double values from a JSON list
   * 
   * @param list list of doubles from JSON
   * 
   * @return an array of doubles
   * 
   * @throws ClassCastException Cast exception
   * 
   * @since 0.0.1
   */
  public static double[] toDoubleArray(Object list) throws ClassCastException {
    return ((ArrayList<?>) list).stream().mapToDouble(Double.class::cast).toArray();
  }

  /**
   * Get an array of 4D-array RGBA colors from a JSON list of lists
   * 
   * @param list list of list of colors from JSON
   * 
   * @return an array or array of RGBA colors
   * 
   * @throws ClassCastException Cast exception
   * 
   * @since 0.0.1
   */
  public static double[][] toColorArray(Object list) throws ClassCastException {
    return ((ArrayList<?>) list).stream().map(l -> toDoubleArray(l))
                                         .map(l -> (new double[] {l[0], l[1], l[2], 1}))
                                         .toArray(double[][]::new);
  }

  /**
   * Get an array of enums from a JSON list
   * 
   * @param list list of strings from JSON
   * 
   * @return an array of enums. Need to be recast
   * 
   * @throws ClassCastException Cast exception
   * 
   * @since 0.0.1
   */
  public static Object[] toEnumArray(Object list, Type enumType) throws ClassCastException {
    return ((ArrayList<?>) list).stream().map(String.class::cast).map(String::toUpperCase).map(enumType.getClass()::cast).toArray();
  }

  /**
   * Obtain color values in cd/m2 from luminance and color mixture.
   * Color values in cd/m^2 will be converted to pixel levels based
   * on calibration in JOVP
   *
   * @param lum the luminance in cd/m^2
   * @param color The color mixture
   * 
   * @return the device-independent color values in cd/m^2
   * 
   * @throws IllegalArgumentException If any value is bad
   * 
   * @since 0.0.1
   */
  public static double[] colorValues(double lum, double[] color) throws IllegalArgumentException {
    if (DoubleStream.of(color).anyMatch(val -> val < 0 || val > 1))
      throw new IllegalArgumentException(String.format(COLOR_OUTSIDE_RANGE, Arrays.toString(color)));
    return new double[] {lum * color[0], lum * color[1], lum * color[2], 1};
  }

}
