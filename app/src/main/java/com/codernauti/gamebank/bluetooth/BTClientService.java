package com.codernauti.gamebank.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.util.EventFactory;
import com.codernauti.gamebank.util.PlayerProfile;
import com.codernauti.gamebank.util.Event;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Eduard on 27-Feb-18.
 */

public class BTClientService extends Service {

    private static final String TAG = "BTClientService";

    // data in start intent
    public static final String HOST_DEVICE = "server_device";
    // action to bluetooth network
    // public static final String ...

    private BTClientConnection mConnection;

    /*private BroadcastReceiver mFromUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null) {
            }
        }
    };*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            BluetoothDevice host = (BluetoothDevice) bundle.get(HOST_DEVICE);

            if (host != null) {
                mConnection = new BTClientConnection(
                        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"),
                        host,
                        LocalBroadcastManager.getInstance(this));

                /*IntentFilter filters = new IntentFilter();
                filters.addAction();
                registerReceiver(mFromUiReceiver, filters);*/

                connect();

            } else {
                Log.e(TAG, "Start intent service not initialized!");
            }
        }

        return START_REDELIVER_INTENT;
    }

    private void connect() {
        Log.d(TAG, "Connection requested to ClientConnection");

        // TODO: get this data from shared preferences
        PlayerProfile playerProfile = new PlayerProfile("Gino", UUID.randomUUID());

        try {
            mConnection.connectAndSubscribe(
                    EventFactory.newInitInformation(playerProfile));
        } catch (IOException e) {
            Log.e(TAG, "Something in connection went wrong");
            e.printStackTrace();
            // TODO: send to Activity Error to send
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mFromUiReceiver);
    }
}
