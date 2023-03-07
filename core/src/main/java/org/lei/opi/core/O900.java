package org.lei.opi.core;

import static org.lei.opi.core.definitions.JsonProcessor.toIntArray;
import static org.lei.opi.core.definitions.JsonProcessor.toDoubleArray;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;

import javafx.scene.Scene;

import org.lei.opi.core.OpiListener.Packet;

/**
 * Octopus O900 client
 *
 * @since 0.0.1
 */
public class O900 extends OpiMachine {

  /** Allowed eye values */
  private enum Eye {LEFT, RIGHT}
  /** Allowed background luminances */
  private enum BackgroundLuminance {BG_OFF, BG_1, BG_10, BG_100}
  /** Allowed background colors */
  private enum BackgroundColor {WHITE, YELLOW}
  /** Allowed fixation types */
  private enum Fixation {CENTER, CROSS, RING}
  /** Stimulus type */
  private enum Type {STATIC, KINETIC}
  /** Allowed stimulus colors */
  private enum Color {WHITE, RED, BLUE}
  /** Allowed sizes */
  private enum Size {GI, GII, GIII, GIV, GV, GVI}

  /** {@value NA_STRING} */
  private static final String NA_STRING = "NA";
  /** {@value OPI_INITIALIZE} */
  private static final String OPI_INITIALIZE = "OPI_INITIALIZE ";
  /** {@value OPI_SET_BACKGROUND} */
  private static final String OPI_SET_BACKGROUND = "OPI_SET_BACKGROUND";
  /** {@value OPI_PRESENT_STATIC} */
  private static final String OPI_PRESENT_STATIC = "OPI_PRESENT_STATIC";
  /** {@value OPI_PRESENT_STATIC_F310} */
  private static final String OPI_PRESENT_STATIC_F310 = "OPI_PRESENT_STATIC_F310";
  /** {@value OPI_PRESENT_KINETIC} */
  private static final String OPI_PRESENT_KINETIC = "OPI_PRESENT_KINETIC";
  /** {@value OPI_CLOSE} */
  private static final String OPI_CLOSE = "OPI_CLOSE";
  /** {@value OPI_INITIALIZE_FAILED} */
  private static final String OPI_INITIALIZE_FAILED = "Problem with OPI_INITIALIZE: ";
  /** {@value OPI_SET_BACKGROUND_FAILED} */
  private static final String OPI_SET_BACKGROUND_FAILED = "Problem with OPI_SET_BACKGROUND: ";
  /** {@value LIST_SIZE_TOO_LONG} */
  private static final String XY_SIZE_TOO_LONG = "Stimulus list sizes must be either 1 or 2 (for sending the position of the next stimulus).";
  /** {@value INCONSISTENT_XY_SIZES} */
  private static final String INCONSISTENT_XY_SIZES = "Lists for 'x' and 'y' coordinates have to be of the same size..";
  /** {@value WRONG_SIZE} */
  private static final String WRONG_SIZE = "Wrong stimulus size. It is ";
  /** {@value WRONG_PRESENTATION_TIME} */
  private static final String WRONG_STATIC_PRESENTATION_TIME = "Presentation time cannot be greater than response window";
  /** {@value WRONG_PRESENTATION_TIME} */
  private static final String WRONG_SPEED_SIZE = "List of speeds for kinetic presentations must be length of x or y minus 1";

  /** O900 constants */
  private static int EYE_RIGHT;
  private static int EYE_LEFT;
  private static int EYE_BOTH;
  private static int EYE_BINOCULAR;
  private static int EYE_UNDEF;
  private static double BLIND_SPOT_POS_X;
  private static double BLIND_SPOT_POS_Y;
  private static double BLIND_SPOT_WIDTH;
  private static double BLIND_SPOT_HEIGHT;
  private static int MIN_STIMULUS_DURATION;
  private static int MAX_STIMULUS_DURATION;
  private static int COL_WHITE;
  private static int COL_BLUE;
  private static int COL_YELLOW;
  private static int COL_RED;
  private static int COL_GREEN;
  private static int FIX_CENTER;
  private static int FIX_CROSS;
  private static int FIX_RING;
  private static int BG_OFF;
  private static int BG_1;
  private static int BG_10;
  private static int BG_100;
  private static int STIM_WHITE;
  private static int STIM_BLUE;
  private static int STIM_RED;
  private static int BG_WHITE;
  private static int BG_YELLOW;
  private static int MET_COL_WW;
  private static int MET_COL_BY;
  private static int MET_COL_RW;
  private static int MET_COL_BLUE_WHITE;
  private static int MET_COL_RED_YELLOW;
  private static int MET_COL_WHITE_YELLOW;

