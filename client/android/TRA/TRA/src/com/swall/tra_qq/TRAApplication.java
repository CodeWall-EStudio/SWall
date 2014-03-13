package com.swall.tra_qq;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.swall.tra_qq.model.AccountInfo;
import com.swall.tra_qq.network.*;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.auth.QQToken;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static final String QQ_APP_ID = "100548719";//hord
//    public static final String QQ_APP_ID = "101031164";// oscar
    //"";
    private Tencent mTencent;


    private ServiceManager mServiceManager;
    private LoginService mLoginService;
    private DownloadService mDownloadService;
    private UploadService mUploadService;
    private static TRAApplication sInstance;
    private QQAuth mQAuth;


    public ActionListener listener = new ActionListener(null) {
        @Override
        public void onReceive(int action, Bundle data) {
            switch (action){
                case ServiceManager.Constants.ACTION_LOGIN:
                case ServiceManager.Constants.ACTION_AUTO_LOGIN:
                    if(data == null || !data.getBoolean(ServiceManager.Constants.KEY_STATUS,false)){
                        // login fail
                        updateCurrentAccount(null);
                    }else{
                        AccountInfo accountInfo = new AccountInfo(
                                data.getString(KEY_USER_NAME),
                                data.getString(KEY_USER_PASSWORD),
                                data.getString(KEY_SHOW_NAME),
                                data.getString(KEY_ENCODE_KEY));
                        updateCurrentAccount(accountInfo);
                    }
                    break;
            }
        }
    };
    private DataService mDataService;

    public TRAApplication(){
        sInstance = this;
    }

    public QQAuth getQQAuth(){
        return mQAuth;
    }
    public Tencent getTencentContext(){
        return mTencent;
    }


    private boolean isRelogining = false;
    private Toast reloginingToast;

    public boolean doAction(final int action,final Bundle data,final ActionListener listener){
        if(mAccountInfo != null &&
                System.currentTimeMillis() - mAccountInfo.getTime() > ServiceManager.Constants.MAX_LOGIN_EXPIRED_TIME){
            Toast.makeText(getApplicationContext(),"登录失败，请重新登录",Toast.LENGTH_LONG).show();
            updateCurrentAccount(null);
            startActivity(new Intent(getApplicationContext(),LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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


    @Override
    public void onCreate() {
        super.onCreate();
        initServices();
        initVolley();
        initUIL();
        Context context = getApplicationContext();
        mQAuth = QQAuth.createInstance(QQ_APP_ID,context);
        mTencent = Tencent.createInstance(QQ_APP_ID, context);
        updateCurrentAccount(getCachedAccount());






    }

    private void initUIL() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())

                .build();

        com.nostra13.universalimageloader.core.ImageLoader.getInstance().init(config);
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
        mQAuth = null;
        mTencent = null;
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
            Map<String,String> data = getMappingData(CACHED_USER_DATA,new String[]{KEY_USER_NAME,KEY_USER_PASSWORD,KEY_SHOW_NAME,KEY_ENCODE_KEY,KEY_LOGIN_TIME});
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


            mQAuth.setOpenId(getApplicationContext(),name);
            mQAuth.setAccessToken(encodeKey,loginTime+"");

            if(TextUtils.isEmpty(name) || TextUtils.isEmpty(encodeKey) || loginTime == 0l || !mQAuth.isSessionValid()){
                return new AccountInfo("","","","");
            }
            mAccountInfo = new AccountInfo(name,password,showName,encodeKey);
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
            account = new AccountInfo("","","","");
            mAccountInfo = null;
            currentTime = 0;
        }else{
            mAccountInfo = account;
            if(account.getTime() == 0){
                mAccountInfo.setTime(currentTime);
            }
        }
        ActionService.setEncodeKey(account.encodeKey);
        saveMappingData(CACHED_USER_DATA,
                new String[]{KEY_USER_NAME,KEY_USER_PASSWORD,KEY_AUTO_LOGIN,KEY_SHOW_NAME,KEY_ENCODE_KEY,KEY_LOGIN_TIME},
                new String[]{account.userName,account.password,autoLogin?"true":"false",account.showName,account.encodeKey,String.valueOf(account.getTime())}
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

    public void doAction(final int action, Object maybeNull, final ActionListener listener, final LoginActivity activity) {
        switch(action){
            case ServiceManager.Constants.ACTION_LOGIN_WITH_QQ:

                TRAApplication.getApp().getTencentContext().loginWithOEM(activity, "all", new IUiListener() {
                    @Override
                    public void onComplete(Object o) {
                        try {
                            JSONObject result = new JSONObject(o.toString());
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("result",true);

                            QQToken token = mQAuth.getQQToken();

                            AccountInfo accountInfo = new AccountInfo(
                                    token.getOpenId(),
                                    "",
                                    "QQ用户",
                                    token.getAccessToken());
                            accountInfo.setTime(token.getExpireTimeInSecond());
                            updateCurrentAccount(accountInfo);
                            listener.onReceive(action,bundle);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onReceive(action,null);
                            Toast.makeText(activity,"登录失败"+o.toString(),Toast.LENGTH_SHORT).show();
                            updateCurrentAccount(null);
                        }
                    }

                    @Override
                    public void onError(UiError uiError) {
                        listener.onReceive(action,null);
                        updateCurrentAccount(null);
                    }

                    @Override
                    public void onCancel() {
                        listener.onReceive(action,null);
                        updateCurrentAccount(null);
                    }
                }, QQ_APP_ID, QQ_APP_ID, "新媒体教研中心");
                break;
        }
    }

    // ############ 基本接口END  ####################

}