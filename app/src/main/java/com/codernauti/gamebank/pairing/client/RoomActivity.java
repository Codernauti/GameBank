package com.codernauti.gamebank.pairing.client;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.RoomLogic;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTClientService;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.pairing.RoomPlayerAdapter;
import com.codernauti.gamebank.Event;
import com.codernauti.gamebank.stateMonitors.ClientSyncStateService;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class RoomActivity extends AppCompatActivity implements RoomLogic.Listener {

    private static final String TAG = "RoomActivity";

    public static final String HOST_SELECTED_KEY = "host_selected_key";

    @BindView(R.id.room_members)
    ListView mMembersList;
    @BindView(R.id.member_set_status)
    FloatingActionButton status;
    @BindView(R.id.room_activity_toolbar)
    Toolbar mToolbar;


    private RoomPlayerAdapter mMembersAdapter;
    private boolean isReady = false;
    private LocalBroadcastManager mLocalBroadcastManager;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            if (BTEvent.HOST_DISCONNECTED.equals(action)) {
                Log.d(TAG, "onHostDisconnect");

                AlertDialog alertDialog = new AlertDialog.Builder(RoomActivity.this)
                        .setTitle(R.string.host_disconnected_title)
                        .setMessage(R.string.host_disconnected_message)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                closeServices();
                                RoomActivity.this.finish();
                            }
                        })
                        .create();

                alertDialog.show();
            } else if (BTEvent.CONN_ESTABLISHED.equals(action)) {

                Toast.makeText(context, "Connect successfully",
                        Toast.LENGTH_SHORT).show();

            } else if (BTEvent.CONN_ERRONEOUS.equals(action)) {

                Toast.makeText(context, "Impossible to connect to this device",
                        Toast.LENGTH_SHORT).show();
                closeServices();
                finish();
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mMembersAdapter = new RoomPlayerAdapter(this);
        mMembersList.setAdapter(mMembersAdapter);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BTEvent.HOST_DISCONNECTED);
        filter.addAction(BTEvent.CONN_ESTABLISHED);
        filter.addAction(BTEvent.CONN_ERRONEOUS);
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);

        ((GameBank)getApplication()).getRoomLogic().setListener(this);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            BluetoothDevice selectedHost = (BluetoothDevice) extras.get(HOST_SELECTED_KEY);

            Intent syncIntent = new Intent(this, ClientSyncStateService.class);
            startService(syncIntent);

            Intent intent = new Intent(this, BTClientService.class);
            intent.putExtra(BTClientService.HOST_DEVICE, selectedHost);
            startService(intent);

            // Create database associated with match
            RealmConfiguration myConfig = new RealmConfiguration.Builder()
                    .name("Test.realm") // TODO which name set in client device?
                    .directory(new File(getFilesDir(), "matches"))
                    .build();

            Realm.setDefaultConfiguration(myConfig);

        } else {
            Log.d(TAG, "No host pass, cannot start services");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
        ((GameBank)getApplication()).getRoomLogic().removeListener();
        mMembersAdapter.clear();

        closeServices();
    }

    @Override
    public void onBackPressed() {
        closeServices();
        super.onBackPressed();
    }

    private void closeServices() {
        Intent syncServiceIntent = new Intent(this, ClientSyncStateService.class);
        stopService(syncServiceIntent);

        Intent clientServiceIntent = new Intent(this, BTClientService.class);
        stopService(clientServiceIntent);

        ((GameBank)getApplication()).getRoomLogic().clearDatabase();
    }

    @OnClick(R.id.room_poke_fab)
    public void sendPoke() {
        Intent intent = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.POKE).append("GOOO!")
        );

        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @OnClick(R.id.member_set_status)
    public void toggleReadiness() {
        isReady = !isReady;

        int icon = isReady ? R.drawable.ic_close_white_24dp : R.drawable.ic_check_white_24dp;

        Intent intent = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.MEMBER_READY).append(isReady)
        );

        mLocalBroadcastManager.sendBroadcast(intent);

        status.setImageResource(icon);
    }


    // RoomLogic callbacks

    @Override
    public void onNewPlayerJoined(List<Player> members) {
        mMembersAdapter.clear();
        mMembersAdapter.addAll(members);
        Log.d(TAG, "Update all " + members.size() + " player.");
    }

    @Override
    public void onRoomNameChange(@NonNull  final String roomName) {
        Log.d(TAG, "Room name: " + roomName);
        mToolbar.setTitle(getString(R.string.match) + ": " + roomName);
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
