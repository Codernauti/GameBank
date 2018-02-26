package com.codernauti.gamebank.game;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codernauti.gamebank.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BankFragment extends Fragment {

    private static final String TAG = "BankFragment";

    @BindView(R.id.bank_account_balance)
    private TextView mAccountBalanceText;

    // TODO: move this to another Model class
    private int mAccountBalance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.bank_fragment, container, false);
        ButterKnife.bind(this, root);

        return root;
    }

    @OnClick(R.id.bank_plus_5)
    public void addFive() {
        mAccountBalance += 5;
        mAccountBalanceText.setText(mAccountBalance);

        // TODO: send data to BTClientConnection
    }

    @OnClick(R.id.bank_minus_5)
    public void subtractFive() {
        mAccountBalance -= 5;
        mAccountBalanceText.setText(mAccountBalance);
    }


}