package org.lei.opi.jovp;

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
 * @param lum stimulus luminance
 * @param color stimulus color
 * @param rotation rotation of the stimulus in degrees
 * @param contrast stimulus contrast
 * @param defocus stimulus defocus in Diopters for stimulus such as "Gaussian blob", Gabors, etc
 * @param frequency stimulus spatial frequency
 * @param patRotation stimulus pattern rotation in degrees
 * @param t presentation time in ms
 * @param w response window in ms
 *
 * @since 0.0.1
 */
public record Stimulus(Eye[] eye, ModelType[] shape, TextureType[] type,
                       double[] x, double[] y, double[] sx, double[] sy,
                       double[] lum, double[][] color, double[] rotation,
                       double[] contrast, double[] defocus, double[] frequency,
                       double[] patRotation, int[] t, int w) {

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
  static Stimulus set(HashMap<String, Object> args) throws ClassCastException {
    try {
      Eye[] eye = new Eye[] {Eye.LEFT, Eye.RIGHT};
      ModelType[] shape = new ModelType[] {ModelType.CIRCLE, ModelType.SQUARE};
      TextureType[] type = new TextureType[] {TextureType.FLAT, TextureType.SINE};
      double[] x = new double[] {1, 2};
      double[] y = new double[] {3, 4};
      double[] sx = new double[] {3, 5};
      double[] sy = new double[] {5, 2};
      double[] rotation = new double[] {0, 45};
      double[] lum = new double[] {30, 20};
      double[][] color = new double[][] {{1, 1, 1, 1}, {1, 0, 1, 1}};
      double[] contrast = new double[] {1, 0.5};
      double[] frequency = new double[] {4, 2};
      double[] defocus = new double[] {0, 1};
      double[] patRotation = new double[] {30, 60};
      int[] t = new int[] {200, 500};
      return new Stimulus(eye, shape, type, x, y, sx, sy, lum, color, rotation,
                          contrast, defocus, frequency, patRotation, t, 1500);
    } catch(ClassCastException e) {
      String errorMessage = "Problem setting stimulus. Pairs of values were: " + args;
      System.err.println(errorMessage);
      throw new ClassCastException(errorMessage);
    }
  }

}