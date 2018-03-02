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


    // TODO: understand what is this fields
    private static final UUID mHardcodedUUID =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private final BluetoothDevice mServer;

    private UUID mHostUUID;
    private BTio mConnectionWithHost;   // FIXME: use super.mConnections

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
                    BluetoothSocket socket = mServer.createRfcommSocketToServiceRecord(mHardcodedUUID);
                    socket.connect();
                    Log.d(TAG, "Connected with " + mServer.getName());

                    // Create connection with host
                    mHostUUID = UUID.randomUUID();
                    GameBank.setBtHostAddress(mHostUUID);   // DANGER global variable from one thread
                    mConnectionWithHost = new BTio(socket);

                    // Send rendezvous to server
                    mConnectionWithHost.writeData(rendezvous);

                    // Start listening for data
                    BTClientConnection.super.startListeningData(mConnectionWithHost);

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

    void sendDataToHost(BTBundle data) {
        try {
            mConnectionWithHost.writeData(data);
        } catch (IOException e) {
            Log.d(TAG, "Event: " + Event.Network.SEND_DATA_ERROR);

            Intent error = new Intent(Event.Network.SEND_DATA_ERROR);
            mLocalBroadcastManager.sendBroadcast(error);

            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (mConnectionWithHost != null) {
            mConnectionWithHost.close();
            mConnectionWithHost = null;
        }
    }
}
