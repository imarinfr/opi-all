package org.lei.opi.jovp;

import java.util.HashMap;

import org.lei.opi.core.Jovp.Color;

import es.optocom.jovp.structures.ModelType;

/**
 * Background and fixation target settings
 * 
 * @param bgLum background luminance from 0 to 1
 * @param bgCol background color
 * @param fixShape fixation type
 * @param fixLum fixation luminance from 0 to 1
 * @param fixCol fixation color
 * @param fixCx x center of the fixation target in degrees of visual angle
 * @param fixCy y center of the fixation target in degrees of visual angle
 * @param fixSx mayor axis size of the fixation target in degrees of visual angle
 * @param fixSy minor axis size of the fixation target in degrees of visual angle
 * @param fixRotation rotation of the fixation target in degrees
 *
 * @since 0.0.1
 */
public record Background(double bgLum, double[] bgCol, ModelType fixShape, double fixLum, double[] fixCol,
                         double fixCx, double fixCy, double fixSx, double fixSy, double fixRotation) {

  /**
   * Set background record 
   * 
   * @param args pairs of argument name and value
   * 
   * @return a background record
   * 
   * @since 0.0.1
   */
  static Background set(HashMap<String, Object> args) throws ClassCastException {
    return new Background((double) args.get("bgLum"),
                          OpiJovp.color2rgba(Color.valueOf(((String) args.get("fixCol")).toUpperCase())),
                          ModelType.valueOf(((String) args.get("fixShape")).toUpperCase()),
                          (double) args.get("fixLum"),
                          OpiJovp.color2rgba(Color.valueOf(((String) args.get("fixCol")).toUpperCase())),
                          (double) args.get("fixCx"), (double) args.get("fixCy"),
                          (double) args.get("fixSx"), (double) args.get("fixSy"),
                          (double) args.get("fixRotation"));
  }

}
