package com.codernauti.gamebank.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.trans_list_row, parent, false);

        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder viewHolder,
                                 int position) {
        Transaction transaction = getItem(position);

        if (transaction != null) {

            viewHolder.userFromTextView.setText(transaction.getFromName());
            viewHolder.userToTextView.setText(transaction.getToName());
            viewHolder.cashTextView.setText(String.valueOf(transaction.getAmount()));

            updateIconView(viewHolder.userFromIcon, transaction.getSender());
            updateIconView(viewHolder.userToIcon, transaction.getRecipient());
        }

    }

    private void updateIconView(ImageView imageView, String userId) {
        File file = new File(mFilesDir, Player.getPictureNameFile(userId));

        if (file.exists()) {
            Glide.with(imageView.getContext())
                    .load(file)
                    // TODO: remove, this is useful for DEBUG
                    .apply(requestOpts)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher_c);
            Log.w(TAG, file.getAbsolutePath() + " doesn't exist");
        }
    }

}