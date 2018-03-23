package com.codernauti.gamebank.lobby;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTStateChange;
import com.codernauti.gamebank.pairing.RoomActivity;
import com.codernauti.gamebank.pairing.CreateMatchActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class LobbyActivity extends AppCompatActivity {

    private final static String TAG = "LobbyActivity";

    private final static String BT_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";
    private final static int REQUEST_ACCESS_COARSE_LOCATION = 1;

    @BindView(R.id.list)
    ListView devicesList;

    @BindView(R.id.lobby_toolbar)
    Toolbar toolbar;

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private BluetoothAdapter mBluetoothAdapter;
    private BTStateChange mBTStateChangeReceiver;

    private BTHostAdapter mAdapter;
    private AlertDialog mPermissionDialog;

    private final BroadcastReceiver mBTDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Found: "
                        + device.getName()
                        + " with address "
                        + device.getAddress()
                        + " bonded? "
                        + (device.getBondState() == BluetoothDevice.BOND_BONDED));

                // This removes devices with "null" name and devices that are find multiple times

                String deviceName = device.getName();

                /* FIXME @ed revert to base adapter and check if the bt devices has already been
                 * discovered
                 */
                if (deviceName != null && !deviceName.equals("null")){
                    mAdapter.add(device);

                    Log.d(TAG, device.getName() + " added in the list.");
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.d(TAG, "BT Discovery finished");
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        mBTStateChangeReceiver = new BTStateChange();

        mAdapter = new BTHostAdapter(this);
        devicesList.setAdapter(mAdapter);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        restartBTDiscovery();
                    }
                }
        );
    }

    private void restartBTDiscovery() {

        if (mBluetoothAdapter.isEnabled()) {

            // Start BT discovery
            if (mBluetoothAdapter.isDiscovering()) {
                Log.d(TAG, "Stopping previous BT discovery");
                swipeRefreshLayout.setRefreshing(false);
                mBluetoothAdapter.cancelDiscovery();
            }

            Log.d(TAG, "Starting BT discovery");
            mAdapter.clear();

            swipeRefreshLayout.setRefreshing(true);
            mBluetoothAdapter.startDiscovery();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mBTStateChangeReceiver);
        unregisterReceiver(mBTDiscoveryReceiver);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Registering BT state change
        IntentFilter btChangeStateFilter = new IntentFilter(BT_STATE_CHANGED);

        // Register for broadcasts when a device is discovered
        IntentFilter btActionFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        btActionFoundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mBTDiscoveryReceiver, btActionFoundFilter);
        registerReceiver(mBTStateChangeReceiver, btChangeStateFilter);

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermission();
    }

    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String[] permissions = {
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "Permission not granted, asking for it");

                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        REQUEST_ACCESS_COARSE_LOCATION);
            } else {

                Log.d(TAG, "Permission granted, going forward");

                BTStateChange.enableBTIfDisabled(this);
                restartBTDiscovery();
            }
        } else {
            BTStateChange.enableBTIfDisabled(this);
            restartBTDiscovery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           final String permissions[],
                                           final int[] grantResults) {

        Log.d(TAG, "onRequestPermissionResult");

        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {

            if (mPermissionDialog != null) {
                mPermissionDialog.hide();
                mPermissionDialog.dismiss();
                mPermissionDialog = null;
            }

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                BTStateChange.enableBTIfDisabled(this);
                restartBTDiscovery();

            } else {
                mPermissionDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_coarse_permission_title)
                        .setMessage(R.string.dialog_coarse_permission_description)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                mPermissionDialog.show();
            }
        }
    }

    @OnClick(R.id.fab)
    void onClickFab() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        Intent intent = new Intent(this, CreateMatchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @OnItemClick(R.id.list)
    void onItemClick(final int position) {
        Log.d(TAG, "Start CLIENT connection");

        mBluetoothAdapter.cancelDiscovery();
        BluetoothDevice selectedHost = mAdapter.getItem(position);

        Intent startLobby = new Intent(this, RoomActivity.class);
        startLobby.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startLobby.putExtra(RoomActivity.HOST_SELECTED_KEY, selectedHost);
        startActivity(startLobby);
    }
}
