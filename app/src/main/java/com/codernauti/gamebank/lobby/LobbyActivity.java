package com.codernauti.gamebank.lobby;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTHost;
import com.codernauti.gamebank.bluetooth.BTStateChange;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LobbyActivity extends AppCompatActivity {


    private final static String BT_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";
    private final static String TAG = "LobbyActivity";

    @BindView(R.id.device_list)
    ListView devicesList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private BluetoothAdapter mBluetoothAdapter;
    private BTStateChange mBTStateChangeReceiver;
    private ArrayList<BTHost> mBTDevices;
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

                BTHost newHost = new BTHost(
                        device.getName(),
                        device.getAddress(),
                        device.getBondState() == BluetoothDevice.BOND_BONDED);

                // This removes devices with "null" name and devices that are find multiple times
                if (!device.getName().equals("null") && !mBTDevices.contains(newHost)){
                    mBTDevices.add(newHost);
                    Log.d(TAG, device.getName() + " added in the list.");
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.d(TAG, "BT Discovery finished");
                devicesList.setAdapter(new BTHostAdapter(LobbyActivity.this, mBTDevices));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // TODO lobby creation
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        mBTStateChangeReceiver = new BTStateChange();
        mBTDevices = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Registering BT state change
        IntentFilter intentFilter = new IntentFilter(BT_STATE_CHANGED);
        registerReceiver(mBTStateChangeReceiver, intentFilter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBTDiscoveryReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBTDiscoveryReceiver, filter);

        // Start BT discovery
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        Log.d(TAG, "Starting BT discovery");
        mBluetoothAdapter.startDiscovery();
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

        BTStateChange.enableBTIfDisabled(this);
    }
}
