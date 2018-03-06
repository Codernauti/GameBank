package com.codernauti.gamebank.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.TransModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class TransAdapter extends RecyclerView.Adapter<TransactionViewHolder> {

    private String mMyUUID;
    private List<TransModel> mTransactionsList = new ArrayList<>();

    TransAdapter(@NonNull UUID myUUID) {
        mMyUUID = myUUID.toString();
    }

    @Override
        public TransactionViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.trans_list_row;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder viewHolder, int position) {

        TransModel transaction = mTransactionsList.get(position);

        if (mMyUUID.equals(transaction.getToUser())) {
            viewHolder.positiveArrow();
        } else {
            viewHolder.negativeArrow();
        }

        viewHolder.userFromTextView.setText(
                mTransactionsList.get(position).getFromUser());
        viewHolder.userToTextView.setText(
                mTransactionsList.get(position).getToUser());
        viewHolder.cashTextView.setText(
                String.valueOf(mTransactionsList.get(position).getCash()));
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