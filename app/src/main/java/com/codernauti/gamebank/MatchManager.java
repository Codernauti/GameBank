package com.codernauti.gamebank;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.internal.OsRealmConfig;

/**
 * Created by dpolonio on 15/03/18.
 */

public class MatchManager {

    private final static String DATABASE_FOLDER_NAME = "matches";
    private final static String TAG = "MatchManager";

    private final File databaseFolder;
    private Realm dbIstance;

    public MatchManager(Context context) throws IOException {

        String path = context.getFilesDir().getAbsolutePath();
        databaseFolder = new File(path + "/" + DATABASE_FOLDER_NAME);

        if (!databaseFolder.exists()) {
            boolean res = databaseFolder.mkdir();

            Log.d(TAG, "Database folder created successfully? " + res);
            if (!res) {
                throw new IOException("Impossible to create database folder in " + databaseFolder);
            }
        }
    }

    public Realm getMatch(){
        return dbIstance;
    }

    public List<File> getSavedMatches() {

        return Arrays.asList(databaseFolder.listFiles());
    }

    // Passing a builder here cause in this way we can add a custom directory
    public void initializeNewMatch(@NonNull RealmConfiguration.Builder toBuildConfig) {

        if (dbIstance != null) {
            dbIstance.close();
        }
        toBuildConfig.directory(databaseFolder);
        dbIstance = Realm.getInstance(toBuildConfig.build());
    }

    public void deleteMatch(File match) throws IOException {

        // Same database, IOException for now
        if (dbIstance != null && dbIstance.getPath().equals(match.getPath())) {
            throw new IOException("Cannot delete a database that's currently in use! Create a new one first");
        }
    }

    public void saveMatchToDisk(String filename) {

        if (dbIstance != null) {
            RealmConfiguration dbConfig = dbIstance.getConfiguration();
            Random r = new Random();

            if (dbConfig.getDurability().equals(OsRealmConfig.Durability.MEM_ONLY)) {

                File toSave = new File(databaseFolder, filename != null? filename : "Match" + r.nextInt());
                dbIstance.writeCopyTo(toSave);
            }
        }
    }

    public void loadMatchFromDisk(String matchPath) {

    }

}
