package com.codernauti.gamebank.lobby.client;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codernauti.gamebank.R;

import java.util.ArrayList;

/**
 * Created by dpolonio on 19/02/18.
 */

public class BTHostAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BluetoothDevice> btDevices;

    BTHostAdapter(Context context, ArrayList<BluetoothDevice> btDevices) {
        this.context = context;
        this.btDevices = btDevices;
    }

    @Override
    public int getCount() {
        return btDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return btDevices.get(i);
    }

    @Override
    public long getItemId(int i) { //?
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ConstraintLayout btDeviceItem;
        BluetoothDevice device = btDevices.get(i);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            btDeviceItem = (ConstraintLayout) inflater.inflate(
                    R.layout.bt_list_row, null);
        } else {
            btDeviceItem = (ConstraintLayout) view;
        }

        ((TextView)btDeviceItem.findViewById(R.id.lobby_name))
                .setText(device.getName());

        ((TextView)btDeviceItem.findViewById(R.id.lobby_status))
                .setText(device.getAddress());

        int drawableResource = R.drawable.ic_lock_outline_black_24dp;
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            drawableResource = R.drawable.ic_lock_open_black_24dp;
        }

        ((ImageView)btDeviceItem.findViewById(R.id.status_icon))
                .setImageDrawable(btDeviceItem
                        .getContext()
                        .getDrawable(drawableResource));

        return btDeviceItem;
    }
}
