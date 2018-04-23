package com.codernauti.gamebank.lobby;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codernauti.gamebank.R;

/**
 * Created by dpolonio on 19/02/18.
 */

class BTHostAdapter extends ArrayAdapter<BluetoothDevice> {

    BTHostAdapter(Context context) {
        super(context, R.layout.bt_list_row);
    }

    @Override
    @NonNull
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        BluetoothDevice device = getItem(i);

        if (view == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.bt_list_row, viewGroup, false);
        }

        ((TextView)view.findViewById(R.id.lobby_name))
                .setText(device.getName());

        ((TextView)view.findViewById(R.id.lobby_status))
                .setText(device.getAddress());

        int drawableResource = R.drawable.ic_lock_outline_black_24dp;
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            drawableResource = R.drawable.ic_lock_open_black_24dp;
        }

        ((ImageView)view.findViewById(R.id.status_icon)).setImageDrawable(
                        ContextCompat.getDrawable(getContext(), drawableResource));

        return view;
    }

    @Override
    public void add(@Nullable BluetoothDevice object) {
        boolean isAlreadyAdded = false;
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).getAddress().equals(object.getAddress())) {
                isAlreadyAdded = true;
            }
        }

        if (!isAlreadyAdded) {
            super.add(object);
        }
    }
}
