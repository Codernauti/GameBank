package com.codernauti.gamebank.util;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Eduard on 07-Mar-18.
 */

public class SharePrefUtil {

    private static final String DEFAULT_VALUE = "default";


    public static void saveStringPreference(Context context, String key, String data) {
        Log.d("Save string preference", key + ": " + data);

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(key, data)
                .apply();
    }


    public static String getStringPreference(Context context, String key){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, DEFAULT_VALUE);
    }

}
