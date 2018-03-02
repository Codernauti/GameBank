package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.lobby.RoomPlayer;
import com.codernauti.gamebank.util.Event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Created by dpolonio on 26/02/18.
 */

public class BTHostConnection extends BTConnection {

    private static final String TAG = "BTHostConnection";

    private final int mAcceptedConnections;
    private final BluetoothServerSocket mBtServerSocket;


    BTHostConnection(int acceptedConnections,
                     @NonNull BluetoothServerSocket btServerSocket,
                     @NonNull LocalBroadcastManager localBroadcastManager) {
        super(localBroadcastManager, Executors.newCachedThreadPool());

        if (acceptedConnections > 0 && acceptedConnections < 8) {

            this.mAcceptedConnections = acceptedConnections;
            this.mBtServerSocket = btServerSocket;
        } else {
            throw new RuntimeException();
        }

    }

    void acceptConnections() {

        Log.d(TAG, "Accepting connections");

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < mAcceptedConnections; i++) {

                        Log.d(TAG, "Waiting for a new connection...");
                        final BluetoothSocket btSocket = mBtServerSocket.accept();

                        // Read init information from client just connected
                        try {
                            ObjectInputStream inputStream = new ObjectInputStream(btSocket.getInputStream());
                            Object received = inputStream.readObject();

                            if (received != null) {
                                BTBundle clientInfo = (BTBundle) received;

                                if (Event.Network.INIT_INFORMATION.equals(clientInfo.getBluetoothAction())) {

                                    UUID client = (UUID) clientInfo.getMapData().get(UUID.class.getName());

                                    addConnection(client, btSocket);
                                    startListeningRunnable(client);

                                    // update Ui
                                    String key = RoomPlayer.class.getName();
                                    Intent connection = BTBundle.makeIntentFrom(
                                            new BTBundle(Event.Network.CONN_ESTABLISHED).append(
                                                    clientInfo.get(key))
                                    );

                                    mLocalBroadcastManager.sendBroadcast(connection);
                                }
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    Log.d(TAG, "Ended waiting for new connections");
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void close() {
        super.close();
        try {
            mBtServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Impossible to close server socket: " +
                    mBtServerSocket.toString() + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
