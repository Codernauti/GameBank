package com.codernauti.gamebank;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.lobby.RoomPlayer;
import com.codernauti.gamebank.util.Event;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Eduard on 03-Mar-18.
 */

public final class GameLogic {

    private static final String TAG = "GameLogic";

    public interface Listener {

        void onNewPlayerJoined(ArrayList<RoomPlayer> newPlayer);
        void onPlayerStateChange(RoomPlayer player);
    }


    private final LocalBroadcastManager mLocalBroadcastManager;
    private Listener mListener;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "Action received: " + action);

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (Event.Game.MEMBER_JOINED.equals(action)) {

                    ArrayList<RoomPlayer> members = (ArrayList<RoomPlayer>)
                            btBundle.get(ArrayList.class.getName());
                    mPlayers.addAll(members);

                    if (mListener != null) {
                        mListener.onNewPlayerJoined(members);
                    }

                } else if (Event.Game.MEMBER_READY.equals(action)) {

                    UUID uuid = (UUID) btBundle.get(UUID.class.getName());
                    boolean isReady = (boolean) btBundle.get(Boolean.class.getName());

                    for (RoomPlayer player : mPlayers) {
                        if (player.getId().equals(uuid)) {
                            player.setReady(isReady);

                            if (mListener != null) {
                                mListener.onPlayerStateChange(player);
                            }
                        }
                    }

                }

            }
        }
    };

    // Game logic fields
    private ArrayList<RoomPlayer> mPlayers = new ArrayList<>();


    public GameLogic(LocalBroadcastManager broadcastManager) {
        mLocalBroadcastManager = broadcastManager;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Event.Game.MEMBER_JOINED);
        filter.addAction(Event.Game.MEMBER_READY);

        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    /**
     * @param listener: the Activity that listen the game state
     */
    public void setListener(@NonNull Listener listener) {
        mListener = listener;

        // TODO: push the actual state to the listener
        listener.onNewPlayerJoined(mPlayers);
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

    public boolean canStartMatch() {
        for (RoomPlayer player : mPlayers) {
            if (!player.isReady()) {
                return false;
            }
        }

        return true;
    }


}
