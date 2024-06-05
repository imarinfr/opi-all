package org.lei.opi.jovp;

import static org.lei.opi.jovp.JsonProcessor.toIntArray;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.lei.opi.core.Jovp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.optocom.jovp.definitions.Paradigm;
import es.optocom.jovp.definitions.ViewMode;

/**
 * Setup for OPI JOVP machine
 * 
 * @param machine the OPI machine
 * @param screen screen number: 0 is main, any number > 0 are external monitors
 * @param physicalSize the physical size of the display (in case it is necessary to input it manually) or empty.
 * @param pseudoGray whether to use bit-stealing during stimulus presentation (for 24-bit machines to be able to show 1786 shades of gray)
 * @param fullScreen whether to run in full screen or windowed mode
 * @param distance viewing distance in mm
 * @param viewMode viewing mode: MONO or STEREO
 * @param input Either 'mouse', 'keypad', or the name of a suitable USB controller
 * @param tracking whether device allows eye tracking
 * @param invGammaFile path of the display-specific calibration file of R, G, B inv gamma functions
 * @param calibration the RGB calibration data
 * @param webcam Information about eye monitoring cameras and where they should be streamed 
 *
 * @since 0.0.1
 */
public record Configuration(Machine machine, int screen, int[] physicalSize, boolean pseudoGray, boolean fullScreen, int distance,
                           ViewMode viewMode, String input, boolean tracking, String invGammaFile, Calibration calibration,
                           WebCamConfiguration webcam) {

    /** Implemented display-based machines */
    enum Machine {IMOVIFA, PICOVR, PHONEHMD, DISPLAY}

    /** {@value PARADIGM} */
    static final Paradigm PARADIGM = Paradigm.CLICKER;
    /** {@value VALIDATION_LAYERS} */
    static final boolean VALIDATION_LAYERS = false; // PsychoEngine.VALIDATION_LAYERS;
    /** {@value API_DUMP} */
    static final boolean API_DUMP = false;

    /** {@value WRONG_SCREEN} */
    private static final String WRONG_SCREEN = "'screen' value has to be 0 (default screen) or positive. It is %s";
    /** {@value WRONG_SCREEN} */
    private static final String WRONG_PHYSICAL_SIZE = "'physicalSize' has to be an array with two positive integers for width and size in mm. It is %s";
    /** {@value WRONG_DISTANCE} */
    private static final String WRONG_DISTANCE = "'distance' cannot be negative, you silly goose. It is %s";

    /**
     * Parse JSON configuration file
     * 
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
    public static Configuration set(HashMap<String, Object> args) throws IllegalArgumentException, ClassCastException, IOException, NullPointerException {
        Machine machine = Machine.valueOf(args.get("machine").toString().toUpperCase());

        int screen = ((Double) args.get("screen")).intValue();
        if(screen < 0)
            throw new IllegalArgumentException(String.format(WRONG_SCREEN, screen));

        int[] physicalSize = toIntArray(args.get("physicalSize"));
            if(physicalSize.length != 0 && (physicalSize.length != 2 || physicalSize[0] <= 0 || physicalSize[1] <= 0))
                throw new IllegalArgumentException(String.format(WRONG_PHYSICAL_SIZE, Arrays.toString(physicalSize)));

        int distance = ((Double) args.get("distance")).intValue();
        if(distance < 0)
            throw new IllegalArgumentException(String.format(WRONG_DISTANCE, distance));

        ViewMode viewMode = ViewMode.valueOf(args.get("viewMode").toString().toUpperCase());

        String invGammaFile = args.get("gammaFile").toString();

        return new Configuration(machine, screen, physicalSize, (boolean) args.get("pseudoGray"), (boolean) args.get("fullScreen"),
                                 distance, viewMode, args.get("input").toString().toUpperCase(),
                                 (boolean) args.get("tracking"), invGammaFile, loadCalibration(invGammaFile), 
                                 WebCamConfiguration.set(args));
    }

    /**
     * Fill the R, G, and B invgamma functions
     * 
     * @param invGammaFile resource file or path with display-specific calibration of R, G, B inverse gamma functions
     * 
     * @throws IllegalArgumentException Illegal argument for screen, distance, or depth
     * @throws ClassCastException Cast exception
     * @throws IOException IO exception for calibration file
     *
     * @since 0.0.1
     */
    private static Calibration loadCalibration(String invGammaFile) throws IllegalArgumentException, ClassCastException, IOException {
        //List<String> files = IOUtils.readLines(Jovp.class.getResourceAsStream("."), StandardCharsets.UTF_8);
        //System.out.println("resources: " + files);

        Gson gson = new Gson();
        String jsonStr;
        System.out.println("Attempting to load inverse gamma file from path " + invGammaFile + "...");
        // Get calibration from a path or from resources
        try(InputStream inputStream = new FileInputStream(invGammaFile)) {
            jsonStr = calibrationFromPath(invGammaFile);
        } catch (IOException e) { // if inv gamma not path, then see if it is in resources
            System.out.println("\tCannot load inverse gamma file from path " + invGammaFile + " trying core/resources");
            try {
              jsonStr = calibrationFromResources(invGammaFile);
            } catch (Exception e2) {
              System.out.println("\tCannot load inverse gamma file from resources either. Giving up.");
              // if inv gamma not path and not a resource file, then throw IOException
              throw e2;
            }
        }
        System.out.println("Success");

        HashMap<String, Object> pairs = gson.fromJson(jsonStr, new TypeToken<HashMap<String, Object>>() {}.getType());

        double[] invGammaRed = ((ArrayList<?>) pairs.get("invGammaRed")).stream().mapToDouble(Double.class::cast).toArray();
        double[] invGammaGreen = ((ArrayList<?>) pairs.get("invGammaGreen")).stream().mapToDouble(Double.class::cast).toArray();
        double[] invGammaBlue = ((ArrayList<?>) pairs.get("invGammaBlue")).stream().mapToDouble(Double.class::cast).toArray();
        return new Calibration(
          (int)Math.round((double)pairs.get("lumPrecision")),
          (double)pairs.get("maxLum"),
          (int)Math.round((double)pairs.get("maxPixel")),
          invGammaRed, invGammaGreen, invGammaBlue);
    }

    /**
     * Get calibration from Core resources
     * 
     * @param file resource file for display-specific calibration file of R, G, B inv gamma functions
     *
     * @throws IOException
     *
     * @since 0.0.1
     */
    private static String calibrationFromResources(String file) throws IOException {
      InputStream inputStream = Jovp.class.getResourceAsStream(file);
      return IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));
    }

    /**
     * Get calibration from a path
     * 
     * @param file path to a file with display-specific calibration of R, G, B inv gamma functions
     *
     * @throws IOException
     *
     * @since 0.0.1
     */
    private static String calibrationFromPath(String file) throws IOException {
      InputStream inputStream = new FileInputStream(file);
      return IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));
    }
    
    public Configuration withWebCam(WebCamConfiguration webcam) {
        return new Configuration(
            machine(), screen(), physicalSize(), pseudoGray(), fullScreen(), distance(),
            viewMode(), input(), tracking(), invGammaFile(), calibration(),
            webcam);
    }
}