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
    private int mId;
    private int mAmount;
    private String mFrom;
    private String mTo;
    private int mMatchId;

    public Transaction() {}

    public Transaction(
            int id, int amount,
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

    public int getId() {
        return mId;
    }

    public int getAmount() {
        return mAmount;
    }

    public void setAmount(int quantity) {
        this.mAmount = quantity;
    }

    public String getSender() {
        return mFrom;
    }

    public void setSender(@NonNull String sender) {
        this.mFrom = sender;
    }

    public String getRecipient() {
        return mTo;
    }

    public void setRecipient(@NonNull String to) {
        this.mTo = to;
    }

    public int getMatchId() {
        return mMatchId;
    }

    public void setMatch(int match) {
        this.mMatchId = match;
    }
}
