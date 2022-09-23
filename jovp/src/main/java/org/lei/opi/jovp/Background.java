package org.lei.opi.jovp;

import java.util.ArrayList;
import java.util.HashMap;

import es.optocom.jovp.structures.ModelType;

/**
 * Background and fixation target settings
 * 
 * @param bgCol background RGBA color where each channel range is from 0 to max luminance
 * @param fixShape fixation type
 * @param fixCol fixation RGBA color where each channel range is from 0 to max luminance
 * @param fixCx x center of the fixation target in degrees of visual angle
 * @param fixCy y center of the fixation target in degrees of visual angle
 * @param fixSx mayor axis size of the fixation target in degrees of visual angle
 * @param fixSy minor axis size of the fixation target in degrees of visual angle
 * @param fixRotation rotation of the fixation target in degrees
 *
 * @since 0.0.1
 */
public record Background(double[] bgCol, ModelType fixShape, double[] fixCol,
                         double fixCx, double fixCy, double fixSx, double fixSy,
                         double fixRotation) {

  /**
   * Set background record 
   * 
   * @param args pairs of argument name and value
   * 
   * @return a background record
   * 
   * @throws ClassCastException Cast exception
   * @throws IllegalArgumentException If any value is bad
   * 
   * @since 0.0.1
   */
  static Background set(HashMap<String, Object> args, Calibration calibration) throws ClassCastException, IllegalArgumentException {
    double bgLum = (double) args.get("bgLum");
    double fixLum = (double) args.get("fixLum");
    double[] bgCol = ((ArrayList<?>) args.get("bgCol")).stream().mapToDouble(Double.class::cast).toArray();
    double[] fixCol = ((ArrayList<?>) args.get("fixCol")).stream().mapToDouble(Double.class::cast).toArray();
    return new Background(calibration.colorValues(bgLum, bgCol),
                          ModelType.valueOf(((String) args.get("fixShape")).toUpperCase()),
                          calibration.colorValues(fixLum, fixCol),
                          (double) args.get("fixCx"), (double) args.get("fixCy"),
                          (double) args.get("fixSx"), (double) args.get("fixSy"),
                          (double) args.get("fixRotation"));
  }

}
