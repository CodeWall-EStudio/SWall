package com.swall.tra;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.swall.tra.model.AccountInfo;
import com.swall.tra.network.*;
import com.swall.tra.utils.Utils;
import com.swall.tra_demo.R;
import com.umeng.analytics.MobclickAgent;

import java.util.*;

/**
 *
 * 先不使用数据库而是全使用 SharedReferences
 *
 * Created by ippan on 13-12-24.
 */
public class TRAApplication extends Application {

    public static final String LIST_TYPE_AVAILABLE = "available_list";
    public static final String LIST_TYPE_EXPIRED = "expired_list";
    public static final String LIST_TYPE_RESOURCES = "resource_list";
    public static final String KEY_SP_CONTENT = "content";

    public static final String CACHED_USER_DATA = "user_data";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_PASSWORD = "user_pwd";
    private static final String KEY_AUTO_LOGIN = "auto_login";
    public static final String KEY_SHOW_NAME = "show_name";
    public static final String KEY_ENCODE_KEY = "encode_key";
    public static final String KEY_LOGIN_TIME = "login_time";
    public static final String KEY_SESION_ID = "session";
    private static Context mContext;


    private ServiceManager mServiceManager;
    private LoginService mLoginService;
    private DownloadService mDownloadService;
    private UploadService mUploadService;
    private static TRAApplication sInstance;



    public ActionListener listener = new ActionListener(null) {
        @Override
        public void onReceive(int action, Bundle data) {
            switch (action){
                case ServiceManager.Constants.ACTION_LOGIN:
                case ServiceManager.Constants.ACTION_AUTO_LOGIN:
                    if(data == null || !data.getBoolean(ServiceManager.Constants.KEY_STATUS,false)){
                        // login fail
//                        updateCurrentAccount(null);
                    }else{
                        AccountInfo accountInfo = new AccountInfo(
                                data.getString(KEY_USER_NAME),
                                data.getString(KEY_USER_PASSWORD),
                                data.getString(KEY_SHOW_NAME),
                                data.getString(KEY_ENCODE_KEY),
                                data.getString(KEY_SESION_ID));
                        updateCurrentAccount(accountInfo);
                    }
                    break;
            }
        }
    };
    private DataService mDataService;
    private int mBgColor = -1;

    public TRAApplication(){
        sInstance = this;
        mContext = this;
    }


    private boolean isRelogining = false;
    private Toast reloginingToast;

