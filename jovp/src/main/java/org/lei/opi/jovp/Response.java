package org.lei.opi.jovp;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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
public class Response extends TypeAdapter<Response> {
    boolean seen; 
    long time;
    double eyex; 
    double eyey;
    double eyed;
    int eyet;
    
    Response(boolean seen, long time, double eyex, double eyey, double eyed, int eyet) {
        this.seen = seen;
        this.time = time;
        this.eyex = eyex;
        this.eyey = eyey;
        this.eyed = eyed;
        this.eyet = eyet;
    }

    public Response() { this(false, -1, 0, 0, 0, 0); }  // need this for gsonbuilder.registerTypeAdapter

    public void write(JsonWriter out, Response value) throws IOException {
        out.beginObject();
        out.name("seen").value(value.seen);
        out.name("time").value(value.time);
        out.name("eyex").value(value.eyex);
        out.name("eyey").value(value.eyey);
        out.name("eyed").value(value.eyed);
        out.name("eyet").value(value.eyet);
        out.endObject();
    }

        // defaults written by copilot
    public Response read(JsonReader in) throws IOException {
        in.beginObject();
        boolean seen = false;
        long time = -1;
        double eyex = 0;
        double eyey = 0;
        double eyed = 0;
        int eyet = 0;
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("seen")) {
                seen = in.nextBoolean();
            } else if (name.equals("time")) {
                time = in.nextLong();
            } else if (name.equals("eyex")) {
                eyex = in.nextDouble();
            } else if (name.equals("eyey")) {
                eyey = in.nextDouble();
            } else if (name.equals("eyed")) {
                eyed = in.nextDouble();
            } else if (name.equals("eyet")) {
                eyet = in.nextInt();
            } else {
                throw new IOException("Unexpected field in converting Json to Response: " + name);
            }
        }
        in.endObject();
        return new Response(seen, time, eyex, eyey, eyed, eyet);
    }

  /**
   * Convert to string to return back to monitor and then R OPI
   * 
   * @param tracking Whether to include result of tracking
   *
   * @return a JSON with the results of OPI PRESENT
   * 
   * @since 0.0.1
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
   */

}