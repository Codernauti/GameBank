package com.codernauti.gamebank.bluetooth;

/**
 * Created by dpolonio on 19/02/18.
 */

public class BTClient implements BTDevice {

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
}
