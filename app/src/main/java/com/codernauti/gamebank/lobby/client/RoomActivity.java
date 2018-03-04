package com.codernauti.gamebank.lobby.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.GameLogic;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.game.DashboardActivity;
import com.codernauti.gamebank.lobby.RoomPlayer;
import com.codernauti.gamebank.lobby.RoomPlayerAdapter;
import com.codernauti.gamebank.util.Event;

import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class RoomActivity extends AppCompatActivity implements GameLogic.Listener {

    private static final String TAG = "RoomActivity";

    @BindView(R.id.room_members)
    ListView mMembersList;
    @BindView(R.id.member_set_status)
    FloatingActionButton status;


    private RoomPlayerAdapter mMembersAdapter;
    private boolean isReady = false;
    private LocalBroadcastManager mLocalBroadcastManager;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            if (Event.Game.START_GAME.equals(action)) {
                Intent startGame = new Intent(context, DashboardActivity.class);
                startActivity(startGame);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        ButterKnife.bind(this);

        mMembersAdapter = new RoomPlayerAdapter(this);
        mMembersList.setAdapter(mMembersAdapter);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        IntentFilter filter = new IntentFilter(Event.Game.START_GAME);
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);

        ((GameBank)getApplication()).getGameLogic().setListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mLocalBroadcastManager.unregisterReceiver(mReceiver);

        ((GameBank)getApplication()).getGameLogic().removeListener();
    }

    @OnClick(R.id.room_poke_fab)
    public void sendPoke() {
        Intent intent = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.POKE).append("GOOO!")
        );

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);

        // Snackbar.make(, R.string.poke_action, Snackbar.LENGTH_SHORT);
    }

    @OnClick(R.id.member_set_status)
    public void toggleReadiness() {
        isReady = !isReady;

        int icon = isReady ? R.drawable.ic_close_white_24dp : R.drawable.ic_add_white_24dp;

        Intent intent = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.MEMBER_READY).append(isReady)
        );

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);

        status.setImageResource(icon);
    }


    // GameLogic callbacks

    @Override
    public void onNewPlayerJoined(ArrayList<RoomPlayer> members) {
        mMembersAdapter.clear();
        mMembersAdapter.addAll(members);
        Log.d(TAG, "Update all " + members.size() + " players.");
    }

    @Override
    public void onPlayerChange(RoomPlayer player) {
        mMembersAdapter.updatePlayerState(player);
        Log.d(TAG, "Update ui of: " + player.getId() + "\nisReady? " + player.getId());
    }

    @Override
    public void onPlayerRemove(RoomPlayer player) {
        mMembersAdapter.removePlayer(player.getId());
        Log.d(TAG, "Remove player: " + player.getId());
    }
}
