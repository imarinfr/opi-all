package org.lei.opi.core.definitions;

import java.util.Arrays;
import java.util.HashMap;

import org.lei.opi.core.OpiManager.Command;

import static org.lei.opi.core.definitions.JsonProcessor.toDoubleArray;
import static org.lei.opi.core.definitions.JsonProcessor.colorValues;

import es.optocom.jovp.definitions.Eye;
import es.optocom.jovp.definitions.ModelType;

/**
 * Background and fixation target settings
 * 
 * @param eye the eye for which to apply the settings
 * @param bgCol background RGBA color where each channel range is from 0 to max luminance
 * @param fixShape fixation type
 * @param fixCol fixation RGBA color where each channel range is from 0 to max luminance
 * @param fixCx x center of the fixation target in degrees of visual angle
 * @param fixCy y center of the fixation target in degrees of visual angle
 * @param fixSx mayor axis size of the fixation target in degrees of visual angle
 * @param fixSy minor axis size of the fixation target in degrees of visual angle
 * @param fixRotation rotation of the fixation target in degrees
 * @param tracking whether to activate or deactivate tracking (if device permits it)
 *
 * @since 0.0.1
 */
public record Setup(Eye eye, double[] bgCol, ModelType fixShape, double[] fixCol,
                    double fixCx, double fixCy, double fixSx, double fixSy,
                    double fixRotation, boolean tracking) {

  /**
   * Processes arguments and create a background record from R OPI.
   * The major difference with set is that 'process' expects bgLum and
   * bgCol and fixLum and fixCol whereas 'set' only expects 'bgCol' and
   * 'fixCol'. The vectors 'bgCol' and 'fixCol' are color mixtures with
   * values between 0 and 1 in 'process' but channel luminances in cd/m^2
   * in 'set'. Ranges of values goes unchecked
   * 
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
  public static Setup process(HashMap<String, Object> args) throws ClassCastException, IllegalArgumentException {
    return new Setup(Eye.valueOf(((String) args.get("eye")).toUpperCase()),
                     colorValues((double) args.get("bgLum"), toDoubleArray(args.get("bgCol"))),
                     ModelType.valueOf(((String) args.get("fixShape")).toUpperCase()),
                     colorValues((double) args.get("fixLum"), toDoubleArray(args.get("fixCol"))),
                     (double) args.get("fixCx"), (double) args.get("fixCy"),
                     (double) args.get("fixSx"), (double) args.get("fixSy"),
                     (double) args.get("fixRotation"),
                     ((double) args.get("tracking") == 1 ? true : false));
  }

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
  public static Setup set(HashMap<String, Object> args) throws ClassCastException {
    return new Setup(Eye.valueOf(((String) args.get("eye")).toUpperCase()),
                     toDoubleArray(args.get("bgCol")),
                     ModelType.valueOf(((String) args.get("fixShape")).toUpperCase()),
                     toDoubleArray(args.get("fixCol")),
                     (double) args.get("fixCx"), (double) args.get("fixCy"),
                     (double) args.get("fixSx"), (double) args.get("fixSy"),
                     (double) args.get("fixRotation"),
                     (boolean) args.get("tracking"));
  }

  /**
   * Present to JSON
   * 
   * @return The Present record as JSON
   * 
   * @since 0.0.1
   */
  public String toJson() {
    return new StringBuilder("{\n  \"command\": " + Command.SETUP + ",\n")
      .append("  \"eye\": " + eye.toString() + ",\n")
      .append("  \"bgCol\": " + Arrays.toString(bgCol) + ",\n")
      .append("  \"fixShape\": " + fixShape.toString() + ",\n")
      .append("  \"fixCol\": " + Arrays.toString(fixCol) + ",\n")
      .append("  \"fixCx\": " + fixCx + ",\n")
      .append("  \"fixCy\": " + fixCy + ",\n")
      .append("  \"fixSx\": " + fixSx + ",\n")
      .append("  \"fixSy\": " + fixSy + ",\n")
      .append("  \"fixRotation\": " + fixRotation + ",\n")
      .append("  \"tracking\": " + tracking)
      .append("\n}").toString();
  }

}
