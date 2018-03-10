package com.codernauti.gamebank.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import com.codernauti.gamebank.util.generators.NicknameGenerator;
import com.codernauti.gamebank.util.generators.ProfilePicGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Eduard on 07-Mar-18.
 */

public class SharePrefUtil {

    public static final String DEFAULT_STRING_VALUE = "default";
    private static final String PROFILE_PICTURE_DEFAULT = "defaultProfilePicture.png";
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

    public static String getProfilePicturePreference(Context context) {

        String fileName = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PrefKey.PROFILE_PICTURE, DEFAULT_STRING_VALUE);

        if (fileName.equals(DEFAULT_STRING_VALUE)) {

            ProfilePicGenerator ppg = new ProfilePicGenerator();
            Bitmap profilePicture = BitmapFactory.decodeResource(context.getResources(), ppg.generateRandomContent());

            try (final FileOutputStream fos = context.openFileOutput(PROFILE_PICTURE_DEFAULT, Context.MODE_PRIVATE)) {

                profilePicture.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fileName = PROFILE_PICTURE_DEFAULT;

                Log.d(TAG, "Random profile picture copied");
            } catch (IOException e) {
                e.printStackTrace();

                return null;
            }
        }

        Log.d(TAG, "Returning pp name: " + fileName);
        return fileName;
    }

}
