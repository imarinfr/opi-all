package org.lei.opi.jovp;

import java.util.ArrayList;
import java.util.HashMap;

import org.lei.opi.core.Jovp.Shape;
import org.lei.opi.core.Jovp.Type;

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

    double[] x = ((ArrayList<?>) (args.get("x"))).stream().mapToDouble(Double.class::cast).toArray();
    double[] y = ((ArrayList<?>) (args.get("y"))).stream().mapToDouble(Double.class::cast).toArray();
    return new Stimulus(Eye.valueOf(((String) args.get("eye")).toUpperCase()),
                        Shape.valueOf(((String) args.get("shape")).toUpperCase()),
                        Type.valueOf(((String) args.get("type")).toUpperCase()),
                        parToDouble(args.get("x")), parToDouble(args.get("y")),
                        parToDouble(args.get("sx")), parToDouble(args.get("sy")),
                        color,
                        parToDouble(args.get("rotation")), parToDouble(args.get("contrast")),
                        parToDouble(args.get("phase")), parToDouble(args.get("frequency")),
                        parToDouble(args.get("defocus")), parToDouble(args.get("texRotation")),
                        parToDouble(args.get("t")), (double) args.get("t"));

  }

  private static double[] parToDouble(Object param) {
    return ((ArrayList<?>) param).stream().mapToDouble(Double.class::cast).toArray();
  }
}