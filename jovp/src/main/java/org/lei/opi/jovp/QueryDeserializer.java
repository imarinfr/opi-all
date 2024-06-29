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
        double maxLum = context.deserialize(jsonObject.get("maxLum"), double.class);
        int maxPixel = context.deserialize(jsonObject.get("maxPixel"), int.class);
        double lumPrecision = context.deserialize(jsonObject.get("lumPrecision"), double.class);
        String invGammaFile = jsonObject.get("invGammaFile").getAsString();
        Monitor monitor = context.deserialize(jsonObject.get("monitor"), Monitor.class);
        String webcam = jsonObject.get("webcam").getAsString();
        double leftEyex = context.deserialize(jsonObject.get("leftEyex"), double.class);
        double leftEyey = context.deserialize(jsonObject.get("leftEyey"), double.class);
        double leftEyed = context.deserialize(jsonObject.get("leftEyed"), double.class);
        double rightEyex = context.deserialize(jsonObject.get("rightEyex"), double.class);
        double rightEyey = context.deserialize(jsonObject.get("rightEyey"), double.class);
        double rightEyed = context.deserialize(jsonObject.get("rightEyed"), double.class);

        return new Query(distance, fov, viewMode, input, pseudoGray, fullScreen, tracking, maxLum, maxPixel, lumPrecision, invGammaFile, monitor, 
        webcam, leftEyex, leftEyey, leftEyed, rightEyex, rightEyey, rightEyed);
    }
}
