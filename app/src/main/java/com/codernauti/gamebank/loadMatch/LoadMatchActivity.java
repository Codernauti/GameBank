package com.codernauti.gamebank.loadMatch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.codernauti.gamebank.DatabaseMatchManager;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.pairing.CreateMatchActivity;

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

        mMatchManager = new DatabaseMatchManager(getFilesDir());
        mMatchAdapter = new LoadMatchAdapter(this);

        // load files
        mMatchAdapter.addAll(mMatchManager.getSavedMatches());

        mMatchList.setAdapter(mMatchAdapter);
        registerForContextMenu(mMatchList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.load_match_options, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)
                item.getMenuInfo();

        switch(item.getItemId()){
            case R.id.load_match_opt:
                loadSavedMatch(info.position);
                break;
            case R.id.delete_match_opt:
                // TODO: create a dialog that ask to user if he is sure
                deleteSavedMatch(info.position);
                break;
        }

        return true;
    }

    @OnItemClick(R.id.match_list)
    void loadSavedMatch(final int i) {

        DatabaseFile savedMatchSelected = mMatchAdapter.getItem(i);

        Realm.setDefaultConfiguration(savedMatchSelected.getDbConficuration());

        Intent loadMatch = new Intent(this, CreateMatchActivity.class);
        loadMatch.putExtra(CreateMatchActivity.LOAD_MATCH, true);
        startActivity(loadMatch);
    }

    private void deleteSavedMatch(final int position) {

        DatabaseFile savedMatchSelected = mMatchAdapter.getItem(position);

        boolean isDeleted = savedMatchSelected.deleteFiles(getFilesDir());

        if (isDeleted) {
            Toast.makeText(this, "Deleted succesfully", Toast.LENGTH_SHORT).show();
            mMatchAdapter.remove(savedMatchSelected);
        } else {
            Toast.makeText(this, "Error tring to delete saved match", Toast.LENGTH_SHORT).show();
        }
    }


}
