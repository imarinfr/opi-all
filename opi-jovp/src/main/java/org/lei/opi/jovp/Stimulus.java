package org.lei.opi.jovp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import es.optocom.jovp.structures.Eye;
import es.optocom.jovp.structures.ModelType;
import es.optocom.jovp.structures.TextureType;

/**
 * Stimulus
 * 
 * @param eye eye where to present the stimulus
 * @param shape stimulus shape
 * @param type stimulus type
 * @param x x center of the stimulus in degrees of visual angle
 * @param y y center of the stimulus in degrees of visual angle
 * @param sx major axis size of the stimulus in degrees of visual angle
 * @param sy minor axis size of the stimulus in degrees of visual angle
 * @param color stimulus color
 * @param rotation rotation of the stimulus in degrees
 * @param contrast stimulus contrast
 * @param phase stimulus spatial phase
 * @param frequency stimulus spatial frequency
 * @param defocus stimulus defocus in Diopters for stimulus such as "Gaussian blob", Gabors, etc
 * @param texRotation stimulus pattern rotation in degrees
 * @param t presentation time in ms
 * @param w response window in ms
 *
 * @since 0.0.1
 */
public record Stimulus(Eye[] eye, ModelType[] shape, TextureType[] type,
                       double[] x, double[] y, double[] sx, double[] sy,
                       double[][] color, double[] rotation, double[] contrast,
                       double[] phase, double[] frequency, double[] defocus,
                       double[] texRotation, int[] t, int w) {

  /**
   * Set stimulus record
   * 
   * @param args pairs of argument name and value
   * 
   * @return a stimulus record
   * 
   * @throws ClassCastException Cast exception
   * 
   * @since 0.0.1
   */
  static Stimulus set(HashMap<String, Object> args) throws ClassCastException, IllegalArgumentException {
    Eye[] eye = Arrays.stream(listToStringArray(args.get("eye"))).map(Eye::valueOf).toArray(Eye[]::new);
    ModelType[] shape = Arrays.stream(listToStringArray(args.get("shape"))).map(ModelType::valueOf).toArray(ModelType[]::new);
    TextureType[] type = Arrays.stream(listToStringArray(args.get("type"))).map(TextureType::valueOf).toArray(TextureType[]::new);
    return new Stimulus(eye, shape, type,
                        listToDoubleArray(args.get("x")), listToDoubleArray(args.get("y")),
                        listToDoubleArray(args.get("sx")), listToDoubleArray(args.get("sy")),
                        listOfColorsToArray(args.get("color")),
                        listToDoubleArray(args.get("rotation")), listToDoubleArray(args.get("contrast")),
                        listToDoubleArray(args.get("phase")), listToDoubleArray(args.get("frequency")),
                        listToDoubleArray(args.get("defocus")), listToDoubleArray(args.get("texRotation")),
                        listToIntArray(args.get("t")), listToIntArray(args.get("w"))[0]);
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
  private static String[] listToStringArray(Object list) throws ClassCastException {
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
  private static double[] listToDoubleArray(Object list) throws ClassCastException {
    return ((ArrayList<?>) list).stream().mapToDouble(Double.class::cast).toArray();
  }

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
  private static int[] listToIntArray(Object list) throws ClassCastException {
    return ((ArrayList<?>) list).stream().mapToDouble(Double.class::cast).mapToInt(d -> (int) d).toArray();  
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
  private static double[][] listOfColorsToArray(Object list) throws ClassCastException {
    return ((ArrayList<?>) list).stream().map(l -> listToDoubleArray(l))
                                         .map(l -> (new double[] {l[0], l[1], l[2], 1}))
                                         .toArray(double[][]::new);
  }

}