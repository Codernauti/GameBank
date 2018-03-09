package com.codernauti.gamebank.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by dpolonio on 09/03/18.
 */

public class PermissionManager {

    public static void requestPermission(AppCompatActivity activity, String[] permissions, int request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (needPermissions(activity, permissions)) {
                ActivityCompat.requestPermissions(activity, permissions, request);
            }
        }
    }

    private static boolean needPermissions(@NonNull Context context,
                                           @NonNull String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }
}
