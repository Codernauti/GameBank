package com.codernauti.gamebank.database;

import android.support.annotation.NonNull;

import com.codernauti.gamebank.pairing.ImagePlayerProfile;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dpolonio on 13/03/18.
 */

public class Player extends RealmObject {

    @PrimaryKey
    private final UUID mPlayerId;
    private final String mUsername;
    private final String mPhotoName;

    public Player (
            @NonNull UUID playerId,
            @NonNull String username,
            @NonNull String photo_path
    ) {
        this.mPlayerId = playerId;
        this.mUsername = username;
        this.mPhotoName = photo_path;
    }

    public Player (@NonNull ImagePlayerProfile playerProfile) {

        this.mPlayerId = playerProfile.getId();
        this.mUsername = playerProfile.getNickname();
        this.mPhotoName = playerProfile.getImageName();
    }

    @NonNull
    public UUID getPlayerId() {
        return mPlayerId;
    }

    @NonNull
    public String getUsername() {
        return mUsername;
    }

    @NonNull
    public String getPhotoPath() {
        return mPhotoName;
    }
}
