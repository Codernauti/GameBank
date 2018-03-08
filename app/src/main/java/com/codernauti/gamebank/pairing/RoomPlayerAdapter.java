package com.codernauti.gamebank.pairing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codernauti.gamebank.R;

import java.util.UUID;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class RoomPlayerAdapter extends ArrayAdapter<RoomPlayer> {

    public RoomPlayerAdapter(@NonNull Context context) {
        super(context, R.layout.member_list_row);
    }

    @NonNull
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        RoomPlayer client = getItem(i);

        if (view == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.member_list_row, viewGroup, false);
        }

        ((TextView)view.findViewById(R.id.member_name))
                .setText(client.getNickname());

        TextView readyText = view.findViewById(R.id.member_ready);

        if (client.isReady()) {
            readyText.setText(getContext().getString(R.string.status_member_ready));
        } else {
            readyText.setText(getContext().getString(R.string.status_member_not_ready));
        }

        return view;
    }

    public void updatePlayerState(RoomPlayer player) {
        for (int i = 0; i < getCount(); i++) {
            if (player.getId().equals(getItem(i).getId())) {
                getItem(i).setReady(player.isReady());
                notifyDataSetChanged();
            }
        }
    }

    public void removePlayer(UUID playerToRemoveUuid) {
        for (int i = 0; i < getCount(); i++) {
            if (playerToRemoveUuid.equals(getItem(i).getId())) {
                remove(getItem(i));
            }
        }
    }
}
