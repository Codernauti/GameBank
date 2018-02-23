package com.codernauti.gamebank.bluetooth;

/**
 * Created by dpolonio on 20/02/18.
 */

public class BTHost extends BTDevice {

    private boolean paired;

    public BTHost (String name, String address) {

        this(name, address, false);
    }

    public BTHost (String name, String address, boolean paired) {
        super(name, address);

        this.paired = paired;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof BTHost)) {
            return false;
        }

        BTHost that = (BTHost) other;

        return
                address.equals(that.address) &&
                paired == that.paired;
    }

    public boolean isPaired() {
        return paired;
    }
}