package com.codernauti.gamebank.lobby.server;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dpolonio on 22/02/18.
 */

class BTClientAdapter extends ArrayAdapter<BTClient> {

    BTClientAdapter(Context context) {
        super(context, R.layout.member_lobby_row);
    }

    @NonNull
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        BTClient client = getItem(i);

        if (view == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.member_lobby_row, viewGroup, false);
        }

        ((TextView)view.findViewById(R.id.member_name))
                .setText(client.getName());

        TextView readyText = view.findViewById(R.id.member_ready);

        if (client.isReady()) {
            readyText.setText(getContext().getString(R.string.status_member_ready));
        } else {
            readyText.setText(getContext().getString(R.string.status_member_not_ready));
        }

        return view;
    }

    ArrayList<BTClient> getAllBTClients() {
        ArrayList<BTClient> result = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            result.add(getItem(i));
        }

        return result;
    }
}
