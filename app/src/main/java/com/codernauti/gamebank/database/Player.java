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
    private String mPhotoName;
    private boolean mReady;

    public Player() {}

    public Player(String uuid) {
        mId = uuid;
    }

    public Player(
            @NonNull String playerId,
            @NonNull String username,
            @NonNull String photo_path
    ) {
        this.mId = playerId;
        this.mUsername = username;
        this.mPhotoName = photo_path;
        this.mReady = false;
    }

    public Player (@NonNull ImagePlayerProfile playerProfile) {

        this.mId = playerProfile.getId().toString();
        this.mUsername = playerProfile.getNickname();
        this.mPhotoName = playerProfile.getImageName();
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

    public String getPhotoName() {
        return mPhotoName;
    }

    public void setPhotoName(@NonNull String photoPath) {
        this.mPhotoName = photoPath;
    }

    public boolean isReady() {
        return mReady;
    }

    public void setReady(boolean mIsReady) {
        this.mReady = mIsReady;
    }
}
