package com.codernauti.gamebank.bluetooth;

/**
 * Created by dpolonio on 19/02/18.
 */

public class BTClient extends BTDevice {

    private boolean isReady;

    public BTClient (String name, String address, boolean isReady) {
        super(name, address);

        this.isReady = isReady;
    }

    public boolean isReady() {
        return isReady;
    }

    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (!(other instanceof BTClient)) {
            return false;
        }

        BTClient that = (BTClient) other;

        return address.equals(that.address);
    }
}
