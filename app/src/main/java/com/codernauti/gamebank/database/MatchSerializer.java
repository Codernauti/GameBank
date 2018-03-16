package com.codernauti.gamebank.database;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class MatchSerializer implements JsonSerializer<Match> {

    @Override
    public JsonElement serialize(Match src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mId", src.getId());
        jsonObject.addProperty("mMatchName", src.getMatchName());
        jsonObject.addProperty("mMatchStarted", src.getMatchStarted());

        jsonObject.add("mTransaction",
                context.serialize(src.getTransactionList().toArray(), Transaction[].class));

        jsonObject.add("mPlayerList",
                context.serialize(src.getPlayerList().toArray(), Player[].class));

        return jsonObject;
    }
}
