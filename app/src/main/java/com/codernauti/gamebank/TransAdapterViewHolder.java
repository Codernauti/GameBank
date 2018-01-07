package com.codernauti.gamebank;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class TransAdapterViewHolder extends RecyclerView.ViewHolder{

    public final TextView userFromTextView;
    public final TextView userToTextView;
    public final TextView cashTextView;

    public TransAdapterViewHolder(View itemView) {
        super(itemView);

        userFromTextView = (TextView) itemView.findViewById(R.id.from_name_text);
        userToTextView = (TextView) itemView.findViewById(R.id.to_name_text);
        cashTextView = (TextView) itemView.findViewById(R.id.cash_text);
    }
}
