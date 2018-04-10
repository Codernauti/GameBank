package com.codernauti.gamebank.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;

import java.io.File;
import java.util.UUID;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

class TransAdapter extends RealmRecyclerViewAdapter<Transaction, TransactionViewHolder> {

    private static final String TAG = "TransAdapter";
    private final File mFilesDir;

    private static final RequestOptions requestOpts = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true);

    TransAdapter(OrderedRealmCollection<Transaction> data, File filesDir) {
        super(data, true);

        mFilesDir = filesDir;
        Log.d(TAG, "Data received: " + data.size() + " transactions");

        setHasStableIds(true);
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.trans_list_row, parent, false);

        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder viewHolder,
                                 int position) {

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


        if (sender.getPictureNameFile() != null) {
            File file = new File(mFilesDir, sender.getPictureNameFile());

            if (file.exists()) {
                Glide.with(viewHolder.userFromIcon.getContext())
                        .load(file)
                        // TODO: remove, this is useful for DEBUG
                        .apply(requestOpts)
                        .into(viewHolder.userFromIcon);
            } else {
                viewHolder.userFromIcon.setImageBitmap(null);
                Log.w(TAG, file.getAbsolutePath() + " doesn't exist");
            }
        } else {
            viewHolder.userFromIcon.setImageBitmap(null);
        }

        if (receiver.getPictureNameFile() != null) {
            File file = new File(mFilesDir, sender.getPictureNameFile());

            if (file.exists()) {
                Glide.with(viewHolder.userToIcon.getContext())
                        .load(file)
                        // TODO: remove, this is useful for DEBUG
                        .apply(requestOpts)
                        .into(viewHolder.userToIcon);
            } else {
                viewHolder.userToIcon.setImageBitmap(null);
                Log.w(TAG, file.getAbsolutePath() + " doesn't exist");
            }
        } else {
            viewHolder.userToIcon.setImageBitmap(null);
        }
    }

}