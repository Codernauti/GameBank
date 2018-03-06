package com.codernauti.gamebank;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.EditTextActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

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
    }

    @OnClick(R.id.change_name_button)
    public void onChangeNameClickedButton() {

        Intent intent = new Intent(this, EditTextActivity.class);
        intent.putExtra(EditTextActivity.TOOLBAR_TITLE, "Change player name");
        intent.putExtra(EditTextActivity.EDIT_FIELD_NAME, "Player name");
        intent.putExtra(EditTextActivity.DESCRIPTION, "Bla bla bla bla");

        startActivity(intent);
    }
}
