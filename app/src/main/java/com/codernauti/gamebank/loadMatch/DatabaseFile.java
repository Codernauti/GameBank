package com.codernauti.gamebank.loadMatch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.codernauti.gamebank.DatabaseMatchManager;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmConfiguration;

/**
 * Created by Eduard on 21-Mar-18.
 */

public class DatabaseFile {

    private final String mDbName;
    private final RealmConfiguration mConfiguration;
    private String mMatchName;


    public DatabaseFile(@NonNull File savedMatchFile, @NonNull RealmConfiguration config) {
        mDbName = savedMatchFile.getName();
        mConfiguration = config;
    }

    public String getDbName() {
        return mDbName;
    }

    String getPrettySavedMatchName() {
        DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        String filenameArray[] = mDbName.split("\\.");

        DateFormat prettyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

        try {
            Date date = timestampFormat.parse(filenameArray[0]);

            String name = prettyFormat.format(date);
            return name;

        } catch (ParseException e) {
            Log.e("DatabaseFile", "Error parsing name of file");
            return mDbName;
        }
    }

    public String getMatchName() {
        return mMatchName;
    }

    public void setMatchName(String matchName) {
        this.mMatchName = matchName;
    }

    @NonNull
    RealmConfiguration getDbConficuration() {
        return mConfiguration;
    }

    boolean deleteFiles(File filesDir) {

        String location = filesDir +  "/" + DatabaseMatchManager.DATABASE_FOLDER_NAME;

        File dbFile = new File(location, mDbName);
        File dbLockFile = new File(location, mDbName + ".lock");
        File dbManagementFolder = new File(location, mDbName + ".management");

        boolean isRealmFileDeleted = dbFile.delete();
        boolean isLockFileDeleted = dbLockFile.delete();

        if (dbManagementFolder.isDirectory()) {
            String[] children = dbManagementFolder.list();
            for (String child : children) {
                new File(dbManagementFolder, child).delete();
            }
        }

        return isRealmFileDeleted;
    }
}
