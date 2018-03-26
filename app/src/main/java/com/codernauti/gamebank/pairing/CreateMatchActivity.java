package com.codernauti.gamebank.pairing;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.codernauti.gamebank.DatabaseMatchManager;
import com.codernauti.gamebank.Event;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.stateMonitors.HostJoinService;
import com.codernauti.gamebank.util.SharePrefUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class CreateMatchActivity extends AppCompatActivity {

    private static final String TAG = "CreateMatchActivity";

    public static final String LOAD_MATCH = "load_match";

    @BindView(R.id.create_match_toolbar)
    Toolbar toolbar;

    @BindView(R.id.open_lobby)
    Button openLobbyButton;

    @BindView(R.id.cancel_match)
    Button cancelMatchButton;

    @BindView(R.id.start_match)
    FloatingActionButton startMatchButton;

    @BindView(R.id.match_name)
    TextInputEditText mLobbyName;

    @BindView(R.id.match_init_budget_form)
    TextInputEditText mInitBudget;

    @BindView(R.id.member_list_joined)
    ListView memberListJoined;


    private BluetoothAdapter mBluetoothAdapter;
    private RoomPlayerAdapter mMembersAdapter;
    private LocalBroadcastManager mLocalBroadcastManager;

    private DatabaseMatchManager mDbManager;

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

        startMatchButton.setVisibility(View.INVISIBLE);
        cancelMatchButton.setEnabled(false);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mDbManager = new DatabaseMatchManager(getFilesDir());

        if (savedInstanceState == null) {
            savedInstanceState = getIntent().getExtras();
        }

        if (savedInstanceState != null && savedInstanceState.getBoolean(LOAD_MATCH)) {
            Log.d(TAG, "Load saved match");
            loadSavedMatch();
        }
    }

    private void loadSavedMatch() {

        makeDeviceDiscoverable();

        startBTServices();

        Realm realm = Realm.getDefaultInstance();
        // TODO: this assume that exist a single object Match inside database
        Match savedMatch = realm.where(Match.class).findFirst();

        if (savedMatch != null) {

            SharePrefUtil.saveCurrentMatchId(this, savedMatch.getId());

            // update UI
            mLobbyName.setText(savedMatch.getMatchName());
            mInitBudget.setText(String.valueOf(savedMatch.getInitBudget()));

            // TODO: apply some filter for player already into database
            updateUi(Realm.getDefaultInstance()
                    .where(Player.class)
                    .notEqualTo("mId", GameBank.BANK_UUID)
                    .findAll());
        } else {
            Log.e(TAG, "No match found into database!");
        }
    }

    private void makeDeviceDiscoverable() {
        // Making the server discoverable for 5 minutes
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    private void startBTServices() {
        Intent hostService = new Intent(this, BTHostService.class);
        hostService.putExtra(BTHostService.ACCEPTED_CONNECTIONS, 7);
        startService(hostService);

        Intent joinService = new Intent(this, HostJoinService.class);
        startService(joinService);
    }

    private void updateUi(RealmResults<Player> data) {
        mLobbyName.setEnabled(false);
        mInitBudget.setEnabled(false);
        openLobbyButton.setEnabled(false);
        cancelMatchButton.setEnabled(true);
        startMatchButton.setVisibility(View.VISIBLE);

        mMembersAdapter = new RoomPlayerAdapter(data);
        memberListJoined.setAdapter(mMembersAdapter);
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
        Log.d(TAG, "onDestroy");
        stopService(new Intent(this, BTHostService.class));
        super.onDestroy();

    }

    private void closeRoom() {

        Intent joinService = new Intent(this, HostJoinService.class);
        stopService(joinService);

        Intent intent = new Intent(this, BTHostService.class);
        stopService(intent);
    }



    @OnClick(R.id.open_lobby)
    void onOpenMatch() {
        Log.d(TAG, "Open Match");

        String roomName = mLobbyName.getText().toString();
        String initBudget = mInitBudget.getText().toString();

        if (roomName.isEmpty()) {

            Toast.makeText(this, "Room name cannot be empty", Toast.LENGTH_SHORT).show();

        } else if (initBudget.isEmpty()) {

            Toast.makeText(this, "Initial budget cannot be empty", Toast.LENGTH_SHORT).show();

        } else {

            makeDeviceDiscoverable();

            startBTServices();

            mDbManager.createMatchInstance(this,
                    roomName,
                    Integer.parseInt(initBudget)
            );

            updateUi(Realm.getDefaultInstance()
                    .where(Player.class)
                    .notEqualTo("mId", GameBank.BANK_UUID)
                    .findAll());
        }
    }

    @OnClick(R.id.cancel_match)
    void onCancelMatch() {

        mLobbyName.setEnabled(true);
        mInitBudget.setEnabled(true);
        openLobbyButton.setEnabled(true);
        cancelMatchButton.setEnabled(false);
        startMatchButton.setVisibility(View.INVISIBLE);

        closeRoom();

        // TODO: remove the database file
        //mRoomLogic.deleteDatabase();
        //mDataSource.deleteDatabase();
    }

    @OnClick(R.id.start_match)
    void onMatchStart() {

        Log.d(TAG, "onMatchStart");

        if (matchCanStart()) {

            Intent startGame = BTBundle.makeIntentFrom(
                   new BTBundle(BTEvent.START)
            );

            mLocalBroadcastManager.sendBroadcast(startGame);

        } else {
            Toast.makeText(this, "Not all players are ready", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean matchCanStart() {

        RealmResults<Player> playersNotReady = Realm.getDefaultInstance()
                .where(Player.class)
                .equalTo("mReady", false)
                .findAll();

        return playersNotReady.isEmpty();
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

    @Override
    public void onBackPressed() {
        showWarningDialog();
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
}
