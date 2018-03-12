package com.codernauti.gamebank;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.pairing.RoomPlayerProfile;
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
        void onPlayerChange(RoomPlayerProfile player);
        void onPlayerRemove(RoomPlayerProfile player);
        void onNewPlayerJoined(ArrayList<RoomPlayerProfile> newPlayer);
    }

    private final LocalBroadcastManager mLocalBroadcastManager;
    private Listener mListener;

    // Game logic fields
    // Lobby fields
    private ArrayList<RoomPlayerProfile> mPlayers = new ArrayList<>();
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

                    RoomPlayerProfile newPlayer = (RoomPlayerProfile) btBundle.get(RoomPlayerProfile.class.getName());
                    mPlayers.add(newPlayer);

                    if (mListener != null) {
                        mListener.onNewPlayerJoined(mPlayers);
                    }

                } else if (Event.Game.MEMBER_READY.equals(action)) {

                    UUID uuid = btBundle.getUuid();
                    boolean isReady = (boolean) btBundle.get(Boolean.class.getName());

                    for (RoomPlayerProfile player : mPlayers) {
                        if (player.getId().equals(uuid)) {
                            player.setReady(isReady);

                            if (mListener != null) {
                                mListener.onPlayerChange(player);
                            }
                        }
                    }

                } else if (Event.Network.MEMBER_DISCONNECTED.equals(action)) {

                    UUID playerDisconnected = btBundle.getUuid();

                    Iterator<RoomPlayerProfile> iterator = mPlayers.iterator();
                    while (iterator.hasNext()) {
                        RoomPlayerProfile player = iterator.next();

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

    public void setIamHost(String pictureName) {
        Log.d(TAG, "I am host!");
        mPlayers.add(new RoomPlayerProfile(mNickname, GameBank.BT_ADDRESS, pictureName, true));

        if (mListener != null) {
            mListener.onNewPlayerJoined(mPlayers);
        }
    }


    public boolean matchCanStart() {
        for (RoomPlayerProfile player : mPlayers) {
            if (!player.isReady()) {
                return false;
            }
        }

        return true;
    }

    List<UUID> getMembersUUID() {
        ArrayList<UUID> result = new ArrayList<>();

        for (RoomPlayerProfile player : mPlayers) {
            result.add(player.getId());
        }

        return result;
    }

    public ArrayList<RoomPlayerProfile> getRoomPlayers() {
        return mPlayers;
    }

    public void syncState(ArrayList<RoomPlayerProfile> hostRoomPlayerProfiles) {
        mPlayers.addAll(hostRoomPlayerProfiles);

        if (mListener != null) {
            mListener.onNewPlayerJoined(mPlayers);
        }
    }

    public void clear() {
        mPlayers.clear();

        if (mListener != null) {
            mListener.onNewPlayerJoined(mPlayers);
        }
    }

}
