package com.codernauti.gamebank.pairing.client;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.RoomLogic;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.pairing.RoomPlayer;
import com.codernauti.gamebank.pairing.RoomPlayerAdapter;
import com.codernauti.gamebank.util.Event;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class RoomActivity extends AppCompatActivity implements RoomLogic.ClientListener {

    private static final String TAG = "RoomActivity";

    @BindView(R.id.room_members)
    ListView mMembersList;
    @BindView(R.id.member_set_status)
    FloatingActionButton status;


    private RoomPlayerAdapter mMembersAdapter;
    private boolean isReady = false;
    private LocalBroadcastManager mLocalBroadcastManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        ButterKnife.bind(this);

        mMembersAdapter = new RoomPlayerAdapter(this);
        mMembersList.setAdapter(mMembersAdapter);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        ((GameBank)getApplication()).getRoomLogic().setListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((GameBank)getApplication()).getRoomLogic().removeListener();
    }

    @OnClick(R.id.room_poke_fab)
    public void sendPoke() {
        Intent intent = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.POKE).append("GOOO!")
        );

        mLocalBroadcastManager.sendBroadcast(intent);

        // Snackbar.make(, R.string.poke_action, Snackbar.LENGTH_SHORT);
    }

    @OnClick(R.id.member_set_status)
    public void toggleReadiness() {
        isReady = !isReady;

        int icon = isReady ? R.drawable.ic_close_white_24dp : R.drawable.ic_add_white_24dp;

        Intent intent = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.MEMBER_READY).append(isReady)
        );

        mLocalBroadcastManager.sendBroadcast(intent);

        status.setImageResource(icon);
    }


    // RoomLogic callbacks

    @Override
    public void onNewPlayerJoined(ArrayList<RoomPlayer> members) {
        mMembersAdapter.clear();
        mMembersAdapter.addAll(members);
        Log.d(TAG, "Update all " + members.size() + " players.");
    }

    @Override
    public void onHostDisconnect() {
        Log.d(TAG, "onHostDisconnect");

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("AB")
                .setMessage("GNEHU")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        RoomActivity.this.finish();
                    }
                })
                .create();

        alertDialog.show();
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
