package com.codernauti.gamebank.util;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by dpolonio on 27/02/18.
 */

public class PlayerProfile implements Serializable {

    private String mNickname;
    private final UUID mId;
    private Uri mPicture;

    public PlayerProfile(@NonNull String nickname,
                         @NonNull UUID id, @NonNull Uri picture) {
        this.mNickname = nickname;
        this.mId = id;
        this.mPicture = picture;
    }

    public PlayerProfile(@NonNull String nickname,
                         @NonNull UUID id) {
        this.mNickname = nickname;
        this.mId = id;
    }

    @NonNull
    public UUID getId() {
        return mId;
    }

    @NonNull
    public String getNickname() {
        return mNickname;
    }

    public void setNickname(@NonNull String newNickname) {
        this.mNickname = newNickname;
    }

    public Uri getUriPicture() {
        return mPicture;
    }
}
