package com.codernauti.gamebank;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by dpolonio on 27/02/18.
 */

public class PlayerProfile implements Serializable {

    private final String mNickname;
    private final UUID mId;
    // TODO profile picture

    public PlayerProfile(@NonNull String nickname,
                         @NonNull UUID id) {
        this.mNickname = nickname;
        this.mId = id;
    }

    public UUID getId() {
        return mId;
    }

    public String getNickname() {
        return mNickname;
    }
}
