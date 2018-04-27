package com.codernauti.gamebank.game.sendTransaction;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.codernauti.gamebank.Event;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.MainActivity;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.game.DashboardActivity;
import com.codernauti.gamebank.pairing.RoomPlayerAdapter;
import com.codernauti.gamebank.util.SharePrefUtil;
import com.google.gson.Gson;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import io.realm.Realm;

/**
 * Created by Eduard on 13-Mar-18.
 */

public class SelectPlayerActivity extends AppCompatActivity {

    private static final String TAG = "SelectPlayerAct";

    public static final String TRANSACTION_VALUE_KEY = "transaction_value";

    @BindView(R.id.select_player_list)
    ListView mPlayersList;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BTEvent.HOST_DISCONNECTED.equals(action)) {

                Log.d(TAG, "onHostDisconnect");

                new AlertDialog.Builder(SelectPlayerActivity.this)
                        .setTitle(R.string.host_disconnected_title)
                        .setMessage(R.string.host_disconnected_message)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(false)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                returnToTheFirstCommonActivity();
                            }
                        })
                        .create()
                        .show();
            }

        }
    };


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

            IntentFilter filter = new IntentFilter(BTEvent.HOST_DISCONNECTED);
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(mReceiver, filter);

        } else {
            Toast.makeText(this, "No transaction value set.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No transaction value set, use createActivity static method for open this activity");
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mReceiver);
    }

    @OnItemClick(R.id.select_player_list)
    public void sendTransactionValueTo(int position) {
        Realm realm = Realm.getDefaultInstance();
        Gson converter = GameBank.gsonConverter;

        String fromPlayerId = GameBank.BT_ADDRESS.toString();
        String fromPlayerName = SharePrefUtil.getNicknamePreference(this);

        Player toPlayer = mAdapter.getItem(position);
        String toPlayerId = toPlayer.getPlayerId();
        String toPlayerName = toPlayer.getUsername();

        Log.d(TAG, "Send " + mTransactionValue + " to: " + toPlayerId);

        int matchId = SharePrefUtil.getCurrentMatchId(this);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                Math.abs(mTransactionValue),
                fromPlayerId,
                fromPlayerName,
                toPlayerId,
                toPlayerName,
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

        Intent resultIntent = new Intent();
        resultIntent.putExtra(TRANSACTION_VALUE_KEY, mTransactionValue);
        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }


    public static Intent createActivity(Context context, int transactionValue) {
        Intent result = new Intent(context, SelectPlayerActivity.class);
        result.putExtra(TRANSACTION_VALUE_KEY, transactionValue);

        return result;
    }

    private void returnToTheFirstCommonActivity() {
        // Returning to the first activity in common between the client and the host
        // Should we add an end match activity or something like that?
        Intent returnToLobby = new Intent(this, MainActivity.class);
        returnToLobby.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(returnToLobby);
    }

}
