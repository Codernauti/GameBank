package com.codernauti.gamebank.game;

import android.app.Activity;
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
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.game.sendTransaction.SelectPlayerActivity;
import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.util.UUID;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

public class BankFragment extends Fragment {

    private static final String TAG = "BankFragment";

    private static final int SEND_TO_PLAYER = 100;

    @BindView(R.id.bank_account_balance)
    TextView mAccountBalanceText;

    @BindView(R.id.bank_total_trans)
    TextView mTransactionValueView;

    @BindView(R.id.bank_to_bank)
    ImageButton toBankChoice;

    @BindView(R.id.bank_to_users)
    ImageButton toUserChoice;

    private static final int ONE_HUNDRED_MILLION = 100000000;

    private LocalBroadcastManager mLocalBroadcastManager;
    // TODO: move these to a Model class
    private int mAccountBalance;
    private int mTransactionValue;

    private RealmResults<Transaction> mTransactionToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        mTransactionToken = Realm.getDefaultInstance()
                .where(Transaction.class)
                .equalTo("mTo", GameBank.BT_ADDRESS.toString())
                .findAll();

        mTransactionToken.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Transaction>>() {
            @Override
            public void onChange(RealmResults<Transaction> transactions,
                                 @Nullable OrderedCollectionChangeSet changeSet) {

                if (changeSet == null) {
                    return;
                }

                final int[] insertions = changeSet.getInsertions();
                if (insertions.length == 1) {
                    // new transaction to me! -> update my balance

                    final Transaction transaction = transactions.get(insertions[0]);
                    Log.d(TAG, "Value added: " + transaction.getAmount());

                    setTotalAccountBalance(mAccountBalance + transaction.getAmount());
                }
            }
        });

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
        setTransactionValue(0);
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

        setTotalAccountBalance(match.getInitBudget());

        if (transactions.size() != 0) { // restore saved match

            for (final Transaction t : transactions) {

                if (GameBank.BT_ADDRESS.toString().equals(t.getRecipient())) {
                    setTotalAccountBalance(mAccountBalance + t.getAmount());
                } else if (GameBank.BT_ADDRESS.toString().equals(t.getSender())) {
                    setTotalAccountBalance(mAccountBalance - t.getAmount());
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && requestCode == SEND_TO_PLAYER) {

            int valueSent = data.getIntExtra(SelectPlayerActivity.TRANSACTION_VALUE_KEY, 0);

            setTotalAccountBalance(mAccountBalance - valueSent);

        } else {
            Log.e(TAG, "Something wrong happen from SelectPlayerActivity");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.bank_multiply)
    public void multiplyByTen() {
        updateTransactionValue(mTransactionValue * 10);
    }

    @OnClick(R.id.bank_divide)
    public void divideByTen() {
        if (mTransactionValue >= 10 || mTransactionValue <= -10) {
            updateTransactionValue(mTransactionValue / 10);
        }
    }

    private void updateTransactionValue(int newTransactionValue) {

        if (transactionWithBank()) {

            if (mAccountBalance + newTransactionValue > ONE_HUNDRED_MILLION) {
                setTransactionValue(ONE_HUNDRED_MILLION - mAccountBalance);
            } else if (mAccountBalance + newTransactionValue >= 0) {
                setTransactionValue(newTransactionValue);
            } else {
                setTransactionValue(-mAccountBalance);
            }

        } else if (transactionWithOtherPlayer()) { // take money from balance

            if (newTransactionValue > mAccountBalance) {
                showAToast(R.string.bank_balance_insufficient);
            } else if (newTransactionValue < 0) {
                showAToast(R.string.bank_value_send_positive);
            } else if (newTransactionValue > ONE_HUNDRED_MILLION) {
                setTransactionValue(mAccountBalance);
            } else {
                setTransactionValue(newTransactionValue);
            }

        }
    }

    private boolean transactionWithBank() {
        return !toBankChoice.isEnabled();   // disable button mean it is activated its function
    }

    private boolean transactionWithOtherPlayer() {
        return !toUserChoice.isEnabled();
    }

    private void setTransactionValue(int totalTransValue) {
        mTransactionValue = totalTransValue;
        mTransactionValueView.setText(String.valueOf(mTransactionValue));
    }


    @OnClick(R.id.bank_plus_1)
    public void addOne() {
        changeTransactionBy(1);
    }

    @OnClick(R.id.bank_minus_1)
    public void subtractOne() {
        changeTransactionBy(-1);
    }

    @OnClick(R.id.bank_plus_5)
    public void addFive() {
        changeTransactionBy(5);
    }

    @OnClick(R.id.bank_minus_5)
    public void subtractFive() {
        changeTransactionBy(-5);
    }

    @OnClick(R.id.bank_plus_10)
    public void addTen() {
        changeTransactionBy(10);
    }

    @OnClick(R.id.bank_minus_10)
    public void subtractTen() {
        changeTransactionBy(-10);
    }

    // NB this method in bank mode add money to balance
    // in to user mode remove money to balance
    private void changeTransactionBy(int value) {
        updateTransactionValue(mTransactionValue + value);
    }


    @OnClick(R.id.bank_sent_btn)
    public void executeTransaction() {

        if (mTransactionValue == 0) {
            showAToast(R.string.bank_trans_zero);
            return;
        }

        if (transactionWithBank()) {

            Log.d(TAG, "Execute transaction. Emit event: " + Event.Game.TRANSACTION);

            String from, fromName;
            String to, toName;

            if (mTransactionValue < 0) {
                // update UI, TODO: negative value don't have Realm onUpdateListener
                setTotalAccountBalance(mAccountBalance + mTransactionValue);

                from = GameBank.BT_ADDRESS.toString();
                fromName = SharePrefUtil.getNicknamePreference(getContext());

                to = SharePrefUtil.getStringPreference(getContext(), PrefKey.BANK_UUID);
                toName = GameBank.BANK_NAME;

            } else {
                from = SharePrefUtil.getStringPreference(getContext(), PrefKey.BANK_UUID);
                fromName = GameBank.BANK_NAME;

                to = GameBank.BT_ADDRESS.toString();
                toName = SharePrefUtil.getNicknamePreference(getContext());
            }

            sendTransaction(to, toName, from, fromName);

            resetTransactionValue();

        } else {
            Intent intent = SelectPlayerActivity.createActivity(getContext(), mTransactionValue);
            startActivityForResult(intent, SEND_TO_PLAYER);
            resetTransactionValue();
        }
    }

    private void sendTransaction(String toPlayerId, String toPlayerName,
                                 String fromPlayerId, String fromPlayerName) {

        int matchId = SharePrefUtil.getCurrentMatchId(getContext());

        Log.d(TAG, "Transaction from " + fromPlayerId +
                        " to " + toPlayerId);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                Math.abs(mTransactionValue),
                fromPlayerId,
                fromPlayerName,
                toPlayerId,
                toPlayerName,
                matchId
        );

        // Send Transaction to all
        String jsonToSend = GameBank.gsonConverter.toJson(transaction);
        Log.d(TAG, "Sending this json object: \n" + jsonToSend);

        Intent transIntent = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.TRANSACTION)
                        .append(jsonToSend)
        );

        mLocalBroadcastManager.sendBroadcast(transIntent);
    }

    @OnClick({R.id.bank_to_bank, R.id.bank_to_users})
    public void changeReceiver() {

        if (transactionWithOtherPlayer()) {

            // enableTransactionWithBank
            toBankChoice.setEnabled(false);
            toUserChoice.setEnabled(true);
            resetTransactionValue();

        } else {
            // enableTransactionWithOtherPlayer
            toBankChoice.setEnabled(true);
            toUserChoice.setEnabled(false);
            resetTransactionValue();
        }
    }

    private void setTotalAccountBalance(int totalAccountBalance) {
        if (totalAccountBalance > ONE_HUNDRED_MILLION) {
            mAccountBalance = ONE_HUNDRED_MILLION;
        } else {
            mAccountBalance = totalAccountBalance;
        }

        mAccountBalanceText.setText(String.valueOf(mAccountBalance));
    }

    private Toast mToast;

    private void showAToast(int messageResId){
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getContext(), messageResId, Toast.LENGTH_SHORT);
        mToast.show();
    }

}