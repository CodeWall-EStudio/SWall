package com.swall.tra;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.swall.tra.utils.Utils;

/**
 * Created by pxz on 13-12-17.
 */
public class SettingActivity extends SherlockPreferenceActivity implements Preference.OnPreferenceClickListener {
    public static final String PRE_NAME = "global_settings";


    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setCustomView(R.layout.title_bar);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setIcon(R.drawable.icon);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setBackgroundDrawable(getResources().getDrawable((R.color.bg)));
        }

        getPreferenceManager().setSharedPreferencesName(PRE_NAME);
        getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
        addPreferencesFromResource(R.xml.preferences);


        Preference clearCachePref = findPreference("clear_cache");
        clearCachePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(SettingActivity.this,"清除中...",Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.clearFileCache();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SettingActivity.this, "清除完毕", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
                return true;
            }
        });


        final ListPreference saveImageTargetListPreference = (ListPreference)findPreference("save_images_target");
        saveImageTargetListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference,final Object newValue) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateSaveTarget(newValue);
                    }
                });
                return true;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        updateSummaries();
    }

    private void updateSummaries() {

        updateAccount();
        updateSaveTarget(null);
    }

    private void updateSaveTarget(Object newValue) {
        if(isFinishing())return;
        ListPreference saveImageTargetListPreference = (ListPreference)findPreference("save_images_target");
        if(Utils.has2SDCard()){
            saveImageTargetListPreference.setSummary(saveImageTargetListPreference.getEntry());
        }else{
            if(newValue != null && ((String)newValue).equals("2") ) {
                Toast.makeText(this,"本机无第二张sdcard",Toast.LENGTH_LONG).show();
                saveImageTargetListPreference.setValueIndex(0);
            }
            saveImageTargetListPreference.setSummary(saveImageTargetListPreference.getEntry());
        }
    }

    private void updateAccount() {

        PreferenceScreen account = (PreferenceScreen)findPreference("account");
        account.setSummary("当前登录帐号:"+ TRAApplication.getApp().getCachedAccount().showName);
        account.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if(key.equals("account")){
            gotoLogin();
            return true;
        }
        return false;
    }


    private void gotoLogin() {
        TRAApplication.getApp().updateCurrentAccount(null);
        Intent intent = new Intent(this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
