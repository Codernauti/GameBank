package com.codernauti.gamebank.pairing.server;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.RoomLogic;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.pairing.RoomPlayerAdapter;
import com.codernauti.gamebank.Event;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.stateMonitors.HostJoinService;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;

public class CreateMatchActivity extends AppCompatActivity {

    private static final String TAG = "CreateMatchActivity";

    @BindView(R.id.create_match_toolbar)
    Toolbar toolbar;

    @BindView(R.id.members_number)
    NumberPicker membersNumber;

    @BindView(R.id.open_lobby)
    Button openLobbyButton;

    @BindView(R.id.start_match)
    Button startMatchButton;

    @BindView(R.id.cancel_match)
    Button cancelMatchButton;

    @BindView(R.id.hot_join_allowed)
    CheckBox hotJoinCheckbox;

    @BindView(R.id.starting_match)
    ProgressBar startingMatchProgressBar;

    @BindView(R.id.member_list_joined)
    ListView memberListJoined;

    @BindView(R.id.name)
    EditText mLobbyName;


    private BluetoothAdapter mBluetoothAdapter;
    private RoomPlayerAdapter mMembersAdapter;
    private RoomLogic mRoomLogic;
    private LocalBroadcastManager mLocalBroadcastManager;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (Event.Game.POKE.equals(action)) {
                    String msg = (String) btBundle.get(String.class.getName());
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_match);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        membersNumber.setMinValue(1);
        membersNumber.setMaxValue(7);

        startMatchButton.setVisibility(View.INVISIBLE);
        startingMatchProgressBar.setVisibility(View.INVISIBLE);
        cancelMatchButton.setEnabled(false);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mRoomLogic = ((GameBank)getApplication()).getRoomLogic();
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(Event.Game.POKE);
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mLocalBroadcastManager.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeRoom();
    }

    private void closeRoom() {
        // TODO: stop discoverability

        Log.d(TAG, "onCloseRoom()");

        Intent joinService = new Intent(this, HostJoinService.class);
        stopService(joinService);

        Intent intent = new Intent(this, BTHostService.class);
        stopService(intent);

        mRoomLogic.clearDatabase();
    }


    @OnClick(R.id.open_lobby)
    void onOpenMatch() {
        Log.d(TAG, "Open Match");

        mRoomLogic.setRoomName(mLobbyName.getText().toString());

        mLobbyName.setEnabled(false);
        openLobbyButton.setEnabled(false);
        cancelMatchButton.setEnabled(true);
        startMatchButton.setVisibility(View.VISIBLE);

        // Making the server discoverable for 5 minutes
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        Intent hostService = new Intent(this, BTHostService.class);
        hostService.putExtra(BTHostService.ACCEPTED_CONNECTIONS, 7);
        startService(hostService);

        Intent joinService = new Intent(this, HostJoinService.class);
        startService(joinService);

        mRoomLogic.createMatchInstance(this, mLobbyName.getText().toString());

        mMembersAdapter = new RoomPlayerAdapter(
                Realm.getDefaultInstance().where(Player.class)
                        .notEqualTo("mId", GameBank.BANK_UUID)
                        .findAll()
        );
        memberListJoined.setAdapter(mMembersAdapter);
    }

    @OnClick(R.id.cancel_match)
    void onCancelMatch() {

        mLobbyName.setEnabled(true);
        openLobbyButton.setEnabled(true);
        cancelMatchButton.setEnabled(false);
        startMatchButton.setVisibility(View.INVISIBLE);

        closeRoom();
    }

    @OnClick(R.id.start_match)
    void onMatchStart() {

        Log.d(TAG, "onMatchStart");

        if (mRoomLogic.matchCanStart()) {

            cancelMatchButton.setVisibility(View.INVISIBLE);
            openLobbyButton.setVisibility(View.INVISIBLE);
            startingMatchProgressBar.setVisibility(View.VISIBLE);
            startMatchButton.setEnabled(false);
            startingMatchProgressBar.animate();

            Intent startGame = BTBundle.makeIntentFrom(
                   new BTBundle(BTEvent.START)
            );

            mLocalBroadcastManager.sendBroadcast(startGame);

        } else {
            Toast.makeText(this, "Not all players are ready", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                showWarningDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showWarningDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage("Are you sure to go back and close the room?")
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeRoom();
                        CreateMatchActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        showWarningDialog();
    }

}
