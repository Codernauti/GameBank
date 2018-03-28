package com.codernauti.gamebank.pairing;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.RoomLogic;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTClientService;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.Event;
import com.codernauti.gamebank.stateMonitors.ClientSyncStateService;
import com.codernauti.gamebank.util.SharePrefUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class RoomActivity extends AppCompatActivity {

    private static final String TAG = "RoomActivity";

    public static final String HOST_SELECTED_KEY = "host_selected_key";

    @BindView(R.id.room_members)
    ListView mMembersList;

    @BindView(R.id.room_poke_fab)
    FloatingActionButton mPokeFab;

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

            } else if (Event.STATE_SYNCHRONIZED.equals(action)) {

                initAdapter();

                String matchName = Realm.getDefaultInstance()
                        .where(Match.class)
                        .equalTo("mId", SharePrefUtil.getCurrentMatchId(context))
                        .findFirst()
                        .getMatchName();

                mToolbar.setTitle(matchName);

                status.setVisibility(View.VISIBLE);
                mPokeFab.setVisibility(View.VISIBLE);
            }
        }
    };

    private void initAdapter() {
        mMembersAdapter = new RoomPlayerAdapter(
                Realm.getDefaultInstance().where(Player.class)
                        .notEqualTo("mId", GameBank.BANK_UUID)
                        .findAll()
        );
        mMembersList.setAdapter(mMembersAdapter);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BTEvent.HOST_DISCONNECTED);
        filter.addAction(BTEvent.CONN_ESTABLISHED);
        filter.addAction(BTEvent.CONN_ERRONEOUS);
        filter.addAction(Event.STATE_SYNCHRONIZED);
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            BluetoothDevice selectedHost = (BluetoothDevice) extras.get(HOST_SELECTED_KEY);

            Intent syncIntent = new Intent(this, ClientSyncStateService.class);
            startService(syncIntent);

            Intent intent = new Intent(this, BTClientService.class);
            intent.putExtra(BTClientService.HOST_DEVICE, selectedHost);
            startService(intent);

        } else {
            Log.d(TAG, "No host pass, cannot start services");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mReceiver);

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

}
