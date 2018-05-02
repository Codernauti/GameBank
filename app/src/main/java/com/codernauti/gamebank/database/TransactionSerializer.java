package com.codernauti.gamebank.database;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by davide on 14/03/18.
 */

public class TransactionSerializer implements JsonSerializer<Transaction> {
    @Override
    public JsonElement serialize(Transaction src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mId", src.getId());
        jsonObject.addProperty("mAmount", src.getAmount());

        jsonObject.addProperty("mFrom", src.getSender());
        jsonObject.addProperty("mFromName", src.getFromName());
        jsonObject.addProperty("mTo", src.getRecipient());
        jsonObject.addProperty("mToName", src.getToName());

        jsonObject.addProperty("mMatchId", src.getMatchId());

        return jsonObject;
    }
}
