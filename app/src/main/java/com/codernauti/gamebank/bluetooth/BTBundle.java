package com.codernauti.gamebank.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by dpolonio on 26/02/18.
 */

public class BTBundle implements Serializable {

    private final static String BTBUNDLE_KEY = "BTBUNDLE";

    private String action;
    private HashMap<String, Serializable> data;

    public BTBundle(String bluetoothAction) {
        this(bluetoothAction, new HashMap<String, Serializable>());
    }

    private BTBundle(String bluetoothAction, HashMap<String, Serializable> data) {
        this.action = bluetoothAction;
        this.data = data;
    }

    @NonNull
    public String getBluetoothAction() {
        return action;
    }

    @NonNull
    public HashMap<String, Serializable> getMapData() {
        return data;
    }

    @NonNull
    public Intent getIntent(@NonNull String eventType) {
        Intent res = new Intent(eventType);
        res.putExtra(BTBUNDLE_KEY, this);

        return res;
    }

    @NonNull
    public Intent getIntent() {
        Intent res = new Intent(action);
        res.putExtra(BTBUNDLE_KEY, this);

        return res;
    }

    @Nullable
    public static BTBundle extract(@NonNull Intent intent) {
        Bundle tmp = intent.getExtras();
        Object received = tmp.get(BTBUNDLE_KEY);

        if (received != null) {
            return (BTBundle) received;
        }

        return null;
    }

    public BTBundle appned() {

        return this;
    }

    static class Builder {
        private String mEvent;
        private final HashMap<String, Serializable> data;

        public Builder() {
            data = new HashMap<>();
        }

        public Builder setAction(String event) {
            mEvent = event;
            return this;
        }

        public Builder append(Serializable content) {
            data.put(content.getClass().getName(), content);
            return this;
        }

        public BTBundle build() {
            return new BTBundle(mEvent, data);
        }

    }

}
