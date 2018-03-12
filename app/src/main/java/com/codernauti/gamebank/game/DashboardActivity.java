package com.codernauti.gamebank.game;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTClientService;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.util.Event;
import com.codernauti.gamebank.util.JoinService;
import com.codernauti.gamebank.util.SyncStateService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    @BindView(R.id.dashboard_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout tabLayout;
    @BindView(R.id.dashboard_toolbar)
    Toolbar myToolbar;

    private DashboardPagerAdapter mAdapter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Event.Network.HOST_DISCONNECTED.equals(action)) {

                Log.d(TAG, "onHostDisconnect");

                new AlertDialog.Builder(DashboardActivity.this)
                        .setTitle(R.string.host_disconnected_title)
                        .setMessage(R.string.host_disconnected_message)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                DashboardActivity.this.finish();
                            }
                        })
                        .create()
                        .show();
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);
        ButterKnife.bind(this);

        setSupportActionBar(myToolbar);

        mAdapter = new DashboardPagerAdapter(getSupportFragmentManager(),
                getApplicationContext());
        mViewPager.setAdapter(mAdapter);

        setupTopTabber();

        IntentFilter filter = new IntentFilter(Event.Network.HOST_DISCONNECTED);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mReceiver);

        stopService(new Intent(this, BTClientService.class));
        stopService(new Intent(this, BTHostService.class));
        stopService(new Intent(this, JoinService.class));
        stopService(new Intent(this, SyncStateService.class));
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exiting_match_title))
                .setMessage(getString(R.string.exiting_match_message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "User closed the match");
                        dialog.dismiss();

                        DashboardActivity.super.onBackPressed();

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "User aborted the match end");
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

    }

    private void setupTopTabber() {
        // Give the TabLayout the ViewPager
        tabLayout.setupWithViewPager(mViewPager);

        // Show HomePage first
        mViewPager.setCurrentItem(DashboardPagerAdapter.BANK_TAB);
    }
}
