package com.codernauti.gamebank.game;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.util.Event;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BankFragment extends Fragment {

    private static final String TAG = "BankFragment";

    @BindView(R.id.bank_account_balance)
    TextView mAccountBalanceText;

    @BindView(R.id.bank_total_trans)
    TextView mTransactionValueView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup root = (ViewGroup) inflater
                .inflate(R.layout.bank_fragment, container, false);
        ButterKnife.bind(this, root);

        mAccountBalanceText.setText(String.valueOf(mAccountBalance));

        return root;
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
        Log.d(TAG, "Execute transaction. Emit event: " + Event.Game.TRANSACTION);

        mAccountBalance += mTransactionValue;
        mAccountBalanceText.setText(String.valueOf(mAccountBalance));

        Intent transaction = BTBundle.makeIntentFrom(
                new BTBundle(Event.Game.TRANSACTION).append(mTransactionValue)
        );
        mLocalBroadcastManager.sendBroadcast(transaction);

        mTransactionValue = 0;
        mTransactionValueView.setText("0");
    }



}