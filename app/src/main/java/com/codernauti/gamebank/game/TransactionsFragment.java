package com.codernauti.gamebank.game;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.GameLogic;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.TransModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionsFragment extends Fragment implements GameLogic.ListenerBank {

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

        mAdapter = new TransAdapter();
        mRecyclerView.setAdapter(mAdapter);

        ((GameBank) getActivity().getApplication()).getGameLogic().setListenerBank(this);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((GameBank) getActivity().getApplication()).getGameLogic().setListenerBank(null);
    }

    // GameLogic callbacks
    @Override
    public void onNewTransaction(TransModel newTrans) {
        mAdapter.addTransaction(newTrans);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }
}
