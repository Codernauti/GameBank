package com.codernauti.gamebank.util;

import android.support.annotation.NonNull;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.bluetooth.BTBundle;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class EventFactory {

    private EventFactory(){}

    private static BTBundle addBasicInfo(@NonNull BTBundle bundle) {
        bundle.append(GameBank.BT_ADDRESS);
        return bundle;
    }

    public static BTBundle newInitInformation(@NonNull PlayerProfile profile) {
        return addBasicInfo(new BTBundle(Event.Network.INIT_INFORMATION)
                .append(profile));
    }

}
