package com.codernauti.gamebank.bluetooth;

/**
 * Created by dpolonio on 19/02/18.
 */

public abstract class BTDevice {

    private String name;
    private String address;

    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }

    @Override
    public abstract boolean equals (Object other);
}
