package com.codernauti.gamebank;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.codernauti.gamebank.util.EditTextActivity;
import com.codernauti.gamebank.util.NicknameGenerator;
import com.codernauti.gamebank.util.PlayerProfile;
import com.codernauti.gamebank.util.PrefKey;
import com.codernauti.gamebank.util.SharePrefUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import pyxis.uzuki.live.naraeimagepicker.NaraeImagePicker;
import pyxis.uzuki.live.naraeimagepicker.impl.OnPickResultListener;
import pyxis.uzuki.live.naraeimagepicker.item.PickerSettingItem;
import pyxis.uzuki.live.naraeimagepicker.item.enumeration.ViewMode;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private static final int NICKNAME_RESULT = 10;

    @BindView(R.id.settings_toolbar)
    Toolbar toolbar;

    @BindView(R.id.current_username)
    TextView mCurrentUsername;

    @BindView(R.id.settings_image)
    CircleImageView mProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final String fileName = SharePrefUtil
                .getStringPreference(
                        this,
                        PrefKey.PROFILE_PICTURE);

        if (fileName.equals(SharePrefUtil.DEFAULT_STRING_VALUE)) {
            // Load standard profile pic
            Glide.with(this)
                    .load(R.drawable.default_profile_pic)
                    .into(mProfilePicture);
        } else {
            // Load user picture
            Glide.with(this)
                    .load(getFilesDir() + "/" + fileName)
                    .into(mProfilePicture);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        mCurrentUsername.setText(SharePrefUtil.getNicknamePreference(this));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NICKNAME_RESULT && resultCode == RESULT_OK) {

            String newNickname = data.getExtras().getString(EditTextActivity.TEXT_RESULT);
            Log.d(TAG, "From EditTextActivity: " + newNickname);

            SharePrefUtil.saveStringPreference(this, PrefKey.NICKNAME, newNickname);
            mCurrentUsername.setText(newNickname);

        } else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "Result Canceled");
        }
    }

    @OnClick(R.id.change_name_button)
    public void onChangeNameClickedButton() {

        Intent intent = new Intent(this, EditTextActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(EditTextActivity.TOOLBAR_TITLE, getString(R.string.username));
        bundle.putString(EditTextActivity.EDIT_FIELD_NAME, getString(R.string.edit_username));
        bundle.putString(EditTextActivity.DESCRIPTION, getString(R.string.set_username_description));

        String prevNickname = SharePrefUtil.getNicknamePreference(this);
        bundle.putString(EditTextActivity.PREVIOUS_VALUE, prevNickname);

        intent.putExtras(bundle);

        startActivityForResult(intent, NICKNAME_RESULT);
    }

    @OnClick(R.id.random_username_button)
    public void onRandomUsernameClickedButton() {

        String randomNick = new NicknameGenerator(this).getRandomNameWithoutPreferences();

        SharePrefUtil.saveStringPreference(
                this,
                PrefKey.NICKNAME,
                randomNick);

        mCurrentUsername.setText(randomNick);
    }

    @OnClick(R.id.settings_change_image_button)
    public void onChangeProfilePictureClickedButton() {

        final PickerSettingItem item = new PickerSettingItem();
        item.setPickLimit(1);
        item.setViewMode(ViewMode.FolderView);
        item.setEnableUpInParentView(true);

        NaraeImagePicker.instance.start(this, item, new OnPickResultListener() {
            @Override
            public void onSelect(int resultCode, @NotNull ArrayList<String> imageList) {
                if (resultCode == NaraeImagePicker.PICK_SUCCESS) {

                    final File previousFile = new File(SharePrefUtil
                            .getStringPreference(
                                    SettingsActivity.this,
                                    PrefKey.PROFILE_PICTURE));
                    final File pickedUpFile = new File(imageList.get(0));

                    if (!previousFile.getName().equals(pickedUpFile.getName())) {

                        Log.d(TAG, "Profile picture changed, updating it");

                        if (deleteFile(previousFile.getName())) {
                            Log.d(TAG, "Previous profile picture successfully deleted");
                        }

                        final String fileName = pickedUpFile.getName();

                        SharePrefUtil.saveStringPreference(
                                SettingsActivity.this,
                                PrefKey.PROFILE_PICTURE,
                                fileName);

                        try(final FileInputStream is = new FileInputStream(pickedUpFile);
                            final FileOutputStream os = openFileOutput(fileName, MODE_PRIVATE);
                        ) {

                            // Transfer bytes from in to out
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = is.read(buf)) > 0) {
                                os.write(buf, 0, len);
                            }

                            Log.d(TAG, "Copy completed");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Glide.with(SettingsActivity.this)
                                .load(getFilesDir() + "/" + fileName)
                                .into(mProfilePicture);
                    }


                } else {
                    Toast.makeText(SettingsActivity.this, "failed to pick image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
