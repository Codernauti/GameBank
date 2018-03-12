package com.codernauti.gamebank.pairing;

import android.support.annotation.NonNull;

import com.codernauti.gamebank.util.ImagePlayerProfile;

import java.util.UUID;

/**
 * Created by davide on 01/03/18.
 */

public class RoomPlayerProfile extends ImagePlayerProfile {

    private boolean isReady;

    public RoomPlayerProfile(@NonNull String nickname, @NonNull UUID id,
                             @NonNull String picture, boolean isReady) {
        super(nickname, id, picture);
        this.isReady = isReady;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

}
