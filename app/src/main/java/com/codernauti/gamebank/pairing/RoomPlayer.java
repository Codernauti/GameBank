package com.codernauti.gamebank.pairing;

import android.net.Uri;
import android.support.annotation.NonNull;

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

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

}