    public boolean doAction(final int action,final Bundle data,final ActionListener listener){

        if(!isNetworkConnected()){
            MobclickAgent.onEvent(getApplicationContext(),"network_status_fail");
            Toast.makeText(this,"网络未连接，请检查网络",Toast.LENGTH_LONG);
//            return false; 先不做处理，因为Volley有重试逻辑
        }

        if(mAccountInfo != null &&
                mAccountInfo.password != "" &&
                System.currentTimeMillis() - mAccountInfo.getTime() > ServiceManager.Constants.MAX_LOGIN_EXPIRED_TIME){
            Toast.makeText(getApplicationContext(),"离上次登录时间太长，请重新登录",Toast.LENGTH_LONG).show();
            MobclickAgent.onEvent(getApplicationContext(),"needRelogin");
            updateCurrentAccount(mAccountInfo);
            startActivity(new Intent(getApplicationContext(),LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    );
            /*
            if(reloginingToast != null){
                reloginingToast.cancel();
            }else{
                reloginingToast = Toast.makeText(getApplicationContext(),"登录状态过期，正在重新获取登录信息",Toast.LENGTH_LONG);
            }
            reloginingToast.show();
            if(!isRelogining){
                isRelogining = true;
                Bundle mLoginData = new Bundle();
                mLoginData.putString(ServiceManager.Constants.KEY_USER_NAME, mAccountInfo.userName);
                mLoginData.putString(ServiceManager.Constants.KEY_PASSWORD, mAccountInfo.password);
                mServiceManager.doAction(ServiceManager.Constants.ACTION_LOGIN,mLoginData,new ActionListener(null) {
                    @Override
                    public void onReceive(int action2, Bundle data2) {
                        isRelogining = false;
                        if(data2 != null && data2.getBoolean(ServiceManager.Constants.KEY_STATUS,false)){
                            reloginingToast.cancel();
                            Toast.makeText(getApplicationContext(),"登录失败，请重新登录",Toast.LENGTH_LONG).show();
                            updateCurrentAccount(null);
                            startActivity(new Intent(getApplicationContext(),LoginActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }else{
                            mServiceManager.doAction(action,data,listener);
                        }
                    }
                });
            }
            */
            return false;
        }else{
            return mServiceManager.doAction(action,data,listener);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        initServices();
        initVolley();
//        initUIL();
        updateCurrentAccount(getCachedAccount());
    }


    private void initVolley() {
        MyVolley.init(getApplicationContext());
    }


    private void initServices() {
        mServiceManager = new ServiceManager(this);

        Context context = getApplicationContext();
        mLoginService = new LoginService(context,mServiceManager);
        mDownloadService = new DownloadService(context,mServiceManager);
        mUploadService = new UploadService(context,mServiceManager);
        mDataService = new DataService(context,mServiceManager);


        mServiceManager.registerServices(
                mLoginService,
                mDownloadService,
                mUploadService,
                mDataService
        );

        mServiceManager.addObserver(new int[]{ServiceManager.Constants.ACTION_AUTO_LOGIN, ServiceManager.Constants.ACTION_LOGIN},listener);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sInstance = null;
    }

    public void addObserver(int action, ActionListener listener) {
        mServiceManager.addObserver(action,listener);
    }

    public void removeObserver(int action, ActionListener listener) {
        mServiceManager.removeObserver(action,listener);
    }
    public void removeObserver(ActionListener listener){
        mServiceManager.removeObserver(listener);
    }

    public static TRAApplication getApp(){
        return sInstance;
    }


    private AccountInfo mAccountInfo;
    // ###########
    public AccountInfo getCachedAccount(){
        if(mAccountInfo == null){
            Map<String,String> data = getMappingData(CACHED_USER_DATA,new String[]{KEY_USER_NAME,KEY_USER_PASSWORD,KEY_SHOW_NAME,KEY_ENCODE_KEY,KEY_LOGIN_TIME,KEY_SESION_ID});
            String name = data.get(KEY_USER_NAME);
            String password = data.get(KEY_USER_PASSWORD);
            String showName = data.get(KEY_SHOW_NAME);
            String encodeKey = data.get(KEY_ENCODE_KEY);
            long loginTime = 0l;
            try{
                loginTime = Long.parseLong(data.get(KEY_LOGIN_TIME));
            }catch(Exception e){
                // do nothing
            }
//            if(TextUtils.isEmpty(name) || TextUtils.isEmpty(password) || loginTime == 0l || TextUtils.isEmpty(data.get(KEY_SESION_ID))){
//                return new AccountInfo("","","","", "");
//            }
            mAccountInfo = new AccountInfo(name,password,showName,encodeKey, data.get(KEY_SESION_ID));
            mAccountInfo.setTime(loginTime);
        }
        return mAccountInfo;
    }

    public void updateCurrentAccount(AccountInfo account){
        updateCurrentAccount(account,true);
    }

    public void updateCurrentAccount(AccountInfo account,boolean autoLogin){
        long currentTime = System.currentTimeMillis();
        if(account == null){
            account = new AccountInfo("","","","", "");
            mAccountInfo = null;
            currentTime = 0;
        }else{
            mAccountInfo = account.clone();
            mAccountInfo.setTime(currentTime);
        }
        ActionService.setEncodeKey(account.encodeKey,account.sessionId);
        saveMappingData(CACHED_USER_DATA,
                new String[]{KEY_SESION_ID,KEY_USER_NAME,KEY_USER_PASSWORD,KEY_AUTO_LOGIN,KEY_SHOW_NAME,KEY_ENCODE_KEY,KEY_LOGIN_TIME},
                new String[]{account.sessionId,account.userName,account.password,autoLogin?"true":"false",account.showName,account.encodeKey,String.valueOf(currentTime)}
        );
    }

    public void changeEnv(int env) {
        ServiceManager.Constants.setEnv(env);
    }


    // ############ 基本接口，后续可换为 SQLite ####################

    private boolean saveMappingData(String cacheType, String[] keys, String[] valuse) {
        return saveMappingData(cacheType,  Arrays.asList(keys),  Arrays.asList(valuse));
    }

    private boolean saveMappingData(String cacheType, List<String> keys, List<String> values) {
        if(keys.size() != values.size()){
            throw new Error("key size not match with value size!");
        }
        SharedPreferences.Editor editor = getSharedPreferencesEditor(cacheType);
        for(int i=0;i<keys.size();++i){
            String key = keys.get(i);
            String value = values.get(i);
            editor.putString(key,value);
        }
        return editor.commit();
    }

    public Map<String,String> getMappingData(String cacheType,String[] keys){
        return getMappingData(cacheType, Arrays.asList(keys));
    }
    public Map<String,String> getMappingData(String cacheType,List<String> keys){
        if(keys.isEmpty()){
            return Collections.EMPTY_MAP;
        }

        Map<String, String> result = new HashMap<String, String>(keys.size());
        SharedPreferences sp = getSharedPreferences(cacheType);
        for(String key : keys){
            result.put(key, sp.getString(key, ""));
        }
        return result;
    }
    public String getCachedData(String cacheType,String key){
        return getCachedData(cacheType, key, "");
    }
    public String getCachedList(String listType){
        return getCachedData(listType, KEY_SP_CONTENT, "[]");
    }

    public String getCachedData(String cacheType,String key,String defaultValue){
        SharedPreferences sp = getSharedPreferences(cacheType);
        return sp.getString(key, defaultValue);
    }
    public boolean updateCachedData(String listType,String updatedStr){
        SharedPreferences.Editor editor = getSharedPreferencesEditor(listType);
        editor.putString(KEY_SP_CONTENT,updatedStr);
        return editor.commit();
    }

    public boolean removeCachedData(String cacheType){
        SharedPreferences.Editor editor = getSharedPreferencesEditor(cacheType);
        editor.clear();
        return editor.commit();
    }
    public boolean removeCachedData(String cacheType,String key){
        SharedPreferences.Editor editor = getSharedPreferencesEditor(cacheType);
        editor.remove(key);
        return editor.commit();
    }


    private SharedPreferences getSharedPreferences(String name){
        return getApplicationContext().getSharedPreferences(name, MODE_PRIVATE);
    }
    private SharedPreferences.Editor getSharedPreferencesEditor(String name){
        return getSharedPreferences(name).edit();
    }

    public static Context getContext() {
        return mContext;
    }

    public int getBgColor() {
        if(mBgColor == -1){
            mBgColor = Utils.getColorOfPic(0, 0, R.drawable.login_school_normal, this);
        }
        return mBgColor;
    }

    public void clearAccount(boolean leaveUserName) {
        if(leaveUserName){
            mAccountInfo.password = "";
            mAccountInfo.encodeKey = "";
            mAccountInfo.sessionId = "";
            updateCurrentAccount(mAccountInfo);
        }else{
            updateCurrentAccount(null);
        }
    }

    // ############ 基本接口END  ####################

}
