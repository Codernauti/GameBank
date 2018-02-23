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

    private BluetoothDevice mServer;

    private BluetoothSocket mBTSocket;

    public BTClientConnection(@NonNull UUID uuid, @NonNull BluetoothDevice server) {
        super(uuid);

        this.mServer = server;
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

                    Log.d(TAG, "Just read " + new String(tmpData).substring(0, byteRead));

                    if (byteRead != 0) {
                        for (int i = 0; i < byteRead && read; i++) {

                            if (tmpData[i] == Byte.MIN_VALUE) {
                                Log.d(TAG, "Read MIN_VALUE");
                                read = false;
                            } else {

                                data.add(tmpData[i]);
                            }
                        }
                    } else {
                        read = false;
                    }
                }

                return fromByteList(data);

                /*byte[] res = new byte[1024];
                int bytes = is.read(res);

                return new String(res).substring(0, bytes).getBytes();*/

            } catch (IOException e) {

                Log.e(TAG, "Error while reading data from " + mServer.getName());
                e.printStackTrace();

                Log.e(TAG, "Data size: " + data.size());

                return fromByteList(data);
            }
        }

        return new byte[0];
    }

    @Override
    public void close() throws IOException {

        if (mBTSocket != null && mBTSocket.isConnected()) {

            Log.d(TAG, "Closing BT connection");
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
