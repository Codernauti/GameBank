package com.codernauti.gamebank;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Eduard on 03-Mar-18.
 */

public final class RoomLogic {

    private static final String TAG = "RoomLogic";

    public interface Listener {
        void stateSynchronized();
    }

    private final LocalBroadcastManager mLocalBroadcastManager;
    private Listener mListener;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "Received action: " + action + "\n" +
                    Thread.currentThread().getName());

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (BTEvent.MEMBER_CONNECTED.equals(action)) {

                    final String newPlayerJson = (String) btBundle.get(String.class.getName());
                    Log.d(TAG, "Player json: \n" + newPlayerJson);

                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Player playerFromJson = GameBank.gsonConverter.fromJson(newPlayerJson, Player.class);
                            Log.d(TAG, "Valid: " + playerFromJson.isValid());

                            realm.copyToRealmOrUpdate(playerFromJson);
                        }
                    });

                } else if (Event.Game.MEMBER_READY.equals(action)) {

                    String uuid = btBundle.getUuid().toString();
                    final boolean isReady = (boolean) btBundle.get(Boolean.class.getName());
                    Log.d(TAG, "Player " + uuid + " is ready? " + isReady);

                    final Player player = Realm.getDefaultInstance()
                            .where(Player.class)
                            .equalTo("mId", uuid)
                            .findFirst();

                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            player.setReady(isReady);
                        }
                    });

                } else if (BTEvent.MEMBER_DISCONNECTED.equals(action)) {

                    final String playerDisconnected = btBundle.get(UUID.class.getName()).toString();
                    Log.d(TAG, "Player to remove: " + playerDisconnected);

                    final Player player = Realm.getDefaultInstance()
                            .where(Player.class)
                            .equalTo("mId", playerDisconnected)
                            .findFirst();

                    if (player != null) {
                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                player.deleteFromRealm();
                            }
                        });
                    }
                }

            }
        }
    };


    RoomLogic(LocalBroadcastManager broadcastManager) {
        Log.d(TAG, "Create RoomLogic");
        mLocalBroadcastManager = broadcastManager;
    }


    public void createMatchInstance(final Context context, final String matchName, final int initBudget) {
        Log.d(TAG, "Create new match (aka new db).\n" +
                "MatchName: " + matchName + " InitBudget: " + initBudget);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String nameDatabase = isoFormat.format(Calendar.getInstance().getTime());

        initRealmDatabase(context, nameDatabase);

        // insert initial data
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // Get the current max id in the EntityName table
                //Number id = realm.where(Match.class).max("mId");
                // If id is null, set it to 1, else set increment it by 1
                int matchId = 42;//(id == null) ? 1 : id.intValue() + 1;
                final Match newMatch = realm.createObject(Match.class, matchId);

                SharePrefUtil.saveCurrentMatchId(context, matchId);


                newMatch.setMatchName(matchName);
                newMatch.setNowAsTimeStarted();
                newMatch.setInitBudget(initBudget);
                newMatch.setPlayerList(new RealmList<Player>());
                newMatch.setTransactionList(new RealmList<Transaction>());


                Player bank = realm.createObject(Player.class, GameBank.BANK_UUID);
                bank.setUsername("Bank");
                bank.setReady(true);

                Player myself = realm.createObject(Player.class, GameBank.BT_ADDRESS.toString());
                myself.setUsername(SharePrefUtil.getNicknamePreference(context));
                myself.setReady(true);

                newMatch.getPlayerList().add(bank);
                newMatch.getPlayerList().add(myself);
            }
        });
    }

    public void initRealmDatabase(@NonNull Context context, @NonNull String nameDatabase) {
        // Create database associated with match
        RealmConfiguration.Builder configBuilder = new RealmConfiguration.Builder()
                .name(nameDatabase + ".realm")
                .directory(new File(context.getFilesDir(), "matches"));

        if (nameDatabase.equals(DatabaseMatchManager.CLIENT_DB_NAME)) {
            Log.d(TAG, "init ClientDatabase");

            Realm.setDefaultConfiguration(configBuilder.build());

            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.deleteAll();
                }
            });

        } else {
            Realm.setDefaultConfiguration(configBuilder.build());
        }
    }

    public void clearDatabase() {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
    }

    /**
     * @param listener: the Activity that listen the game state
     */
    public void setListener(@NonNull Listener listener) {
        Log.d(TAG, "Set listener: " + listener.getClass());
        mListener = listener;
    }

    /**
     * Remove the reference to the Activity that listen game state
     */
    public void removeListener() {
        mListener = null;
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BTEvent.MEMBER_CONNECTED);
        filter.addAction(Event.Game.MEMBER_READY);
        filter.addAction(BTEvent.MEMBER_DISCONNECTED);

        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    public void unregisterReceiver() {
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
    }


    public boolean matchCanStart() {

        RealmResults<Player> playersNotReady = Realm.getDefaultInstance()
                .where(Player.class)
                .equalTo("mReady", false)
                .findAll();

        return playersNotReady.isEmpty();
    }

    public void syncState() {
        Log.d(TAG, "sync state completed. Update UI");

        if (mListener != null) {
            mListener.stateSynchronized();
        }
    }

}
