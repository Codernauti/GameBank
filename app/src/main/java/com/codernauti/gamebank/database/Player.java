package com.codernauti.gamebank.database;

import android.support.annotation.NonNull;

import com.codernauti.gamebank.pairing.ImagePlayerProfile;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dpolonio on 13/03/18.
 */

public class Player extends RealmObject {

    @PrimaryKey
    private String mId;
    private String mUsername;
    private boolean mReady;

    private String mImageBase64 = "";

    public Player() {}

    public Player(String uuid) {
        mId = uuid;
    }

    public Player(
            @NonNull String playerId,
            @NonNull String username
    ) {
        this.mId = playerId;
        this.mUsername = username;
    }

    public Player(
            @NonNull String playerId,
            @NonNull String username,
            boolean isReady
    ) {
        this(playerId, username);
        mReady = isReady;
    }

    @NonNull
    public String getPlayerId() {
        return mId;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(@NonNull String username) {
        this.mUsername = username;
    }

    public boolean isReady() {
        return mReady;
    }

    public void setReady(boolean mIsReady) {
        this.mReady = mIsReady;
    }

    public String getImageBase64() {
        return mImageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.mImageBase64 = imageBase64;
    }

    public String getPictureNameFile() {
        return mId + ".jpeg";
    }
}
