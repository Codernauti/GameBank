package com.codernauti.gamebank.game;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.codernauti.gamebank.R;

public class DashboardPagerAdapter extends FragmentPagerAdapter {

    private final static int NUM_TAB = 2;

    final static int BANK_TAB = 0;
    final static int TRANS_TAB = 1;

    // For getString From Resource
    private final Context mContext;

    DashboardPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case BANK_TAB:
                return new BankFragment();
            case TRANS_TAB:
                return new TransactionsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return NUM_TAB;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case BANK_TAB:
                return mContext.getString(R.string.bank_tab_title);
            case TRANS_TAB:
                return mContext.getString(R.string.trans_tab_title);
        }
        return null;
    }
}