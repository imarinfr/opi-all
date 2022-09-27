package org.lei.opi.core.definitions;

import es.optocom.jovp.Monitor;
import es.optocom.jovp.structures.Input;
import es.optocom.jovp.structures.ViewMode;

/**
 * Device settings to return upon query from OPI monitor
 *
 * @param error whether an error happened was seen
 * @param message return message. If error happened, then error message
 * @param distance viewing distance
 * @param viewMode viewing mode: MONO or STEREO
 * @param input input device for responses
 * @param setupDepth depth for each color channel
 * @param fov current horizontal and vertical field of view in degrees of visual angle
 * @param monitor display of the OPI JOVP machine
 * @since 0.0.1
 */
public record Query(boolean error, String message,
                    int distance, ViewMode viewMode, Input input, int setupDepth,
                    double[] fov, Monitor monitor) {

  /**
   * Convert to string to return back to R OPI
   *
   * @since 0.0.1
   */
  public String toJson() {
    StringBuilder str = new StringBuilder("{\n").append("\"error\": " + (error ? 1 : 0) + ",\n").append("\"msg\":");
    if (error) str.append(message); // if error occured, then send message
    else { // otherwise, send results
      str.append("\"distance\": " + distance + ",\n")
        .append("\"viewMode\": " + viewMode + ",\n")
        .append("\"input\": " + input);
      if (monitor != null) {
        int[] colorDepth = monitor.getColorDepth();
        double[] dpi = monitor.getDpi();  
        str.append("\n{\n")
          .append("\"xFov\": " + fov[0] + ",\n")
          .append("\"yFov\": " + fov[1] + ",\n")
          .append("\"monitorName\": " + monitor.getName() + ",\n")
          .append("\"width\": " + monitor.getWidth() + ",\n")
          .append("\"height\": " + monitor.getHeight() + ",\n")
          .append("\"widthMM\": " + monitor.getWidthMM() + ",\n")
          .append("\"heightMM\": " + monitor.getHeightMM() + ",\n")
          .append("\"pixelWidth\": " + 1000 * monitor.getPixelWidth() + ",\n")
          .append("\"pixelHeight\": " + 1000 * monitor.getPixelHeight() + ",\n")
          .append("\"aspectRatio\": " + monitor.getPixelAspect() + ",\n")
          .append("\"xDpi\": " + dpi[0] + ",\n")
          .append("\"yDpi\": " + dpi[1] + ",\n")
          .append("\"refreshRate\": " + monitor.getRefreshRate() + ",\n")
          .append("\"Rbits\": " + colorDepth[0] + ",\n")
          .append("\"Gbits\": " + colorDepth[1] + ",\n")
          .append("\"Bbits\": " + colorDepth[2] + ",\n}");
      }
    }
    return str.append("\n}").toString();
  }

}