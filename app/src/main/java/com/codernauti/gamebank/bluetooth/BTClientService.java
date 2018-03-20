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

import com.codernauti.gamebank.Event;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.io.IOException;

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
            Log.d(TAG, "Received action: " + action);

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (btBundle.isSentByMe()) {
                    mConnection.sendToHost(btBundle);
                } else {
                    Log.e(TAG, "BTBundle from other nodes, don't forward it.");
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            BluetoothDevice host = (BluetoothDevice) bundle.get(HOST_DEVICE);

            if (host != null) {
                mConnection = new BTClientConnection(host,
                        LocalBroadcastManager.getInstance(this),
                        getFilesDir().toString());

                IntentFilter filters = new IntentFilter();
                filters.addAction(Event.Game.POKE);
                filters.addAction(Event.Game.MEMBER_READY);

                filters.addAction(Event.Game.TRANSACTION);
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

        String nickname = SharePrefUtil.getNicknamePreference(this);

        // TODO: remove from this level
        Player me = new Player(GameBank.BT_ADDRESS.toString(), nickname, false);
        String jsonMe = GameBank.gsonConverter.toJson(me);

        Log.d(TAG, "Send rendezvous: " + jsonMe);

        try {
            mConnection.connectAndListen(
                    new BTBundle(BTEvent.MEMBER_CONNECTED)
                            .append(jsonMe)
            );
        } catch (IOException e) {
            Log.e(TAG, "Something in the connection went wrong");
            e.printStackTrace();

            Intent intent = new Intent(BTEvent.CONN_ERRONEOUS);
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

        Log.d(TAG, "Calling onDestroy()");

        mConnection.close();
        mLocalBroadcastManager.unregisterReceiver(mFromUiReceiver);

        super.onDestroy();
    }
}
