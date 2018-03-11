package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by dpolonio on 23/02/18.
 */

class BTio implements Closeable {

    private static final String TAG = "BTio";

    private final BluetoothSocket mBTSocket;
    private final UUID mUuid;

    BTio(UUID uuid, BluetoothSocket socket) {
        mBTSocket = socket;
        mUuid = uuid;
    }

    void writeData(@NonNull Serializable toSend) throws IOException {

        ObjectOutputStream objos = new ObjectOutputStream(mBTSocket.getOutputStream());

        Log.d(TAG, "Sending data\n\tThread: " + Thread.currentThread().getName());

        objos.writeObject(toSend);

        Log.d(TAG, "DATA SENT");
    }

    void writeFile(File file) throws IOException {

        Log.d(TAG, "Sending file\n\tThread: " + Thread.currentThread().getName());

        OutputStream socketOs = mBTSocket.getOutputStream();
        //BufferedOutputStream socketBos = new BufferedOutputStream(socketOs);
        DataOutputStream dataOutputStream = new DataOutputStream(socketOs);

        // Read file from storage
        FileInputStream inputStream = new FileInputStream(file);

        int bytesCount = (int) file.length();
        Log.d(TAG, "Bytes to send: " + bytesCount);
        byte[] buffer = new byte[bytesCount];


        // send length
        dataOutputStream.writeInt(bytesCount);

        // send image file
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            Log.d(TAG, "Send bytes: " + bytesRead);
            socketOs.write(buffer);
        }

        /*int bytesRead;
        while((bytesRead = inputStream.read(buffer, 0, bytesCount)) != -1)
        {
            socketOs.write(buffer, 0, bytesRead);
        }*/

        inputStream.close();

        Log.d(TAG, "FILE SENT");
    }

    Object readData() throws IOException {
        if (mBTSocket.isConnected()) {

            try {
                ObjectInputStream objis = new ObjectInputStream(mBTSocket.getInputStream());

                Log.d(TAG, "Read data\n\tThread: " + Thread.currentThread().getName());

                return objis.readObject();
            } catch (ClassNotFoundException e) {

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

    public UUID getUUID(){
        return mUuid;
    }

}
