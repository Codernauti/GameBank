package com.codernauti.gamebank;

import android.app.Application;
import android.support.v4.content.LocalBroadcastManager;

import java.util.UUID;

/**
 * Created by davide on 01/03/18.
 */

public class GameBank extends Application {

    public static final UUID BT_ADDRESS = UUID.randomUUID();

    private GameLogic mGameLogic;

    @Override
    public void onCreate() {
        super.onCreate();

        mGameLogic = new GameLogic(LocalBroadcastManager.getInstance(this));
    }

    public GameLogic getGameLogic() {
        return mGameLogic;
    }
}