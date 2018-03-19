package com.codernauti.gamebank;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.game.DashboardActivity;
import com.codernauti.gamebank.util.SharePrefUtil;
import com.codernauti.gamebank.bluetooth.BTDataMetric;

import java.util.List;
import java.util.UUID;
import java.io.IOException;

/**
 * Created by davide on 01/03/18.
 */

public class GameBank extends Application {

    private static final String TAG = "GameBankApp";

    public static final UUID BT_ADDRESS = UUID.randomUUID();
    public static String FILES_DIR;
    public static BTDataMetric BT_METRIC;

    private RoomLogic mRoomLogic;
    private BankLogic mBankLogic;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            if (BTEvent.START.equals(action)) {

                List<UUID> membersUUID = mRoomLogic.getMembersUUID();
                mBankLogic = new BankLogic(
                        LocalBroadcastManager.getInstance(context),
                        membersUUID);

                Intent startGameAct = new Intent(context, DashboardActivity.class);
                startGameAct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startGameAct);
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter(BTEvent.START);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mReceiver, filter);

        Log.d(TAG, "Files directory: " + getFilesDir());
        FILES_DIR = getFilesDir().toString();

        try {
            BT_METRIC = new BTDataMetric(getFilesDir().toString());
        } catch (IOException e){
            Log.e(TAG, "Impossible to open BTDataMetric");
            e.printStackTrace();
        }
    }

    public void initRoomLogic() {
        String nickname = SharePrefUtil.getNicknamePreference(this);

        mRoomLogic = new RoomLogic(
                LocalBroadcastManager.getInstance(this), nickname);
    }

    public void cleanRoomLogic() {
        initRoomLogic();
    }

    public RoomLogic getRoomLogic() {
        return mRoomLogic;
    }

    public BankLogic getBankLogic() { return mBankLogic; }
}
