package com.codernauti.gamebank;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.MatchSerializer;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.PlayerSerializer;
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.database.TransactionSerializer;
import com.codernauti.gamebank.game.DashboardActivity;
import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by davide on 01/03/18.
 */

public class GameBank extends Application {

    private static final String TAG = "GameBankApp";

    public static UUID BT_ADDRESS;
    public static String FILES_DIR;

    public static Gson gsonConverter;

    private RoomLogic mRoomLogic;
    private BankLogic mBankLogic;

    private boolean isHost = false;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);

            if (BTEvent.START.equals(action)) {

                BTBundle bundle = BTBundle.extractFrom(intent);

                /*int matchId = -1;
                if (!isHost) {

                    Realm db = Realm.getDefaultInstance();

                    db.beginTransaction();
                    Match match = db.createObjectFromJson(
                            Match.class,
                            (String)bundle.get(String.class.getName()));

                    matchId = match.getId();
                    db.commitTransaction();
                } else {
                    // I'm the host, I only need the id
                    try {
                        JSONObject jsonMatch = new JSONObject((String)bundle.get(String.class.getName()));

                        matchId = jsonMatch.getInt("mId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }*/

                /*mBankLogic = new BankLogic(
                        LocalBroadcastManager.getInstance(context),
                        matchId,
                        SharePrefUtil.getStringPreference(GameBank.this, PrefKey.BANK_UUID)
                );*/


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

        BT_ADDRESS = SharePrefUtil.getBTAddressPreference(this);

        // Add bank player if it doesn't exist
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Player bank = Realm
                        .getDefaultInstance()
                        .where(Player.class)
                        .equalTo("mId", bankuuid)
                        .findFirst();

                if (bank == null) {

                    bank = Realm.getDefaultInstance().createObject(Player.class, bankuuid);
                    bank.setUsername("Bank");
                    bank.setMatchPlayed(new RealmList<Match>());
                    bank.setPhotoName("aaa");
                }
            }
        });

        // TODO inizialize GSON with custom TypeAdapter for realm proxy object
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
                    .create();
        } catch (ClassNotFoundException e) {
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

    public void setHost(boolean value) {
        isHost = value;
    }

    public boolean isHost() {
        return isHost;
    }
}
