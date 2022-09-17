package org.lei.opi.jovp;

import es.optocom.jovp.Monitor;
import es.optocom.jovp.structures.Input;
import es.optocom.jovp.structures.ViewMode;

/**
 * Device settings to return upon query from OPI monitor
 *
 * @param distance viewing distance
 * @param viewMode viewing mode: MONO or STEREO
 * @param input input device for responses
 * @param setupDepth depth for each color channel
 * @param fov current horizontal and vertical field of view in degrees of visual angle
 * @param monitor display of the OPI JOVP machine
 * @since 0.0.1
 */
public record Query(int distance, ViewMode viewMode, Input input, int setupDepth, double[] fov, Monitor monitor) {

  /**
   * Convert to string to return back to OPI monitor
   *
   * @since 0.0.1
   */
  public String toString() {
    StringBuilder str = new StringBuilder("[{\n");
    str.append("\"distance\": " + distance + ",\n")
       .append("\"viewMode\": " + viewMode + ",\n")
       .append("\"input\": " + input);
    if (monitor != null) {
      int[] colorDepth = monitor.getColorDepth();
      double[] dpi = monitor.getDpi();  
      str.append(",\n")
         .append("\"xfov\": " + fov[0] + ",\n")
         .append("\"yfov\": " + fov[1] + ",\n")
         .append("\"monitorName\": " + monitor.getName() + ",\n")
         .append("\"width\": " + monitor.getWidth() + ",\n")
         .append("\"height\": " + monitor.getHeight() + ",\n")
         .append("\"widthMM\": " + monitor.getWidthMM() + ",\n")
         .append("\"heightMM\": " + monitor.getHeightMM() + ",\n")
         .append("\"pixelWidth\": " + 1000 * monitor.getPixelWidth() + ",\n")
         .append("\"pixelHeight\": " + 1000 * monitor.getPixelHeight() + ",\n")
         .append("\"pixelHeight\": " + monitor.getPixelAspect() + ",\n")
         .append("\"xdpi\": " + dpi[0] + ",\n")
         .append("\"ydpi\": " + dpi[1] + ",\n")
         .append("\"refreshRate\": " + monitor.getRefreshRate() + ",\n")
         .append("\"rdepth\": " + colorDepth[0] + ",\n")
         .append("\"gdepth\": " + colorDepth[1] + ",\n")
         .append("\"bdepth\": " + colorDepth[2]);
    }
    str.append("\n}]");
    return str.toString();
  }

}