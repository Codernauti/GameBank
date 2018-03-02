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

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.lobby.RoomPlayer;
import com.codernauti.gamebank.util.Event;
import com.codernauti.gamebank.util.PlayerProfile;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTClient;
import com.codernauti.gamebank.bluetooth.BTHostService;

import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateMatchActivity extends AppCompatActivity {

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
    private BTClientAdapter mMemberAdapter;

    // FIXME: Game Logic field
    private ArrayList<RoomPlayer> mRoomPlayers = new ArrayList<>();

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            if (Event.Network.CONN_ESTABLISHED.equals(action)) {
                Bundle bundle = intent.getExtras();

                if (bundle != null) {

                    // Decode data from intent
                    RoomPlayer newPlayer = (RoomPlayer) bundle.get(RoomPlayer.class.getName());
                    Log.d(TAG, "Adding a new player into the list: " + newPlayer.getNickname());

                    // Update Game Logic
                    mRoomPlayers.add(newPlayer);

                    // Update UI
                    mMemberAdapter.add(newPlayer.getBTClient()); // TODO how about user pic?

                    // Synchronize clients
                    Intent newMemberIntent = new Intent(Event.Game.MEMBER_JOINED);
                    newMemberIntent.putExtra(mRoomPlayers.getClass().getName(),
                            mRoomPlayers);

                    LocalBroadcastManager.getInstance(CreateMatchActivity.this)
                            .sendBroadcast(newMemberIntent);
                }
            } else if (Event.Game.MEMBER_READY.equals(action)) {
                 // Decode
                    boolean isReady = (boolean) BTBundle.extractFrom(intent)
                            .getMapData().get(boolean.class.getName());
                    UUID uuid = (UUID) BTBundle.extractFrom(intent)
                            .getMapData().get(UUID.class.getName());

                    Intent i = new Intent(action);
                    i.putExtra(UUID.class.getName(), uuid);
                    i.putExtra(boolean.class.getName(), isReady);

                    LocalBroadcastManager.getInstance(CreateMatchActivity.this)
                            .sendBroadcast(i);
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

        mMemberAdapter = new BTClientAdapter(this);
        memberListJoined.setAdapter(mMemberAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(Event.Network.CONN_ESTABLISHED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @OnClick(R.id.open_lobby)
    void onOpenMatch() {

        openLobbyButton.setEnabled(false);
        cancelMatchButton.setEnabled(true);
        startMatchButton.setVisibility(View.VISIBLE);

        // Game logic
        mRoomPlayers.add(new RoomPlayer("HostName", UUID.randomUUID(), true));

        // Making the server discoverable for 5 minutes
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    @OnClick(R.id.cancel_match)
    void onCancelMatch() {

        openLobbyButton.setEnabled(true);
        cancelMatchButton.setEnabled(false);
        startMatchButton.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.start_match)
    void onMatchStart() {

        Log.d(TAG, "onMatchStart");

        cancelMatchButton.setVisibility(View.INVISIBLE);
        openLobbyButton.setVisibility(View.INVISIBLE);
        startingMatchProgressBar.setVisibility(View.VISIBLE);
        startMatchButton.setEnabled(false);
        startingMatchProgressBar.animate();

        Intent intent = new Intent(this, BTHostService.class);
        intent.putExtra(BTHostService.ACCEPTED_CONNECTIONS, membersNumber.getValue());
        startService(intent);
    }
}
