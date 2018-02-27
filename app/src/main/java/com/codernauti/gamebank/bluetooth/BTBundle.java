package com.codernauti.gamebank.bluetooth;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by dpolonio on 26/02/18.
 */

public class BTBundle implements Serializable {

    public final static String BTBUNDLE_KEY = "BTBUNDLE";

    private String action;
    private final HashMap<String, Serializable> data;

    public BTBundle(String action) {
        this.action = action;
        this.data = new HashMap<>();
    }

    public String getAction() {
        return action;
    }

    public HashMap<String, Serializable> getMapData() {
        return data;
    }
}
