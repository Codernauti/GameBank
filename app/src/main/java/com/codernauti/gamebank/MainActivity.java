package com.codernauti.gamebank;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.v7.widget.Toolbar;

import com.codernauti.gamebank.lobby.client.LobbyActivity;
import com.luolc.emojirain.EmojiRainLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.main_activity_toolbar)
    Toolbar toolbar;

    @BindView(R.id.group_emoji_container)
    EmojiRainLayout mEmojiRainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        mEmojiRainLayout.addEmoji(getDrawable(R.drawable.banknote_with_dollar_sign));
        mEmojiRainLayout.addEmoji(getDrawable(R.drawable.banknote_with_euro_sign));
        mEmojiRainLayout.addEmoji(getDrawable(R.drawable.banknote_with_yen_sign));
        mEmojiRainLayout.addEmoji(getDrawable(R.drawable.banknote_with_pound_sign));
        mEmojiRainLayout.addEmoji(getDrawable(R.drawable.money_bag));
        mEmojiRainLayout.addEmoji(getDrawable(R.drawable.money_mouth_face));
        mEmojiRainLayout.addEmoji(getDrawable(R.drawable.money_with_wings));
        mEmojiRainLayout.addEmoji(getDrawable(R.drawable.bank));
        mEmojiRainLayout.addEmoji(getDrawable(R.drawable.credit_card));
        mEmojiRainLayout.addEmoji(getDrawable(R.drawable.gem_stone));

        mEmojiRainLayout.setPer(3);
        mEmojiRainLayout.setDuration(Integer.MAX_VALUE);

    }

    @Override
    public void onResume() {
        super.onResume();
        mEmojiRainLayout.startDropping();
    }

    @Override
    public void onStop() {
        super.onStop();
        mEmojiRainLayout.stopDropping();
    }

    @OnClick(R.id.lobby_button)
    void onClickButton() {

        startActivity(new Intent(this, LobbyActivity.class));
    }

    @OnClick(R.id.load_match_button)
    void onLoadingMatchClickButton() {

        Log.e(TAG, "Missing implementation for onLoadingMatchClickButton");
    }

    @OnClick(R.id.settings_button)
    void onSettingsClickButton() {

        startActivity(new Intent(this, SettingsActivity.class));
    }
}
