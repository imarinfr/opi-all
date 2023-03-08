package org.lei.opi.jovp;

/**
 * Presentation results to report back to OPI monitor
 * 
 * @param seen whether stimulus was seen
 * @param time response time in ms if stimulus was seen or -1 otherwise
 * @param eyex x coordinate of eye fixation at the time of presentation in degrees of visual angle
 * @param eyey y coordinate of eye fixation at the time of presentation in degrees of visual angle
 * @param eyed eye diameter in mm
 * @param eyet time of the recording of eye fixation and diameter relative to stimulus onset in ms
 *
 * @since 0.0.1
 */
public record Response(boolean seen, long time, double eyex, double eyey, double eyed, int eyet) {
                       
  /**
   * Convert to string to return back to monitor and then R OPI
   * 
   * @param tracking Whether to include result of tracking
   *
   * @return a JSON with the results of OPI PRESENT
   * 
   * @since 0.0.1
   */
  public String toJson(boolean tracking) {
    StringBuilder str = new StringBuilder("\n  {\n")
      .append("    \"seen\": " + seen + ",\n")
      .append("    \"time\": " + time);
    if(tracking)
      str.append(",\n")
         .append("    \"eyex\": " + eyex + ",\n")
         .append("    \"eyey\": " + eyey + ",\n")
         .append("    \"eyed\": " + eyed + ",\n")
         .append("    \"eyet\": " + eyet + "\n}")
         .append("    \"eyed\": " + eyed + ",\n}");
    return str.append("\n  }").toString();
  }

}