package com.codernauti.gamebank.loadMatch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;


import com.codernauti.gamebank.DatabaseMatchManager;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.pairing.server.CreateMatchActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import io.realm.Realm;

public class LoadMatchActivity extends AppCompatActivity {

    private static final String TAG = "LoadMatchActivity";


    @BindView(R.id.load_match_activity_toolbar)
    Toolbar toolbar;

    @BindView(R.id.match_list)
    ListView mMatchList;


    private LoadMatchAdapter mMatchAdapter;
    private DatabaseMatchManager mMatchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_match);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mMatchManager = new DatabaseMatchManager(this);
        mMatchAdapter = new LoadMatchAdapter(this);

        // load files
        mMatchAdapter.addAll(mMatchManager.getSavedMatches());

        mMatchList.setAdapter(mMatchAdapter);
    }


    @OnItemClick(R.id.match_list)
    void onMatchClicked(final int i) {

        DatabaseFile saving = mMatchAdapter.getItem(i);

        Realm.setDefaultConfiguration(saving.getDbConficuration());

        Intent loadMatch = new Intent(this, CreateMatchActivity.class);
        loadMatch.putExtra(CreateMatchActivity.LOAD_MATCH, true);
        startActivity(loadMatch);
    }
}