  public static class Settings extends OpiMachine.Settings {
    public String eyeSuiteDirectory;
    public String gazeFeedPath;
    public boolean bigWheel;
    public boolean max10000;
    public boolean f310;
  };

  private Settings settings;
  public Settings getSettings() { return this.settings; }

  public O900(Scene parentScene) throws RuntimeException {
    super(parentScene);
    this.settings = (Settings) OpiMachine.fillSettings(this.getClass().getSimpleName());
    this.parentScene = parentScene;
  }

  /**
   * Get constants
   * 
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   *
   * @since 0.0.1
  private static void fillConstants() {
    Field[] fields = com.hs.eyesuite.ext.extperimetryviewer.peristatic.data.exam.Const.class.getDeclaredFields();
    EYE_RIGHT = getFieldValue("EYE_RIGHT", fields);
    EYE_LEFT = getFieldValue("EYE_LEFT", fields);
    EYE_BOTH = getFieldValue("EYE_BOTH", fields);
    EYE_BINOCULAR = getFieldValue("EYE_BINOCULAR", fields);
    EYE_UNDEF = getFieldValue("EYE_UNDEF", fields);
    BLIND_SPOT_POS_X = getFieldValue("BLIND_SPOT_POS_X", fields) / 10.0;
    BLIND_SPOT_POS_Y = getFieldValue("BLIND_SPOT_POS_Y", fields) / 10.0;
    BLIND_SPOT_WIDTH = getFieldValue("BLIND_SPOT_WIDTH", fields) / 10.0;
    BLIND_SPOT_HEIGHT = getFieldValue("BLIND_SPOT_HEIGHT", fields) / 10.0;
  }
   */
  
  /**
   * Get O900 specific constants
   * 
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   *
   * @since 0.0.1
  private static void fillO900Constants() {
    Field[] fields = com.hs.eyesuite.ext.extperimetry.octo900.ifocto.device.OCTO900.class.getDeclaredFields();
    MIN_STIMULUS_DURATION = getFieldValue("MIN_STIMULUS_DURATION", fields);
    MAX_STIMULUS_DURATION = getFieldValue("MAX_STIMULUS_DURATION", fields);
    FIX_CENTER = getFieldValue("FIX_CENTRE", fields);
    FIX_CROSS = getFieldValue("FIX_CROSS", fields);
    FIX_RING = getFieldValue("FIX_RING", fields);
    BG_OFF = getFieldValue("BG_OFF", fields);
    BG_1 = getFieldValue("BG_1", fields);
    BG_10 = getFieldValue("BG_10", fields);
    BG_100 = getFieldValue("BG_100", fields);
    COL_WHITE = getFieldValue("COL_WHITE", fields);
    COL_BLUE = getFieldValue("COL_BLUE", fields);
    COL_YELLOW = getFieldValue("COL_YELLOW", fields);
    COL_RED = getFieldValue("COL_RED", fields);
    COL_GREEN = getFieldValue("COL_GREEN", fields);
    STIM_WHITE = getFieldValue("STIM_WHITE", fields);
    STIM_BLUE = getFieldValue("STIM_BLUE", fields);
    STIM_RED = getFieldValue("STIM_RED", fields);
    BG_WHITE = getFieldValue("BG_WHITE", fields);
    BG_YELLOW = getFieldValue("BG_YELLOW", fields);
    MET_COL_WW = getFieldValue("MET_COL_WW", fields);
    MET_COL_BY = getFieldValue("MET_COL_BY", fields);
    MET_COL_RW = getFieldValue("MET_COL_RW", fields);
    MET_COL_BLUE_WHITE = getFieldValue("MET_COL_BLUE_WHITE", fields);
    MET_COL_RED_YELLOW = getFieldValue("MET_COL_RED_YELLOW", fields);
    MET_COL_WHITE_YELLOW = getFieldValue("MET_COL_WHITE_YELLOW", fields);
  }

  private static int getFieldValue(String constant, Field[] fields) {
    Integer value = Arrays.stream(fields).filter(field -> field.getName() == constant).findFirst()
                         .map(field -> getValue(field)).map(Integer.class::cast)
                         .orElse(null);
    return value != null ? (int) value : - 1;
  }
   */

