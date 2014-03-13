package com.swall.tra_qq.network;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.swall.tra_qq.TRAApplication;
import com.swall.tra_qq.utils.AccountDatabaseHelper;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.auth.QQToken;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pxz on 13-12-13.
 */
public abstract class ActionService {
    protected static Map<String, String> sRequestCookies = new HashMap<String, String>();
    public static Map<String, String> getRequestHeaders(){
//        if(sRequestCookies.isEmpty() || TextUtils.isEmpty(sEncodeKey)){
//            sRequestCookies.put(HttpHeaders.CACHE_CONTROL,"no-cache");
//            sRequestCookies.put("Cookie","skey="+sEncodeKey);
//        }
        if(sRequestCookies.isEmpty()){
            TRAApplication app = TRAApplication.getApp();
            QQAuth qqAuth = app.getQQAuth();
            QQToken token = qqAuth.getQQToken();
            sRequestCookies.put("Cookie","skey="+token.getAccessToken()+"; uid="+token.getOpenId());
        }
        return sRequestCookies;
    }

    public static String getUrlWithSKEY(String url) {
        URI uri = URI.create(url);
        if(url.indexOf("?") != -1){
            url += "?";
        }else{
            url += "&";
        }

        return url + "skey="+sEncodeKey;
    }
    public static void setEncodeKey(String encodeKey){
        sEncodeKey = encodeKey;
        if(!TextUtils.isEmpty(encodeKey)){
            sRequestCookies.clear();
        }
    }
    protected String TAG;
    protected static String sEncodeKey;
    protected WeakReference<Context> contextRef;
    protected WeakReference<ServiceManager> managerRef;
    public ActionService(Context context, ServiceManager manager){
        if(context == null || manager == null)throw new Error("ActionService should init with Context and ServiceManager");

        contextRef = new WeakReference<Context>(context);
        managerRef = new WeakReference<ServiceManager>(manager);
        TAG = this.getClass().getSimpleName();
    }
    protected void notifyListener(final int action, final Bundle data,final ActionListener localListener){
        if(localListener != null){
            localListener.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    localListener.onReceive(action,data);
                }
            });
        }
        notifyListener(action,data);
    }
    protected void notifyListenerError(final int action, final Exception error, final ActionListener localListener){

    }
    protected void notifyListener(final int action,final Bundle data){
        ServiceManager manager =managerRef.get();
        if(manager != null){
            manager.notifyListener(action, data);
        }
    }
    protected void notifyListener(final int action, final Bundle data,List<ActionListener> localListeners){
        if(localListeners != null){
            for(Object listenerObj:localListeners){
                if(listenerObj instanceof ActionListener){
                    final ActionListener listener = (ActionListener)listenerObj;
                    listener.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onReceive(action,data);
                    }
                });
                }
            }
        }
        notifyListener(action,data);
    }
    protected AccountDatabaseHelper getDBHelper(){
        ServiceManager manager =managerRef.get();
        return manager.getDBHelper();
    }
    public void doAction(final int action, final Bundle data, final ActionListener listener){
        // do nothing but call the callback?
        listener.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onReceive(action,null);
            }
        });
    }
    public abstract int[] getActionList();

}