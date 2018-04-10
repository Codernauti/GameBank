package com.codernauti.gamebank.game;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codernauti.gamebank.R;

import butterknife.BindView;
import butterknife.ButterKnife;

class TransactionViewHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.trans_from_icon)
    ImageView userFromIcon;

    @BindView(R.id.trans_to_icon)
    ImageView userToIcon;

    @BindView(R.id.trans_row_from_name)
    TextView userFromTextView;

    @BindView(R.id.trans_row_to_name)
    TextView userToTextView;

    @BindView(R.id.trans_row_quantity)
    TextView cashTextView;

    @BindView(R.id.trans_row_arrow)
    ImageView arrow;


    TransactionViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
