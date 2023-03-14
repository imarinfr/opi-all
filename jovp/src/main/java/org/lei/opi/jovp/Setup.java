package org.lei.opi.jovp;

import static org.lei.opi.jovp.JsonProcessor.toDoubleArray;

import java.util.Arrays;
import java.util.HashMap;

import org.lei.opi.core.OpiListener.Command;

import es.optocom.jovp.definitions.Eye;
import es.optocom.jovp.definitions.ModelType;

/**
 * Background and fixation target settings
 * 
 * @param eye the eye for which to apply the settings
 * @param bgCol background RGBA color where each channel range is from 0 to max luminance
 * @param bgLum cd/m^2 for background 
 * @param fixShape fixation type
 * @param fixCol fixation RGBA color where each channel range is from 0 to max luminance
 * @param fixLum cd/m^2 for fixation 
 * @param fixCx x center of the fixation target in degrees of visual angle
 * @param fixCy y center of the fixation target in degrees of visual angle
 * @param fixSx mayor axis size of the fixation target in degrees of visual angle
 * @param fixSy minor axis size of the fixation target in degrees of visual angle
 * @param fixRotation rotation of the fixation target in degrees
 * @param tracking whether to activate or deactivate tracking (if device permits it)
 *
 * @since 0.0.1
 */
public record Setup(Eye eye, double[] bgCol, double bgLum, ModelType fixShape, double[] fixCol, double fixLum,
                    double fixCx, double fixCy, double fixSx, double fixSy,
                    double fixRotation, double tracking) {


  /**
   * Sets arguments create a background record from R OPI
   * 
   * @param args pairs of argument name and value
   * 
   * @return a background record
   * 
   * @throws ClassCastException Cast exception
   * 
   * @since 0.0.1
   */
  public static Setup create2(HashMap<String, Object> args) throws ClassCastException {
    return new Setup(Eye.valueOf(((String) args.get("eye")).toUpperCase()),
                     toDoubleArray(args.get("bgCol")),
                     (double)(args.get("bgLum")),
                     ModelType.valueOf(((String) args.get("fixShape")).toUpperCase()),
                     toDoubleArray(args.get("fixCol")),
                     (double)(args.get("fixLum")),
                     (double) args.get("fixCx"), (double) args.get("fixCy"),
                     (double) args.get("fixSx"), (double) args.get("fixSy"),
                     (double) args.get("fixRotation"),
                     (double) args.get("tracking"));
  }

  /**
   * Setup to JSON
   * 
   * @return The Present record as JSON
   * 
   * @since 0.0.1
   */
  public String toJson() {
    return new StringBuilder("{\n  \"command\": " + Command.SETUP + ",\n")
      .append("  \"eye\": " + eye.toString() + ",\n")
      .append("  \"bgCol\": " + Arrays.toString(bgCol) + ",\n")
      .append("  \"bgLum\": " + bgLum + ",\n")
      .append("  \"fixShape\": " + fixShape.toString() + ",\n")
      .append("  \"fixCol\": " + Arrays.toString(fixCol) + ",\n")
      .append("  \"fixLum\": " + fixLum + ",\n")
      .append("  \"fixCx\": " + fixCx + ",\n")
      .append("  \"fixCy\": " + fixCy + ",\n")
      .append("  \"fixSx\": " + fixSx + ",\n")
      .append("  \"fixSy\": " + fixSy + ",\n")
      .append("  \"fixRotation\": " + fixRotation + ",\n")
      .append("  \"tracking\": " + tracking)
      .append("\n}").toString();
  }

}
