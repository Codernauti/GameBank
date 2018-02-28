package com.codernauti.gamebank.lobby.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.lobby.server.CreateMatchActivity;
import com.codernauti.gamebank.util.Event;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class RoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_act);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.room_poke_btn)
    public void sendPoke() {
        Intent intent = new Intent(Event.Game.POKE);
        intent.putExtra(String.class.getName(), "GOOOO!!");

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }
}
