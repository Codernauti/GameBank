package com.codernauti.gamebank.stateMonitors;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.Event;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.RoomLogic;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.game.Transaction;
import com.codernauti.gamebank.pairing.RoomPlayerProfile;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by dpolonio on 08/03/18.
 */

public class JoinService extends Service {

    private static final String TAG = "JoinService";

    private LocalBroadcastManager mLocalBroadcastManager;

    private final BroadcastReceiver mFromBTHostConnection = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            final BTBundle bundle = BTBundle.extractFrom(intent);

            if (bundle != null) {

                if (BTEvent.MEMBER_CONNECTED.equals(action)) {

                    RoomLogic roomLogic = ((GameBank) getApplication()).getRoomLogic();


                    final ArrayList<RoomPlayerProfile> mPlayers = roomLogic.getRoomPlayers();
                    final RoomPlayerProfile newPlayer = (RoomPlayerProfile)bundle.get(RoomPlayerProfile.class.getName());


                    Log.d(TAG, "(only host) Synchronize state with the new player.\n" +
                            "Send players: " + mPlayers.size() + "\n" +
                            "Room name: " + roomLogic.getRoomName() + "\n" +
                            "Init budget: " + roomLogic.getInitBudget());

                    Intent stateIntent = BTBundle.makeIntentFrom(
                            new BTBundle(BTEvent.CURRENT_STATE)
                                    .append(mPlayers)
                                    .append(roomLogic.getRoomName())
                                    .append(roomLogic.getInitBudget())
                    );
                    stateIntent.putExtra(BTHostService.RECEIVER_UUID, newPlayer.getId());
                    mLocalBroadcastManager.sendBroadcast(stateIntent);
                }
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BTEvent.MEMBER_CONNECTED);

        mLocalBroadcastManager.registerReceiver(mFromBTHostConnection, filter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mLocalBroadcastManager.unregisterReceiver(mFromBTHostConnection);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
