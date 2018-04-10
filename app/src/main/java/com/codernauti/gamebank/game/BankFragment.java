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

    @BindView(R.id.bank_minus_1)
    Button minusOneBtn;
    @BindView(R.id.bank_minus_5)
    Button minusFiveBtn;
    @BindView(R.id.bank_minus_10)
    Button minusTenBtn;

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

                    mAccountBalance += transaction.getAmount();
                    mAccountBalanceText.setText(String.valueOf(mAccountBalance));
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && requestCode == SEND_TO_PLAYER) {

            int valueSent = data.getIntExtra(SelectPlayerActivity.TRANSACTION_VALUE_KEY, 0);

            mAccountBalance -= valueSent;
            mAccountBalanceText.setText(String.valueOf(mAccountBalance));

        } else {
            Log.e(TAG, "Something wrong happen from SelectPlayerActivity");
        }

        super.onActivityResult(requestCode, resultCode, data);
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

    @OnClick(R.id.bank_plus_10)
    public void addTen() {
        mTransactionValue += 10;
        mTransactionValueView.setText(String.valueOf(mTransactionValue));
    }

    @OnClick(R.id.bank_minus_10)
    public void subtractTen() {
        mTransactionValue -= 10;
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

            String from;
            String to;

            if (mTransactionValue < 0) {
                // update UI, TODO: negative value don't have Realm onUpdateListener
                mAccountBalance += mTransactionValue;
                mAccountBalanceText.setText(String.valueOf(mAccountBalance));

                from = GameBank.BT_ADDRESS.toString();
                to = SharePrefUtil.getStringPreference(getContext(), PrefKey.BANK_UUID);

            } else {
                from = SharePrefUtil.getStringPreference(getContext(), PrefKey.BANK_UUID);
                to = GameBank.BT_ADDRESS.toString();
            }

            sendTransaction(to, from);

            resetTransactionValue();

        } else {
            Intent intent = SelectPlayerActivity.createActivity(getContext(), mTransactionValue);
            startActivityForResult(intent, SEND_TO_PLAYER);
            resetTransactionValue();
        }
    }

    private void sendTransaction(String toPlayerId, String fromPlayerId) {

        int matchId = SharePrefUtil.getCurrentMatchId(getContext());

        Log.d(TAG, "Transaction from " + fromPlayerId +
                        " to " + toPlayerId);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                Math.abs(mTransactionValue),
                fromPlayerId,
                toPlayerId,
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

        if (toBankChoice.isEnabled()) {

            toBankChoice.setEnabled(false);
            toUserChoice.setEnabled(true);
            resetTransactionValue();
            setEnableMinusButtons(true);

        } else {

            toBankChoice.setEnabled(true);
            toUserChoice.setEnabled(false);
            resetTransactionValue();
            setEnableMinusButtons(false);
        }
    }

    private void setEnableMinusButtons(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;

        minusOneBtn.setVisibility(visibility);
        minusFiveBtn.setVisibility(visibility);
        minusTenBtn.setVisibility(visibility);
    }

}