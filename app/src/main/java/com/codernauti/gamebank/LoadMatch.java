package com.codernauti.gamebank;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.codernauti.gamebank.database.Match;

import io.realm.Realm;

public class LoadMatch extends AppCompatActivity {

    private LoadMatchAdapter mMatchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_match);

        this.mMatchAdapter = new LoadMatchAdapter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMatchAdapter.addAll(Realm.getDefaultInstance().where(Match.class).findAll());
    }
}
