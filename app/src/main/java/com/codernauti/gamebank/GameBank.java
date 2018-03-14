package com.codernauti.gamebank;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.game.DashboardActivity;
import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by davide on 01/03/18.
 */

public class GameBank extends Application {

    private static final String TAG = "GameBankApp";

    public static UUID BT_ADDRESS;
    public static String FILES_DIR;

    private Gson gsonConverter;

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
                        LocalBroadcastManager.getInstance(context));

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

        // Initialize realm
        Realm.init(this);

        final String bankuuid = "610b1d4d-81b1-4487-956b-2b5c964339cc";
        SharePrefUtil.saveStringPreference(this, PrefKey.BANK_UUID, bankuuid);

        BT_ADDRESS = UUID.fromString(SharePrefUtil.getStringPreference(this, PrefKey.BT_ADDRESS));
        if (BT_ADDRESS == null) {
            BT_ADDRESS = UUID.randomUUID();
            SharePrefUtil.saveStringPreference(this, PrefKey.BT_ADDRESS, BT_ADDRESS.toString());
        }

        // Add bank player if it doesn't exist
        Player bank = Realm
                .getDefaultInstance()
                .where(Player.class)
                .equalTo("mId", bankuuid)
                .findFirst();

        if (bank != null) {
            bank = Realm.getDefaultInstance().createObject(Player.class);
            bank.setUsername("Bank");
            bank.setMatchPlayed(new RealmList<Match>());
        }

        // TODO inizialize GSON with custom TypeAdapter for realm proxy object
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
