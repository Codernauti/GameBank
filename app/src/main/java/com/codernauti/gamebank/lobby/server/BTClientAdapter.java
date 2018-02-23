package com.codernauti.gamebank.lobby.server;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTClient;

import java.util.ArrayList;

/**
 * Created by dpolonio on 22/02/18.
 */

public class BTClientAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BTClient> clientList;

    BTClientAdapter(Context context, ArrayList<BTClient> clientList) {
        this.context = context;
        this.clientList = clientList;
    }

    @Override
    public int getCount() {
        return clientList.size();
    }

    @Override
    public Object getItem(int i) {
        return clientList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0; // ?
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // view : older view to recycle if needed
        // viewGroup : ListView

        ConstraintLayout btDeviceItem;
        BTClient client = clientList.get(i);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            btDeviceItem = (ConstraintLayout) inflater.inflate(R.layout.member_lobby_row, null);
        } else {
            btDeviceItem = (ConstraintLayout) view;
        }

        ((TextView) btDeviceItem.findViewById(R.id.member_name))
                .setText(client.getName());

        TextView readyText = ((TextView)btDeviceItem.findViewById(R.id.member_ready));

        if (client.isReady()) {
            readyText.setText(context.getString(R.string.status_member_ready));
        } else {
            readyText.setText(context.getString(R.string.status_member_not_ready));
        }

        return btDeviceItem;
    }
}
