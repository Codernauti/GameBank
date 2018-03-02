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

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTBundle;
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

public class RoomActivity extends AppCompatActivity {

    private static final String TAG = "RoomActivity";

    @BindView(R.id.room_members)
    ListView mMembersList;
    @BindView(R.id.member_set_status)
    FloatingActionButton status;


    private RoomPlayerAdapter mMembersAdapter;
    private boolean isReady;

    private BroadcastReceiver mUpdateUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (Event.Game.MEMBER_JOINED.equals(action)) {

                    ArrayList<RoomPlayer> members = (ArrayList<RoomPlayer>)
                            btBundle.get(ArrayList.class.getName());

                    mMembersAdapter.clear();
                    mMembersAdapter.addAll(members);

                } else if (Event.Game.MEMBER_READY.equals(action)) {

                    UUID uuid = (UUID) btBundle.get(UUID.class.getName());
                    boolean isReady = (boolean) btBundle.get(Boolean.class.getName());

                    // Update Ui
                    mMembersAdapter.updatePlayerState(uuid, isReady);

                    Log.d(TAG, "Update ui of: " + uuid + "\nisReady? " + isReady);
                }
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
        isReady = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filters = new IntentFilter(Event.Game.MEMBER_JOINED);
        filters.addAction(Event.Game.MEMBER_READY);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mUpdateUiReceiver, filters);
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mUpdateUiReceiver);
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
}