  private static Integer getValue(Field field) {
    try {
      return (int) field.get(null);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      return null;
    }
  }

  /**
   * opiInitialise: initialize OPI
   * 
   * @param args A map of name:value pairs for Params
   * 
   * @return A JSON object with machine specific initialise information
   * 
   * @since 0.0.1
   */
  public Packet initialize(HashMap<String, Object> args) {
      return new OpiListener.Packet(String.format(CONNECTED_TO_HOST, settings.ip, settings.port));
  };

  /**
   * opiQuery: Query device
   * 
   * @return settings and state machine state
   *
   * @since 0.0.1
   */
  public Packet query() {
    return new OpiListener.Packet(queryResults());
  };

  /**
   * opiSetup: Change device background and overall settings
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  @Parameter(name = "eye", className = Eye.class, desc = "Eye to set.", defaultValue = "left")
  @Parameter(name = "bgLum", className = BackgroundLuminance.class, desc = "Background luminance for eye.", min = 0, max = 3183.099, defaultValue = "10")
  @Parameter(name = "bgCol", className = BackgroundColor.class, desc = "Background color for eye.", defaultValue = "white")
  @Parameter(name = "fixShape", className = Fixation.class, desc = "Fixation target.", defaultValue = "center")
  @Parameter(name = "fixIntensity", className = Double.class, desc = "Fixation intensity(from 0% to 100%).", min = 0, max = 100, defaultValue = "50")
  @Parameter(name = "pres", className = Double.class, desc = "Volume for auditory feedback when a stimulus is presented: 0 means no buzzer.",min = 0, max = 3, defaultValue = "0")
  @Parameter(name = "resp", className = Double.class, desc = "Volume for auditory feedback when observer presses the clicker: 0 means no buzzer.", min = 0, max = 3, defaultValue = "0")
  public Packet setup(HashMap<String, Object> args) {
    if (this.socket.isClosed()) return OpiListener.error(DISCONNECTED_FROM_HOST);
    StringBuilder message;
    Packet result;
    try {
      // Prepare OPI_INITIALIZE instruction
      message = new StringBuilder(OPI_INITIALIZE).append(" ")
        .append("\"").append(settings.eyeSuiteDirectory).append("\"").append(" ")
        .append("\"").append(((String) args.get("eye"))).append("\"").append(" ")
        .append((int) ((double) args.get("pres"))).append(" ")
        .append((int) ((double) args.get("resp"))).append(" ")
        .append(settings.max10000).append(" ")
        .append("\"").append(settings.gazeFeedPath).append("\"").append(" ");
      // Send OPI_INITIALIZE instruction
      try {
        this.send(message.toString());
        result = this.receive();
      } catch (IOException e) {
        return OpiListener.error(OPI_INITIALIZE_FAILED, e);
      }
      if (result.getError()) return OpiListener.error(OPI_INITIALIZE_FAILED + result);

      // Prepare OPI_SET_BACKGROUND instruction
      int bgCol = switch (BackgroundColor.valueOf(((String) args.get("bgCol")).toUpperCase())) {
        case WHITE -> BG_WHITE;
        case YELLOW -> BG_YELLOW;
      };
      int bgLum = switch (BackgroundLuminance.valueOf(((String) args.get("bgLum")).toUpperCase())) {
        case BG_OFF -> BG_OFF;
        case BG_1 -> BG_1;
        case BG_10 -> BG_10;
        case BG_100 -> BG_100;
      };
      int fixShape = switch (Fixation.valueOf(((String) args.get("fixShape")).toUpperCase())) {
        case CENTER -> FIX_CENTER;
        case CROSS -> FIX_CROSS;
        case RING -> FIX_RING;
      };
      message = new StringBuilder(OPI_SET_BACKGROUND).append(" ")
        .append(bgCol).append(" ")
        .append(bgLum).append(" ")
        .append(fixShape).append(" ")
        .append((int) ((double) args.get("fixIntensity")));  
      // Send OPI_SET_BACKGROUND instruction
      try {
        this.send(message.toString());
        result = this.receive();
      } catch (IOException e) {
        return OpiListener.error(OPI_SET_BACKGROUND_FAILED, e);
      }
      if (result.getError()) return OpiListener.error(OPI_SET_BACKGROUND_FAILED + result);

      return new OpiListener.Packet(queryResults());
    } catch (ClassCastException | IllegalArgumentException e) {
      return OpiListener.error(OPI_SETUP_FAILED, e);
    }
  }

  /**
   * opiPresent: Present OPI stimulus in perimeter
   * 
   * @param args pairs of argument name and value
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  @Parameter(name = "type", className = Type.class, desc = "Stimulus type: STATIC or KINETIC.", defaultValue = "static")
  @Parameter(name = "x", className = Double.class, desc = "List of x co-ordinates of stimuli (degrees).", isList = true, min = -90, max = 90, defaultValue = "list(0)")
  @Parameter(name = "y", className = Double.class, desc = "List of y co-ordinates of stimuli (degrees).", isList = true, min = -90, max = 90, defaultValue = "list(0)")
  @Parameter(name = "lum", className = Double.class, desc = "List of stimuli luminances (cd/m^2).", min = 0, max = 3183.099, defaultValue = "3183.099")
  @Parameter(name = "size", className = Size.class, desc = "Stimulus size (degrees). Can be Goldmann Size I to V (or VI if device has a big wheel)", defaultValue = "list('GV')")
  @Parameter(name = "color", className = Color.class, desc = "Stimulus color (degrees).", defaultValue = "white")
  @Parameter(name = "t", className = Double.class, desc = "List of Stimulus presentation times (ms). For STATIC, list must be of length 1. For KINETIC, it must the same length and 'x' and 'y' co-ordinates minus 1", isList = true, optional = true, min = 0, defaultValue = "list(200)")
  @Parameter(name = "w", className = Double.class, desc = "[STATIC] Response window (ms).", optional = true, min = 0, defaultValue = "1500")
  @ReturnMsg(name = "res.msg.eyex", className = Double.class, desc = "x co-ordinates of pupil at times eyet (degrees).")
  @ReturnMsg(name = "res.msg.eyey", className = Double.class, desc = "y co-ordinates of pupil at times eyet (degrees).")
  @ReturnMsg(name = "res.msg.x", className = Double.class, desc = "[KINETIC] x co-ordinate when oberver responded (degrees).")
  @ReturnMsg(name = "res.msg.y", className = Double.class, desc = "[KINETIC] y co-ordinate when oberver responded (degrees).")
  public Packet present(HashMap<String, Object> args) {
    if (this.socket.isClosed()) return OpiListener.error(DISCONNECTED_FROM_HOST);
    try {
      // get common parameters
      Type type = Type.valueOf(((String) args.get("type")).toUpperCase());
      double[] x = toDoubleArray(args.get("x"));
      double[] y = toDoubleArray(args.get("y"));
      if (x.length != y.length) throw new IllegalArgumentException(INCONSISTENT_XY_SIZES);
      double lum = (double) args.get("lum");
      int size = switch (Size.valueOf(((String) args.get("size")).toUpperCase())) {
        case GI -> 1;
        case GII -> 2;
        case GIII -> 3;
        case GIV -> 4;
        case GV -> 5;
        case GVI -> 6;
      };
      if (!settings.bigWheel && size == 6) throw new IllegalArgumentException(WRONG_SIZE + size);
      String color =  Color.valueOf(((String) args.get("color")).toUpperCase()).toString().toLowerCase();
      int[] t = toIntArray(args.get("t"));
      // get specific parameters and mount the message to send
      String message = switch (type) {
        case STATIC -> presentStatic(x, y, lum, size, color, t, (int) (double) args.get("w"));
        case KINETIC -> presentKinetic(x, y, lum, size, color, t);
      };

      try {
        this.send(message.toString());
        return new OpiListener.Packet("\"To be constructed\"");  // parseResult(type, this.receive())); TODO
      } catch (IOException e) {
        return OpiListener.error(OPI_PRESENT_FAILED, e);
      }
    } catch (ClassCastException | IllegalArgumentException | SecurityException e) {
      return OpiListener.error(OPI_PRESENT_FAILED, e);
    }
  }

  /**
   * opiClose: Send "close" to real machine and then close the connection to the real machine.
   * 
   * @param args pairs of argument name and value
   *
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  public Packet close() {
    try {
      this.send(OPI_CLOSE);
      this.closeSocket();
    } catch (IOException e) {
      return OpiListener.error(OPI_CLOSE, e);
    }
    return new OpiListener.Packet(true, DISCONNECTED_FROM_HOST);
  };

  /**
   * Parse query results
   * 
   * @return A JSON object with query results
   *
   * @since 0.0.1
   */
  private String queryResults() {
    return new StringBuilder("\n \"{\n")
    .append("    \"EYE_RIGHT\": " + EYE_RIGHT + ",\n")
    .append("    \"EYE_LEFT\": " + EYE_LEFT + ",\n")
    .append("    \"EYE_BOTH\": " + EYE_BOTH + ",\n")
    .append("    \"EYE_BINOCULAR\": " + EYE_BINOCULAR + ",\n")
    .append("    \"EYE_UNDEF\": " + EYE_UNDEF + ",\n")
    .append("    \"BLIND_SPOT_POS_X\": " + BLIND_SPOT_POS_X + ",\n")
    .append("    \"BLIND_SPOT_POS_Y\": " + BLIND_SPOT_POS_Y + ",\n")
    .append("    \"BLIND_SPOT_WIDTH\": " + BLIND_SPOT_WIDTH + ",\n")
    .append("    \"BLIND_SPOT_HEIGHT\": " + BLIND_SPOT_HEIGHT + ",\n")
    .append("    \"MIN_STIMULUS_DURATION\": " + MIN_STIMULUS_DURATION + ",\n")
    .append("    \"MAX_STIMULUS_DURATION\": " + MAX_STIMULUS_DURATION + ",\n")
    .append("    \"COL_WHITE\": " + COL_WHITE + ",\n")
    .append("    \"COL_BLUE\": " + COL_BLUE + ",\n")
    .append("    \"COL_YELLOW\": " + COL_YELLOW + ",\n")
    .append("    \"COL_RED\": " + COL_RED + ",\n")
    .append("    \"COL_GREEN\": " + COL_GREEN + ",\n")
    .append("    \"FIX_CENTER\": " + FIX_CENTER + ",\n")
    .append("    \"FIX_CROSS\": " + FIX_CROSS + ",\n")
    .append("    \"FIX_RING\": " + FIX_RING + ",\n")
    .append("    \"BG_OFF\": " + BG_OFF + ",\n")
    .append("    \"BG_1\": " + BG_1 + ",\n")
    .append("    \"BG_10\": " + BG_10 + ",\n")
    .append("    \"BG_100\": " + BG_100 + ",\n")
    .append("    \"STIM_WHITE\": " + STIM_WHITE + ",\n")
    .append("    \"STIM_BLUE\": " + STIM_BLUE + ",\n")
    .append("    \"STIM_RED\": " + STIM_RED + ",\n")
    .append("    \"BG_WHITE\": " + BG_WHITE + ",\n")
    .append("    \"BG_YELLOW\": " + BG_YELLOW + ",\n")
    .append("    \"MET_COL_WW\": " + MET_COL_WW + ",\n")
    .append("    \"MET_COL_BY\": " + MET_COL_BY + ",\n")
    .append("    \"MET_COL_RW\": " + MET_COL_RW + ",\n")
    .append("    \"MET_COL_BLUE_WHITE\": " + MET_COL_BLUE_WHITE + ",\n")
    .append("    \"MET_COL_RED_YELLOW\": " + MET_COL_RED_YELLOW + ",\n")
    .append("    \"MET_COL_WHITE_YELLOW\": " + MET_COL_WHITE_YELLOW)
    .append("\n  }\"").toString();
  };

