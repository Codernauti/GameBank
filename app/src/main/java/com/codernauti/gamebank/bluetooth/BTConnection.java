package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Parcelable;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by dpolonio on 23/02/18.
 */

public abstract class BTConnection {

    final UUID UUID;

    BTConnection(UUID uuid) {
        this.UUID = uuid;
    }

    protected abstract void writeData(Parcelable toSend, BluetoothDevice device);
    protected abstract void writeData(byte[] toSend, BluetoothDevice device);
    protected abstract byte[] readData() throws IOException;

    public abstract void disconnect() throws IOException;
}
