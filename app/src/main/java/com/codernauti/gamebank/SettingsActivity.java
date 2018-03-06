package com.codernauti.gamebank;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.settings_toolbar)
    Toolbar toolbar;

    @BindView(R.id.settings_nickname_input)
    TextInputEditText mNicknameInput;

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


    @OnClick(R.id.settings_save_btn)
    public void saveInputs() {
        String nickname = mNicknameInput.getText().toString();

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(PrefKey.NICKNAME, nickname)
                .apply();
    }
}
