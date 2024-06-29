package org.lei.opi.jovp;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Presentation results to report back to OPI monitor from OpiLogic (used for present and query to get eye position)
 * 
 * @param seen whether stimulus was seen
 * @param time response time in ms if stimulus was seen or -1 otherwise
 * @param eyexStart x coordinate of eye fixation at the time of presentation (pixels)
 * @param eyeyStart y coordinate of eye fixation at the time of presentation (pixels)
 * @param eyedStart eye diameter (pixels)
 * @param eyetStart time of the recording of eye fixation and diameter relative to stimulus onset in ms
 * @param eyexEnd x coordinate of eye fixation at button press or response window expiry (pixels)
 * @param eyeyEnd y coordinate of eye fixation at button press of response window expiry (pixels)
 * @param eyedEnd eye diameter at button press of response window expiry (pixels)
 * @param eyetEnd time of the recording of eye fixation and diameter relative to stimulus onset in ms
 *
 * @since 0.0.1
 */
public class Response extends TypeAdapter<Response> {
    boolean seen; 
    long time;

    double eyexStart; 
    double eyeyStart;
    double eyedStart;
    int eyetStart;

    double eyexEnd; 
    double eyeyEnd;
    double eyedEnd;
    int eyetEnd;
    
    Response(boolean seen, long time,
            double eyexStart, double eyeyStart, double eyedStart, int eyetStart,
            double eyexEnd, double eyeyEnd, double eyedEnd, int eyetEnd) {
        this.seen = seen;
        this.time = time;
        this.eyexStart = eyexStart;
        this.eyeyStart = eyeyStart;
        this.eyedStart = eyedStart;
        this.eyetStart = eyetStart;
        this.eyexEnd = eyexEnd;
        this.eyeyEnd = eyeyEnd;
        this.eyedEnd = eyedEnd;
        this.eyetEnd = eyetEnd;
    }

    Response(Response r) {
        this.seen = r.seen;
        this.time = r.time;
        this.eyexStart = r.eyexStart;
        this.eyeyStart = r.eyeyStart;
        this.eyedStart = r.eyedStart;
        this.eyetStart = r.eyetStart;
        this.eyexEnd = r.eyexEnd;
        this.eyeyEnd = r.eyeyEnd;
        this.eyedEnd = r.eyedEnd;
        this.eyetEnd = r.eyetEnd;
    }

    public Response() { this(false, -1, 0, 0, 0, 0, 0, 0, 0, 0); }  // need this for gsonbuilder.registerTypeAdapter
    public Response(boolean seen, long time) { this(seen, time, 0, 0, 0, 0, 0, 0, 0, 0); }  // need this for gsonbuilder.registerTypeAdapter

    /**
     * 
     * @param isStart  True for updating eyexStart, eyeStart, eyedStart, eyetStart; False for eyexEnd, eyeyEnd, eyedEnd, eyetEnd
     * @param x Value for eyexStart or eyexEnd
     * @param y Value for eyeyStart or eyeyEnd
     * @param d Value for eyedStart or eyedEnd
     * @param t Value for eyetStart or eyetEnd
     */
    public void updateEye(boolean isStart, double x, double y, double d, int t) {
        if (isStart) {
            eyexStart = x;
            eyeyStart = y;
            eyedStart = d;
            eyetStart = t;
        } else {
            eyexEnd = x;
            eyeyEnd = y;
            eyedEnd = d;
            eyetEnd = t;
        }
    }
    public void write(JsonWriter out, Response value) throws IOException {
        out.beginObject();
        out.name("seen").value(value.seen);
        out.name("time").value(value.time);
        out.name("eyexStart").value(value.eyexStart);
        out.name("eyeyStart").value(value.eyeyStart);
        out.name("eyedStart").value(value.eyedStart);
        out.name("eyetStart").value(value.eyetStart);
        out.name("eyexEnd").value(value.eyexEnd);
        out.name("eyeyEnd").value(value.eyeyEnd);
        out.name("eyedEnd").value(value.eyedEnd);
        out.name("eyetEnd").value(value.eyetEnd);
        out.endObject();
    }

        // defaults written by copilot
    public Response read(JsonReader in) throws IOException {
        in.beginObject();
        boolean seen = false;
        long time = -1;
        double eyexStart = 0;
        double eyeyStart = 0;
        double eyedStart = 0;
        int eyetStart = 0;
        double eyexEnd = 0;
        double eyeyEnd = 0;
        double eyedEnd = 0;
        int eyetEnd = 0;
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("seen")) {
                seen = in.nextBoolean();
            } else if (name.equals("time")) {
                time = in.nextLong();
            } else if (name.equals("eyexStart")) {
                eyexStart = in.nextDouble();
            } else if (name.equals("eyeyStart")) {
                eyeyStart = in.nextDouble();
            } else if (name.equals("eyedStart")) {
                eyedStart = in.nextDouble();
            } else if (name.equals("eyetStart")) {
                eyetStart = in.nextInt();
            } else if (name.equals("eyexEnd")) {
                eyexEnd = in.nextDouble();
            } else if (name.equals("eyeyEnd")) {
                eyeyEnd = in.nextDouble();
            } else if (name.equals("eyedEnd")) {
                eyedEnd = in.nextDouble();
            } else if (name.equals("eyetEnd")) {
                eyetEnd = in.nextInt();
            } else {
                throw new IOException("Unexpected field in converting Json to Response: " + name);
            }
        }
        in.endObject();
        return new Response(seen, time, eyexStart, eyeyStart, eyedStart, eyetStart, eyexEnd, eyeyEnd, eyedEnd, eyetEnd);
    }
}