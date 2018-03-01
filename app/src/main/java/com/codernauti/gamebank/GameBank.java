package com.codernauti.gamebank;

import android.app.Application;

import java.util.UUID;

/**
 * Created by davide on 01/03/18.
 */

public class GameBank extends Application {

    public static final UUID BT_ADDRESS = UUID.randomUUID();
    private static UUID mBtHostAddress;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static UUID getBtHostAddress() {
        return mBtHostAddress;
    }

    public static void setBtHostAddress(UUID address) {
        mBtHostAddress = address;
    }
}