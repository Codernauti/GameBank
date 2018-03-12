package com.codernauti.gamebank.game;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codernauti.gamebank.BankLogic;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionsFragment extends Fragment implements BankLogic.ListenerBank {

    private static final String TAG = "TransactionsFragment";

    @BindView(R.id.recyclerview_transactions)
    RecyclerView mRecyclerView;

    private TransAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.trans_fragment, container, false);
        ButterKnife.bind(this, root);

        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TransAdapter(GameBank.BT_ADDRESS);
        mRecyclerView.setAdapter(mAdapter);

        ((GameBank) getActivity().getApplication()).getBankLogic().setListener(this);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((GameBank) getActivity().getApplication()).getBankLogic().setListener(null);
    }

    // RoomLogic callbacks
    @Override
    public void onNewTransaction(Transaction newTrans) {
        mAdapter.addTransaction(newTrans);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }
}
