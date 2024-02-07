package org.lei.opi.core.definitions;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PacketSerializer implements JsonSerializer<Packet> {
    public PacketSerializer() {
        super();
    }

    @Override
    public JsonElement serialize(final Packet p, final Type type, final JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.add("error", context.serialize(p.getError()));
        jsonObject.add("close", context.serialize(p.getClose()));
        jsonObject.add("type", context.serialize(p.getType().getName()));
        jsonObject.add("msg", context.serialize(p.getMsg()));

        return jsonObject;
    }
    
}