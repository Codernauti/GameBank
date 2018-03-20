package com.codernauti.gamebank;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;


import com.codernauti.gamebank.database.Match;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class LoadMatchActivity extends AppCompatActivity {

    private static final String TAG = "LoadMatchActivity";


    @BindView(R.id.load_match_activity_toolbar)
    Toolbar toolbar;

    @BindView(R.id.match_list)
    ListView mMatchList;


    private LoadMatchAdapter mMatchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_match);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        this.mMatchAdapter = new LoadMatchAdapter(this);
        mMatchList.setAdapter(mMatchAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
        A little note right there: there is no need to implement a callback that triggers when a new
        match is saved, since this view is recreated every time and the 'res' value is hence
        recalculated accordingly to the db.
         */
        RealmResults<Match> res = Realm.getDefaultInstance().where(Match.class).findAll();
        mMatchAdapter.addAll(res); // TODO reverse res to improve user experience
    }

    @OnItemClick(R.id.match_list)
    void onMatchClicked(final int i) {

        Match selected = (Match) mMatchList.getItemAtPosition(i);

        Log.d(TAG, "Selected match: " + selected.getMatchName());
        Log.e(TAG, "Loading match not implemented yet!");
    }
}
