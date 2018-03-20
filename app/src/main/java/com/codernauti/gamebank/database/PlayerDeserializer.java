package com.codernauti.gamebank.database;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.codernauti.gamebank.GameBank;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by Eduard on 19-Mar-18.
 */

public class PlayerDeserializer implements JsonDeserializer<Player> {

    private static final String TAG = "PlayerDeserializer";


    @Override
    public Player deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Log.d(TAG, "Deserializing player");

        JsonObject o = json.getAsJsonObject();

        Player res = new Player(
                o.get("mId").getAsString(),
                o.get("mUsername").getAsString(),
                o.get("mReady").getAsBoolean()
        );


        String imageBase64 = o.get("mImageBase64").getAsString();

        if (!imageBase64.isEmpty() || imageBase64.equals(GameBank.BT_ADDRESS)) {
            writeToPersistentStorage(Base64.decode(imageBase64, Base64.DEFAULT), res.getPictureNameFile());
        } else {
            Log.w(TAG, "Image empty or equal to device id. Skip saving into storage.");
        }

        Log.d(TAG, "Deserializing finished");

        return res;
    }

    private void writeToPersistentStorage(@NonNull byte[] mImage, String mImageName) {

        File toWrite = new File(GameBank.FILES_DIR, mImageName);

        Log.d(TAG, "Writing into memory, in: " + toWrite.getAbsolutePath());

        try(final FileOutputStream fos = new FileOutputStream(toWrite, false)) {
            fos.write(mImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
