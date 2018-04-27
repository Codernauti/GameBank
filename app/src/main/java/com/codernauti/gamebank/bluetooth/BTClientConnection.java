package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Created by dpolonio on 23/02/18.
 */

public class BTClientConnection extends BTConnection {

    private static final String TAG = "BTClientConnection";

    private final BluetoothDevice mServer;
    private UUID mHostUUID;

    BTClientConnection(@NonNull BluetoothDevice server,
                       @NonNull LocalBroadcastManager mLocalBroadcastManager,
                       @NonNull String logPath) {
        super(mLocalBroadcastManager, Executors.newCachedThreadPool(), logPath);

        this.mServer = server;
    }

    void connectAndListen(@NonNull final BTBundle rendezvous) throws IOException {
        connectToHost(rendezvous);
    }

    private void connectToHost(@NonNull final BTBundle rendezvous) throws IOException {

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothSocket hostSocket = mServer.createRfcommSocketToServiceRecord(MY_UUID);
                    hostSocket.connect();
                    Log.d(TAG, "Connected with " + mServer.getName());

                    // Create connection with host
                    mHostUUID = UUID.randomUUID();  // Unique for each client device

                    addConnection(mHostUUID, hostSocket);
                    setReady(mHostUUID);
                    sendTo(rendezvous, mHostUUID);
                    startListeningRunnable(mHostUUID);

                    Intent connectionCompleted = new Intent(BTEvent.CONN_ESTABLISHED);
                    mLocalBroadcastManager.sendBroadcast(connectionCompleted);

                } catch (IOException e) {
                    e.printStackTrace();

                    Intent error = new Intent(BTEvent.CONN_ERRONEOUS);
                    mLocalBroadcastManager.sendBroadcast(error);
                }
            }
        });
    }

    void sendToHost(@NonNull final BTBundle data) {
        sendTo(data, mHostUUID);
    }

    @Override
    void onStopReadingDataFrom(UUID who) {
        Intent intent = BTBundle.makeIntentFrom(
                new BTBundle(BTEvent.HOST_DISCONNECTED)
                        .append(who)
        );
        mLocalBroadcastManager.sendBroadcast(intent);
    }
}
