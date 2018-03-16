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

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.pairing.RoomPlayerProfile;
import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.SharePrefUtil;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

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

                if (BTEvent.MEMBER_CONNECTED.equals(action)) {

                    addPlayerIntoDb(bundle);

                    /*final ArrayList<RoomPlayerProfile> mPlayers = ((GameBank) getApplication())
                            .getRoomLogic()
                            .getRoomPlayers();
                    final RoomPlayerProfile newPlayer = (RoomPlayerProfile)bundle.get(RoomPlayerProfile.class.getName());

                    Log.d(TAG, "(only host) Synchronize state with the new player.\n" +
                            "Send players: " + mPlayers.size());

                    Intent stateIntent = BTBundle.makeIntentFrom(
                            new BTBundle(BTEvent.CURRENT_STATE)
                                    .append(mPlayers)
                                    .append(((GameBank) getApplication())
                                            .getRoomLogic()
                                            .getRoomName())
                    );
                    stateIntent.putExtra(BTHostService.RECEIVER_UUID, newPlayer.getId());
                    LocalBroadcastManager.getInstance(JoinService.this).sendBroadcast(stateIntent);*/
                }
            }

        }
    };

    private void addPlayerIntoDb(final BTBundle bundle) {
        Realm db = Realm.getDefaultInstance();

        db.beginTransaction();
        String playerJson = (String) bundle.get(String.class.getName());
        Log.d(TAG, "Player json: \n" + playerJson);
        // FIXME: test carefully
        final Player newPlayer = db.createOrUpdateObjectFromJson(Player.class, playerJson);

        Match match = db.where(Match.class)
                .equalTo("mId", SharePrefUtil.getCurrentMatchId(this))
                .findFirst();

        match.getPlayerList().add(newPlayer);


        // Send state to client
        for (Player p : match.getPlayerList()) {
            Log.d(TAG, "Player name: " + p.getUsername());
        }
        db.commitTransaction();


        Intent stateIntent = BTBundle.makeIntentFrom(
                new BTBundle(BTEvent.CURRENT_STATE)
                        .appendJson("MATCH", GameBank.gsonConverter.toJson(match))
                        //.appendJson("PLAYERS", GameBank.gsonConverter.toJson(match.getPlayerList()))  // TODO test
        );
        stateIntent.putExtra(BTHostService.RECEIVER_UUID,
                UUID.fromString(newPlayer.getPlayerId()));

        LocalBroadcastManager.getInstance(JoinService.this)
                .sendBroadcast(stateIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BTEvent.MEMBER_CONNECTED);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mFromBTHostConnection, filter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mFromBTHostConnection);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
