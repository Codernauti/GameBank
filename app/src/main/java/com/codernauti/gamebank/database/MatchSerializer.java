package com.codernauti.gamebank.database;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class MatchSerializer implements JsonSerializer<Match> {

    private static final String TAG = "MatchSerializer";

    @Override
    public JsonElement serialize(Match src, Type typeOfSrc, JsonSerializationContext context) {
        Log.d(TAG, "serialize: " + src.getMatchName());

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mId", src.getId());
        jsonObject.addProperty("mMatchName", src.getMatchName());
        jsonObject.addProperty("mMatchStarted", src.getMatchStarted());
        jsonObject.addProperty("mInitBudget", src.getInitBudget());

        jsonObject.add("mTransactionList",
                context.serialize(src.getTransactionList().toArray(), Transaction[].class));

        jsonObject.add("mPlayerList",
                context.serialize(src.getPlayerList().toArray(), Player[].class));

        return jsonObject;
    }

}
