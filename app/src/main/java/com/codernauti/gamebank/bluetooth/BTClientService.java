package com.codernauti.gamebank.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.lobby.RoomPlayer;
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

    private LocalBroadcastManager mLocalBroadcastManager;
    private BTClientConnection mConnection;

    private BroadcastReceiver mFromUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Event.Game.POKE.equals(action)) {
                Log.e(TAG, "Cannot send poke to host, not implemented yet");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

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

                IntentFilter filters = new IntentFilter();
                filters.addAction(Event.Game.POKE);
                mLocalBroadcastManager.registerReceiver(mFromUiReceiver, filters);

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
        RoomPlayer roomPlayer = new RoomPlayer("Gino", UUID.randomUUID(), false);

        try {
            mConnection.connectAndSubscribe(
                    EventFactory.newInitInformation(roomPlayer));
        } catch (IOException e) {
            Log.e(TAG, "Something in the connection went wrong");
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
        mLocalBroadcastManager.unregisterReceiver(mFromUiReceiver);
    }
}
