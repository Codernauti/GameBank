package com.codernauti.gamebank.game;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.TransModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionsFragment extends Fragment {

    private static final String TAG = "TransactionsFragment";

    @BindView(R.id.recyclerview_transactions)
    RecyclerView mRecyclerView;

    private TransAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.trans_fragment, container, false);
        ButterKnife.bind(this, root);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new TransAdapter();

        mRecyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        TransModel newModel = new TransModel("Cicciolina", "Pierino", 50);
        mAdapter.addTransaction(newModel);
    }
}
