package com.codernauti.gamebank.lobby.server;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.GameLogic;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.game.DashboardActivity;
import com.codernauti.gamebank.lobby.RoomPlayer;
import com.codernauti.gamebank.lobby.RoomPlayerAdapter;
import com.codernauti.gamebank.util.Event;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTHostService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateMatchActivity extends AppCompatActivity implements GameLogic.Listener {

    private static final String TAG = "CreateMatchActivity";

    @BindView(R.id.toolbar)
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
    private GameLogic mGameLogic;
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

        mMembersAdapter = new RoomPlayerAdapter(this);
        memberListJoined.setAdapter(mMembersAdapter);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mGameLogic = ((GameBank)getApplication()).getGameLogic();
        mGameLogic.setListener(this);
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
        mGameLogic.removeListener();
    }

    @OnClick(R.id.open_lobby)
    void onOpenMatch() {

        openLobbyButton.setEnabled(false);
        cancelMatchButton.setEnabled(true);
        startMatchButton.setVisibility(View.VISIBLE);

        mGameLogic.setIamHost();

        // Making the server discoverable for 5 minutes
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        Intent intent = new Intent(this, BTHostService.class);
        intent.putExtra(BTHostService.ACCEPTED_CONNECTIONS, membersNumber.getValue());
        startService(intent);
    }

    @OnClick(R.id.cancel_match)
    void onCancelMatch() {

        openLobbyButton.setEnabled(true);
        cancelMatchButton.setEnabled(false);
        startMatchButton.setVisibility(View.INVISIBLE);

        // TODO: stop discoverability

        Intent intent = new Intent(this, BTHostService.class);
        stopService(intent);
    }

    @OnClick(R.id.start_match)
    void onMatchStart() {

        Log.d(TAG, "onMatchStart");

        if (mGameLogic.matchCanStart()) {

            cancelMatchButton.setVisibility(View.INVISIBLE);
            openLobbyButton.setVisibility(View.INVISIBLE);
            startingMatchProgressBar.setVisibility(View.VISIBLE);
            startMatchButton.setEnabled(false);
            startingMatchProgressBar.animate();

            Intent startGame = BTBundle.makeIntentFrom(
                    new BTBundle(Event.Game.START_GAME)
            );
            mLocalBroadcastManager.sendBroadcast(startGame);

            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);

        } else {
            Toast.makeText(this, "Not all players are ready", Toast.LENGTH_SHORT).show();
        }
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
