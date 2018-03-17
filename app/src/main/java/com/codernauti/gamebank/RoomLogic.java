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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
        void onPlayerChange(Player player);
        void onPlayerRemove(Player player);
        void onNewPlayerJoined(List<Player> players);
        void onRoomNameChange(String roomName);
    }

    private final LocalBroadcastManager mLocalBroadcastManager;
    private Listener mListener;

    // Game logic fields
    // Lobby fields
    private RealmResults<Player> mPlayers;
    private final String mNickname;
    private String mRoomName;


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

                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Player playerFromJson = GameBank.gsonConverter.fromJson(newPlayerJson, Player.class);
                            Log.d(TAG, "Valid: " + playerFromJson.isValid());

                            realm.createOrUpdateObjectFromJson(Player.class, newPlayerJson);
                        }
                    });

                    if (mListener != null) {
                        RealmResults<Player> players = Realm.getDefaultInstance()
                                .where(Player.class)
                                .findAll();
                        mListener.onNewPlayerJoined(players);
                    }

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

                    if (mListener != null) {
                        mListener.onPlayerChange(player);
                    }

                } else if (BTEvent.MEMBER_DISCONNECTED.equals(action)) {

                    final String playerDisconnected = btBundle.getUuid().toString();

                    final Player player = Realm.getDefaultInstance()
                            .where(Player.class)
                            .equalTo("mId", playerDisconnected)
                            .findFirst();

                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            player.deleteFromRealm();
                        }
                    });

                    if (mListener != null) {
                        mListener.onPlayerRemove(player);
                    }

                }

            }
        }
    };


    RoomLogic(LocalBroadcastManager broadcastManager, String hostNickname) {
        Log.d(TAG, "Create RoomLogic");
        mNickname = hostNickname;
        mLocalBroadcastManager = broadcastManager;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BTEvent.MEMBER_CONNECTED);
        filter.addAction(Event.Game.MEMBER_READY);
        filter.addAction(BTEvent.MEMBER_DISCONNECTED);

        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }


    public void createMatchInstance(final Context context, final String matchName) {

        // Create database associated with match
        RealmConfiguration myConfig = new RealmConfiguration.Builder()
                .name("Test.realm")
                .directory(new File(context.getFilesDir(), "matches"))
                .build();

        Realm.setDefaultConfiguration(myConfig);
        Realm db = Realm.getDefaultInstance();

        mPlayers = Realm.getDefaultInstance().where(Player.class).findAll();
        mPlayers.addChangeListener(new RealmChangeListener<RealmResults<Player>>() {
            @Override
            public void onChange(RealmResults<Player> players) {
                Log.d(TAG, "Players change! Size: " + players.size());
                if (mListener != null) {
                    mListener.onNewPlayerJoined(players);
                }
            }
        });

        // insert initial data
        db.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // Get the current max id in the EntityName table
                //Number id = realm.where(Match.class).max("mId");
                // If id is null, set it to 1, else set increment it by 1
                int matchId = 42;//(id == null) ? 1 : id.intValue() + 1;
                final Match newMatch = realm.createObject(Match.class, matchId);

                SharePrefUtil.saveCurrentMatchId(context, matchId);

                //((GameBank)getApplication()).getBankLogic().setMatchId(matchId);
                // Set match nickname
                newMatch.setMatchName(matchName);

                // Set game date
                Calendar now = Calendar.getInstance();
                newMatch.setMatchStarted(
                        now.get(Calendar.DATE) + "/" + now.get(Calendar.MONTH) + "/" + now.get(Calendar.YEAR)
                );

                newMatch.setPlayerList(new RealmList<Player>());
                newMatch.setTransactionList(new RealmList<Transaction>());


                Player bank = realm.createObject(Player.class, GameBank.BANK_UUID);
                bank.setUsername("Bank");
                bank.setPhotoName("aaa");
                bank.setReady(true);

                Player myself = realm.createObject(Player.class, GameBank.BT_ADDRESS.toString());
                myself.setUsername(SharePrefUtil.getNicknamePreference(context));
                myself.setPhotoName(SharePrefUtil.getProfilePicturePreference(context));
                myself.setReady(true);

                newMatch.getPlayerList().add(bank);
                newMatch.getPlayerList().add(myself);
            }
        });
    }

    public void clearDatabase() {
        Realm.getDefaultInstance().deleteAll();
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
        Log.d(TAG, "TEST: sync state");

        if (mListener != null) {

            mListener.onNewPlayerJoined(
                    Realm.getDefaultInstance().where(Player.class)
                    .findAll()
            );
        }
    }

    public void setRoomName(@NonNull String roomName) {
        mRoomName = roomName;
    }

}
