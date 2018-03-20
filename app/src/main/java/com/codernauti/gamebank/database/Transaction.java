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
    private Player mFrom;
    private Player mTo;
    private Match mMatch;

    public Transaction() {}

    public Transaction(
            int id,
            int amount,
            @NonNull Player from,
            @NonNull Player to,
            @NonNull Match match
    ) {
        this.mId = id;
        this.mAmount = amount;
        this.mFrom = from;
        this.mTo = to;
        this.mMatch = match;
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

    public Player getSender() {
        return mFrom;
    }

    public void setSender(@NonNull Player sender) {
        this.mFrom = sender;
    }

    public Player getRecipient() {
        return mTo;
    }

    public void setRecipient(@NonNull Player to) {
        this.mTo = to;
    }

    public Match getMatch() {
        return mMatch;
    }

    public void setMatch(@NonNull Match match) {
        this.mMatch = match;
    }
}
