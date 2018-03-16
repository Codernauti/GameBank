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

public class CreateMatchActivity extends AppCompatActivity implements RoomLogic.Listener {

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

        mMembersAdapter = new RoomPlayerAdapter(this);
        memberListJoined.setAdapter(mMembersAdapter);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mRoomLogic = ((GameBank)getApplication()).getRoomLogic();
        mRoomLogic.setListener(this);
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
        mRoomLogic.removeListener();
    }

    @OnClick(R.id.open_lobby)
    void onOpenMatch() {


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

        createMatchInstance();

    }

    private void createMatchInstance() {

        // Create database associated with match
        RealmConfiguration myConfig = new RealmConfiguration.Builder()
                .name("Test.realm")
                .directory(new File(getFilesDir(), "matches"))
                .build();

        Realm.setDefaultConfiguration(myConfig);
        Realm db = Realm.getDefaultInstance();


        // insert initial data
        db.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // Get the current max id in the EntityName table
                //Number id = realm.where(Match.class).max("mId");
                // If id is null, set it to 1, else set increment it by 1
                int matchId = 42;//(id == null) ? 1 : id.intValue() + 1;
                final Match newMatch = realm.createObject(Match.class, matchId);

                SharePrefUtil.saveCurrentMatchId(CreateMatchActivity.this, matchId);

                //((GameBank)getApplication()).getBankLogic().setMatchId(matchId);
                // Set match nickname
                newMatch.setMatchName(mLobbyName.getText().toString());

                // Set game date
                Calendar now = Calendar.getInstance();
                newMatch.setMatchStarted(
                        now.get(Calendar.DATE) + "/" + now.get(Calendar.MONTH) + "/" + now.get(Calendar.YEAR)
                );

                newMatch.setPlayerList(new RealmList<Player>());
                newMatch.setTransactionList(new RealmList<Transaction>());


                Player bank = realm.createObject(Player.class, GameBank.BANK_UUID);
                bank.setUsername("Bank");
                bank.setPhotoName("aaa");
                bank.setReady(true);

                Player myself = realm.createObject(Player.class, GameBank.BT_ADDRESS.toString());
                myself.setUsername(SharePrefUtil.getNicknamePreference(CreateMatchActivity.this));
                myself.setPhotoName(SharePrefUtil.getProfilePicturePreference(CreateMatchActivity.this));
                myself.setReady(true);

                newMatch.getPlayerList().add(bank);
                newMatch.getPlayerList().add(myself);
            }
        });
    }

    @OnClick(R.id.cancel_match)
    void onCancelMatch() {

        mLobbyName.setEnabled(true);
        openLobbyButton.setEnabled(true);
        cancelMatchButton.setEnabled(false);
        startMatchButton.setVisibility(View.INVISIBLE);

        closeRoom();
    }

    private void closeRoom() {
        // TODO: stop discoverability

        Log.d(TAG, "onCloseRoom()");

        ((GameBank)getApplication()).cleanRoomLogic();

        Intent joinService = new Intent(this, HostJoinService.class);
        stopService(joinService);

        Intent intent = new Intent(this, BTHostService.class);
        stopService(intent);

        Realm.getDefaultInstance().refresh();
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

    // RoomLogic callbacks

    @Override
    public void onNewPlayerJoined(List<Player> members) {
        mMembersAdapter.clear();
        mMembersAdapter.addAll(members);
        Log.d(TAG, "Update all " + members.size() + " player.");
    }

    @Override
    public void onRoomNameChange(String roomName) {
        // Empty, nothing to do!
    }

    @Override
    public void onPlayerChange(Player player) {
        mMembersAdapter.notifyDataSetChanged();//updatePlayerState(player);
        Log.d(TAG, "Update ui of: " + player.getPlayerId() + "\nisReady? " + player.getPlayerId());
    }

    @Override
    public void onPlayerRemove(Player player) {
        mMembersAdapter.removePlayer(player.getPlayerId());
        Log.d(TAG, "Remove player: " + player.getPlayerId());
    }
}
