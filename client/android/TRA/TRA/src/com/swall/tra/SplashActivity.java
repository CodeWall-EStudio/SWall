package com.swall.tra;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.swall.tra.model.AccountInfo;

/**
 * Created by ippan on 13-12-24.
 */
public class SplashActivity extends BaseActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountInfo info = app.getCachedAccount();
        if(info == null || TextUtils.isEmpty(info.userName)){
            gotoLogin();
        }else{
            gotoMain();
        }
        finish();
    }

    private void gotoMain() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    private void gotoLogin() {
        startActivity(new Intent(SplashActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}