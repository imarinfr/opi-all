package org.lei.opi.jovp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.structures.Input;
import es.optocom.jovp.structures.Paradigm;
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

  /** Paradigm is a click response for seen or not seen */
  static final Paradigm PARADIGM = Paradigm.CLICKER;
  /** Debugging parameter: whether to enable Vulkan validation layers */
  static final boolean VALIDATION_LAYERS = PsychoEngine.VALIDATION_LAYERS;
  /** Debugging parameter: whether to dump Vulkan API feedback */
  static final boolean API_DUMP = false;
  
  /** Screen number: 0 is main, any number > 0 are external monitors */
  int screen;
  /** Whether to run in full screen or windowed mode */
  boolean fullScreen;
  /** Viewing distance */
  int distance;
  /** Viewing mode: mono or stereo */
  ViewMode viewMode;
  /** Input device for responses */
  Input input;

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
 * @param impl Implementation configuration
 *
 * @since 0.0.1
 */
  Configuration(Impl impl) {
    try {
      URL asdf = Configuration.class.getClassLoader().getResource(IMO_PARAMS);
      switch(impl) {
        case IMO -> loadConfFile((Configuration.class.getClassLoader().getResource(IMO_PARAMS)).toURI());
        case PICOVR -> loadConfFile((Configuration.class.getClassLoader().getResource(PHONEHMD_PARAMS)).toURI());
        case PHONEHMD -> loadConfFile((Configuration.class.getClassLoader().getResource(PICOVR_PARAMS)).toURI());
        case PC -> loadConfFile((Configuration.class.getClassLoader().getResource(PC_PARAMS)).toURI());
      }
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }
  }

/**
 * Custom configuration for OPI JOVP driver
 * 
 * @param impl Implementation configuration
 * @param file Configuration file in JSON format
 *
 * @since 0.0.1
 */
Configuration(Impl impl, String file) {
  try {
    loadConfFile(new URI(file));
  } catch (URISyntaxException | IOException e) {
    e.printStackTrace();
  }
}

  /**
   * Load configuration file
   * 
   * @param uri The URI
   *
   * @since 0.0.1
   */
  private void loadConfFile(URI uri) throws IOException, URISyntaxException {
    Gson gson = new Gson();
    HashMap<String, String> pairs;
    byte[] bytes = Files.readAllBytes(Paths.get(uri));
    pairs = gson.fromJson(new String(bytes), new TypeToken<HashMap<String, String>>() {}.getType());
    parseParams(pairs);
  }

  /**
   * Parse parameters from configuration file
   * 
   * @param uri The URI
   *
   * @since 0.0.1
   */
  private void parseParams(HashMap<String, String> pairs) {
    screen = Integer.parseInt(pairs.get("screen"));
    fullScreen = Boolean.parseBoolean(pairs.get("fullScreen"));
    distance = Integer.parseInt(pairs.get("screen"));
    viewMode = ViewMode.valueOf(pairs.get("viewMode"));
    input = Input.valueOf(pairs.get("input"));
  }

}