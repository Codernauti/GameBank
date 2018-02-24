package com.codernauti.gamebank.lobby.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
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

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTClient;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateMatchActivity extends AppCompatActivity {

    private static final String TAG = "CreateMatchActivity";
    private static final String CONNECTION_NAME = "Game Bank";
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

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

        // FIXME: remove this fake entities
        mMemberAdapter.add(new BTClient("a", "b", true));
        mMemberAdapter.add(new BTClient("c", "d", false));
    }

    @OnClick(R.id.open_lobby)
    void onOpenMatch() {

        openLobbyButton.setEnabled(false);
        cancelMatchButton.setEnabled(true);
        startMatchButton.setVisibility(View.VISIBLE);

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        this.startActivity(discoverableIntent);
    }

    @OnClick(R.id.cancel_match)
    void onCancelMatch() {

        openLobbyButton.setEnabled(true);
        cancelMatchButton.setEnabled(false);
        startMatchButton.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.start_match)
    void onMatchStart() {

        cancelMatchButton.setVisibility(View.INVISIBLE);
        openLobbyButton.setVisibility(View.INVISIBLE);
        startingMatchProgressBar.setVisibility(View.VISIBLE);
        startMatchButton.setEnabled(false);
        startingMatchProgressBar.animate();

        Log.d(TAG, "Accepting connections");
        AcceptThread ac = new AcceptThread();
        ac.start();
        Log.d(TAG, "Thread launched");
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(CONNECTION_NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.

            Log.d(TAG, "In AccountThread run() function");

            boolean flag = true;
            while (flag) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    flag = false;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.

                    Log.d(TAG, "AAAAAAA CONNECTION ESTABLISHED");

                    // FIXME sending data, just for test
                    try {
                        // Important! Don't close the os otherwise data transfer will fail!
                        OutputStream os = socket.getOutputStream();

                        byte[] nameToByte = mLobbyName.getText().toString().getBytes();

                        Log.d(TAG, "Sending this message: " + mLobbyName.getText());

                        os.write(nameToByte);
                        os.write(Byte.MIN_VALUE);
                        Log.d(TAG, "DATA SENT");

                    } catch (IOException e) {
                        e.printStackTrace();

                        Log.e(TAG, "DATA NOT SENT");
                    }
                    // END FIXME

                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    flag = false;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
