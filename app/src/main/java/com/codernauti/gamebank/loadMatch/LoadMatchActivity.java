package com.codernauti.gamebank.loadMatch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;


import com.codernauti.gamebank.MatchManager;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.database.Match;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    private MatchManager mMatchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_match);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mMatchManager = new MatchManager(this);
        mMatchAdapter = new LoadMatchAdapter(this);

        // load files
        mMatchAdapter.addAll(mMatchManager.getSavedMatches());

        mMatchList.setAdapter(mMatchAdapter);
    }


    @OnItemClick(R.id.match_list)
    void onMatchClicked(final int i) {

        //Match selected = (Match) mMatchList.getItemAtPosition(i);

        //Log.d(TAG, "Selected match: " + selected.getMatchName());
        Log.e(TAG, "Loading match not implemented yet!");
    }
}
