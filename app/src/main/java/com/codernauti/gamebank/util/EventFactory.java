package com.codernauti.gamebank.util;

import android.support.annotation.NonNull;

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTClient;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class EventFactory {

    public static BTBundle newInitInformation(@NonNull PlayerProfile profile) {
        return new BTBundle(Event.Network.INIT_INFORMATION)
                .append(profile.getId())
                .append(profile);
    }

}
