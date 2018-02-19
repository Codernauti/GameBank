package com.codernauti.gamebank.lobby;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTClient;
import com.codernauti.gamebank.bluetooth.BTDevice;
import com.codernauti.gamebank.bluetooth.BluetoothStateChange;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LobbyActivity extends AppCompatActivity {


    @BindView(R.id.device_list)
    ListView devicesList;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        BluetoothStateChange.enableBTIfDisabled(this);

        BTDevice prova = new BTClient("aa", "bb");
        BTDevice prova2 = new BTClient("cc", "dd");

        ArrayList<BTDevice> btDevices = new ArrayList<>();
        btDevices.add(prova);
        btDevices.add(prova2);

        devicesList.setAdapter(new BTDevicesAdapter(this, btDevices));
    }
}
