package com.codernauti.gamebank.loadMatch;

import java.io.File;

/**
 * Created by Eduard on 21-Mar-18.
 */

public class DatabaseFile {

    private String mDbName;
    private String mMatchName;

    public DatabaseFile(String dbName, String matchName) {
        mDbName = dbName;
        mMatchName = matchName;
    }

    public DatabaseFile(File savedMatchFile) {
        mDbName = savedMatchFile.getName();
    }

    public String getDbName() {
        return mDbName;
    }

    public void setDbName(String dbName) {
        this.mDbName = dbName;
    }

    public String getMatchName() {
        return mMatchName;
    }

    public void setMatchName(String matchName) {
        this.mMatchName = matchName;
    }
}
