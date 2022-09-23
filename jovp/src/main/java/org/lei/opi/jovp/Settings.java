package org.lei.opi.jovp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.structures.Input;
import es.optocom.jovp.structures.Paradigm;
import es.optocom.jovp.structures.ViewMode;

/**
 * Configurations for OPI JOVP driver
 * 
 * @param screen screen number: 0 is main, any number > 0 are external monitors
 * @param fullScreen whether to run in full screen or windowed mode
 * @param distance viewing distance
 * @param viewMode viewing mode: MONO or STEREO
 * @param input input device for responses
 * @param depth depth for each color channel
 * @param tracking whether device allows eye tracking
 * @param gamma path of the display-specific calibration file of R, G, B gamma functions
 * @param calibration the RGB calibration data
 *
 * @since 0.0.1
 */
public record Settings(Machine machine, int screen, boolean fullScreen, int distance,
                       ViewMode viewMode, Input input, boolean tracking, int depth,
                       String gamma, Calibration calibration) {

  /** Implemented display-based machines */
  enum Machine {
    /** IMO perimeter through the jovp */
    IMOVIFA,
    /** PicoVR as perimeter through the jovp */
    PICOVR,
    /** Android phone as perimeter through the jovp */
    PHONEHMD,
    /** Display monoscopic view */
    DISPLAY_MONO,
    /** Display stereoscopic view */
    DISPLAY_STEREO
  }

  /** Default configuration file for IMO */
  public static final String IMO_PARAMS = "IMOParams.json";
  /** Default configuration file for Display monoscopic view */
  public static final String DISPLAY_MONO_PARAMS = "DisplayMonoParams.json";
  /** Default configuration file for Display monoscopic view */
  public static final String DISPLAY_STEREO_PARAMS = "DisplayStereoParams.json";
  /** Default configuration file for PhoneHMD */
  public static final String PHONEHMD_PARAMS = "PhoneHMDParams.json";
  /** Default configuration file for PicoVR */
  public static final String PICOVR_PARAMS = "PicoVRParams.json";

  /** Paradigm is a click response for seen or not seen */
  static final Paradigm PARADIGM = Paradigm.CLICKER;
  /** Debugging parameter: whether to enable Vulkan validation layers */
  static final boolean VALIDATION_LAYERS = PsychoEngine.VALIDATION_LAYERS;
  /** Debugging parameter: whether to dump Vulkan API feedback */
  static final boolean API_DUMP = false;

  /** Error message: wrong screen */
  private static final String WRONG_SCREEN = "'screen' value has to be 0 (default screen or positive). It is %s";
  /** Error message: wrong distance */
  private static final String WRONG_DISTANCE = "'distance' cannot be negative, you silly goose. It is %s";
  /** Error message: wrong depth */
  private static final String WRONG_DEPTH = "Cannot run on a display with a color 'depth' lower than 8 bits. It is %s";

  /**
   * Load from a JSON file
   * 
   * @param machine the OPI JOVP machine
   * 
   * @return a record with the OPI JOVP machine settings
   *
   * @throws IOException IO exception
   *
   * @since 0.0.1
   */
  public static Settings defaultSettings(Machine machine) throws IOException {
    String jsonStr = switch (machine) {
      case IMOVIFA -> loadJsonResource(Settings.IMO_PARAMS);
      case PICOVR -> loadJsonResource(Settings.PICOVR_PARAMS);
      case PHONEHMD -> loadJsonResource(Settings.PHONEHMD_PARAMS);
      case DISPLAY_MONO -> loadJsonResource(Settings.DISPLAY_MONO_PARAMS);
      case DISPLAY_STEREO -> loadJsonResource(Settings.DISPLAY_STEREO_PARAMS);
    };
    return jsonToSettings(machine, jsonStr);
  }

  /**
   * Load from a JSON file
   * 
   * @param machine the OPI JOVP machine
   * @param file the file path and name
   * 
   * @return a record with the OPI JOVP machine settings
   * 
   * @throws IOException IO exception
   * @throws IllegalArgumentException Illegal argument
   *
   * @since 0.0.1
   */
  public static Settings load(Machine machine, String file) throws IOException, IllegalArgumentException {
    return jsonToSettings(machine, IOUtils.toString(new FileInputStream(file), String.valueOf(StandardCharsets.UTF_8)));
  }

  /**
   * Load resource file into a String
   * 
   * @param file string with JSON file name
   * 
   * @return a settings record
   * 
   * @throws IOException IO exception
   *
   * @since 0.0.1
   */
  private static String loadJsonResource(String file) throws IOException {
    return IOUtils.toString(Settings.class.getResourceAsStream(file), String.valueOf(StandardCharsets.UTF_8));
  }

  /**
   * Parse JSON configuration file
   * 
   * @param machine the OPI JOVP machine
   * @param jsonStr A JSON file with the OPI JOVP machine settings
   * 
   * @return a settings record
   * 
   * @throws IllegalArgumentException Illegal argument for screen, distance, or depth
   * @throws ClassCastException Cast exception
   * @throws IOException IO exception for calibration file
   *
   * @since 0.0.1
   */
  private static Settings jsonToSettings(Machine machine, String jsonStr) throws IllegalArgumentException, ClassCastException, IOException {
    Gson gson = new Gson();
    HashMap<String, Object> pairs = gson.fromJson(jsonStr, new TypeToken<HashMap<String, Object>>() {}.getType());
    int screen = ((Double) pairs.get("screen")).intValue();
    if(screen < 0)
      throw new IllegalArgumentException(String.format(WRONG_SCREEN, screen));
    int distance = ((Double) pairs.get("distance")).intValue();
    if(distance < 0)
      throw new IllegalArgumentException(String.format(WRONG_DISTANCE, distance));
    int depth = ((Double) pairs.get("depth")).intValue();
    if(depth < 8)
      throw new IllegalArgumentException(String.format(WRONG_DEPTH, depth));
    String gamma = pairs.get("gamma").toString();
    return new Settings(machine, screen, (boolean) pairs.get("fullScreen"), distance,
                        ViewMode.valueOf(pairs.get("viewMode").toString().toUpperCase()),
                        Input.valueOf(pairs.get("input").toString().toUpperCase()),
                        (boolean) pairs.get("tracking"), depth, gamma, loadCalibration(depth, gamma));
  }

  /**
   * Fill the R, G, and B gamma functions
   * 
   * @param depth the bit depth for the display
   * @param gamma resource file or path with display-specific calibration of R, G, B gamma functions
   * 
   * @throws IllegalArgumentException Illegal argument for screen, distance, or depth
   * @throws ClassCastException Cast exception
   * @throws IOException IO exception for calibration file
   *
   * @since 0.0.1
   */
  private static Calibration loadCalibration(int depth, String gamma) throws IllegalArgumentException, ClassCastException, IOException {
    Gson gson = new Gson();
    String jsonStr;
    // Get calibration from a path or from resources
    try(InputStream inputStream = new FileInputStream(gamma)) {
      jsonStr = calibrationFromPath(gamma);
    } catch (IOException e) { // if gamma not path, then see if it is in resources
      jsonStr = calibrationFromResources(gamma);
      // if gamma not path and not a resource file, then throw IOException
    }
    HashMap<String, Object> pairs = gson.fromJson(jsonStr, new TypeToken<HashMap<String, Object>>() {}.getType());
    double[] gammaRed = ((ArrayList<?>) pairs.get("gammaRed")).stream().mapToDouble(Double.class::cast).toArray();
    double[] gammaGreen = ((ArrayList<?>) pairs.get("gammaGreen")).stream().mapToDouble(Double.class::cast).toArray();
    double[] gammaBlue = ((ArrayList<?>) pairs.get("gammaBlue")).stream().mapToDouble(Double.class::cast).toArray();
    return Calibration.set((double) pairs.get("maxRed"), depth, gammaRed,
                           (double) pairs.get("maxGreen"), depth, gammaGreen,
                           (double) pairs.get("maxBlue"), depth, gammaBlue);
  }

  /**
   * Get calibration from a path
   * 
   * @param file path to a file with display-specific calibration of R, G, B gamma functions
   *
   * @throws IOException
   *
   * @since 0.0.1
   */
  static String calibrationFromPath(String file) throws IOException {
    InputStream inputStream = new FileInputStream(file);
    return IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));
  }

  /**
   * Get calibration from resources
   * 
   * @param file resource file for display-specific calibration file of R, G, B gamma functions
   *
   * @throws IOException
   *
   * @since 0.0.1
   */
  static String calibrationFromResources(String file) throws IOException {
    InputStream inputStream = Settings.class.getResourceAsStream(file);
    return IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));
  }
  
}