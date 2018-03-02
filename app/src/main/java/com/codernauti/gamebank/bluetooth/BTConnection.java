package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by dpolonio on 27/02/18.
 */

abstract class BTConnection implements Closeable {

    private final static String TAG = "BTConnection";

    final LocalBroadcastManager mLocalBroadcastManager;

    final ExecutorService mExecutorService;
    private final Map<UUID, BTio> mConnections;

    BTConnection (@NonNull LocalBroadcastManager mLocalBroadcastManager,
                  @NonNull ExecutorService executorService) {
        this.mLocalBroadcastManager = mLocalBroadcastManager;
        this.mExecutorService = executorService;

        this.mConnections = new ConcurrentHashMap<>();
    }

    void addNewAcceptedConnection(UUID clientUuid, BluetoothSocket newSocket) {
        Log.d(TAG, "Connection accepted from " + clientUuid);

        BTio btio = new BTio(newSocket);
        mConnections.put(clientUuid, btio);
    }


    void startListeningData(@NonNull final UUID who) {
        BTio receiverConn = mConnections.get(who);

        if (receiverConn != null) {
            startListeningData(receiverConn);
        } else {
            Log.e(TAG, "Connection with " + who + " not found");
        }
    }

    void startListeningData(@NonNull final BTio btio) {
        // Start perpetual task
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                boolean flag = true;
                while (flag) {

                    try {
                        Object tmp = btio.readData();
                        if (tmp != null) {

                            BTBundle dataReceived = (BTBundle) tmp;

                            Log.d(TAG, "Data event received: " +
                                    dataReceived.getBluetoothAction());

                            mLocalBroadcastManager.sendBroadcast(dataReceived.getIntent());
                        }

                    } catch (IOException e) {

                        Log.d(TAG, "Stopped receiving data");
                        e.printStackTrace();
                        flag = false;
                    }
                }
            }
        });
    }

    /* SEND METHODS */

    void sendTo(@NonNull Serializable data, @NonNull UUID who) throws IOException {
        Log.d(TAG, "Sending data to " + who);
        mConnections.get(who).writeData(data);
    }

    void sendBroadcast(@NonNull Serializable data) throws IOException {
        for (Map.Entry<UUID, BTio> btc : mConnections.entrySet()) {
            sendTo(data, btc.getKey());
        }
    }

    void sendMulticast(@NonNull Serializable data, @NonNull List<UUID> exceptions) throws IOException {
        for (Map.Entry<UUID, BTio> btc : mConnections.entrySet()) {
            if (!exceptions.contains(btc.getKey())) {
                sendTo(data, btc.getKey());
            }
        }
    }

    @Override
    @CallSuper
    public void close() throws IOException {
        for (Map.Entry<UUID, BTio> btc : mConnections.entrySet()) {
            btc.getValue().close();
        }
        mExecutorService.shutdownNow();
    }
}
