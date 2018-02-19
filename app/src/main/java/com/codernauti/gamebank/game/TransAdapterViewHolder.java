package com.codernauti.gamebank.game;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.codernauti.gamebank.R;

import butterknife.BindView;
import butterknife.ButterKnife;

class TransAdapterViewHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.from_name_text)
    TextView userFromTextView;
    @BindView(R.id.to_name_text)
    TextView userToTextView;
    @BindView(R.id.cash_text)
    TextView cashTextView;

    TransAdapterViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);

    }
}
