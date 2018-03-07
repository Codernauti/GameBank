package com.codernauti.gamebank;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.EditTextActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private static final int NICKNAME_RESULT = 10;

    @BindView(R.id.settings_toolbar)
    Toolbar toolbar;

  
    @BindView(R.id.settings_save_btn)
    ImageButton mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NICKNAME_RESULT && resultCode == RESULT_OK) {
            Log.d(TAG, "From EditTextActivity: " + data.getExtras().get(EditTextActivity.TEXT_RESULT));

        } else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "Result Canceled");
        }
    }

    @OnClick(R.id.change_name_button)
    public void onChangeNameClickedButton() {

        Intent intent = new Intent(this, EditTextActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(EditTextActivity.TOOLBAR_TITLE, "Change player name");
        bundle.putString(EditTextActivity.EDIT_FIELD_NAME, "Player name");
        bundle.putString(EditTextActivity.DESCRIPTION, "Bla bla bla bla");

        intent.putExtras(bundle);

        //startActivity(intent);
        startActivityForResult(intent, NICKNAME_RESULT);
    }

}
