package com.codernauti.gamebank.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by davide on 14/03/18.
 */

public class PlayerSerializer implements JsonSerializer<Player> {
    @Override
    public JsonElement serialize(Player src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mId", src.getPlayerId());
        jsonObject.addProperty("mUsername", src.getUsername());
        jsonObject.addProperty("mPhotoName", src.getPhotoName());

        JsonArray matches = new JsonArray();
        for (Match m : src.getMatchPlayed()) {
            matches.add(context.serialize(m, Match.class));
        }

        jsonObject.add("matches", matches);

        return jsonObject;
    }
}
