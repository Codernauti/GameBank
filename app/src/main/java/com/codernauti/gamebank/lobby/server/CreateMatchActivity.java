package com.codernauti.gamebank.lobby.server;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTClient;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateMatchActivity extends AppCompatActivity {

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

    private BTClientAdapter mBTClientAdapter;
    private ArrayList<BTClient> mBTDevices;

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

        mBTDevices = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mBTDevices.add(new BTClient("a", "b", true));
        mBTDevices.add(new BTClient("c", "d", false));

        memberListJoined.setAdapter(new BTClientAdapter(this, mBTDevices));
    }

    @OnClick(R.id.open_lobby)
    void onStartMatch() {

        openLobbyButton.setEnabled(false);
        cancelMatchButton.setEnabled(true);
        startMatchButton.setVisibility(View.VISIBLE);
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
    }
}
