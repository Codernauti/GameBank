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
import com.codernauti.gamebank.util.SharePrefUtil;
import com.google.gson.Gson;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmList;

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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action " + action);

            final BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (Event.Game.TRANSACTION.equals(action)) {

                    final String jsonTransaction = (String) btBundle.get(String.class.getName());

                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            Transaction newTransaction = realm
                                    .createOrUpdateObjectFromJson(Transaction.class, jsonTransaction);

                            // Update match transitions
                            Match currentMatch = realm.where(Match.class)
                                    .equalTo("mId", SharePrefUtil.getCurrentMatchId(context))
                                    .findFirst();

                            currentMatch.getTransactionList().add(newTransaction);
                        }
                    });


                    Transaction transaction = GameBank.gsonConverter.fromJson(
                            jsonTransaction, Transaction.class);

                    if (mListenerBank != null) {
                        mListenerBank.onNewTransaction(transaction);
                    }
                }
            }
        }
    };


    BankLogic(@NonNull LocalBroadcastManager broadcastManager, final int matchId, final String bankuuid) {

        this.mLocalBroadcastManager = broadcastManager;

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
