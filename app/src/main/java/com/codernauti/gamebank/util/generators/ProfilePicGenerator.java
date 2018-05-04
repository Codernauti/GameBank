package com.codernauti.gamebank.util.generators;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.codernauti.gamebank.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by davide on 10/03/18.
 */

public class ProfilePicGenerator implements RandomContentGenerator {

    private final ArrayList<Integer> mDefaultPictures;
    private final Random r;

    public ProfilePicGenerator() {

        this.mDefaultPictures = new ArrayList<>();
        this.r = new Random();


        //mDefaultPictures.add(R.drawable.default_profile_pic_1);
        mDefaultPictures.add(R.drawable.default_profile_pic_2);
        /*mDefaultPictures.add(R.drawable.default_profile_pic_3);
        mDefaultPictures.add(R.drawable.default_profile_cat_1);
        mDefaultPictures.add(R.drawable.default_profile_cat_2);
        mDefaultPictures.add(R.drawable.default_profile_cat_3);
        mDefaultPictures.add(R.drawable.default_profile_dog_1);
        mDefaultPictures.add(R.drawable.default_profile_dog_2);
        mDefaultPictures.add(R.drawable.default_profile_dog_3);*/
    }

    @NonNull
    @Override
    public Integer generateRandomContent() {

        return mDefaultPictures.get(0/*r.nextInt(mDefaultPictures.size() -1 )*/);
    }
}
