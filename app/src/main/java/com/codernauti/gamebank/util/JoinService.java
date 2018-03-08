package com.codernauti.gamebank.util;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.RoomLogic;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.pairing.RoomPlayer;

import java.util.ArrayList;

/**
 * Created by dpolonio on 08/03/18.
 */

public class JoinService extends Service {

    private static final String TAG = "JoinService";

    private final BroadcastReceiver mFromBTHostConnection = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            final BTBundle bundle = BTBundle.extractFrom(intent);

            if (bundle != null) {

                if (Event.Game.MEMBER_JOINED.equals(action)) {

                    final ArrayList<RoomPlayer> mPlayers = ((GameBank)getApplication())
                            .getRoomLogic()
                            .getRoomPlayers();
                    final RoomPlayer newPlayer = (RoomPlayer)bundle.get(RoomPlayer.class.getName());

                    Log.d(TAG, "(only host) Synchronize state with the new player.\n" +
                            "Send players: " + mPlayers.size());

                    Intent stateIntent = BTBundle.makeIntentFrom(
                            new BTBundle(Event.Game.CURRENT_STATE)
                                    .append(mPlayers)
                    );
                    stateIntent.putExtra(BTHostService.RECEIVER_UUID, newPlayer.getId());
                    LocalBroadcastManager.getInstance(JoinService.this).sendBroadcast(stateIntent);
                }
            }

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Event.Game.MEMBER_JOINED);

        LocalBroadcastManager.getInstance(this).registerReceiver(mFromBTHostConnection, filter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFromBTHostConnection);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
