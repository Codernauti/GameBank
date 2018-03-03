package com.codernauti.gamebank.bluetooth;

import android.app.Service;
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

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.lobby.RoomPlayer;
import com.codernauti.gamebank.util.Event;
import com.codernauti.gamebank.util.EventFactory;

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
    private RoomPlayer playerInfo;

    private BroadcastReceiver mFromUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action received: " + action);

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (btBundle.getUuid().equals(GameBank.BT_ADDRESS)) {
                    mConnection.sendToHost(btBundle);
                } else {
                    Log.d(TAG, "BTBundle from other nodes, don't forward it.");
                }

            } else {
                Log.e(TAG, "BTBundle null!");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        // TODO: get this data from shared preferences
        playerInfo = new RoomPlayer("ClientNickname", GameBank.BT_ADDRESS, false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            BluetoothDevice host = (BluetoothDevice) bundle.get(HOST_DEVICE);

            if (host != null) {
                mConnection = new BTClientConnection(host,
                        LocalBroadcastManager.getInstance(this));

                IntentFilter filters = new IntentFilter();
                filters.addAction(Event.Game.POKE);
                filters.addAction(Event.Game.MEMBER_READY);
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

        try {
            mConnection.connectAndListen(
                    EventFactory.newInitInfo(playerInfo));
        } catch (IOException e) {
            Log.e(TAG, "Something in the connection went wrong");
            e.printStackTrace();

            Intent intent = new Intent(Event.Network.CONN_ERRONEOUS);
            mLocalBroadcastManager.sendBroadcast(intent);
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

        mConnection.close();

        mLocalBroadcastManager.unregisterReceiver(mFromUiReceiver);
    }
}
