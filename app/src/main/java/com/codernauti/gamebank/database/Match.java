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
    private int mId;
    private String mMatchName;
    private String mMatchStarted;
    private RealmList<Transaction> mTransactionList;
    private RealmList<Player> mPlayerList;

    public Match() {}

    public Match(int id, @NonNull String matchName) {

        Calendar c = Calendar.getInstance();

        this.mId = id;
        this.mMatchName = matchName;
        this.mMatchStarted = c.get(Calendar.DATE) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR);
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

    public void setMatchName(@NonNull String matchName) {
        this.mMatchName = matchName;
    }

    @NonNull
    public String getMatchStarted() {
        return mMatchStarted;
    }

    public void setMatchStarted(@NonNull String matchStarted) {
        this.mMatchStarted = matchStarted;
    }

    @NonNull
    public RealmList<Transaction> getTransactionList() {
        return mTransactionList;
    }

    public void setTransactionList(@NonNull RealmList<Transaction> transactions) {
        this.mTransactionList = transactions;
    }

    @NonNull
    public RealmList<Player> getPlayerList() {
        return mPlayerList;
    }

    public void setPlayerList(@NonNull RealmList<Player> players) {
        this.mPlayerList = players;
    }
}
