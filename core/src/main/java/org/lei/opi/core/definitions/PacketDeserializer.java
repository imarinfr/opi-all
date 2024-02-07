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
        String type = jsonObject.get("type").getAsString();

        Object msg;

        if (type.equalsIgnoreCase("java.lang.String"))
            msg = jsonObject.get("msg").getAsString();
        else
            msg = jsonObject.get("msg").getAsJsonObject();

        try {
            return new Packet(error, close, msg, Class.forName(type));
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Cannot find class for type: " + type);
        }
    }
}
