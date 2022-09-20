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
 * @param color stimulus color
 * @param rotation rotation of the stimulus in degrees
 * @param contrast stimulus contrast
 * @param defocus stimulus defocus in Diopters for stimulus such as "Gaussian blob", Gabors, etc
 * @param phase stimulus spatial phase
 * @param frequency stimulus spatial frequency
 * @param texRotation stimulus pattern rotation in degrees
 * @param t presentation time in ms
 * @param w response window in ms
 *
 * @since 0.0.1
 */
public record Stimulus(Eye[] eye, ModelType[] shape, TextureType[] type,
                       double[] x, double[] y, double[] sx, double[] sy,
                       double[][] color, double[] rotation, double[] contrast,
                       double[] defocus, double[] phase, double[] frequency,
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
  static Stimulus set(HashMap<String, Object> args) throws ClassCastException {
    try {
      Eye[] eye = new Eye[] {Eye.RIGHT};
      ModelType[] shape = new ModelType[] {ModelType.CIRCLE};
      TextureType[] type = new TextureType[] {TextureType.FLAT};
      double[] x = new double[] {5};
      double[] y = new double[] {8};
      double[] sx = new double[] {3};
      double[] sy = new double[] {5};
      double[] rotation = new double[] {45};
      double[][] color = new double[][] {{1, 1, 1, 1}};
      double[] contrast = new double[] {1};
      double[] phase = new double[] {0};
      double[] frequency = new double[] {4};
      double[] defocus = new double[] {0};
      double[] texRotation = new double[] {0};
      int[] t = new int[] {500};
      return new Stimulus(eye, shape, type, x, y, sx, sy, color, rotation,
                          contrast, defocus, phase, frequency, texRotation, t, 1500);
    } catch(ClassCastException e) {
      String errorMessage = "Problem setting stimulus. Pairs of values were: " + args;
      System.err.println(errorMessage);
      throw new ClassCastException(errorMessage);
    }
  }

}