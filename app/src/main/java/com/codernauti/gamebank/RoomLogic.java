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
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
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

                            //realm.insert(playerFromJson);
                            /*Player newPlayer = realm.createObject(Player.class, playerFromJson.getPlayerId());
                            newPlayer.setUsername(playerFromJson.getUsername());
                            newPlayer.setPhotoName(playerFromJson.getPhotoName());
                            newPlayer.setReady(playerFromJson.isReady());*/
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
        mNickname = hostNickname;
        mLocalBroadcastManager = broadcastManager;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BTEvent.MEMBER_CONNECTED);
        filter.addAction(Event.Game.MEMBER_READY);
        filter.addAction(BTEvent.MEMBER_DISCONNECTED);

        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    /**
     * @param listener: the Activity that listen the game state
     */
    public void setListener(@NonNull Listener listener) {
        Log.d(TAG, "Set listener: " + listener.getClass());
        mListener = listener;

        RealmResults<Player> players = Realm.getDefaultInstance()
                .where(Player.class)
                .findAll();

        listener.onNewPlayerJoined(players);
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

    /*public ArrayList<RoomPlayerProfile> getRoomPlayers() {
        return mPlayers;
    }

    public void clear() {
        mPlayers.clear();

        if (mListener != null) {
            mListener.onNewPlayerJoined(mPlayers);
        }
    }*/

    public void setRoomName(@NonNull String roomName) {
        mRoomName = roomName;
    }

}
