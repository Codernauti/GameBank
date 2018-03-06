package com.codernauti.gamebank.game;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codernauti.gamebank.R;

import butterknife.BindView;
import butterknife.ButterKnife;

class TransactionViewHolder extends RecyclerView.ViewHolder{

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

    public void positiveArrow() {
        DrawableCompat.setTint(arrow.getDrawable(), Color.GREEN);
    }

    public void negativeArrow() {
        DrawableCompat.setTint(arrow.getDrawable(), Color.RED);
    }
}
