package com.codewalle.tra;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.codewalle.tra.Model.TRAApp;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateStatus;
import org.androidannotations.annotations.EActivity;

/**
 * Created by xiangzhipan on 14-10-5.
 */

public class BaseFragmentActivity extends SherlockFragmentActivity{
    protected String TAG;
    protected TRAApplication app;
    protected boolean mBackConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = TRAApplication.getApplication();
        TAG = getClass().getName();


        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setCustomView(R.layout.title_bar);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setIcon(R.drawable.icon);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(app.getBgColor()));
        }



        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setDialogListener(new UmengDialogButtonListener() {

            @Override
            public void onClick(int status) {
                switch (status) {
                    case UpdateStatus.Update:

                        break;
                    case UpdateStatus.Ignore:

                        break;
                    case UpdateStatus.NotNow:

                        break;
                }
            }
        });


    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
/*
        if(mBackConfirm && keyCode == KeyEvent.KEYCODE_BACK){
            mHandler.sendEmptyMessage(MESSAGE_CONFIRM_QUIT);
            return true;
        }
*/
        return super.onKeyDown(keyCode, event);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
