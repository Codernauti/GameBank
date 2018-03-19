package com.codernauti.gamebank.stateMonitors;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.util.UUID;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by dpolonio on 08/03/18.
 */

public class HostJoinService extends Service {

    private static final String TAG = "HostJoinService";

    private RealmResults<Player> mPlayers;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        mPlayers = Realm.getDefaultInstance().where(Player.class).findAll();
        mPlayers.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Player>>() {
            @Override
            public void onChange(@NonNull RealmResults<Player> players,
                                 @javax.annotation.Nullable OrderedCollectionChangeSet changeSet) {
                Log.d(TAG, "onChange players list");

                if (changeSet == null) {
                    return;
                }

                final int[] insertions = changeSet.getInsertions();
                if (insertions.length == 1) {
                    // new player! -> sync state

                    final Player playerAdded = players.get(insertions[0]);
                    Log.d(TAG, "Player added: " + playerAdded.getPlayerId());

                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            Match match = Realm.getDefaultInstance()
                                    .where(Match.class)
                                    .equalTo("mId", SharePrefUtil.getCurrentMatchId(HostJoinService.this))
                                    .findFirst();

                            match.getPlayerList().add(playerAdded);

                            // Send to BT layer
                            Intent stateIntent = BTBundle.makeIntentFrom(
                                    new BTBundle(BTEvent.CURRENT_STATE)
                                            .appendJson("MATCH", GameBank.gsonConverter.toJson(match))
                            );
                            stateIntent.putExtra(BTHostService.RECEIVER_UUID,
                                    UUID.fromString(playerAdded.getPlayerId()));

                            LocalBroadcastManager.getInstance(HostJoinService.this)
                                    .sendBroadcast(stateIntent);
                        }
                    });
                }
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
