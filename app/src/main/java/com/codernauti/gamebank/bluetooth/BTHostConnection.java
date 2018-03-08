package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.pairing.RoomPlayer;
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

    private volatile boolean mServerSocketOpen;


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
        mServerSocketOpen = true;

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < mAcceptedConnections && mServerSocketOpen; i++) {
                    try {
                        Log.d(TAG, "Waiting for a new connection...");
                        final BluetoothSocket clientSocket = mBtServerSocket.accept();

                        // Read init information from client just connected
                        ObjectInputStream inputStream = new ObjectInputStream(
                                clientSocket.getInputStream());
                        BTBundle btBundle = (BTBundle) inputStream.readObject();

                        if (Event.Game.MEMBER_JOINED.equals(btBundle.getBluetoothAction())) {

                            RoomPlayer newPlayer = (RoomPlayer)
                                    btBundle.get(RoomPlayer.class.getName());

                            addConnection(newPlayer.getId(), clientSocket);
                            startListeningRunnable(newPlayer.getId());

                            Intent intentJoin = btBundle.getIntent();
                            mLocalBroadcastManager.sendBroadcast(intentJoin);
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Socket closed, connection accept abort.\nAutomatically retry.");
                        i--;
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "Init data from client is not a UUID");
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "Ended waiting for new connections");
                try {
                    mBtServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    void onStopReadingDataFrom(UUID who) {
        Intent intent = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.MEMBER_DISCONNECTED)
                        .append(who)
        );
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void close() {
        super.close();
        closeServerSocket();
    }

    void closeServerSocket() {
        try {
            mBtServerSocket.close();
            mServerSocketOpen = false;
        } catch (IOException e) {
            Log.e(TAG, "Impossible to close server socket: " +
                    mBtServerSocket.toString() + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
