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
        super(mLocalBroadcastManager, Executors.newFixedThreadPool(2));

        this.mServer = server;
    }

    private void connectToHost(@NonNull final BTBundle rendezvous) throws IOException {

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothSocket socket = mServer.createRfcommSocketToServiceRecord(MY_UUID);
                    socket.connect();
                    Log.d(TAG, "Connected with " + mServer.getName());

                    // Create connection with host
                    mHostUUID = UUID.randomUUID();
                    GameBank.setBtHostAddress(mHostUUID);   // DANGER global variable from one thread

                    addConnection(mHostUUID, socket);
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

    void connectAndListen(@NonNull final BTBundle rendezvous) throws IOException {
        connectToHost(rendezvous);
    }

    void sendToHost(@NonNull final BTBundle data) {
        sendTo(data, mHostUUID);
    }
}
