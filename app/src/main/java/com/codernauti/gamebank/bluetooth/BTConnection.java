package com.codernauti.gamebank.bluetooth;

import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
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
    final Map<UUID, BTio> mConnections;

    BTConnection (@NonNull LocalBroadcastManager mLocalBroadcastManager,
                  @NonNull ExecutorService executorService) {
        this.mLocalBroadcastManager = mLocalBroadcastManager;
        this.mExecutorService = executorService;
        this.mConnections = new ConcurrentHashMap<>();
    }

    void subscribeForData(@NonNull final BTio btio) {

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

    public void sendTo(@NonNull Serializable data, @NonNull UUID who) throws IOException {
        Log.d(TAG, "Sending data to " + who);
        mConnections.get(who).writeData(data);
    }
}
