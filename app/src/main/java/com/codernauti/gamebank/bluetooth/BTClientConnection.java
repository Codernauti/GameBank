package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.util.Event;

import java.io.IOException;
import java.io.ObjectOutputStream;
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
                       @NonNull LocalBroadcastManager mLocalBroadcastManager) {
        super(mLocalBroadcastManager, Executors.newCachedThreadPool());

        this.mServer = server;
    }

    @Override
    void onStopReadingDataFrom(UUID who) {
        // TODO: disconnect with server
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
                    sendTo(rendezvous, mHostUUID);
                    startListeningRunnable(mHostUUID);

                    Intent connectionCompleted = new Intent(Event.Network.CONN_ESTABLISHED);
                    mLocalBroadcastManager.sendBroadcast(connectionCompleted);

                } catch (IOException e) {
                    e.printStackTrace();

                    Intent error = new Intent(Event.Network.CONN_ERRONEOUS);
                    mLocalBroadcastManager.sendBroadcast(error);
                }
            }
        });
    }

    void sendToHost(@NonNull final BTBundle data) {
        sendTo(data, mHostUUID);
    }
}
