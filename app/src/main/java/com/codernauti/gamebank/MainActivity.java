package com.codernauti.gamebank;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.codernauti.gamebank.pairing.client.LobbyActivity;
import com.luolc.emojirain.EmojiRainLayout;

import java.util.Random;

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

        Log.d(TAG, "Getting the emojis...");
        Drawable a = getDrawable(R.drawable.banknote_with_dollar_sign);

        Log.d(TAG, a.toString());

        mEmojiRainLayout.setPer(3);
        mEmojiRainLayout.setDuration(Integer.MAX_VALUE);

    }

    @Override
    public void onResume() {
        super.onResume();

        Drawable[] listOfEmojis = {
                getDrawable(R.drawable.banknote_with_dollar_sign),
                getDrawable(R.drawable.banknote_with_euro_sign),
                getDrawable(R.drawable.banknote_with_yen_sign),
                getDrawable(R.drawable.banknote_with_pound_sign),
                getDrawable(R.drawable.money_bag),
                getDrawable(R.drawable.money_with_wings),
                getDrawable(R.drawable.bank),
                getDrawable(R.drawable.credit_card),
                getDrawable(R.drawable.gem_stone)
        };

        Random r = new Random();

        for (int i = 0; i < 3; i ++ ) {

            mEmojiRainLayout.addEmoji(listOfEmojis[r.nextInt(listOfEmojis.length-1)]);
        }

        mEmojiRainLayout.startDropping();
    }

    @Override
    public void onPause() {
        super.onPause();
        mEmojiRainLayout.stopDropping();
        mEmojiRainLayout.clearEmojis();
        mEmojiRainLayout.clearAnimation();
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
