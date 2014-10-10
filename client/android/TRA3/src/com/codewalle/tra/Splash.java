package com.codewalle.tra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.codewalle.tra.utils.Utils;
import org.androidannotations.annotations.EActivity;

@EActivity
public class Splash extends BaseActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!Utils.hasSDCard()){
            noticeNoSDCardAndQuit();
            return;
        }
        if(hasAutoLogin()){
            gotoMain();
        }else{
            gotoLogin();
        }

    }

    private boolean hasAutoLogin(){
        return app.isAutoLogin();
    }

    private void noticeNoSDCardAndQuit() {
        if(!Utils.hasSDCard()){
            Toast.makeText(this, "无存储卡", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void gotoMain() {
        startActivity(new Intent(Splash.this, MainActivity_.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    private void gotoLogin() {
        startActivity(new Intent(Splash.this, LoginActivity_.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("autoLogin", true));
        finish();
    }
}
