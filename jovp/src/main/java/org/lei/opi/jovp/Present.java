package org.lei.opi.jovp;

import static org.lei.opi.jovp.JsonProcessor.colorValues;
import static org.lei.opi.jovp.JsonProcessor.toColorArray;
import static org.lei.opi.jovp.JsonProcessor.toDoubleArray;
import static org.lei.opi.jovp.JsonProcessor.toIntArray;
import static org.lei.opi.jovp.JsonProcessor.toObjectStream;

import java.util.Arrays;
import java.util.HashMap;

import org.lei.opi.core.OpiListener.Command;

import es.optocom.jovp.definitions.Eye;
import es.optocom.jovp.definitions.ModelType;
import es.optocom.jovp.definitions.TextureType;

/**
 * Stimulus
 * 
 * @param length length of arrays for stimulus presentations
 * @param eye eye where to present the stimulus
 * @param shape stimulus shape
 * @param type stimulus type
 * @param x x center of the stimulus in degrees of visual angle
 * @param y y center of the stimulus in degrees of visual angle
 * @param sx major axis size of the stimulus in degrees of visual angle
 * @param sy minor axis size of the stimulus in degrees of visual angle
 * @param color1 stimulus color 1 for flat surfaces and patterns
 * @param color2 stimulus color 2 for patterns
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
public record Present(int length, Eye[] eye, ModelType[] shape, TextureType[] type,
                      double[] x, double[] y, double[] sx, double[] sy,
                      double[][] color1, double[][] color2, double[] rotation, double[] contrast,
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
   * @throws SecurityException
   * @throws NoSuchMethodException
   * 
   * @since 0.0.1
   */
  public static Present set(HashMap<String, Object> args) throws ClassCastException, IllegalArgumentException, NoSuchMethodException, SecurityException {
    return new Present((int) (double) args.get("stim.length"),
                       toObjectStream(args.get("eye"), Eye.class).toArray(Eye[]::new),
                       toObjectStream(args.get("shape"), ModelType.class).toArray(ModelType[]::new),
                       toObjectStream(args.get("type"), TextureType.class).toArray(TextureType[]::new),
                       toDoubleArray(args.get("x")), toDoubleArray(args.get("y")),
                       toDoubleArray(args.get("sx")), toDoubleArray(args.get("sy")),
                       toColorArray(args.get("color1")), toColorArray(args.get("color2")),
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
      .append("  \"stim.length\": " + length + ",\n")
      .append("  \"eye\": " + Arrays.toString(eye) + ",\n")
      .append("  \"type\": " + Arrays.toString(type) + ",\n")
      .append("  \"shape\": " + Arrays.toString(shape) + ",\n")
      .append("  \"x\": " + Arrays.toString(x) + ",\n")
      .append("  \"y\": " + Arrays.toString(y) + ",\n")
      .append("  \"sx\": " + Arrays.toString(sx) + ",\n")
      .append("  \"sy\": " + Arrays.toString(sy)+ ",\n")
      .append("  \"color1\": " + Arrays.deepToString(color1) + ",\n")
      .append("  \"color2\": " + Arrays.deepToString(color2) + ",\n")
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
}