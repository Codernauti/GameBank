package com.codernauti.gamebank.loadMatch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codernauti.gamebank.R;
import com.codernauti.gamebank.database.Match;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by dpolonio on 13/03/18.
 */

public class LoadMatchAdapter extends ArrayAdapter<DatabaseFile> {

    LoadMatchAdapter(@NonNull Context context) {
        super(context, R.layout.match_list_row);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DatabaseFile selectedDatabase = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.match_list_row, parent, false);
        }

        /*((TextView)convertView.findViewById(R.id.match_date))
                .setText(selectedDatabase.getMatchStarted());*/

        ((TextView)convertView.findViewById(R.id.match_name))
                .setText(String.valueOf(selectedDatabase.getDbName()));

        /*((TextView)convertView.findViewById(R.id.match_player_number))
                .setText(String.valueOf(getContext().getResources().getString(R.string.players) + ": " + selectedDatabase.getPlayerList().size()));*/

        return convertView;

    }
}
