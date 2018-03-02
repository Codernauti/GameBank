package com.codernauti.gamebank.util;

import android.support.annotation.NonNull;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.bluetooth.BTBundle;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class EventFactory {

    private EventFactory(){}

    public static BTBundle newInitInfo(@NonNull PlayerProfile profile) {
        return new BTBundle(Event.Network.INIT_INFORMATION)
                .append(profile);
    }


    public static BTBundle newReadinessInfo(boolean isReady) {
        return new BTBundle(Event.Game.MEMBER_READY)
                .append(isReady);
    }

}
