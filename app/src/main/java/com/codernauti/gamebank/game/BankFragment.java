package com.codernauti.gamebank.game;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codernauti.gamebank.Event;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.game.sendTransaction.SelectPlayerActivity;
import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.SharePrefUtil;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class BankFragment extends Fragment {

    private static final String TAG = "BankFragment";

    @BindView(R.id.bank_account_balance)
    TextView mAccountBalanceText;

    @BindView(R.id.bank_total_trans)
    TextView mTransactionValueView;

    @BindView(R.id.bank_to_bank)
    ImageButton toBankChoice;

    @BindView(R.id.bank_to_users)
    ImageButton toUserChoice;

    private LocalBroadcastManager mLocalBroadcastManager;
    // TODO: move these to a Model class
    private int mAccountBalance;
    private int mTransactionValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final ViewGroup root = (ViewGroup) inflater
                .inflate(R.layout.bank_fragment, container, false);
        ButterKnife.bind(this, root);

        toBankChoice.setEnabled(false); // default bank activated
        toUserChoice.setEnabled(true);

        resetTransactionValue();

        setInitBudget();

        return root;
    }

    private void resetTransactionValue() {
        mTransactionValue = 0;
        mTransactionValueView.setText("0");
    }

    private void setInitBudget() {
        int matchId = SharePrefUtil.getCurrentMatchId(getContext());
        Match match = Realm.getDefaultInstance()
                .where(Match.class)
                .equalTo("mId", matchId)
                .findFirst();

        RealmResults<Transaction> transactions = Realm.getDefaultInstance()
                .where(Transaction.class)
                .findAll();

        mAccountBalance = match.getInitBudget();

        if (transactions.size() != 0) {

            for (final Transaction t : transactions) {

                if (GameBank.BT_ADDRESS.toString().equals(t.getRecipient())) {
                    mAccountBalance += t.getAmount();
                } else if (GameBank.BT_ADDRESS.toString().equals(t.getSender())) {
                    mAccountBalance -= t.getAmount();
                }
            }
        }

        mAccountBalanceText.setText(String.valueOf(mAccountBalance));
    }

    @OnClick(R.id.bank_plus_5)
    public void addFive() {
        mTransactionValue += 5;
        mTransactionValueView.setText(String.valueOf(mTransactionValue));
    }

    @OnClick(R.id.bank_minus_5)
    public void subtractFive() {
        mTransactionValue -= 5;
        mTransactionValueView.setText(String.valueOf(mTransactionValue));
    }

    @OnClick(R.id.bank_plus_1)
    public void addOne() {
        mTransactionValue += 1;
        mTransactionValueView.setText(String.valueOf(mTransactionValue));
    }

    @OnClick(R.id.bank_minus_1)
    public void subtractOne() {
        mTransactionValue -= 1;
        mTransactionValueView.setText(String.valueOf(mTransactionValue));
    }

    @OnClick(R.id.bank_sent_btn)
    public void executeTransaction() {

        if (mTransactionValue == 0) {
            Toast.makeText(getContext(), "Transaction cannot be 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!toBankChoice.isEnabled()) { // disable button mean it is activated its function

            Log.d(TAG, "Execute transaction. Emit event: " + Event.Game.TRANSACTION);

            mAccountBalance += mTransactionValue;
            mAccountBalanceText.setText(String.valueOf(mAccountBalance));

            String from;
            String to;

            if (mTransactionValue < 0) {
                from = GameBank.BT_ADDRESS.toString();
                to = SharePrefUtil.getStringPreference(getContext(), PrefKey.BANK_UUID);
            } else {
                from = SharePrefUtil.getStringPreference(getContext(), PrefKey.BANK_UUID);
                to = GameBank.BT_ADDRESS.toString();
            }

            sendTransaction(to, from);

            resetTransactionValue();

        } else {
            startActivity(new Intent(getContext(), SelectPlayerActivity.class));
        }
    }

    private void sendTransaction(String to, String from) {

        Realm realm = Realm.getDefaultInstance();
        Gson converter = GameBank.gsonConverter;

        RealmResults<Player> listOfPlayers = realm.where(Player.class).findAll();

        Player toPlayer = listOfPlayers.where().equalTo("mId", to).findFirst();
        Player fromPlayer = listOfPlayers.where().equalTo("mId", from).findFirst();
        int matchId = SharePrefUtil.getCurrentMatchId(getContext());

        Log.d(TAG, "Transaction from " + fromPlayer.getUsername() +
                        " to " + toPlayer.getUsername());

        Transaction transaction = new Transaction(
                (int)(Calendar.getInstance().getTimeInMillis()/1000L),
                Math.abs(mTransactionValue),
                fromPlayer.getPlayerId(),
                toPlayer.getPlayerId(),
                matchId
        );

        // Send Transaction to all
        String jsonToSend = converter.toJson(transaction);
        Log.d(TAG, "Sending this json object: \n" + jsonToSend);

        Intent transIntent = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.TRANSACTION)
                        .append(jsonToSend)
        );

        mLocalBroadcastManager.sendBroadcast(transIntent);
    }

    @OnClick({R.id.bank_to_bank, R.id.bank_to_users})
    public void changeReceiver() {

        if (toBankChoice.isEnabled()) {

            toBankChoice.setEnabled(false);
            toUserChoice.setEnabled(true);
            resetTransactionValue();

        } else {

            toBankChoice.setEnabled(true);
            toUserChoice.setEnabled(false);
            resetTransactionValue();
            // TODO: disable negative buttons
        }
    }

}