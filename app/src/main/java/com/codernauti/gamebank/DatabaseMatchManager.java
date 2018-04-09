package com.codernauti.gamebank;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.loadMatch.DatabaseFile;
import com.codernauti.gamebank.stateMonitors.ClientSyncStateService;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.internal.OsRealmConfig;

/**
 * Created by dpolonio on 15/03/18.
 */

public class DatabaseMatchManager {

    private static final String TAG = "DatabaseMatchManager";

    public static final String DATABASE_FOLDER_NAME = "matches";
    private static final String REALM_EXTENSION = "realm";

    private static final String CLIENT_DB_NAME = "ClientDatabase";

    private final File databaseFolder;

    public DatabaseMatchManager(File filesDir) {

        databaseFolder = new File(filesDir, DATABASE_FOLDER_NAME);

        if (!databaseFolder.exists()) {
            boolean res = databaseFolder.mkdir();
            Log.d(TAG, "Database folder created successfully? " + res);
        }
    }

    public List<DatabaseFile> getSavedMatches() {
        ArrayList<DatabaseFile> result = new ArrayList<>();
        File[] savedMatchFiles = databaseFolder.listFiles();

        for (File savedMatchFile : savedMatchFiles) {
            if (hasCorrectExtension(savedMatchFile) && !isClientDatabase(savedMatchFile.getName())) {
                RealmConfiguration configuration = new RealmConfiguration.Builder()
                        .name(savedMatchFile.getName())
                        .directory(new File(GameBank.FILES_DIR, "matches"))
                        .build();
                // Realm.getInstance(configuration);

                DatabaseFile dbFile = new DatabaseFile(savedMatchFile, configuration);

                result.add(dbFile);
            }
        }

        return result;
    }

    private static boolean hasCorrectExtension(File savedFile) {
        return REALM_EXTENSION.equals(getFileExtension(savedFile.getName()));
    }

    private static boolean isClientDatabase(String filename) {
        return filename.equals(CLIENT_DB_NAME + ".realm");
    }

    public void createMatchInstance(final Context context, final String matchName, final int initBudget) {
        Log.d(TAG, "Create new match (aka new db).\n" +
                "MatchName: " + matchName + " InitBudget: " + initBudget);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String nameDatabase = isoFormat.format(Calendar.getInstance().getTime());

        initRealmDatabase(nameDatabase);

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

    private void initRealmDatabase(@NonNull String nameDatabase) {

        RealmConfiguration.Builder configBuilder = new RealmConfiguration.Builder()
                .name(nameDatabase + ".realm")
                .directory(databaseFolder);

        Realm.setDefaultConfiguration(configBuilder.build());
    }

    public void createClientMatchInstance(final Context context, final Match matchFromJson) {

        initRealmDatabase(CLIENT_DB_NAME);

        Log.d(TAG, "init ClientDatabase -> deleteAll");

        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });

        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(matchFromJson);
                SharePrefUtil.saveCurrentMatchId(context, matchFromJson.getId());
            }
        });
    }

    public void deleteMatch(File match) throws IOException {

        // Same database, IOException for now
        /*if (dbInstance != null && dbInstance.getPath().equals(match.getPath())) {
            throw new IOException("Cannot delete a database that's currently in use! Create a new one first");
        }*/
    }

    public void saveMatchToDisk(String filename) {

        /*if (dbInstance != null) {
            RealmConfiguration dbConfig = dbInstance.getConfiguration();
            Random r = new Random();

            if (dbConfig.getDurability().equals(OsRealmConfig.Durability.MEM_ONLY)) {

                File toSave = new File(databaseFolder, filename != null? filename : "Match" + r.nextInt());
                dbInstance.writeCopyTo(toSave);
            }
        }*/
    }

    private static String getFileExtension(String filename) {
        String filenameArray[] = filename.split("\\.");
        String extension = filenameArray[filenameArray.length-1];
        return extension;
    }

}
