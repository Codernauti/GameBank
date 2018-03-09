package com.codernauti.gamebank;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.pairing.RoomPlayer;
import com.codernauti.gamebank.util.Event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Created by Eduard on 03-Mar-18.
 */

public final class RoomLogic {

    private static final String TAG = "RoomLogic";

    public interface Listener {
        void onPlayerChange(RoomPlayer player);
        void onPlayerRemove(RoomPlayer player);
        void onNewPlayerJoined(ArrayList<RoomPlayer> newPlayer);
    }

    private final LocalBroadcastManager mLocalBroadcastManager;
    private Listener mListener;

    // Game logic fields
    // Lobby fields
    private ArrayList<RoomPlayer> mPlayers = new ArrayList<>();
    private boolean mIamHost;
    private final String mNickname;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "Received action: " + action + "\n" +
                    Thread.currentThread().getName());

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (Event.Network.MEMBER_CONNECTED.equals(action)) {

                    RoomPlayer newPlayer = (RoomPlayer) btBundle.get(RoomPlayer.class.getName());
                    mPlayers.add(newPlayer);

                    if (mListener != null) {
                        mListener.onNewPlayerJoined(mPlayers);
                    }

                } else if (Event.Game.MEMBER_READY.equals(action)) {

                    UUID uuid = btBundle.getUuid();
                    boolean isReady = (boolean) btBundle.get(Boolean.class.getName());

                    for (RoomPlayer player : mPlayers) {
                        if (player.getId().equals(uuid)) {
                            player.setReady(isReady);

                            if (mListener != null) {
                                mListener.onPlayerChange(player);
                            }
                        }
                    }

                } else if (Event.Network.MEMBER_DISCONNECTED.equals(action)) {

                    UUID playerDisconnected = btBundle.getUuid();

                    Iterator<RoomPlayer> iterator = mPlayers.iterator();
                    while (iterator.hasNext()) {
                        RoomPlayer player = iterator.next();

                        if (player.getId().equals(playerDisconnected)) {
                            iterator.remove();

                            if (mListener != null) {
                                mListener.onPlayerRemove(player);
                            }
                        }
                    }

                }

            }
        }
    };


    RoomLogic(LocalBroadcastManager broadcastManager, String hostNickname) {
        mNickname = hostNickname;
        mLocalBroadcastManager = broadcastManager;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Event.Network.MEMBER_CONNECTED);
        filter.addAction(Event.Game.MEMBER_READY);
        filter.addAction(Event.Network.MEMBER_DISCONNECTED);

        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    /**
     * @param listener: the Activity that listen the game state
     */
    public void setListener(@NonNull Listener listener) {
        Log.d(TAG, "Set listener: " + listener.getClass());
        mListener = listener;

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

    public void setIamHost() {
        Log.d(TAG, "I am host!");
        mIamHost = true;
        mPlayers.add(new RoomPlayer(mNickname, GameBank.BT_ADDRESS, true));

        if (mListener != null) {
            mListener.onNewPlayerJoined(mPlayers);
        }
    }


    public boolean matchCanStart() {
        for (RoomPlayer player : mPlayers) {
            if (!player.isReady()) {
                return false;
            }
        }

        return true;
    }

    List<UUID> getMembersUUID() {
        ArrayList<UUID> result = new ArrayList<>();

        for (RoomPlayer player : mPlayers) {
            result.add(player.getId());
        }

        return result;
    }

    public ArrayList<RoomPlayer> getRoomPlayers() {
        return mPlayers;
    }

    public void syncState(ArrayList<RoomPlayer> hostRoomPlayers) {
        mPlayers.addAll(hostRoomPlayers);

        if (mListener != null) {
            mListener.onNewPlayerJoined(mPlayers);
        }
    }

}
