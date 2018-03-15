package com.codernauti.gamebank.database;

import android.util.Log;

import com.google.gson.JsonArray;
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

        JsonArray transactions = new JsonArray();
        for (Transaction t : src.getTransactionList()) {
            transactions.add(context.serialize(t, Transaction.class));
        }

        JsonArray players = new JsonArray();
        for (Player p : src.getPlayerList()) {
            JsonElement tmp = context.serialize(p, Player.class);
            Log.d("MatchSerializer", "Serialized: \n" + tmp.toString());
            players.add(tmp);
        }

        jsonObject.add("mTransactionList", transactions);
        jsonObject.add("mPlayerList", players);
        
        return jsonObject;
    }
}
