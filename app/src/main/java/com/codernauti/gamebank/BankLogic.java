package com.codernauti.gamebank;

import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.lobby.RoomPlayer;
import com.codernauti.gamebank.util.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Eduard on 05-Mar-18.
 */

// TODO: rename to GameLogic
public class BankLogic {

    private static final String TAG = "BankLogic";

    public interface ListenerBank {
        void onNewTransaction(TransModel newTrans);
    }

    private LocalBroadcastManager mLocalBroadcastManager;
    private ListenerBank mListenerBank;

    // Game fields
    private HashMap<UUID, Integer> mPlayerAccounts = new HashMap<>();
    private ArrayList<TransModel> mTransactions = new ArrayList<>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action " + action);

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (Event.Game.TRANSACTION.equals(action)) {

                    TransModel transaction = (TransModel) btBundle.get(
                            TransModel.class.getName());

                    if (transaction != null) {

                        UUID fromUser = transaction.getFromUUID();
                        UUID toUser = transaction.getToUUID();


                        if (mPlayerAccounts.containsKey(fromUser)) {

                            int fromUserBalance = mPlayerAccounts.get(fromUser);
                            mPlayerAccounts.put(fromUser, fromUserBalance - transaction.getCash());
                        }

                        if (mPlayerAccounts.containsKey(toUser)) {

                            int toUserBalance = mPlayerAccounts.get(toUser);
                            mPlayerAccounts.put(toUser, toUserBalance + transaction.getCash());
                        }


                        mTransactions.add(transaction);

                        if (mListenerBank != null) {
                            mListenerBank.onNewTransaction(transaction);
                        }

                        Log.d(TAG, /*"Transaction from " + fromUser +
                                " to " + toUser + "\n" +*/
                                "Quantity: " + transaction.getCash());

                    } else {
                        Log.e(TAG, "Sent a transaction empty!");
                    }
                }
            }
        }
    };


    BankLogic(@NonNull LocalBroadcastManager broadcastManager,
                     @NonNull List<UUID> playerAccounts) {

        for (UUID uuid : playerAccounts) {
            mPlayerAccounts.put(uuid, 0);
        }

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
