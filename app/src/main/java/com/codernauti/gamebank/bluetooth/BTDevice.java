package com.codernauti.gamebank.bluetooth;

import java.io.Serializable;

/**
 * Created by dpolonio on 19/02/18.
 */

public abstract class BTDevice implements Serializable {

    protected String name;
    protected String address;

    public BTDevice (String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }

    @Override
    public abstract boolean equals (Object other);
}
