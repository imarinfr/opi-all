package org.lei.opi.jovp;

import com.google.gson.JsonParseException;

import es.optocom.jovp.Monitor;
import es.optocom.jovp.definitions.ViewMode;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QueryDeserializer implements JsonDeserializer<Query> {

    public Query deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        int distance = jsonObject.get("distance").getAsInt();
        float[] fov = context.deserialize(jsonObject.get("fov"), float[].class);
        ViewMode viewMode = context.deserialize(jsonObject.get("viewMode"), ViewMode.class);
        String input = jsonObject.get("input").getAsString();
        boolean pseudoGray = jsonObject.get("pseudoGray").getAsBoolean();
        boolean fullScreen = jsonObject.get("fullScreen").getAsBoolean();
        boolean tracking = jsonObject.get("tracking").getAsBoolean();
        double[] maxLum = context.deserialize(jsonObject.get("maxLum"), double[].class);
        String gammaFile = jsonObject.get("gammaFile").getAsString();
        Monitor monitor = null; // TODO context.deserialize(jsonObject.get("monitor"), Monitor.class);

        return new Query(distance, fov, viewMode, input, pseudoGray, fullScreen, tracking, maxLum, gammaFile, monitor);
    }
}
