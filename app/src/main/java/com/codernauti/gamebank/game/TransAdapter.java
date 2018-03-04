package com.codernauti.gamebank.game;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.TransModel;

import java.util.ArrayList;
import java.util.List;

class TransAdapter extends RecyclerView.Adapter<TransAdapterViewHolder> {

    private List<TransModel> mTransactionsList = new ArrayList<>();

    TransAdapter() {
    }

    @Override
        public TransAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.trans_list_row;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new TransAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransAdapterViewHolder transAdapterViewHolder, int position) {

        transAdapterViewHolder.userFromTextView.setText(mTransactionsList.get(position).getFromUser());
        transAdapterViewHolder.userToTextView.setText(mTransactionsList.get(position).getToUser());
        transAdapterViewHolder.cashTextView.setText(String.valueOf(mTransactionsList.get(position).getCash()));
    }

    @Override
    public int getItemCount() {
        return mTransactionsList.size();
    }

    void addTransaction(TransModel trans){
        mTransactionsList.add(trans);
        notifyItemInserted(mTransactionsList.size());
    }

}