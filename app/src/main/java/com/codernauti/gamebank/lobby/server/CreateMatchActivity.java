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

import com.codernauti.gamebank.util.PlayerProfile;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTClient;
import com.codernauti.gamebank.bluetooth.BTHostConnection;
import com.codernauti.gamebank.bluetooth.BTHostService;

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

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Received action: " + intent.getAction());

            if (intent.getAction().equals(BTHostConnection.EVENT_INCOMING_CONNECTION_ESTABLISHED)) {

                Log.d(TAG, "A new client connected");
                Bundle bundle = intent.getExtras();
                if (bundle != null) {

                    // Decode data from intent
                    PlayerProfile pl = (PlayerProfile) bundle.get(PlayerProfile.class.getName());
                    Log.d(TAG, "Adding a new player into the list: " + pl.getNickname());

                    // Update UI
                    mMemberAdapter.add(new BTClient(pl.getNickname(), pl.getId().toString(), true));

                    // DEBUG
                    // send fake response to client
                    Intent fakeResponseToClient = new Intent(BTHostService.SEND_DATA);
                    fakeResponseToClient.putExtra("LOBBY_NAME",
                            mLobbyName.getText().toString());
                    fakeResponseToClient.putExtra("PLAYER_UUID", pl.getId());

                    LocalBroadcastManager.getInstance(CreateMatchActivity.this)
                            .sendBroadcast(fakeResponseToClient);
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

        mMemberAdapter = new BTClientAdapter(this);
        memberListJoined.setAdapter(mMemberAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(BTHostConnection.EVENT_INCOMING_CONNECTION_ESTABLISHED);
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