  /**
   * Build OPI_PRESENT_STATIC or OPI_PRESENT_STATIC_F310 command
   * 
   * @param x list of x coordinates for static stimulus presentation
   * @param y list of y coordinates for static stimulus presentation
   * @param lum stimulus luminance
   * @param size stimulus size
   * @param color stimulus color
   * @param t stimulus presentation time
   * @param w response window
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  private String presentStatic(double[] x, double[] y, double lum, int size, String color, int[] t, int w) {
    if (t[0] > w) throw new IllegalArgumentException(WRONG_STATIC_PRESENTATION_TIME);
    double xNext, yNext;
    switch (x.length) {
      case 1 -> {
        xNext = x[0];
        yNext = y[0];
      }
      case 2 -> { // position of the next stimulus
        xNext = x[1];
        yNext = y[1];
      }
      default -> throw new IllegalArgumentException(XY_SIZE_TOO_LONG);
    }
    return new StringBuilder(settings.f310 ? OPI_PRESENT_STATIC_F310 : OPI_PRESENT_STATIC).append(" ")
      .append((int) Math.round(10.0 * x[0])).append(" ")
      .append((int) Math.round(10.0 * y[0])).append(" ")
      .append((int) Math.round(10.0 * cdToDecibel(lum))).append(" ")
      .append(size).append(" ")
      .append(t[0]).append(" ")
      .append(w).append(" ")
      .append((int) Math.round(10.0 * xNext)).append(" ")
      .append((int) Math.round(10.0 * yNext)).append(" ")
      .append(color).toString();
  }

  /**
   * Build OPI_PRESENT_KINETIC command
   * 
   * @param x list of x coordinates for kinetic stimulus presentation
   * @param y list of y coordinates for kinetic stimulus presentation
   * @param lum stimulus luminance
   * @param size stimulus size
   * @param color stimulus color
   * @param t time between stimulus presentation segments
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  private String presentKinetic(double[] x, double[] y, double lum, int size, String color, int[] t) {
    if (t.length != x.length - 1) throw new IllegalArgumentException(WRONG_SPEED_SIZE);
    StringBuilder message = new StringBuilder(OPI_PRESENT_KINETIC).append(" ")
      .append(x.length).append(" ");
      for (int i = 0; i < x.length; i++)
        message.append((int) Math.round(10.0 * x[i])).append(" ")
               .append((int) Math.round(10.0 * y[i])).append(" ");
      message.append((int) Math.round(-100.0 * Math.log10(lum / (1000 / Math.PI)))).append(" ")
             .append(size).append(" ");
      for (int i = 0; i < t.length; i++)
        message.append((int) Math.round(10.0 * t[i])).append(" ");
      return message.toString();
  }

  /**
   * For static stimuli, convert from luminance in cd/m^2 to dB
   * It depends on the maximum luminance
   * 
   * @return Luminace level in dB
   *
   * @since 0.0.1
   */
  private double cdToDecibel(double lum) {
    return -10.0 * Math.log10(lum / ((settings.max10000 ? 10000.0 : 4000.0) / Math.PI));
  }

