package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by dpolonio on 19/02/18.
 */

public class BTStateChange extends BroadcastReceiver {

    private static final int BT_STATE_OFF = 10;
    private static final String EXTRA_STATE = "android.bluetooth.adapter.extra.STATE";
    private static final String TAG = "BTStateChange";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getIntExtra(EXTRA_STATE, -1) == BT_STATE_OFF) {
            Log.d(TAG, "The BT has been disabled");
            enableBTIfDisabled(context);
        } else {
            Log.d(TAG, "Not requiring BT activation");
        }
    }

    public static void enableBTIfDisabled(Context context) {

        BluetoothAdapter mba = BluetoothAdapter.getDefaultAdapter();
        if (!mba.isEnabled()) {

            Log.d(TAG, "Enabling Bluetooth");

            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableIntent);
        }
    }
}
