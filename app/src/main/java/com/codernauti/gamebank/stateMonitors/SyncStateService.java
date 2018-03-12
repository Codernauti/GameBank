package com.codernauti.gamebank.stateMonitors;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.pairing.RoomPlayerProfile;

import java.util.ArrayList;

/**
 * Created by Eduard on 08-Mar-18.
 */

public class SyncStateService extends Service {

    private static final String TAG = "SyncStateService";

    private final BroadcastReceiver mFromBTClientConnection = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            final BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (BTEvent.CURRENT_STATE.equals(action)) {
                    Log.d(TAG, "(only client) Synchronize state with host");

                    final ArrayList<RoomPlayerProfile> hostRoomPlayerProfiles = (ArrayList<RoomPlayerProfile>)
                            btBundle.get(ArrayList.class.getName());
                    final String roomName = (String) btBundle.get(String.class.getName());

                    ((GameBank) getApplication()).getRoomLogic()
                            .syncState(hostRoomPlayerProfiles, roomName);

                }
            }

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BTEvent.CURRENT_STATE);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mFromBTClientConnection, filter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mFromBTClientConnection);

        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
