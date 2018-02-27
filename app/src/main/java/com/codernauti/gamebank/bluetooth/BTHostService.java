package com.codernauti.gamebank.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Eduard on 27-Feb-18.
 */

public class BTHostService extends Service {

    private static final String TAG = "BTHostService";

    private static final String CONNECTION_NAME = "Game Bank";
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");


    public static final String ACCEPTED_CONNECTIONS = "accepted_connections";
    public static final String SEND_DATA = "send_data";

    private BluetoothAdapter mBluetoothAdapter;
    private BTHostConnection mConnections;


    private BroadcastReceiver mFromUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive()");

            String action = intent.getAction();
            Bundle bundle = intent.getExtras();

            if (action != null && action.equals(SEND_DATA)) {

                if (bundle != null) {
                    String lobbyName = bundle.getString("LOBBY_NAME");
                    UUID uuid = (UUID) bundle.get("PLAYER_UUID");

                    BTBundle btBundle = new BTBundle(lobbyName);
                    btBundle.getMapData().put("prova", "ciao mondo");
                    btBundle.getMapData().put("provina", "ciaone");

                    try {
                        mConnections.sendTo(btBundle, uuid);
                        Log.d(TAG, "Sent back a message");
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to send a message");
                        e.printStackTrace();
                    }


                }
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            int acceptedConn = bundle.getInt(ACCEPTED_CONNECTIONS);

            try {
                BluetoothServerSocket mServerSocket =
                        mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                                CONNECTION_NAME,
                                MY_UUID);

                mConnections = new BTHostConnection(acceptedConn, mServerSocket,
                        LocalBroadcastManager.getInstance(this));

                IntentFilter filters = new IntentFilter();
                filters.addAction(SEND_DATA);
                LocalBroadcastManager.getInstance(this)
                        .registerReceiver(mFromUiReceiver, filters);

                mConnections.acceptConnections();

                Log.d(TAG, "Accepting connections");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Start intent service not initialized!");
        }

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFromUiReceiver);
    }
}
