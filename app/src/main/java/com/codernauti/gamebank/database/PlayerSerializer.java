package com.codernauti.gamebank.database;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.codernauti.gamebank.GameBank;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by davide on 14/03/18.
 */

public class PlayerSerializer implements JsonSerializer<Player> {

    private static final String TAG = "PlayerSerializer";

    @Override
    public JsonElement serialize(Player src, Type typeOfSrc, JsonSerializationContext context) {
        Log.d(TAG, "Serializing Player");

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mId", src.getPlayerId());
        jsonObject.addProperty("mUsername", src.getUsername());
        jsonObject.addProperty("mReady", src.isReady());

        String imageBase64 = Base64.encodeToString(
                loadImageIntoMemory(src.getPictureNameFile()),
                Base64.DEFAULT);

        jsonObject.addProperty("mImageBase64", imageBase64);

        return jsonObject;
    }

    private byte[] loadImageIntoMemory(String fileName) {

        final File toLoad = new File(GameBank.FILES_DIR, fileName);
        byte[] imageBuffer = new byte[(int)toLoad.length()];

        Log.d(TAG, "File path: " + toLoad.getAbsolutePath());

        if (toLoad.exists() && toLoad.isFile() && (toLoad.length() < Integer.MAX_VALUE)) {

            try (final FileInputStream fis = new FileInputStream(toLoad)) {

                int read;
                read = fis.read(imageBuffer);

                Log.d(TAG, "Read " + read + " bytes");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Impossible to load image into memory");
        }

        return imageBuffer;
    }

}

