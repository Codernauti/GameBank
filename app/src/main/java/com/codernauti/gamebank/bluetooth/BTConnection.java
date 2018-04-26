package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
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
    private BTDataMetric btDataMetric;
    private final Map<UUID, BTio> mConnections;

    BTConnection (@NonNull LocalBroadcastManager mLocalBroadcastManager,
                  @NonNull ExecutorService executorService,
                  @NonNull String logPath) {
        this.mLocalBroadcastManager = mLocalBroadcastManager;
        this.mExecutorService = executorService;

        this.mConnections = new ConcurrentHashMap<>();

        if (btDataMetric == null) {

            try {
                btDataMetric = new BTDataMetric(logPath);
            } catch (IOException e){
                Log.e(TAG, "Impossible to open BTDataMetric");
                e.printStackTrace();
            }
        }
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

    /**
     * Be <b>careful!</b> This method is called by a Thread into ThreadPool
     * @param clienUuid
     */
    private void removeConnection(UUID clienUuid) {
        Log.d(TAG, "Disconnect " + clienUuid);
        mConnections.remove(clienUuid);
    }

    /**
     * Be <b>careful!</b> This method is called by a Thread into ThreadPool
     * @param clientUuid
     * @param newSocket
     */
    void addConnection(UUID clientUuid, BluetoothSocket newSocket) {
        Log.d(TAG, "Connection accepted from " + clientUuid);

        BTio btio = new BTio(clientUuid, newSocket, btDataMetric);
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

        if (btDataMetric != null) {
            try {
                btDataMetric.close();
            } catch (IOException e) {
                Log.e(TAG, "Impossible to close metric");
                e.printStackTrace();
            }
        }
    }


    /* SEND METHODS */

    void sendTo(@NonNull BTBundle data, @NonNull UUID who) {

        try {
            Log.d(TAG, "Sending event: " + data.getBluetoothAction() + " to " + who);

            mConnections.get(who).writeData(data);

        } catch (IOException e) {
            Log.d(TAG, "Event: " + BTEvent.SEND_DATA_ERROR);

            Intent error = new Intent(BTEvent.SEND_DATA_ERROR);
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

    /**
     * Call this method when you are sure that client received the state of match.
     * If this 'who' connection is set ready and doesn't received the state the protocol
     * could be broken because 'who' without a match state could receive an event from
     * other clients or host.
     * @param who client uuid of the connection that have to start listen to
     */
    void setReady(UUID who) {
        mConnections.get(who).setReady();
    }

    /**
     * Be <b>careful!</b> this method could be subjected by a race condition
     * @return the total number of connections opened
     */
    int getSizeOpenConnections() {
        return mConnections.size();
    }
}
