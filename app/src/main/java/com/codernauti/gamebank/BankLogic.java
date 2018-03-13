package com.codernauti.gamebank;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;

import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by Eduard on 05-Mar-18.
 */

// TODO: rename to GameLogic
public class BankLogic {

    private static final String TAG = "BankLogic";

    public interface ListenerBank {
        void onNewTransaction(Transaction newTrans);
    }

    private LocalBroadcastManager mLocalBroadcastManager;
    private ListenerBank mListenerBank;

    // Game fields
    private Realm db;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action " + action);

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (Event.Game.TRANSACTION.equals(action)) {

                    Transaction transaction = (Transaction) btBundle.get(
                            Transaction.class.getName());

                    if (transaction != null) {

                        if (!transaction.isValid()) {
                            // Create a copy of the transaction we received
                            Transaction dbTransaction = db.createObject(Transaction.class, transaction.getId());
                            dbTransaction.setAmount(transaction.getAmount());
                            dbTransaction.setRecipient(transaction.getRecipient());
                            dbTransaction.setSender(transaction.getSender());
                            dbTransaction.setMatch(transaction.getMatch());
                            transaction = dbTransaction;
                        }

                        db.where(Match.class)
                                .equalTo("mId", transaction.getMatch().getId())
                                .findFirst().getTransactionList().add(transaction);

                        if (mListenerBank != null) {
                            mListenerBank.onNewTransaction(transaction);
                        }

                        Log.d(TAG, /*"Transaction from " + fromUser +
                                " to " + toUser + "\n" +*/
                                "Quantity: " + transaction.getAmount());

                    } else {
                        Log.e(TAG, "Sent a transaction empty!");
                    }
                }
            }
        }
    };


    BankLogic(@NonNull LocalBroadcastManager broadcastManager) {

        this.db = Realm.getDefaultInstance();

        mLocalBroadcastManager = broadcastManager;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Event.Game.TRANSACTION);

        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }


    public void setListener(@Nullable ListenerBank listenerBank) {
        if (listenerBank != null) {
            Log.d(TAG, "Set listener: " + listenerBank.getClass());
        }

        mListenerBank = listenerBank;
    }

}
