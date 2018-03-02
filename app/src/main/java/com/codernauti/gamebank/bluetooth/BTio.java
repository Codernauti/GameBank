package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.io.Serializable;

/**
 * Created by dpolonio on 23/02/18.
 */

class BTio implements Closeable {

    private static final String TAG = "BTio";

    private BluetoothSocket mBTSocket;

    BTio(BluetoothSocket socket) {
        mBTSocket = socket;
    }

    void writeData(@NonNull Serializable toSend) throws IOException {

        ObjectOutputStream objos = new ObjectOutputStream(mBTSocket.getOutputStream());

        Log.d(TAG, "Sending data\n\tThread: " + Thread.currentThread().getName());

        objos.writeObject(toSend);
        Log.d(TAG, "DATA SENT");
    }

    Object readData() throws IOException {
        if (mBTSocket.isConnected()) {

            try {
                ObjectInputStream objis = new ObjectInputStream(mBTSocket.getInputStream());

                Log.d(TAG, "Read data\n\tThread: " + Thread.currentThread().getName());

                return objis.readObject();
            } catch (ClassNotFoundException | IOException e) {

                Log.e(TAG, "Data stream end unexpectedly: " + e.getMessage());
                e.printStackTrace();

                return null;
            }
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        mBTSocket.close();
    }
}
