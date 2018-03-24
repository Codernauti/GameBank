package com.codernauti.gamebank;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.database.MatchSerializer;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.PlayerDeserializer;
import com.codernauti.gamebank.database.PlayerSerializer;
import com.codernauti.gamebank.database.TransactionSerializer;
import com.codernauti.gamebank.game.DashboardActivity;
import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.SharePrefUtil;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by davide on 01/03/18.
 */

public class GameBank extends Application {

    private static final String TAG = "GameBankApp";

    public static UUID BT_ADDRESS;
    public static String FILES_DIR;
    public static final String BANK_UUID = "610b1d4d-81b1-4487-956b-2b5c964339cc";

    public static Gson gsonConverter;

    // need to keep in memory inner Broadcast Receivers
    private RoomLogic mRoomLogic;
    private BankLogic mBankLogic;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            if (BTEvent.START.equals(action)) {

                mBankLogic = new BankLogic(LocalBroadcastManager.getInstance(context));

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

        SharePrefUtil.saveStringPreference(this, PrefKey.BANK_UUID, BANK_UUID);

        BT_ADDRESS = SharePrefUtil.getBTAddressPreference(this);

        // Set a default image
        SharePrefUtil.loadDefaultProfilePicturePreference(this);

        try {
            gsonConverter = new GsonBuilder()
                    .setExclusionStrategies(new ExclusionStrategy() {
                        @Override
                        public boolean shouldSkipField(FieldAttributes f) {
                            return f.getDeclaringClass().equals(RealmObject.class);
                        }

                        @Override
                        public boolean shouldSkipClass(Class<?> clazz) {
                            return false;
                        }
                    })
                    .registerTypeAdapter(Class.forName("io.realm.MatchRealmProxy"), new MatchSerializer())
                    .registerTypeAdapter(Class.forName("io.realm.PlayerRealmProxy"), new PlayerSerializer())
                    .registerTypeAdapter(Class.forName("io.realm.TransactionRealmProxy"), new TransactionSerializer())
                    .registerTypeAdapter(Player.class, new PlayerSerializer())
                    .registerTypeAdapter(Player.class, new PlayerDeserializer())
                    .create();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        mRoomLogic = new RoomLogic(LocalBroadcastManager.getInstance(this));
    }

}
