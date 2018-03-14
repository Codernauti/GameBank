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

import com.codernauti.gamebank.Event;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.util.Calendar;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;

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
    private String mNickname;

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

        mNickname = SharePrefUtil.getNicknamePreference(getContext());

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

        if (mTransactionValue != 0) {

            Log.d(TAG, "Execute transaction. Emit event: " + Event.Game.TRANSACTION);

            mAccountBalance += mTransactionValue;
            mAccountBalanceText.setText(String.valueOf(mAccountBalance));

            Realm db = Realm.getDefaultInstance();
            db.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    com.codernauti.gamebank.database.Transaction transaction;
                    String bankUUID = SharePrefUtil.getStringPreference(getContext(), PrefKey.BANK_UUID);
                    String myUUUID = GameBank.BT_ADDRESS.toString();

                    transaction = realm.createObject(
                            com.codernauti.gamebank.database.Transaction.class,
                            (int)(Calendar.getInstance().getTimeInMillis()/1000L));
                    transaction.setAmount(Math.abs(mTransactionValue));
                    RealmList<Player> listOfPlayers = realm.where(Match.class)
                            .equalTo("mId", 1/*id match*/)
                            .findFirst()
                            .getPlayerList();

                    Player bank = listOfPlayers.where().equalTo("mId", bankUUID).findFirst();
                    Player myself = listOfPlayers.where().equalTo("mId", myUUUID).findFirst();

                    if (mTransactionValue < 0) {
                        transaction.setRecipient(myself);
                        transaction.setSender(bank);
                    } else {
                        transaction.setRecipient(bank);
                        transaction.setRecipient(myself);
                    }

                    Intent transIntent = BTBundle.makeIntentFrom(
                            new BTBundle(Event.Game.TRANSACTION)
                                    .append(transaction)
                    );
                    mLocalBroadcastManager.sendBroadcast(transIntent);

                    mTransactionValue = 0;
                    mTransactionValueView.setText("0");

                }
            });
        } else {
            Toast.makeText(getContext(), "Transaction cannot be 0", Toast.LENGTH_SHORT).show();
        }
    }



}