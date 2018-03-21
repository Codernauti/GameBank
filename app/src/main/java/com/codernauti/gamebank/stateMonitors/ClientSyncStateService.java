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
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.util.SharePrefUtil;

import io.realm.Realm;

/**
 * Created by Eduard on 08-Mar-18.
 */

public class ClientSyncStateService extends Service {

    private static final String TAG = "ClientSyncStateService";

    private final BroadcastReceiver mFromBTClientConnection = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            final BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (BTEvent.CURRENT_STATE.equals(action)) {
                    Log.d(TAG, "(only client) Synchronize state with host");
                    updateDbWithHostState(btBundle);

                } else if (BTEvent.HOST_DISCONNECTED.equals(action)) {

                    ((GameBank) getApplication()).getRoomLogic().clearDatabase();
                }
            }

        }
    };

    private void updateDbWithHostState(final BTBundle btBundle) {

        String matchJson = (String) btBundle.get("MATCH");

        Log.d(TAG, "updateDbWithHostState() json: \n" + matchJson);
        final Match matchFromJson = GameBank.gsonConverter.fromJson(matchJson, Match.class);

        ((GameBank) getApplication()).getRoomLogic()
                .initRealmDatabase(this);

        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                realm.copyToRealm(matchFromJson);

                SharePrefUtil.saveCurrentMatchId(ClientSyncStateService.this, matchFromJson.getId());
            }
        });

        ((GameBank) getApplication()).getRoomLogic().syncState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BTEvent.CURRENT_STATE);
        filter.addAction(BTEvent.HOST_DISCONNECTED);

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
