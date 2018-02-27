package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dpolonio on 23/02/18.
 */

public class BTClientConnection extends BTConnection implements Closeable{

    public final static String EVENT_CONNECTION_ESTABLISHED = "ce";
    public final static String EVENT_CONNECTION_ERRONEED = "cerr";
    public final static String EVENT_INCOMING_DATA = "id";

    private final static String TAG = "BTClientConnection";

    private final BluetoothDevice mServer;
    private final ExecutorService mExecutorService;
    private final UUID mUuid;

    private BluetoothSocket mBTSocket;
    private BTio mBTio;

    public BTClientConnection(@NonNull UUID uuid,
                              @NonNull BluetoothDevice server,
                              @NonNull LocalBroadcastManager mLocalBroadcastManager) {
        super(mLocalBroadcastManager);

        this.mUuid = uuid;
        this.mServer = server;
        this.mExecutorService = Executors.newFixedThreadPool(1);
    }

    public void connect(@NonNull final BTBundle rendezvous) throws IOException {

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mBTSocket = mServer.createRfcommSocketToServiceRecord(mUuid);
                    mBTSocket.connect();
                    Log.d(TAG, "Connected with " + mServer.getName());

                    ObjectOutputStream objos = new ObjectOutputStream(mBTSocket.getOutputStream());
                    objos.writeObject(rendezvous);

                    Intent connectionCompleted = new Intent(EVENT_CONNECTION_ESTABLISHED);
                    mLocalBroadcastManager.sendBroadcast(connectionCompleted);

                } catch (IOException e) {
                    e.printStackTrace();

                    Intent error = new Intent(EVENT_CONNECTION_ERRONEED);
                    mLocalBroadcastManager.sendBroadcast(error);
                }

                mBTio = new BTio(mBTSocket);
            }
        });
    }

    public void subscribeForData() {

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                boolean flag = true;
                while (flag) {

                    try {

                        Object tmp = mBTio.readData();

                        if (tmp != null) {

                            BTBundle dataReceived = (BTBundle) tmp;

                            Intent toSend = new Intent(EVENT_INCOMING_DATA);
                            toSend.putExtra(BTBundle.BT_IDENTIFIER, dataReceived);

                            Log.d(TAG, "Data received, sending event");

                            mLocalBroadcastManager.sendBroadcast(toSend);
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

    public void connectAndSubscribe(@NonNull final BTBundle rendezvous) throws IOException {
        connect(rendezvous);
        subscribeForData();
    }

    @Override
    public void close() throws IOException {

        if (mBTSocket != null) {

            mBTSocket.close();
            mExecutorService.shutdownNow();
        }
    }
}
