package com.codernauti.gamebank.database;

import android.support.annotation.NonNull;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dpolonio on 13/03/18.
 */

public class Transaction extends RealmObject implements Serializable {

    @PrimaryKey
    private String mId;
    private int mAmount;
    private String mFrom;
    private String mTo;
    private int mMatchId;

    public Transaction() {}

    public Transaction(
            String id, int amount,
            @NonNull String from,
            @NonNull String to,
            int match
    ) {
        this.mId = id;
        this.mAmount = amount;
        this.mFrom = from;
        this.mTo = to;
        this.mMatchId = match;
    }

    public String getId() {
        return mId;
    }

    public int getAmount() {
        return mAmount;
    }

    public String getSender() {
        return mFrom;
    }

    public String getRecipient() {
        return mTo;
    }

    public int getMatchId() {
        return mMatchId;
    }

    public void setMatch(int match) {
        this.mMatchId = match;
    }
}
