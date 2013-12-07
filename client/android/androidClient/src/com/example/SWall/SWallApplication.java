package com.example.SWall;

import android.app.Application;
import android.util.Log;
import com.umeng.analytics.MobclickAgent;

/**
 * Author: iptton
 * Date: 13-12-7
 * Time: 下午9:38
 */
public class SWallApplication extends Application{
    private static final String TAG = "SWallApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "App::onCreate");
        MobclickAgent.setDebugMode(true);

    }
}
