package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by dpolonio on 23/02/18.
 */

class BTio implements Closeable {

    private static final String TAG = "BTio";

    private final BluetoothSocket mBTSocket;
    private final UUID mUuid;
    private final BTDataMetric mMetric;

    private boolean mReady = false;

    BTio(UUID uuid, BluetoothSocket socket, BTDataMetric dataMetric) {
        mBTSocket = socket;
        mUuid = uuid;
        mMetric = dataMetric;
    }

    void writeData(@NonNull BTBundle toSend) throws IOException {
        if (mReady) {

            BTDataMetric.OutputMeasurement outputMeasurement = new BTDataMetric.OutputMeasurement(
                    mBTSocket.getOutputStream(), toSend.getBluetoothAction());
            ObjectOutputStream objos = new ObjectOutputStream(outputMeasurement.getOutputStream());

            Log.d(TAG, "Sending data\n\tThread: " + Thread.currentThread().getName());

            objos.writeObject(toSend);

            Log.d(TAG, "DATA SENT");

            mMetric.log(outputMeasurement);

        } else {
            Log.w(TAG, "BTio " + mUuid + " not ready yet to write data");
        }
    }

    void writeFile(File file) throws IOException {

        Log.d(TAG, "Sending file\n\tThread: " + Thread.currentThread().getName());

        OutputStream socketOs = mBTSocket.getOutputStream();
        BufferedOutputStream socketBos = new BufferedOutputStream(socketOs);
        DataOutputStream dataOutputStream = new DataOutputStream(socketOs);

        // Read file from storage
        FileInputStream inputStream = new FileInputStream(file);

        int bytesCount = (int) file.length();
        Log.d(TAG, "Bytes to send: " + bytesCount);


        // send length
        dataOutputStream.writeInt(bytesCount);

        // send image file
        byte[] buffer = new byte[bytesCount];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            Log.d(TAG, "Send bytes: " + bytesRead);
            socketBos.write(buffer);
        }

        socketBos.flush();
        inputStream.close();

        Log.d(TAG, "FILE SENT");
    }

    BTBundle readData() throws IOException {
        if (mBTSocket.isConnected()) {

            try {
                BTDataMetric.InputMeasurement inputMeasurement = new BTDataMetric.InputMeasurement(
                        mBTSocket.getInputStream());
                ObjectInputStream objis = new ObjectInputStream(inputMeasurement.getInputStream());

                Log.d(TAG, "Read data\n\tThread: " + Thread.currentThread().getName());

                BTBundle res = (BTBundle)objis.readObject();

                inputMeasurement.setInputAction(res.getBluetoothAction());
                mMetric.log(inputMeasurement);

                return res;
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

    void setReady() {
        mReady = true;
    }

}
