package com.codernauti.gamebank;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.codernauti.gamebank.loadMatch.DatabaseFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.internal.OsRealmConfig;

/**
 * Created by dpolonio on 15/03/18.
 */

public class DatabaseMatchManager {

    private static final String TAG = "DatabaseMatchManager";

    private static final String DATABASE_FOLDER_NAME = "matches";
    private static final String REALM_EXTENSION = "realm";

    public static final String CLIENT_DB_NAME = "ClientDatabase";

    private final File databaseFolder;
    private Realm dbInstance;

    public DatabaseMatchManager(Context context) {

        databaseFolder = new File(context.getFilesDir(), DATABASE_FOLDER_NAME);

        if (!databaseFolder.exists()) {
            boolean res = databaseFolder.mkdir();
            Log.d(TAG, "Database folder created successfully? " + res);
        }
    }


    public Realm getMatch(){
        return dbInstance;
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

    // Passing a builder here cause in this way we can add a custom directory
    public void initializeNewMatch(@NonNull RealmConfiguration.Builder toBuildConfig) {

        if (dbInstance != null) {
            dbInstance.close();
        }
        toBuildConfig.directory(databaseFolder);
        dbInstance = Realm.getInstance(toBuildConfig.build());
    }

    public void deleteMatch(File match) throws IOException {

        // Same database, IOException for now
        if (dbInstance != null && dbInstance.getPath().equals(match.getPath())) {
            throw new IOException("Cannot delete a database that's currently in use! Create a new one first");
        }
    }

    public void saveMatchToDisk(String filename) {

        if (dbInstance != null) {
            RealmConfiguration dbConfig = dbInstance.getConfiguration();
            Random r = new Random();

            if (dbConfig.getDurability().equals(OsRealmConfig.Durability.MEM_ONLY)) {

                File toSave = new File(databaseFolder, filename != null? filename : "Match" + r.nextInt());
                dbInstance.writeCopyTo(toSave);
            }
        }
    }

    public void loadMatchFromDisk(String matchPath) {

    }

    private static String getFileExtension(String filename) {
        String filenameArray[] = filename.split("\\.");
        String extension = filenameArray[filenameArray.length-1];
        return extension;
    }

}
