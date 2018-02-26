package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.util.Log;
import android.support.annotation.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dpolonio on 26/02/18.
 */

public class BTHostConnection implements Closeable {

    private static final String TAG = "BTHostConnection";

    private final ExecutorService mExecutorService;
    private final Map<Integer, BTConnection> mConnections;
    private final int mAcceptedConnections;
    private final BluetoothServerSocket mBtServerSocket;


    public BTHostConnection(int acceptedConnections, @NonNull BluetoothServerSocket btServerSocket) {

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
                            BluetoothSocket btSocket = mBtServerSocket.accept();
                            mConnections.put(jobId, new BTConnection(btSocket));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public void sendBroadcast(@NonNull Serializable data) throws IOException {
        for (Map.Entry<Integer, BTConnection> btc : mConnections.entrySet()) {
            Log.d(TAG, "Sending data to " + btc.getKey());
            btc.getValue().writeData(data);
        }
    }

    public void closeAcceptConnections() throws IOException {
        mBtServerSocket.close();
    }

    @Override
    public void close() throws IOException {

        mBtServerSocket.close();
        for (Map.Entry<Integer, BTConnection> btc : mConnections.entrySet()) {
            btc.getValue().close();
        }
        mExecutorService.shutdownNow();
    }
}
