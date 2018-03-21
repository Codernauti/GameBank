package com.codernauti.gamebank.loadMatch;

import android.support.annotation.NonNull;

import java.io.File;

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
}
