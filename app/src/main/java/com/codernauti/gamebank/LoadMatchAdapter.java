package com.codernauti.gamebank;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codernauti.gamebank.database.Match;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by dpolonio on 13/03/18.
 */

public class LoadMatchAdapter extends ArrayAdapter<Match> {

    public LoadMatchAdapter(@NonNull Context context) {
        super(context, R.layout.match_list_row);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Match selectedMatch = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.member_list_row, parent, false);
        }

        Calendar tmpCalendar = selectedMatch.getMatchStarted();

        ((TextView)convertView.findViewById(R.id.match_date))
                .setText(tmpCalendar.get(Calendar.DATE) + "/" +
                        tmpCalendar.get(Calendar.MONTH) + "/" +
                        tmpCalendar.get(Calendar.YEAR));

        ((TextView)convertView.findViewById(R.id.match_name))
                .setText(selectedMatch.getMatchName());

        ((TextView)convertView.findViewById(R.id.match_player_number))
                .setText(selectedMatch.getPlayerList().size());

        return convertView;

    }
}
