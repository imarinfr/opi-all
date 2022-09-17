package org.lei.opi.jovp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

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
 * @param gamma display-specific gamma function
 *
 * @since 0.0.1
 */
public record Settings(Machine machine, int screen, boolean fullScreen, int distance,
                       ViewMode viewMode, Input input, int depth, int[] gamma) {

  /** Implemented display-based machines */
  enum Machine {
    /** IMO perimeter through the jovp */
    IMOVIFA,
    /** PicoVR as perimeter through the jovp */
    PICOVR,
    /** Android phone as perimeter through the jovp */
    PHONEHMD,
    /** PC */
    DISPLAY
  }

  /** Default configuration file for IMO */
  public static final String IMO_PARAMS = "IMOParams.json";
  /** Default configuration file for Display */
  public static final String DISPLAY_PARAMS = "DisplayParams.json";
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

  /**
   * Load from a JSON file
   * 
   * @param machine the OPI JOVP machine
   * 
   * @return a record with the OPI JOVP machine settings
   *
   * @throws IOException
   *
   * @since 0.0.1
   */
  public static Settings defaultSettings(Machine machine) throws IOException {
    String jsonStr = switch (machine) {
      case IMOVIFA -> loadJsonResource(Settings.IMO_PARAMS);
      case PICOVR -> loadJsonResource(Settings.PICOVR_PARAMS);
      case PHONEHMD -> loadJsonResource(Settings.PHONEHMD_PARAMS);
      case DISPLAY -> loadJsonResource(Settings.DISPLAY_PARAMS);
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
   * @throws IOException
   * @throws FileNotFoundException
   * @throws IllegalArgumentException
   *
   * @since 0.0.1
   */
  public static Settings load(Machine machine, String file) throws IllegalArgumentException, FileNotFoundException, IOException {
    return jsonToSettings(machine, IOUtils.toString(new FileInputStream(file), String.valueOf(StandardCharsets.UTF_8)));
  }

  /**
   * Load resource file into a String
   * 
   * @param file string with JSON file name
   * 
   * @return a settings record
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
   * @since 0.0.1
   */
  private static Settings jsonToSettings(Machine machine, String jsonStr) throws IllegalArgumentException {
    Gson gson = new Gson();
    HashMap<String, Object> pairs = gson.fromJson(jsonStr, new TypeToken<HashMap<String, Object>>() {}.getType());
    int screen = ((Double) pairs.get("screen")).intValue();
    if(screen < 0)
      throw new IllegalArgumentException(String.format("'screen' value has to be 0 (default screen or positive). It is %s", screen));
    boolean fullScreen = (boolean) pairs.get("fullScreen");
    int distance = ((Double) pairs.get("distance")).intValue();
    if(distance < 0)
      throw new IllegalArgumentException(String.format("'distance' cannot be negative, you silly goose. It is %s", distance));
    ViewMode viewMode = ViewMode.valueOf(((String) pairs.get("viewMode")).toUpperCase());
    Input input = Input.valueOf(((String) pairs.get("input")).toUpperCase());
    int depth = ((Double) pairs.get("depth")).intValue();
    if(depth < 8)
      throw new IllegalArgumentException(String.format("Cannot run on a display with a color 'depth' lower than 8 bits. It is %s", depth));
    int[] gamma = (((ArrayList<?>) (pairs.get("gamma"))).stream().map(Double.class::cast)
                    .collect(Collectors.toCollection(ArrayList::new))).stream().mapToInt(Double::intValue).toArray();
    return new Settings(machine, screen, fullScreen, distance, viewMode, input, depth, gamma);
  }

}