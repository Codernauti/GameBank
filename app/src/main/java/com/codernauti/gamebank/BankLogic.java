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
    private int matchId;

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

            final BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                /*if (Event.Game.TRANSACTION.equals(action)) {

                    db.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Transaction transaction = realm.createOrUpdateObjectFromJson(
                                    Transaction.class,
                                    (String)btBundle.get(String.class.getName())
                            );

                            if (transaction != null) {

                                Log.d(TAG, "Transaction is not null");

                                if (!transaction.isValid()) {

                                    Log.d(TAG, "Transaction is not valid!");

                                    // Create a copy of the transaction we received
                                    Transaction dbTransaction = realm.createObject(Transaction.class, transaction.getId());
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
                                        /*"Quantity: " + transaction.getAmount());

                            } else {
                                Log.e(TAG, "Sent a transaction empty!");
                            }
                        }
                    });
                }*/
            }
        }
    };


    BankLogic(@NonNull LocalBroadcastManager broadcastManager, final int matchId, final String bankuuid) {

        this.db = Realm.getDefaultInstance();
        this.matchId = matchId;
        this.mLocalBroadcastManager = broadcastManager;

        // Add bank player to this game
        db.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Match.class)
                        .equalTo("mId", matchId)
                        .findFirst()
                        .getPlayerList()
                        .add(realm.where(Player.class)
                                    .equalTo("mId", bankuuid)
                                    .findFirst());
            }
        });


        IntentFilter filter = new IntentFilter();
        //filter.addAction(Event.Game.TRANSACTION);

        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }


    public void setListener(@Nullable ListenerBank listenerBank) {
        if (listenerBank != null) {
            Log.d(TAG, "Set listener: " + listenerBank.getClass());
        }

        mListenerBank = listenerBank;
    }

    public void addTransaction(final int amount, @NonNull final String to, @NonNull final String from) {

        db.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                Gson converter = GameBank.gsonConverter;
                RealmList<Player> listOfPlayers = db.where(Match.class)
                        .equalTo("mId", matchId)
                        .findFirst()
                        .getPlayerList();


                Transaction transaction = realm.createObject(
                        Transaction.class,
                        (int)(Calendar.getInstance().getTimeInMillis()/1000L)
                );
                transaction.setAmount(amount);
                transaction.setRecipient(
                        listOfPlayers
                                .where()
                                .equalTo("mId", to)
                                .findFirst()
                );
                transaction.setSender(
                        listOfPlayers
                                .where()
                                .equalTo("mId", from)
                                .findFirst()
                );

                String jsonToSend = converter.toJson(transaction);
                Log.d(TAG, "Sending this json object: \n" + jsonToSend);

                Intent transIntent = BTBundle.makeIntentFrom(
                        new BTBundle(Event.Game.TRANSACTION)
                                .append(jsonToSend)
                );

                if (mListenerBank != null) {
                    mListenerBank.onNewTransaction(transaction);
                }

                mLocalBroadcastManager.sendBroadcast(transIntent);
            }
        });

    }

}
