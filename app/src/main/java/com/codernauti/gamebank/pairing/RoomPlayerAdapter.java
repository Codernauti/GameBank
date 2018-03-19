package com.codernauti.gamebank.pairing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.database.Player;

import java.io.File;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by Eduard on 28-Feb-18.
 */

public class RoomPlayerAdapter extends RealmBaseAdapter<Player> {

    private static final String TAG = "RoomPlayerAdapter";

    public RoomPlayerAdapter(RealmResults<Player> data) {
        super(data);
    }

    @NonNull
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        RoomPlayerVH roomPlayerVH;
        Player client = getItem(i);

        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.member_list_row, viewGroup, false);

            roomPlayerVH = new RoomPlayerVH();
            roomPlayerVH.mProfileName = view.findViewById(R.id.member_name);
            roomPlayerVH.mProfilePicture = view.findViewById(R.id.member_icon);
            roomPlayerVH.mProfileReadiness = view.findViewById(R.id.member_ready);
            view.setTag(roomPlayerVH);

        } else {
            roomPlayerVH = (RoomPlayerVH) view.getTag();
        }

        roomPlayerVH.mProfileName.setText(client.getUsername());

        if (client.getPictureNameFile() != null) {
            File file = new File(context.getFilesDir(), client.getPictureNameFile());

            if (file.exists()) {
                Glide.with(viewGroup.getContext())
                        .load(file)
                        .apply(new RequestOptions()
                                // TODO: remove, this is useful for DEBUG
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .into(roomPlayerVH.mProfilePicture);
            } else {
                roomPlayerVH.mProfilePicture.setImageBitmap(null);
                Log.w(TAG, file.getAbsolutePath() + " doesn't exist");
            }
        } else {
            roomPlayerVH.mProfilePicture.setImageBitmap(null);
        }

        if (client.isReady()) {
            roomPlayerVH.mProfileReadiness.setText(context.getString(R.string.status_member_ready));
        } else {
            roomPlayerVH.mProfileReadiness.setText(context.getString(R.string.status_member_not_ready));
        }

        return view;
    }

    private class RoomPlayerVH {
        ImageView mProfilePicture;
        TextView mProfileName;
        TextView mProfileReadiness;
    }
}
