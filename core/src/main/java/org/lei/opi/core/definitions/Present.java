package org.lei.opi.core.definitions;

import java.util.HashMap;

import static org.lei.opi.core.definitions.JsonProcessor.toDoubleArray;
import static org.lei.opi.core.definitions.JsonProcessor.toIntArray;
import static org.lei.opi.core.definitions.JsonProcessor.toEnumArray;
import static org.lei.opi.core.definitions.JsonProcessor.toColorArray;

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
public record Present(Eye[] eye, ModelType[] shape, TextureType[] type,
                      double[] x, double[] y, double[] sx, double[] sy,
                      double[][] color, double[] rotation, double[] contrast,
                      double[] phase, double[] frequency, double[] defocus,
                      double[] texRotation, int[] t, int w) {

  /**
   * Set stimulus record from R OPI
   * 
   * @param args pairs of argument name and value
   * 
   * @return a stimulus record
   * 
   * @throws ClassCastException Cast exception
   * @throws IllegalArgumentException If any value is bad
   * 
   * @since 0.0.1
   */
  public static Present set(HashMap<String, Object> args) throws ClassCastException, IllegalArgumentException {
    return new Present((Eye[]) toEnumArray(args.get("eye"), Eye.class),
                       (ModelType[]) toEnumArray(args.get("shape"), ModelType.class),
                       (TextureType[]) toEnumArray(args.get("type"), TextureType.class),
                       toDoubleArray(args.get("x")), toDoubleArray(args.get("y")),
                       toDoubleArray(args.get("sx")), toDoubleArray(args.get("sy")),
                       toColorArray(args.get("color")),
                       toDoubleArray(args.get("rotation")), toDoubleArray(args.get("contrast")),
                       toDoubleArray(args.get("phase")), toDoubleArray(args.get("frequency")),
                       toDoubleArray(args.get("defocus")), toDoubleArray(args.get("texRotation")),
                       toIntArray(args.get("t")), toIntArray(args.get("w"))[0]);
  }

  /**
   * Present to JSON
   * 
   * @return The Present record as JSON
   * 
   * @since 0.0.1
   */
  public String toJson() {
    return new StringBuilder("{").toString();
  }

}