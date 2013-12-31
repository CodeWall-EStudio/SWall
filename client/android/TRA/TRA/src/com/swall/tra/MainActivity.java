package com.swall.tra;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TabHost;
import com.swall.tra.network.ServiceManager;

/**
 * Created by ippan on 13-12-24.
 */
public class MainActivity extends BaseActivity  {
    private TabHost mTabHost;
    private LocalActivityManager mLocalActivityManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(android.os.Build.VERSION.SDK_INT >= 11){
            getWindow().setFlags(0x01000000, 0x01000000);//开启硬件加速
        }

        setContentView(R.layout.main);
        mLocalActivityManager = new LocalActivityManager(this,false);
        mLocalActivityManager.dispatchCreate(savedInstanceState);

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(mLocalActivityManager);

        mTabHost.addTab(mTabHost.newTabSpec("cur")
                .setIndicator(getString(R.string.tab_current_activities))
                .setContent(new Intent(this, CurrentTRAActivity.class)));

        mTabHost.addTab(mTabHost.newTabSpec("expired")
                .setIndicator(getString(R.string.tab_expired_activities))
                .setContent(new Intent(this, ExpiredActivitiesActivity.class)));

        mTabHost.addTab(mTabHost.newTabSpec("setting")
                .setIndicator(getString(R.string.tab_settings))
                .setContent(new Intent(this, SettingActivity.class)));

    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocalActivityManager.dispatchResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocalActivityManager.dispatchPause(isFinishing());
    }
}