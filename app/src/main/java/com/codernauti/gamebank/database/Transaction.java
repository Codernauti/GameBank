package com.codernauti.gamebank.database;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dpolonio on 13/03/18.
 */

public class Transaction extends RealmObject {

    @PrimaryKey
    private int mId;
    private int mAmount;
    private Player mFrom;
    private Player mTo;

    public Transaction(
            int id,
            int amount,
            @NonNull Player from,
            @NonNull Player to
    ) {
        this.mId = id;
        this.mAmount = amount;
        this.mFrom = from;
        this.mTo = to;
    }

    public int getId() {
        return mId;
    }

    public int getAmount() {
        return mAmount;
    }

    @NonNull
    public Player getSender() {
        return mFrom;
    }

    @NonNull
    public Player getRecipient() {
        return mTo;
    }
}
