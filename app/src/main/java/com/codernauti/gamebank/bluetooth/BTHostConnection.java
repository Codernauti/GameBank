package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dpolonio on 26/02/18.
 */

public class BTHostConnection extends BTConnection implements Closeable {

    public static final String EVENT_INCOMING_CONNECTION_ESTABLISHED = "ice";

    private static final String TAG = "BTHostConnection";

    private final ExecutorService mExecutorService;
    private final Map<UUID, BTio> mConnections;
    private final int mAcceptedConnections;
    private final BluetoothServerSocket mBtServerSocket;


    public BTHostConnection(int acceptedConnections,
                            @NonNull BluetoothServerSocket btServerSocket,
                            @NonNull LocalBroadcastManager localBroadcastManager) {
        super(localBroadcastManager);

        if (acceptedConnections > 0 && acceptedConnections < 8) {

            this.mExecutorService = Executors.newCachedThreadPool();
            this.mConnections = new ConcurrentHashMap<>();
            this.mAcceptedConnections = acceptedConnections;
            this.mBtServerSocket = btServerSocket;
        } else {
            throw new RuntimeException();
        }

    }

    public void acceptConnections() {

        Log.d(TAG, "Accepting connections");

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < mAcceptedConnections; i++) {

                        Log.d(TAG, "Waiting for a new connection...");
                        final BluetoothSocket btSocket = mBtServerSocket.accept();

                        mExecutorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ObjectInputStream objis = new ObjectInputStream(btSocket.getInputStream());
                                    Object received = objis.readObject();
                                    if (received != null) {
                                        BTBundle clientInfo = (BTBundle) received;
                                        if (clientInfo.getBluetoothAction().equals(BTActions.CONNECTION_INFO)) {
                                            UUID client = (UUID) clientInfo.getMapData().get("IDENTIFIER");
                                            Log.d(TAG, "Connection accepted from " + client);
                                            mConnections.put(client, new BTio(btSocket));

                                            Intent connection = new Intent(EVENT_INCOMING_CONNECTION_ESTABLISHED);
                                            connection.putExtra("PLAYERPROFILE", clientInfo.getMapData().get("PLAYER_INFO"));

                                            mLocalBroadcastManager.sendBroadcast(connection);
                                        }
                                    }
                                } catch (IOException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    Log.d(TAG, "Ended waiting for new connections");
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        });
    }

    public void sendTo(@NonNull Serializable data, UUID who) throws IOException {
        Log.d(TAG, "Sending data to " + who);
        mConnections.get(who).writeData(data);
    }

    public void sendBroadcast(@NonNull Serializable data) throws IOException {
        for (Map.Entry<UUID, BTio> btc : mConnections.entrySet()) {
            sendTo(data, btc.getKey());
        }
    }

    public void closeAcceptConnections() throws IOException {
        mBtServerSocket.close();
    }

    @Override
    public void close() throws IOException {

        mBtServerSocket.close();
        for (Map.Entry<UUID, BTio> btc : mConnections.entrySet()) {
            btc.getValue().close();
        }
        mExecutorService.shutdownNow();
    }
}
