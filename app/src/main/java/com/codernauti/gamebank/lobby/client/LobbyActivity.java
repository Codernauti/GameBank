package com.codernauti.gamebank.lobby.client;

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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTClientConnection;
import com.codernauti.gamebank.bluetooth.BTStateChange;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.lobby.server.CreateMatchActivity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class LobbyActivity extends AppCompatActivity {


    private final static String BT_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";
    private final static String TAG = "LobbyActivity";
    private final static int REQUEST_ACCESS_COARSE_LOCATION = 1;

    @BindView(R.id.list)
    ListView devicesList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private BluetoothAdapter mBluetoothAdapter;
    private BTStateChange mBTStateChangeReceiver;
    private ArrayList<BluetoothDevice> mBTDevices;
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

                if (deviceName != null &&
                        !deviceName.equals("null") &&
                        !mBTDevices.contains(device)){
                    mBTDevices.add(device);
                    Log.d(TAG, device.getName() + " added in the list.");
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.d(TAG, "BT Discovery finished");
                swipeRefreshLayout.setRefreshing(false);
                devicesList.setAdapter(new BTHostAdapter(LobbyActivity.this, mBTDevices));
            }
        }
    };

    private final BroadcastReceiver mBluetoothTransmissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d(TAG, "Action received: " + action);

            if (BTClientConnection.EVENT_CONNECTION_ESTABLISHED.equals(action)) {
                Log.d(TAG, "Connection established");

            } else if (BTClientConnection.EVENT_INCOMING_DATA.equals(action)) {

                Bundle tmp = intent.getExtras();
                if (tmp != null) {

                    BTBundle b = (BTBundle) tmp.get(BTBundle.BT_IDENTIFIER);
                    Log.d(TAG, "Received: " + b.getAction());
                    Log.d(TAG, "Data received:");

                    for (Map.Entry<String, Serializable> bte : b.getMapData().entrySet()) {
                        Log.d(TAG, "Field: " + bte.getKey() + ". Data: " + bte.getValue());
                    }
                } else {

                    Log.d(TAG, "I received something but the bundle is empty");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        mBTStateChangeReceiver = new BTStateChange();
        mBTDevices = new ArrayList<>();
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        restartBTDiscovery();
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();


        // Registering BT state change
        IntentFilter btChangeStateFilter = new IntentFilter(BT_STATE_CHANGED);
        registerReceiver(mBTStateChangeReceiver, btChangeStateFilter);

        // Register for broadcasts when a device is discovered
        IntentFilter btActionFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBTDiscoveryReceiver, btActionFoundFilter);

        // Register for broadcasts when discovery has finished
        IntentFilter btEndDiscoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBTDiscoveryReceiver, btEndDiscoveryFilter);

        // Register for BT connection and data receiver
        IntentFilter incomingTransmissionFilter = new IntentFilter();
        incomingTransmissionFilter.addAction(BTClientConnection.EVENT_INCOMING_DATA);
        incomingTransmissionFilter.addAction(BTClientConnection.EVENT_CONNECTION_ESTABLISHED);
        incomingTransmissionFilter.addAction(BTClientConnection.EVENT_CONNECTION_ERRONEED);
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(mBluetoothTransmissionReceiver, incomingTransmissionFilter);

        requestPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mBTStateChangeReceiver);
        unregisterReceiver(mBTDiscoveryReceiver);
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(mBluetoothTransmissionReceiver);
    }


    @Override
    protected void onStart() {
        super.onStart();

        swipeRefreshLayout.setRefreshing(false);
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
        startActivity(intent);
    }

    @OnItemClick(R.id.list)
    void onItemClick(final int position) {

        mBluetoothAdapter.cancelDiscovery();
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothDevice selectedHost = mBTDevices.get(position);

                try (BTClientConnection cc =
                             new BTClientConnection(
                                     UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"),
                                     selectedHost)) {
                    cc.connect();
                    Log.d(TAG, "Preparing to read data");
                    Log.d(TAG, "Data read: " + new String(cc.readData()));

                } catch (IOException e) {

                    Log.e(TAG, "Something went wrong");
                    e.printStackTrace();
                }
            }
        }).start();*/

        BluetoothDevice selectedHost = mBTDevices.get(position);
        BTClientConnection btc = new BTClientConnection(
                UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"),
                selectedHost,
                LocalBroadcastManager.getInstance(this));

        try {
            btc.connedAndSubscribe();

        } catch (IOException e) {
            Log.e(TAG, "Something went wrong");

            e.printStackTrace();
        }

        Log.d(TAG, "Connection launched");
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

    private void restartBTDiscovery() {

        if (mBluetoothAdapter.isEnabled()) {

            // Start BT discovery
            if (mBluetoothAdapter.isDiscovering()) {
                Log.d(TAG, "Stopping previous BT discovery");
                swipeRefreshLayout.setRefreshing(false);
                mBluetoothAdapter.cancelDiscovery();
            }

            Log.d(TAG, "Starting BT discovery");
            mBTDevices.clear();
            devicesList.setAdapter(null);
            swipeRefreshLayout.setRefreshing(true);
            mBluetoothAdapter.startDiscovery();
        }
    }
}
