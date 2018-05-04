package com.codernauti.gamebank.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.util.generators.NicknameGenerator;
import com.codernauti.gamebank.util.generators.ProfilePicGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Eduard on 07-Mar-18.
 */

public class SharePrefUtil {

    public static final String DEFAULT_STRING_VALUE = "default";
    private static final String TAG = "SharePrefUtil";


    public static void saveStringPreference(Context context, String key, String data) {
        Log.d("Save string preference", key + ": " + data);

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(key, data)
                .apply();
    }


    public static String getStringPreference(Context context, String key){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, DEFAULT_STRING_VALUE);
    }

    public static String getNicknamePreference(Context context) {
        String nick = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PrefKey.NICKNAME, DEFAULT_STRING_VALUE);

        if (nick.equals(DEFAULT_STRING_VALUE)) {

            NicknameGenerator ng = new NicknameGenerator(context);

            nick = ng.generateRandomContent();
            saveStringPreference(context, PrefKey.NICKNAME, nick);
        }

        return nick;
    }


    public static void loadDefaultProfilePicturePreference(Context context) {

        String fileName = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PrefKey.PROFILE_PICTURE, DEFAULT_STRING_VALUE);

        if (fileName.equals(DEFAULT_STRING_VALUE)) {
            // never set a image -> chose a random one
            Log.d(TAG, "Initialize image.");

            final String PROFILE_PICTURE_NAME = GameBank.BT_ADDRESS + ".jpeg";

            ProfilePicGenerator ppg = new ProfilePicGenerator();
            Bitmap profilePicture = BitmapFactory.decodeResource(context.getResources(), ppg.generateRandomContent());

            final int sizeThreshold = 512;
            int width = profilePicture.getWidth();
            int height = profilePicture.getHeight();

            if (width > sizeThreshold) {
                width = sizeThreshold;
            }

            if (height > sizeThreshold) {
                height = sizeThreshold;
            }

            // Resize image
            profilePicture = Bitmap.createScaledBitmap(profilePicture, width, height, false);

            try (final FileOutputStream fos = context.openFileOutput(PROFILE_PICTURE_NAME, Context.MODE_PRIVATE)) {

                profilePicture.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                saveStringPreference(context, PrefKey.PROFILE_PICTURE, PROFILE_PICTURE_NAME);

                Log.d(TAG, "Random profile picture copied");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @NonNull
    public static UUID getBTAddressPreference(Context context) {

        String uuid = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PrefKey.BT_ADDRESS, DEFAULT_STRING_VALUE);

        UUID res;

        if (uuid.equals(DEFAULT_STRING_VALUE)) {

            res = UUID.randomUUID();
            saveStringPreference(context, PrefKey.BT_ADDRESS, res.toString());

            Log.d(TAG, "Random uuid generated for this device");
        } else {

            res = UUID.fromString(uuid);
        }


        return res;
    }

    @NonNull
    public static int getCurrentMatchId(Context context) {

        int res = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PrefKey.CURRENT_MATCH_ID, -1);

        if (res < 0) {
            throw new RuntimeException("No match id saved here");
        }

        return res;
    }

    public static void saveCurrentMatchId(Context context, int toSave) {

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PrefKey.CURRENT_MATCH_ID, toSave)
                .apply();
    }

}
