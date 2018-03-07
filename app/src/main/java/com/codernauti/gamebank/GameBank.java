package com.codernauti.gamebank;

import android.app.Application;
import android.support.v4.content.LocalBroadcastManager;
import android.support.annotation.NonNull;

import java.util.UUID;

import com.codernauti.gamebank.util.PlayerProfile;
import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.SharePrefUtil;

/**
 * Created by davide on 01/03/18.
 */

public class GameBank extends Application {

    public static final UUID BT_ADDRESS = UUID.randomUUID();

    private GameLogic mGameLogic;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void initGameLogic() {
        String nickname = SharePrefUtil.getStringPreference(
                this, PrefKey.NICKNAME);

        mGameLogic = new GameLogic(
                LocalBroadcastManager.getInstance(this), nickname);
    }

    public GameLogic getGameLogic() {
        return mGameLogic;
    }
}
