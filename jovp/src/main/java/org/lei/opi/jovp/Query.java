package org.lei.opi.jovp;

import java.util.Arrays;

import es.optocom.jovp.Monitor;
import es.optocom.jovp.definitions.ViewMode;

/**
 * Device settings to return upon query from OPI monitor
 *
 * @param distance viewing distance
 * @param fov current horizontal and vertical field of view in degrees of visual angle
 * @param viewMode viewing mode: MONO or STEREO
 * @param input Either 'mouse', 'keypad', or the name of a suitable USB controller
 * @param pseudoGray whether JOVP is using a bit-stealing algorithm
 * @param fullScreen whether JOVP machine is running on full screen mode
 * @param tracking  whether JOVP machine is able to do eye tracking
 * @param maxLum maximum luminance on the R, G, and B channels (cd/m^2)
 * @param gammaFile the path of the gamma file
 * @param monitor display of the OPI JOVP machine
 * @since 0.0.1
 */
public record Query(int distance, float[] fov, ViewMode viewMode, String input, boolean pseudoGray,
                    boolean fullScreen, boolean tracking, double[] maxLum, String gammaFile, Monitor monitor) {

  /**
   * Convert to Json object to return back to R OPI
   * 
   * @return a JSON object with the results of OPI QUERY - should just be { name:value } like a hashmap<String, Object>
   *
   * @since 0.0.1
   */
  public String toJson() {
    StringBuilder str = new StringBuilder("\n  {\n")
      .append("    \"distance\": " + distance + ",\n")
      .append("    \"viewMode\": \"" + viewMode + "\",\n")
      .append("    \"input\": \"" + input + "\",\n")
      .append("    \"pseudoGray\": " + pseudoGray + ",\n")
      .append("    \"fullScreen\": " + fullScreen + ",\n")
      .append("    \"tracking\": " + tracking + ",\n")
      .append("    \"gammaFile\": \"" + gammaFile + "\",\n")
      .append("    \"maxLum\": " + Arrays.toString(maxLum));
    if (monitor != null) {
      int[] colorDepth = monitor.getColorDepth();
      float[] dpi = monitor.getDpi();  
      str.append(",\n")
        .append("    \"xFov\": " + fov[0] + ",\n")
        .append("    \"yFov\": " + fov[1] + ",\n")
        .append("    \"monitorName\": \"" + monitor.getName() + "\",\n")
        .append("    \"width\": " + monitor.getWidth() + ",\n")
        .append("    \"height\": " + monitor.getHeight() + ",\n")
        .append("    \"widthMM\": " + monitor.getWidthMM() + ",\n")
        .append("    \"heightMM\": " + monitor.getHeightMM() + ",\n")
        .append("    \"pixelWidth\": " + 1000 * monitor.getPixelWidth() + ",\n")
        .append("    \"pixelHeight\": " + 1000 * monitor.getPixelHeight() + ",\n")
        .append("    \"aspectRatio\": " + monitor.getPixelAspect() + ",\n")
        .append("    \"xDpi\": " + dpi[0] + ",\n")
        .append("    \"yDpi\": " + dpi[1] + ",\n")
        .append("    \"refreshRate\": " + monitor.getRefreshRate() + ",\n")
        .append("    \"Rbits\": " + colorDepth[0] + ",\n")
        .append("    \"Gbits\": " + colorDepth[1] + ",\n")
        .append("    \"Bbits\": " + colorDepth[2]);
    }
    return str.append("\n  }").toString();
  }

}