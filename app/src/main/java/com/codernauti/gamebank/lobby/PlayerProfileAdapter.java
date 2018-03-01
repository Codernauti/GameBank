package com.codernauti.gamebank.lobby;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.util.PlayerProfile;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class PlayerProfileAdapter extends ArrayAdapter<PlayerProfile> {

    public PlayerProfileAdapter(@NonNull Context context) {
        super(context, R.layout.member_list_row);
    }

    @NonNull
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        PlayerProfile client = getItem(i);

        if (view == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.member_list_row, viewGroup, false);
        }

        ((TextView)view.findViewById(R.id.member_name))
                .setText(client.getNickname());

        TextView readyText = view.findViewById(R.id.member_ready);

/*        if (client.isReady()) {
            readyText.setText(getContext().getString(R.string.status_member_ready));
        } else {
            readyText.setText(getContext().getString(R.string.status_member_not_ready));
        }*/

        return view;
    }

}
