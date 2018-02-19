package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by dpolonio on 19/02/18.
 */

public class BluetoothStateChange extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // If the adapter is null, then Bluetooth is not supported

        enableBTIfDisabled(context);
    }

    public static void enableBTIfDisabled(Context context) {

        BluetoothAdapter mba = BluetoothAdapter.getDefaultAdapter();

        if (!mba.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            context.startActivity(enableIntent);
            // Otherwise, setup the chat session
        }
    }
}