  /**
   * Parse results: construct JSON from the 1-liner string from O900
   * 
   * @param received  1-liner string received from O900
   * 
   * @return A JSON object with return messages
   *
   * @since 0.0.1
   */
  private String parseResult(Type type, String received) {
    String[] message = received.split("\\|\\|\\|");
    if (message[0] != "null") OpiListener.error(OPI_PRESENT_FAILED + "Error code received is: " + message[0]);
    StringBuilder jsonStr = new StringBuilder("\n  {\n")
      .append("    \"seen\": " + message[1] + ",\n")
      .append("    \"time\": " + message[2]);
    switch (type) {
      case STATIC -> {
        jsonStr.append(",\n")
        .append("    \"eyex\": " + (message.length == 5 ? message[3] : NA_STRING) + ",\n")
        .append("    \"eyey\": " + (message.length == 5 ? message[4] : NA_STRING));
      }
      case KINETIC -> {
        jsonStr.append(",\n")
        .append("    \"x\": " + message[3] + ",\n")
        .append("    \"y\": " + message[4] + ",\n")
        .append("    \"eyex\": " + (message.length == 7 ? message[5] : NA_STRING) + ",\n")
        .append("    \"eyey\": " + (message.length == 7 ? message[6] : NA_STRING));
      }
    }
    return jsonStr.append("\n  }").toString();
  }

}