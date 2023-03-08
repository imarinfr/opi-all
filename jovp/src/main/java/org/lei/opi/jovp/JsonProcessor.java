package org.lei.opi.jovp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Routines to convert from list to arrays
 * 
 * @since 0.0.1
 */
public class JsonProcessor {

  /** {@value CANNOT_PARSE_STRING} */
  private static final String CANNOT_PARSE_STRING = "Cannot parse the String to an Enum";

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
   * @throws SecurityException
   * @throws NoSuchMethodException
   * 
   * @throws ClassCastException Cast exception
   * 
   * @since 0.0.1
   */
  public static Stream<Object> toObjectStream(Object list, Class<? extends Enum<?>> enumClass) throws NoSuchMethodException, SecurityException {
    Method method = enumClass.getMethod("valueOf", String.class);
    return ((ArrayList<?>) list).stream().map(String.class::cast).map(str -> mapper(method, str));
  }

  /**
   * Map String to an Enum
   *
   * @param method The mapping method ('valueOf()' the 'Enum')
   * @param str String to map to 'Enum'
   * 
   * @return Enum
   * 
   * @since 0.0.1
   */
  private static Enum<?> mapper(Method method, String str) {
    try {
      return (Enum<?>) method.invoke(JsonProcessor.class, str.toUpperCase());
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(CANNOT_PARSE_STRING, e);
    }
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
   * @since 0.0.1
   */
  public static double[] colorValues(double lum, double[] color) {
    return new double[] {lum * color[0], lum * color[1], lum * color[2], 1};
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
  public static double[][] colorValues(double[] lum, double[][] color) {
    double[][] col = new double[lum.length][];
    for (int i = 0; i < lum.length; i++) col[i] = colorValues(lum[i], color[i]);
    return col;
  }

}
