package com.codernauti.gamebank.pairing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.codernauti.gamebank.DatabaseMatchManager;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Eduard on 23-Mar-18.
 */

class CreateMatchDataSource {

    private static final String TAG = "CreateMatchDS";

    void createMatchInstance(final Context context, final String matchName, final int initBudget) {
        Log.d(TAG, "Create new match (aka new db).\n" +
                "MatchName: " + matchName + " InitBudget: " + initBudget);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String nameDatabase = isoFormat.format(Calendar.getInstance().getTime());

        initRealmDatabase(context, nameDatabase);

        // insert initial data
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                // Get the current max id in the EntityName table
                //Number id = realm.where(Match.class).max("mId");
                // If id is null, set it to 1, else set increment it by 1
                int matchId = 42;//(id == null) ? 1 : id.intValue() + 1;
                final Match newMatch = realm.createObject(Match.class, matchId);

                SharePrefUtil.saveCurrentMatchId(context, matchId);


                newMatch.setMatchName(matchName);
                newMatch.setNowAsTimeStarted();
                newMatch.setInitBudget(initBudget);
                newMatch.setPlayerList(new RealmList<Player>());
                newMatch.setTransactionList(new RealmList<Transaction>());


                Player bank = realm.createObject(Player.class, GameBank.BANK_UUID);
                bank.setUsername("Bank");
                bank.setReady(true);

                Player myself = realm.createObject(Player.class, GameBank.BT_ADDRESS.toString());
                myself.setUsername(SharePrefUtil.getNicknamePreference(context));
                myself.setReady(true);

                newMatch.getPlayerList().add(bank);
                newMatch.getPlayerList().add(myself);
            }
        });
    }

    private void initRealmDatabase(@NonNull Context context, @NonNull String nameDatabase) {
        // Create database associated with match
        RealmConfiguration.Builder configBuilder = new RealmConfiguration.Builder()
                .name(nameDatabase + ".realm")
                .directory(new File(context.getFilesDir(), "matches"));

        if (nameDatabase.equals(DatabaseMatchManager.CLIENT_DB_NAME)) {
            Log.d(TAG, "init ClientDatabase");

            Realm.setDefaultConfiguration(configBuilder.build());

            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.deleteAll();
                }
            });

        } else {
            Realm.setDefaultConfiguration(configBuilder.build());
        }
    }

    boolean matchCanStart() {

        RealmResults<Player> playersNotReady = Realm.getDefaultInstance()
                .where(Player.class)
                .equalTo("mReady", false)
                .findAll();

        return playersNotReady.isEmpty();
    }

}
