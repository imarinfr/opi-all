package org.lei.opi.jovp;

import java.io.InputStream;
import java.util.HashMap;

import com.google.gson.reflect.TypeToken;

import es.optocom.jovp.structures.Input;
import es.optocom.jovp.structures.ViewMode;

/**
 * Configurations for OPI JOVP driver
 *
 * @since 0.0.1
 */
public class Configuration {

  /** Default configuration file for IMO */
  private static final String IMO_PARAMS = "IMOParams.json";
  /** Default configuration file for PC */
  private static final String PC_PARAMS = "PCParams.json";
  /** Default configuration file for PC */
  private static final String PHONEHMD_PARAMS = "PhoneHMDParams.json";
  /** Default configuration file for PC */
  private static final String PICOVR_PARAMS = "PPicoVRParams.json";

  /** Viewing distance */
  int distance;
  /** Viewing mode: mono or stereo */
  ViewMode viewMode;
  /** Input device for responses */
  Input input;
  /** Screen number: 0 is main, any number > 0 are external monitors */
  int screen;
  /** Whether to run in full screen or windowed mode */
  boolean fullScreen;

  /** Implemented display-based machines */
  enum Impl {
    /** IMO perimeter through the jovp */
    IMO,
    /** PicoVR as perimeter through the jovp */
    PICOVR,
    /** Android phone as perimeter through the jovp */
    PHONEHMD,
    /** PC */
    PC
  }

/**
 * Default configuration for OPI JOVP driver
 *
 * @since 0.0.1
 */
  Configuration(Impl impl) {
      switch(impl) {
        case IMO -> loadConfFile(IMO_PARAMS);
        case PICOVR -> loadConfFile(PHONEHMD_PARAMS);
        case PHONEHMD -> loadConfFile(PICOVR_PARAMS);
        case PC -> loadConfFile(PC_PARAMS);
      }
  }

  /**
   * Load configuration for OPI JOVP driver from file
   *
   * @since 0.0.1
   */
  Configuration(String file) {
    loadConfFile(file);
  }

  /**
   * Load configuration file
   *
   * @since 0.0.1
   */
  private void loadConfFile(String file) {
    Gson gson = new Gson();
    HashMap<String, String> pairs;
    try {
      InputStream inputStream = Configuration.class.getResourceAsStream(file);
      pairs = gson.fromJson(jsonStr, new TypeToken<HashMap<String, String>>() {}.getType());
    } catch (Exception e) {

    }
  }

}