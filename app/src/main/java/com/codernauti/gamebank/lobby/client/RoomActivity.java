package com.codernauti.gamebank.lobby.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTClient;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.lobby.server.CreateMatchActivity;
import com.codernauti.gamebank.util.Event;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class RoomActivity extends AppCompatActivity {

    private static final String TAG = "RoomActivity";

    private BroadcastReceiver mUpdateUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Event.Game.MEMBER_JOINED.equals(action)) {
                BTBundle btBundle = BTBundle.extract(intent);

                ArrayList<BTClient> clients = (ArrayList<BTClient>) btBundle.getMapData().get(ArrayList.class.getName());

                for (BTClient client : clients) {
                    Log.d(TAG, client.getName());
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_act);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filters = new IntentFilter(Event.Game.MEMBER_JOINED);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mUpdateUiReceiver, filters);
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mUpdateUiReceiver);
    }

    @OnClick(R.id.room_poke_btn)
    public void sendPoke() {
        Intent intent = new Intent(Event.Game.POKE);
        intent.putExtra(String.class.getName(), "GOOOO!!");

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }
}
