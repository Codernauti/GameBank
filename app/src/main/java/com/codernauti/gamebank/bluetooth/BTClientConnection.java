package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

/**
 * Created by dpolonio on 23/02/18.
 */

public class BTClientConnection extends BTConnection {

    private final static String TAG = "BTClientConnection";

    private final BluetoothDevice mServer;
    private final Thread mConnection;
    private final BluetoothAdapter mBTAdapter;

    private BluetoothSocket mBTSocket;

    public BTClientConnection(@NonNull UUID uuid,
                       @NonNull BluetoothDevice server) {
        super(uuid);

        this.mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mServer = server;
        mConnection = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    mBTSocket = mServer.createRfcommSocketToServiceRecord(UUID);
                    mBTSocket.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void writeData(Parcelable toSend, BluetoothDevice device) {

    }

    @Override
    public void writeData(byte[] toSend, BluetoothDevice device) {

    }

    @Override
    public byte[] readData() {

        if (mBTSocket.isConnected()) {
            final List<Byte> data = new LinkedList<>();

            try (final InputStream is = mBTSocket.getInputStream()) {

                boolean read = true;
                while(read) {

                    byte[] tmpData = new byte[1024];
                    final int byteRead = is.read(tmpData);

                    if (byteRead != 0) {
                        for (int i = 0; i < byteRead; i++) {
                            data.add(tmpData[i]);
                        }
                    } else {
                        read = false;
                    }
                }

                return fromByteList(data);
            } catch (IOException e) {

                Log.e(TAG, "Error while reading data from " + mServer.getName());
                e.printStackTrace();

                return fromByteList(data);
            }
        }

        return new byte[0];
    }

    @Override
    public void disconnect() throws IOException {

        if (mBTSocket != null && mBTSocket.isConnected()) {
            mBTSocket.close();
        }
    }

    public void connect () throws IOException {

        mBTSocket = mServer.createRfcommSocketToServiceRecord(UUID);
        mBTSocket.connect();
    }

    private byte[] fromByteList (List<Byte> byteList) {

        byte[] res = new byte[byteList.size()];
        int i = 0;
        for (Byte b : byteList) {
            res[i] = b;
            i++;
        }
        return res;
    }
}
