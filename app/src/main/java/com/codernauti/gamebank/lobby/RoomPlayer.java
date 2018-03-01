package com.codernauti.gamebank.lobby;

import android.support.annotation.NonNull;

import com.codernauti.gamebank.bluetooth.BTClient;
import com.codernauti.gamebank.util.PlayerProfile;

import java.util.UUID;

/**
 * Created by davide on 01/03/18.
 */

public class RoomPlayer extends PlayerProfile {

    private boolean isReady;

    public RoomPlayer(@NonNull String nickname, @NonNull UUID id, boolean isReady) {
        super(nickname, id);

        this.isReady = isReady;
    }

    public boolean isReady() {
        return isReady;
    }

    public BTClient getBTClient() {
        return new BTClient(getNickname(), getId().toString(), isReady);
    }
}
