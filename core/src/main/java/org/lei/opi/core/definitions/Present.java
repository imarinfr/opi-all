package org.lei.opi.core.definitions;

import static org.lei.opi.core.definitions.JsonProcessor.toColorArray;
import static org.lei.opi.core.definitions.JsonProcessor.toDoubleArray;
import static org.lei.opi.core.definitions.JsonProcessor.toIntArray;
import static org.lei.opi.core.definitions.JsonProcessor.toObjectStream;
import static org.lei.opi.core.definitions.JsonProcessor.colorValues;

import java.util.Arrays;
import java.util.HashMap;

import org.lei.opi.core.OpiManager.Command;

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

  /** Default rotation @value DEFAULT_ROTATION */
  private static final double DEFAULT_ROTATION = 0;
  /** Default contrast @value DEFAULT_CONTRAST */
  private static final double DEFAULT_CONTRAST = 0;
  /** Default phase @value DEFAULT_PHASE */
  private static final double DEFAULT_PHASE = 0;
  /** Default frequency @value DEFAULT_FREQUENCY */
  private static final double DEFAULT_FREQUENCY = 0;
  /** Default defocus @value DEFAULT_DEFOCUS */
  private static final double DEFAULT_DEFOCUS = 0;
  /** Default texture rotation @value DEFAULT_TEXTURE_ROTATION */
  private static final double DEFAULT_TEXTURE_ROTATION = 0;

  /** {@value BAD_RESPONSE_WINDOW} */
  private static final String BAD_RESPONSE_WINDOW = "Response window must be of length 1. It is of length ";
  /** {@value EMPTY_MANDATORY_FIELDS} */
  private static final String EMPTY_MANDATORY_FIELDS = "No mandatory field can be an empty list";
  /** {@value INCONSISTENT_ARRAY_LENGTH} */
  private static final String INCONSISTENT_ARRAY_LENGTH = "List of presentation stimuli is inconsistent. All arrays must be of the same size, except response time w";

  /**
   * Process stimulus record from R OPI. 
   * The major difference with set is that 'process' expects bgLum and
   * bgCol and fixLum and fixCol whereas 'set' only expects 'bgCol' and
   * 'fixCol'. The vectors 'bgCol' and 'fixCol' are color mixtures with
   * values between 0 and 1 in 'process' but channel luminances in cd/m^2
   * in 'set'. Ranges of values goes unchecked
   * 
   * @param args pairs of argument name and value
   * 
   * @return a stimulus record
   * 
   * @throws ClassCastException Cast exception
   * @throws IllegalArgumentException If any value is bad
   * @throws SecurityException
   * @throws NoSuchMethodException
   * 
   * @since 0.0.1
   */

  public static Present process(HashMap<String, Object> args) throws ClassCastException, IllegalArgumentException, NoSuchMethodException, SecurityException {
    // Mandatory fields
    Eye[] eye = toObjectStream(args.get("eye"), Eye.class).toArray(Eye[]::new);
    int length = eye.length;
    if (length == 0) throw new IllegalArgumentException(EMPTY_MANDATORY_FIELDS);
    ModelType[] shape = toObjectStream(args.get("shape"), ModelType.class).toArray(ModelType[]::new);
    TextureType[] type = toObjectStream(args.get("type"), TextureType.class).toArray(TextureType[]::new);
    double[] x = toDoubleArray(args.get("x"));
    double[] y = toDoubleArray(args.get("y"));
    double[] sx = toDoubleArray(args.get("sx"));
    double[] lum = toDoubleArray(args.get("lum"));
    double[][] color = toColorArray(args.get("color"));
    int[] t = toIntArray(args.get("t"));
    // Check size consistency
    if (shape.length != length || type.length != length || x.length != length || y.length != length ||
        sx.length != length || lum.length != length || color.length != length || t.length != length)
      throw new IllegalArgumentException(INCONSISTENT_ARRAY_LENGTH);
    // Fill optional fields if necessary
    double[] sy = optionalField(toDoubleArray(args.get("sy")), sx, length);
    double[] rotation = optionalField(toDoubleArray(args.get("rotation")), DEFAULT_ROTATION, length);
    double[] contrast = optionalField(toDoubleArray(args.get("contrast")), DEFAULT_CONTRAST, length);
    double[] phase = optionalField(toDoubleArray(args.get("phase")), DEFAULT_PHASE, length);
    double[] frequency = optionalField(toDoubleArray(args.get("frequency")), DEFAULT_FREQUENCY, length);
    double[] defocus = optionalField(toDoubleArray(args.get("defocus")), DEFAULT_DEFOCUS, length);
    double[] texRotation = optionalField(toDoubleArray(args.get("texRotation")), DEFAULT_TEXTURE_ROTATION, length);
    int[] w = toIntArray(args.get("w"));
    if (w.length != 1)
      throw new IllegalArgumentException(BAD_RESPONSE_WINDOW + w.length);
    return new Present(eye, shape, type, x, y, sx, sy, colorValues(lum, color), rotation, contrast, phase, frequency, defocus, texRotation, t, w[0]);
  }

  /**
   * Set stimulus record from R OPI
   * 
   * @param args pairs of argument name and value
   * 
   * @return a stimulus record
   * 
   * @throws ClassCastException Cast exception
   * @throws IllegalArgumentException If any value is bad
   * @throws SecurityException
   * @throws NoSuchMethodException
   * 
   * @since 0.0.1
   */
  public static Present set(HashMap<String, Object> args) throws ClassCastException, IllegalArgumentException, NoSuchMethodException, SecurityException {
    return new Present(toObjectStream(args.get("eye"), Eye.class).toArray(Eye[]::new),
                       toObjectStream(args.get("shape"), ModelType.class).toArray(ModelType[]::new),
                       toObjectStream(args.get("type"), TextureType.class).toArray(TextureType[]::new),
                       toDoubleArray(args.get("x")), toDoubleArray(args.get("y")),
                       toDoubleArray(args.get("sx")), toDoubleArray(args.get("sy")),
                       toColorArray(args.get("color")),
                       toDoubleArray(args.get("rotation")), toDoubleArray(args.get("contrast")),
                       toDoubleArray(args.get("phase")), toDoubleArray(args.get("frequency")),
                       toDoubleArray(args.get("defocus")), toDoubleArray(args.get("texRotation")),
                       toIntArray(args.get("t")), (int) (double) args.get("w"));
  }

  /**
   * Present to JSON
   * 
   * @return The Present record as JSON
   * 
   * @since 0.0.1
   */
  public String toJson() {
    return new StringBuilder("{\n  \"command\": " + Command.PRESENT + ",\n")
      .append("  \"eye\": " + Arrays.toString(eye) + ",\n")
      .append("  \"type\": " + Arrays.toString(type) + ",\n")
      .append("  \"shape\": " + Arrays.toString(shape) + ",\n")
      .append("  \"x\": " + Arrays.toString(x) + ",\n")
      .append("  \"y\": " + Arrays.toString(y) + ",\n")
      .append("  \"sx\": " + Arrays.toString(sx) + ",\n")
      .append("  \"sy\": " + Arrays.toString(sy)+ ",\n")
      .append("  \"color\": " + Arrays.deepToString(color) + ",\n")
      .append("  \"rotation\": " + Arrays.toString(rotation) + ",\n")
      .append("  \"contrast\": " + Arrays.toString(contrast) + ",\n")
      .append("  \"phase\": " + Arrays.toString(phase) + ",\n")
      .append("  \"frequency\": " + Arrays.toString(frequency) + ",\n")
      .append("  \"defocus\": " + Arrays.toString(defocus) + ",\n")
      .append("  \"texRotation\": " + Arrays.toString(texRotation) + ",\n")
      .append("  \"t\": " + Arrays.toString(t) + ",\n")
      .append("  \"w\": " + w)
      .append("\n}").toString();
  }


  /**
   * Fill optional fields with default values if they are empty
   * 
   * @param val the values received via JSON
   * @param defaultVal the default value or values to apply if 'val' is empty
   * @param length the length of the final array returned
   * 
   * @return either the values received or default values
   * 
   * @throws IllegalArgumentException If any value is bad
   * 
   * @since 0.0.1
   */
  private static double[] optionalField(double[] val, double[] defaultVal, int length) throws IllegalArgumentException {
    if (val.length == length) return val;
    if (val.length != 0)
      throw new IllegalArgumentException(INCONSISTENT_ARRAY_LENGTH);
    return defaultVal;
  }

  /**
   * Fill optional fields with default values if they are empty
   * 
   * @param val the values received via JSON
   * @param defaultVal the default value or values to apply if 'val' is empty
   * @param length the length of the final array returned
   * 
   * @return either the values received or default values
   * 
   * @throws IllegalArgumentException If any value is bad
   * 
   * @since 0.0.1
   */
  private static double[] optionalField(double[] val, double defaultValue, int length) throws IllegalArgumentException {
    if (val.length == length) return val;
    if (val.length != 0)
      throw new IllegalArgumentException(INCONSISTENT_ARRAY_LENGTH);
    val = new double[length];
    Arrays.fill(val, defaultValue);
    return val;
  }

}