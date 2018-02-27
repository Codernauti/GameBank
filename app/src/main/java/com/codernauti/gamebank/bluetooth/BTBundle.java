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
    private final HashMap<String, Serializable> data;

    public BTBundle(String action) {
        this.action = action;
        this.data = new HashMap<>();
    }

    @NonNull
    public String getAction() {
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

    @Nullable
    public static BTBundle extract(@NonNull Intent intent) {
        Bundle tmp = intent.getExtras();
        Object received = tmp.get(BTBUNDLE_KEY);

        if (received != null) {
            return (BTBundle) received;
        }

        return null;
    }
}
