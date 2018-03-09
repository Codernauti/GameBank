package com.codernauti.gamebank.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.codernauti.gamebank.R;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by dpolonio on 09/03/18.
 */

public class NicknameGenerator {

    private final static String TAG = "NicknameGenerator";

    private final Random r;
    private final List<String> adjectives;
    private final List<String> names;

    public NicknameGenerator(@NonNull Context context) {
        r = new Random();
        adjectives = Arrays.asList(context.getResources().getStringArray(R.array.adjectives));
        names = Arrays.asList(context.getResources().getStringArray(R.array.names));
    }

    @NonNull
    public String getRandomName() {
        StringBuilder res = new StringBuilder();

        res
                .append(adjectives.get(r.nextInt(adjectives.size() - 1)))
                .append(names.get(r.nextInt(names.size() - 1)));

        return res.toString();
    }

    @NonNull
    public String getRandomNameWithNumber() {

        StringBuilder res = new StringBuilder();

        res
                .append(getRandomName())
                .append(r.nextInt(9999));

        return res.toString();
    }

    @NonNull
    public String getRandomNameWithoutPreferences() {

        if (r.nextInt() % 2 == 0) {
            return getRandomName();
        } else {
            return getRandomNameWithNumber();
        }
    }
}