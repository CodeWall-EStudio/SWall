package com.example.SWall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import com.example.SWall.R;
import com.example.SWall.services.ActionListener;
import com.example.SWall.services.ServiceManager;


public class SplashActivity extends BaseActivity {

    private static final int DELAY_TIME = 500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (!isFinishing()) {

                    boolean actionStarted = mApp.doAction(ServiceManager.Constants.ACTION_AUTO_LOGIN,null,new ActionListener(SplashActivity.this){

                        @Override
                        public void onReceive(int action, Bundle data) {
                            Log.i(TAG, "auto login");
                            if(data != null && data.containsKey(ServiceManager.Constants.KEY_USER_NAME)){
                                startActivity(new Intent(SplashActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }else{
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }
                            finish();
                        }
                    });
                    if(!actionStarted){
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                }
            }
        }, DELAY_TIME);
    }
}
