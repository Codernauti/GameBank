package com.codernauti.gamebank.game.sendTransaction;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.pairing.RoomPlayerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by Eduard on 13-Mar-18.
 */

public class SelectPlayerActivity extends AppCompatActivity {

    @BindView(R.id.select_player_list)
    ListView mPlayersList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_player);
        ButterKnife.bind(this);

        RoomPlayerAdapter mAdapter = new RoomPlayerAdapter(
                Realm.getDefaultInstance()
                        .where(Player.class)
                        .notEqualTo("mId", GameBank.BANK_UUID)
                        .findAll()
        );

        mPlayersList.setAdapter(mAdapter);
    }





}
