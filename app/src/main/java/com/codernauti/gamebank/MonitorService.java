package com.codernauti.gamebank;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTEvent;

/**
 * Created by dpolonio on 26/03/18.
 */

public class MonitorService extends Service {

    private static final String TAG = "MonitorService";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "RECEIVED: " + intent.getAction());
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Event.MEMBER_RECONNECTED);
        filter.addAction(Event.STATE_SYNCHRONIZED);
        filter.addAction(Event.Game.LEADERBOARD);
        filter.addAction(Event.Game.LIST_TRANSACTIONS);
        filter.addAction(Event.Game.MEMBER_READY);
        filter.addAction(Event.Game.POKE);
        filter.addAction(Event.Game.TRANSACTION);
        filter.addAction(BTEvent.CONN_ERRONEOUS);
        filter.addAction(BTEvent.CONN_ESTABLISHED);
        filter.addAction(BTEvent.CURRENT_STATE);
        filter.addAction(BTEvent.HOST_DISCONNECTED);
        filter.addAction(BTEvent.MEMBER_CONNECTED);
        filter.addAction(BTEvent.MEMBER_DISCONNECTED);
        filter.addAction(BTEvent.SEND_DATA_ERROR);
        filter.addAction(BTEvent.START);
        filter.addAction(BTEvent.STOP);


        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver, filter);

        return super.onStartCommand(intent, flags, startId);
    }
}
