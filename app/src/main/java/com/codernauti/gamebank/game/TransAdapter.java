package com.codernauti.gamebank.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;

import java.util.UUID;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

class TransAdapter extends RealmRecyclerViewAdapter<Transaction, TransactionViewHolder> {

    private static final String TAG = "TransAdapter";
    private UUID mMyUUID;

    TransAdapter(@NonNull UUID myUUID, OrderedRealmCollection<Transaction> data) {
        super(data, true);

        Log.d(TAG, "Data received: " + data.size() + " transactions");

        mMyUUID = myUUID;
        setHasStableIds(true);
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.trans_list_row;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder viewHolder, int position) {

        Transaction transaction = getItem(position);

        Realm realm = Realm.getDefaultInstance();

        Player sender = realm.where(Player.class)
                .equalTo("mId", transaction.getSender())
                .findFirst();

        Player receiver = realm.where(Player.class)
                .equalTo("mId", transaction.getRecipient())
                .findFirst();

        viewHolder.userFromTextView.setText(sender.getUsername());
        viewHolder.userToTextView.setText(receiver.getUsername());
        viewHolder.cashTextView.setText(String.valueOf(transaction.getAmount()));
    }

    void addTransaction(){
        notifyDataSetChanged();
    }

}