package com.codernauti.gamebank;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.lobby.RoomPlayer;
import com.codernauti.gamebank.util.Event;

import java.util.ArrayList;
import java.util.HashMap;
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

    public interface ClientListener extends Listener {
        void onHostDisconnect();
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

                if (Event.Game.MEMBER_JOINED.equals(action)) {

                    RoomPlayer newPlayer = (RoomPlayer) btBundle.get(RoomPlayer.class.getName());
                    mPlayers.add(newPlayer);

                    if (mListener != null) {
                        mListener.onNewPlayerJoined(mPlayers);
                    }

                    if (mIamHost) {
                        Log.d(TAG, "(only host) Synchronize state with the new player.\n" +
                                "Send players: " + mPlayers.size());

                        // sync the new player (NB this break the layer separation
                        // because RoomLogic need to care about clients)
                        Intent stateIntent = BTBundle.makeIntentFrom(
                                new BTBundle(Event.Game.CURRENT_STATE)
                                        .append(mPlayers)
                        );
                        stateIntent.putExtra(BTHostService.RECEIVER_UUID, newPlayer.getId());
                        mLocalBroadcastManager.sendBroadcast(stateIntent);
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

                } else if (Event.Game.MEMBER_DISCONNECTED.equals(action)) {

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

                } else if (!mIamHost && Event.Game.CURRENT_STATE.equals(action)) {
                    // (NB this break the layer separation
                    // because RoomLogic need to care about host and client)
                    Log.d(TAG, "(only client) Synchronize state with host");

                    ArrayList<RoomPlayer> hostRoomPlayers = (ArrayList<RoomPlayer>)
                            btBundle.get(ArrayList.class.getName());

                    mPlayers.addAll(hostRoomPlayers);

                    if (mListener != null) {
                        mListener.onNewPlayerJoined(mPlayers);
                    }

                } else if (!mIamHost && Event.Game.HOST_DISCONNECTED.equals(action)) {

                    Log.d(TAG, "(only client) Disconnected from host");

                    if (mListener != null) {
                        ((ClientListener) mListener).onHostDisconnect();
                    }

                }

            }
        }
    };


    RoomLogic(LocalBroadcastManager broadcastManager, String hostNickname) {
        mNickname = hostNickname;
        mLocalBroadcastManager = broadcastManager;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Event.Game.MEMBER_JOINED);
        filter.addAction(Event.Game.MEMBER_READY);
        filter.addAction(Event.Game.MEMBER_DISCONNECTED);
        filter.addAction(Event.Game.CURRENT_STATE);
        filter.addAction(Event.Game.HOST_DISCONNECTED);

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

}
