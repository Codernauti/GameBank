package com.codernauti.gamebank.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class TransAdapter extends RecyclerView.Adapter<TransactionViewHolder> {

    private UUID mMyUUID;
    private List<com.codernauti.gamebank.database.Transaction> mTransactionsList = new ArrayList<>();

    TransAdapter(@NonNull UUID myUUID) {
        mMyUUID = myUUID;
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

        com.codernauti.gamebank.database.Transaction transaction = mTransactionsList.get(position);

        /*if (mMyUUID.equals(transaction.getToUUID())) {
            viewHolder.positiveArrow();
        } else {
            viewHolder.negativeArrow();
        }*/

        viewHolder.userFromTextView.setText(transaction.getSender().getUsername());
        viewHolder.userToTextView.setText(transaction.getRecipient().getUsername());
        viewHolder.cashTextView.setText(String.valueOf(transaction.getAmount()));
    }

    @Override
    public int getItemCount() {
        return mTransactionsList.size();
    }

    void addTransaction(com.codernauti.gamebank.database.Transaction trans){
        mTransactionsList.add(trans);
        notifyItemInserted(mTransactionsList.size());
    }

}