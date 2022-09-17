package org.lei.opi.jovp;

/**
 * Presentation results to report back to OPI monitor
 * 
 * @param seen whether stimulus was seen
 * @param time response time in ms if stimulus was seen or -1 otherwise
 * @param eyex x coordinate of eye fixation at the time of presentation in degrees of visual angle
 * @param eyey y coordinate of eye fixation at the time of presentation in degrees of visual angle
 * @param eyed eye diameter in mm
 * @param eyet time of the recording of eye fixation and diameter relative to stimulus onset
 *
 * @since 0.0.1
 */
public record Response(boolean seen, int time, double eyex, double eyey, double eyed, int eyet) {

  /**
   * Convert to string to return back to OPI monitor
   *
   * @since 0.0.1
   */
  public String toString() {
    StringBuilder str = new StringBuilder("[{\n");
    str.append("\"seen\": " + seen + ",\n").append("\"time\": " + time + ",\n")
       .append("\"eyex\": " + eyex + ",\n").append("\"eyey\": " + eyey + ",\n")
       .append("\"eyed\": " + eyed + ",\n").append("\"eyet\": " + eyet + "\n}]");
    return str.toString();
  }

}