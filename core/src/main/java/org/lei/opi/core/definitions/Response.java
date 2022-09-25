package org.lei.opi.core.definitions;

/**
 * Presentation results to report back to OPI monitor
 * 
 * @param error whether an error happened was seen
 * @param message return message. If error happened, then error message
 * @param seen whether stimulus was seen
 * @param time response time in ms if stimulus was seen or -1 otherwise
 * @param eyex x coordinate of eye fixation at the time of presentation in degrees of visual angle
 * @param eyey y coordinate of eye fixation at the time of presentation in degrees of visual angle
 * @param eyed eye diameter in mm
 * @param eyet time of the recording of eye fixation and diameter relative to stimulus onset in ms
 *
 * @since 0.0.1
 */
public record Response(boolean error, String message,
                       boolean seen, int time,
                       double eyex, double eyey, double eyed, int eyet) {
                       
  /**
   * Convert to string to return back to monitor and then R OPI
   *
   * @since 0.0.1
   */
  public String toString() {
    StringBuilder str = new StringBuilder("{\n").append("\"error\": " + (error ? 1 : 0) + ",\n").append("\"msg\": ");
    if (error) str.append(message); // if error occured, then send message
    else { // otherwise, send results
      str.append("\n{\n")
          .append("\"seen\": " + seen + ",\n").append("\"time\": " + time + ",\n")
          .append("\"eyex\": " + eyex + ",\n").append("\"eyey\": " + eyey + ",\n")
          .append("\"eyed\": " + eyed + ",\n").append("\"eyet\": " + eyet + "\n}")
          .append("\"eyed\": " + eyed + ",\n}");
    }
    ;
    return str.append("\n}").toString();
  }

}