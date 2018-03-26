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

import com.codernauti.gamebank.Event;

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
    public static final String RECEIVER_UUID = "receiver_uuid";

    private BluetoothAdapter mBluetoothAdapter;
    private BTHostConnection mConnections;


    private BroadcastReceiver mFromUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (btBundle.isSentByMe()) {

                    if (BTEvent.CURRENT_STATE.equals(action)) {

                        UUID receiver = (UUID) intent.getSerializableExtra(RECEIVER_UUID);
                        Log.d(TAG, "Send to " + receiver);
                        mConnections.sendTo(btBundle, receiver);

                    } else if (BTEvent.START.equals(action)) {

                        Log.d(TAG, "Close server connection.\nSend Broadcast");
                        mConnections.closeServerSocket();
                        mConnections.sendBroadcast(btBundle);

                    } else {
                        // TRANSACTION
                        // MEMBER_DISCONNECTED

                        Log.d(TAG, "Send Broadcast");
                        mConnections.sendBroadcast(btBundle);
                    }

                } else {
                    // MEMBER_CONNECTED
                    // MEMBER_READY
                    // TRANSACTION

                    UUID packetSender = btBundle.getUuid();
                    List<UUID> exceptions = new ArrayList<>();
                    exceptions.add(packetSender);

                    Log.d(TAG, "Send Multicast. Exception: " + packetSender);
                    mConnections.sendMulticast(btBundle, exceptions);

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
        Log.d(TAG, "onStartCommand");
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            int acceptedConn = bundle.getInt(ACCEPTED_CONNECTIONS);

            try {
                clear();

                BluetoothServerSocket mServerSocket =
                        mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                                CONNECTION_NAME,
                                BTConnection.MY_UUID);

                mConnections = new BTHostConnection(acceptedConn,
                        getFilesDir().toString(),
                        mServerSocket,
                        LocalBroadcastManager.getInstance(this),
                        getFilesDir().getAbsolutePath());

                IntentFilter filter = new IntentFilter();
                filter.addAction(BTEvent.MEMBER_CONNECTED);
                filter.addAction(Event.Game.MEMBER_READY);
                filter.addAction(BTEvent.MEMBER_DISCONNECTED);
                filter.addAction(BTEvent.CURRENT_STATE);
                filter.addAction(BTEvent.START);

                filter.addAction(Event.Game.TRANSACTION);

                LocalBroadcastManager.getInstance(this)
                        .registerReceiver(mFromUiReceiver, filter);

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

        Log.d(TAG, "Calling onDestroy()");

        clear();

        super.onDestroy();
    }

    private void clear() {
        if (mConnections != null) {
            mConnections.close();
        }
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mFromUiReceiver);
    }
}
