package com.codewalle.tra;

import android.accounts.Account;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.codewalle.tra.Model.AccountInfo;
import com.codewalle.tra.Model.TRAApp;
import com.codewalle.tra.utils.TRAResponseParser;
import com.codewalle.tra.utils.Constants;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.future.ResponseFuture;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by xiangzhipan on 14-9-29.
 */
public class TRAApplication extends Application implements TRAApp{



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



    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    // ############ 基本接口END  ####################


    private static TRAApplication app;

    public int bgColor = 0xffffffff;

    public static TRAApplication getApplication() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        Bitmap bitmap = ((BitmapDrawable)(getResources().getDrawable(R.drawable.login_top_image_normal))).getBitmap();
        bgColor = 0xffffffff;
        try{
            bgColor = bitmap.getPixel(0,0);

            // 如果右上角全透明，则设为白色
            if(0 == Color.alpha(bgColor)){
                bgColor = 0xffffffff;
            }
        }catch (IllegalArgumentException e){

        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        app = null;
    }

    @Override
    public AccountInfo getCachedAccount() {
        return null;
    }

    @Override
    public void cacheAccount(AccountInfo account) {
    }

    @Override
    public Future<JSONObject> doJSONRequest(Constants requestType,
                                    AsyncHttpRequest request,
                                    AsyncHttpClient.JSONObjectCallback callback) {
        Future returnCallback = null;

        AsyncHttpClient client = AsyncHttpClient.getDefaultInstance();
        returnCallback = client.executeJSONObject(request, callback);


        return returnCallback;
    }

    @Override
    public boolean isAutoLogin() {
        return false;
    }


    @Override
    public Future doLogin(Context context, String username, String password, FutureCallback callback) {
        return Ion.with(context)
            .load(Constants.VALUES.LOGIN_METHOD,Constants.getLoginUrl(username,password))
            .proxy("10.66.93.34",8889)
            .setBodyParameter("name", username)
            .setBodyParameter("pwd",password)
            .setBodyParameter("json","true")
            .setBodyParameter("loginName",username)
            .setBodyParameter("password",password)
            .asString()

            .setCallback(callback);
    }

    @Override
    public int getBgColor() {
        return bgColor;
    }

}
