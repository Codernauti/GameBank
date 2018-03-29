package com.codernauti.gamebank.game.sendTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.codernauti.gamebank.Event;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.pairing.RoomPlayerAdapter;
import com.codernauti.gamebank.util.SharePrefUtil;
import com.google.gson.Gson;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Eduard on 13-Mar-18.
 */

public class SelectPlayerActivity extends AppCompatActivity {

    private static final String TAG = "SelectPlayerAct";

    private static final String TRANSACTION_VALUE_KEY = "transaction_value";

    @BindView(R.id.select_player_list)
    ListView mPlayersList;

    private RoomPlayerAdapter mAdapter;
    private int mTransactionValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_player);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.select_player_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.select_player_title);

        if (savedInstanceState == null) {
            savedInstanceState = getIntent().getExtras();
        }

        if (savedInstanceState != null) {
            mTransactionValue = savedInstanceState.getInt(TRANSACTION_VALUE_KEY);

            String title = getString(R.string.select_player_title)
                    + " " + mTransactionValue
                    + " " + getString(R.string.to);

            setTitle(title);


            mAdapter = new RoomPlayerAdapter(
                    Realm.getDefaultInstance()
                            .where(Player.class)
                            .notEqualTo("mId", GameBank.BANK_UUID)
                            .and()
                            .notEqualTo("mId", GameBank.BT_ADDRESS.toString())
                            .findAll()
            );

            mPlayersList.setAdapter(mAdapter);

        } else {
            Toast.makeText(this, "No transaction value set.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No transaction value set, use createActivity static method for open this activity");
            finish();
        }
    }


    @OnItemClick(R.id.select_player_list)
    public void sendTransactionValueTo(int position) {
        Realm realm = Realm.getDefaultInstance();
        Gson converter = GameBank.gsonConverter;

        String fromPlayerId = GameBank.BT_ADDRESS.toString();
        String toPlayerId = mAdapter.getItem(position).getPlayerId();

        Log.d(TAG, "Send " + mTransactionValue + " to: " + toPlayerId);

        int matchId = SharePrefUtil.getCurrentMatchId(this);

        Transaction transaction = new Transaction(
                (int)(Calendar.getInstance().getTimeInMillis()/1000L),
                Math.abs(mTransactionValue),
                fromPlayerId,
                toPlayerId,
                matchId
        );

        // Send Transaction to all
        String jsonToSend = converter.toJson(transaction);
        Log.d(TAG, "Sending this json object: \n" + jsonToSend);

        Intent transIntent = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.TRANSACTION)
                        .append(jsonToSend)
        );

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(transIntent);

        finish();
    }


    public static Intent createActivity(Context context, int transactionValue) {
        Intent result = new Intent(context, SelectPlayerActivity.class);
        result.putExtra(TRANSACTION_VALUE_KEY, transactionValue);

        return result;
    }





}
