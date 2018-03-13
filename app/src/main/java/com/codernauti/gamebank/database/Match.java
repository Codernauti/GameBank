package com.codernauti.gamebank.database;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dpolonio on 13/03/18.
 */

public class Match extends RealmObject {

    @PrimaryKey
    private final int mId;
    private final String mMatchName;
    private final Calendar mMatchStarted;
    private final RealmList<Transaction> mTransactionList;
    private final RealmList<Player> mPlayerList;

    public Match(
            int id,
            @NonNull String matchName
    ) {
        this.mId = id;
        this.mMatchName = matchName;
        this.mMatchStarted = Calendar.getInstance();
        this.mTransactionList = new RealmList<>();
        this.mPlayerList = new RealmList<>();
    }

    public int getId() {
        return mId;
    }

    @NonNull
    public String getMatchName() {
        return mMatchName;
    }

    @NonNull
    public Calendar getMatchStarted() {
        return mMatchStarted;
    }

    @NonNull
    public RealmList<Transaction> getTransactionList() {
        return mTransactionList;
    }

    @NonNull
    public RealmList<Player> getPlayerList() {
        return mPlayerList;
    }
}
