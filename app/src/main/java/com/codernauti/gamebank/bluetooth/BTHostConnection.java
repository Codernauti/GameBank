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

        this.mExecutorService = Executors.newCachedThreadPool();
        this.mConnections = new ConcurrentHashMap<>();
        this.mAcceptedConnections = acceptedConnections;
        this.mBtServerSocket = btServerSocket;

    }

    public void acceptConnections() {

        Log.d(TAG, "Accepting connections");

        if (mAcceptedConnections < 8) {

            for (int i = 0; i < mAcceptedConnections; i++) {

                final int jobId = i;

                mExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d(TAG, "Waiting for a new connection...");
                            BluetoothSocket btSocket = mBtServerSocket.accept();

                            ObjectInputStream objis = new ObjectInputStream(btSocket.getInputStream());

                            Object received = objis.readObject();
                            if (received != null) {
                                BTBundle clientInfo = (BTBundle) received;
                                if (clientInfo.getAction().equals("CONNECTION_INFO")) {
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
        }
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
