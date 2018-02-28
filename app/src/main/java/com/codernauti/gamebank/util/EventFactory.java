package com.codernauti.gamebank.util;

import android.support.annotation.NonNull;

import com.codernauti.gamebank.bluetooth.BTBundle;

import java.util.UUID;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class EventFactory {

    public static BTBundle newInitInformation(@NonNull PlayerProfile profile) {
        BTBundle btBundle = new BTBundle(Event.Network.INIT_INFORMATION);
        btBundle.getMapData().put(
                profile.getId().getClass().getName(),
                profile.getId());
        btBundle.getMapData().put(
                profile.getClass().getName(),
                profile);

        return btBundle;
    }

}
