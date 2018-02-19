package com.codernauti.gamebank.game;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.codernauti.gamebank.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    @BindView(R.id.dashboard_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout tabLayout;
    @BindView(R.id.dashboard_toolbar)
    Toolbar myToolbar;

    private DashboardPagerAdapter mAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);
        ButterKnife.bind(this);

        mContext = getApplicationContext();

        setSupportActionBar(myToolbar);

        mAdapter = new DashboardPagerAdapter(getSupportFragmentManager(), mContext);
        mViewPager.setAdapter(mAdapter);

        setupTopTabber();
    }

    private void setupTopTabber() {
        // Give the TabLayout the ViewPager
        tabLayout.setupWithViewPager(mViewPager);

        // Show HomePage first
        mViewPager.setCurrentItem(DashboardPagerAdapter.BANK_TAB);
    }
}
