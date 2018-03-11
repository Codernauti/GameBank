package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.util.Event;

import java.io.Closeable;
import java.io.File;
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

    static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    final LocalBroadcastManager mLocalBroadcastManager;

    final ExecutorService mExecutorService;
    private final Map<UUID, BTio> mConnections;

    BTConnection (@NonNull LocalBroadcastManager mLocalBroadcastManager,
                  @NonNull ExecutorService executorService) {
        this.mLocalBroadcastManager = mLocalBroadcastManager;
        this.mExecutorService = executorService;

        this.mConnections = new ConcurrentHashMap<>();
    }


    void startListeningRunnable(@NonNull final UUID who) {
        BTio receiverConn = mConnections.get(who);

        if (receiverConn != null) {
            startListeningRunnable(receiverConn);
        } else {
            Log.e(TAG, "Connection with " + who + " not found");
        }
    }

    private void startListeningRunnable(@NonNull final BTio btio) {
        // Start perpetual task
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "StartListening \n\tThread: " + Thread.currentThread().getName());

                boolean isConnected = true;
                while (isConnected) {

                    try {
                        Object tmp = btio.readData();
                        if (tmp != null) {

                            BTBundle dataReceived = (BTBundle) tmp;

                            Log.d(TAG, "Data event received: " +
                                    dataReceived.getBluetoothAction());

                            mLocalBroadcastManager.sendBroadcast(dataReceived.getIntent());
                        }

                    } catch (IOException e) {

                        UUID clientDisconnected = btio.getUUID();

                        Log.d(TAG, "Stopped receiving data from: " + clientDisconnected);
                        e.printStackTrace();

                        removeConnection(clientDisconnected);
                        onStopReadingDataFrom(clientDisconnected);

                        isConnected = false;
                    }
                }
            }
        });
    }

    private void removeConnection(UUID clienUuid) {
        Log.d(TAG, "Disconnect " + clienUuid);
        mConnections.remove(clienUuid);
    }

    void addConnection(UUID clientUuid, BluetoothSocket newSocket) {
        Log.d(TAG, "Connection accepted from " + clientUuid);

        BTio btio = new BTio(clientUuid, newSocket);
        mConnections.put(clientUuid, btio);
    }

    abstract void onStopReadingDataFrom(UUID who);

    @Override
    @CallSuper
    public void close() {
        for (Map.Entry<UUID, BTio> btc : mConnections.entrySet()) {
            try {
                btc.getValue().close();
            } catch (IOException e) {
                Log.e(TAG, "Impossible to close socket: " +
                        btc.getValue().toString() + "\n" + e.getMessage());
                e.printStackTrace();
            }
        }
        mExecutorService.shutdownNow();
    }


    /* SEND METHODS */

    void sendTo(@NonNull BTBundle data, @NonNull UUID who) {

        File file = (File) data.get(File.class.getName());
        if (file != null) {
            data.getMapData().remove(File.class.getName());
        }

        try {
            Log.d(TAG, "Sending event: " + data.getBluetoothAction() + " to " + who);

            mConnections.get(who).writeData(data);

            if (file != null) {
                mConnections.get(who).writeFile(file);
            }

        } catch (IOException e) {
            Log.d(TAG, "Event: " + Event.Network.SEND_DATA_ERROR);

            Intent error = new Intent(Event.Network.SEND_DATA_ERROR);
            mLocalBroadcastManager.sendBroadcast(error);
            e.printStackTrace();
        }
    }

    void sendBroadcast(@NonNull BTBundle data) {
        for (Map.Entry<UUID, BTio> btc : mConnections.entrySet()) {
            sendTo(data, btc.getKey());
        }
    }

    void sendMulticast(@NonNull BTBundle data, @NonNull List<UUID> exceptions) {
        for (Map.Entry<UUID, BTio> btc : mConnections.entrySet()) {
            if (!exceptions.contains(btc.getKey())) {
                sendTo(data, btc.getKey());
            }
        }
    }

}
