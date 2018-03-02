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
import android.widget.Toast;

import com.codernauti.gamebank.util.Event;
import com.codernauti.gamebank.util.EventFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Eduard on 27-Feb-18.
 */

public class BTHostService extends Service {

    private static final String TAG = "BTHostService";

    private static final String CONNECTION_NAME = "Game Bank";


    public static final String ACCEPTED_CONNECTIONS = "accepted_connections";

    private BluetoothAdapter mBluetoothAdapter;
    private BTHostConnection mConnections;


    private BroadcastReceiver mFromUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action:" + action);

            if (Event.Game.MEMBER_JOINED.equals(action)) {

                mConnections.sendBroadcast(BTBundle.extractFrom(intent));

            } else if (Event.Game.MEMBER_READY.equals(action)) {
                BTBundle btBundle = BTBundle.extractFrom(intent);

                boolean isReady = (boolean) btBundle.get(boolean.class.getName());
                UUID address = (UUID) btBundle.get(UUID.class.getName());

                List<UUID> exceptions = new ArrayList<>();
                exceptions.add(address);

                mConnections.sendMulticast(EventFactory.newReadinessInfo(isReady), exceptions);

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
                                BTConnection.MY_UUID);

                mConnections = new BTHostConnection(acceptedConn, mServerSocket,
                        LocalBroadcastManager.getInstance(this));

                IntentFilter filters = new IntentFilter();
                filters.addAction(Event.Game.MEMBER_JOINED);
                filters.addAction(Event.Game.MEMBER_READY);
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
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mFromUiReceiver);
    }
}
