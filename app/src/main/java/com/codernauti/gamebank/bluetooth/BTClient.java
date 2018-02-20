package com.codernauti.gamebank.bluetooth;

/**
 * Created by dpolonio on 19/02/18.
 */

public class BTClient extends BTDevice {

    private String name;
    private String address;

    public BTClient (String name, String address) {
        this.name = name;
        this.address = address;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAddress() {
        return address;
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
