package com.codernauti.gamebank;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

class TransAdapter extends RecyclerView.Adapter<TransAdapterViewHolder> {

    private List<TransModel> mTransactionsList = new ArrayList<>();

    public TransAdapter() {
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

    public void addTransaction(TransModel trans){
        mTransactionsList.add(trans);
        notifyDataSetChanged();
    }

}