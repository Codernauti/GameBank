package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.util.Event;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Created by dpolonio on 23/02/18.
 */

public class BTClientConnection extends BTConnection {

    public final static String EVENT_INCOMING_DATA = "id";

    private final static String TAG = "BTClientConnection";

    private final BluetoothDevice mServer;
    private final UUID mBluetoothConnectionUuid;

    private BluetoothSocket mBTSocket;
    private UUID mHostUUID;

    BTClientConnection(@NonNull UUID uuid,
                       @NonNull BluetoothDevice server,
                       @NonNull LocalBroadcastManager mLocalBroadcastManager) {
        super(mLocalBroadcastManager, Executors.newFixedThreadPool(1));

        this.mBluetoothConnectionUuid = uuid;
        this.mServer = server;

    }

    private void connect(@NonNull final BTBundle rendezvous) throws IOException {

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mBTSocket = mServer.createRfcommSocketToServiceRecord(mBluetoothConnectionUuid);
                    mBTSocket.connect();
                    Log.d(TAG, "Connected with " + mServer.getName());

                    ObjectOutputStream objos = new ObjectOutputStream(mBTSocket.getOutputStream());
                    objos.writeObject(rendezvous);

                    Intent connectionCompleted = new Intent(Event.Network.CONN_ESTABLISHED);
                    mLocalBroadcastManager.sendBroadcast(connectionCompleted);

                } catch (IOException e) {
                    e.printStackTrace();

                    Intent error = new Intent(Event.Network.CONN_ERRONEOUS);
                    mLocalBroadcastManager.sendBroadcast(error);
                }
            }
        });

        mHostUUID = UUID.randomUUID();
        GameBank.setBtHostAddress(mHostUUID);
        mConnections.put(mHostUUID, new BTio(mBTSocket));
    }

    void connectAndSubscribe(@NonNull final BTBundle rendezvous) throws IOException {

        // FIXME connect should be a FutureTask that returns a uuid, and at that point I subscribe for data
        connect(rendezvous);
        if (mHostUUID == null) {
            Log.e(TAG, "Mhostuuid is null");
        } else {

            subscribeForData(mConnections.get(mHostUUID));
        }
    }

    @Override
    public void close() throws IOException {

        if (mBTSocket != null) {

            mBTSocket.close();
            mExecutorService.shutdownNow();
        }
    }
}
