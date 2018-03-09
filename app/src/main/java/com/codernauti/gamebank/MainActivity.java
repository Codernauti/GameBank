package com.codernauti.gamebank;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.codernauti.gamebank.bluetooth.BTStateChange;
import com.codernauti.gamebank.pairing.client.LobbyActivity;
import com.luolc.emojirain.EmojiRainLayout;

import java.io.File;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_RW_EXTERNAL_STORAGE = 10;

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

        requestPermission();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RW_EXTERNAL_STORAGE) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // FIXME: debug code
                /*String filename = "pictures/m_6.jpg";
                String path = Environment.getExternalStorageDirectory() + "/" + filename;
                File pictureFile = new File(path);

                Bitmap bitmap = BitmapFactory.decodeFile(path);
                ((ImageView) findViewById(R.id.main_app_logo)).setImageBitmap(bitmap);*/

            } else {

                Log.d(TAG, "User doesn't accept permissions");
                requestPermission();
               /* mPermissionDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_coarse_permission_title)
                        .setMessage(R.string.dialog_coarse_permission_description)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                mPermissionDialog.show();*/
            }

        }

    }

    // Clicks callbacks

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

    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            final String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };


            if (needPermissions(this, permissions)) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RW_EXTERNAL_STORAGE);
            }
        }
    }

    private static boolean needPermissions(@NonNull Context context,
                                          @NonNull String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }
}
