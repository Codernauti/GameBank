package com.codernauti.gamebank.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.codernauti.gamebank.GameBank;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by dpolonio on 26/02/18.
 */

public class BTBundle implements Serializable {

    private final static String BTBUNDLE_KEY = "BTBUNDLE";

    private final String mAction;
    private final HashMap<String, Serializable> data = new HashMap<String, Serializable>();

    public BTBundle(String bluetoothAction) {
        mAction = bluetoothAction;
        append(GameBank.BT_ADDRESS);
    }

    @NonNull
    public String getBluetoothAction() {
        return mAction;
    }

    @NonNull
    @Deprecated
    public HashMap<String, Serializable> getMapData() {
        return data;
    }

    @Nullable
    public Serializable get(String key) {
        return data.get(key);
    }

    @NonNull
    public UUID getUuid() {
        return (UUID) data.get(UUID.class.getName());
    }

    public boolean isSentByMe() {
        return getUuid().equals(GameBank.BT_ADDRESS);
    }

    @NonNull
    public Intent getIntent() {
        Intent res = new Intent(mAction);
        res.putExtra(BTBUNDLE_KEY, this);

        return res;
    }

    @Nullable
    public static BTBundle extractFrom(@NonNull Intent intent) {
        Bundle tmp = intent.getExtras();
        Object received = tmp.get(BTBUNDLE_KEY);

        if (received != null) {
            return (BTBundle) received;
        }

        return null;
    }

    public BTBundle append(Serializable content) {
        data.put(content.getClass().getName(), content);
        return this;
    }

    public static Intent makeIntentFrom(BTBundle bundle) {
        Intent intent = new Intent(bundle.mAction);
        intent.putExtra(BTBUNDLE_KEY, bundle);

        return intent;
    }

}
