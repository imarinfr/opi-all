package org.lei.opi.core.definitions;

import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PacketDeserializer implements JsonDeserializer<Packet> {

    public Packet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        boolean error = jsonObject.get("error").getAsBoolean();
        boolean close = jsonObject.get("close").getAsBoolean();

        Object msg = jsonObject.get("msg");

        return new Packet(error, close, msg);
    }
}
