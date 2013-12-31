package com.swall.tra;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.swall.tra.model.AccountInfo;
import com.swall.tra.LoginActivity;
import com.swall.tra.MainActivity2;
import com.swall.tra2.LoginActivity2;

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
        startActivity(new Intent(SplashActivity.this, MainActivity2.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    private void gotoLogin() {
        startActivity(new Intent(SplashActivity.this, LoginActivity2.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}