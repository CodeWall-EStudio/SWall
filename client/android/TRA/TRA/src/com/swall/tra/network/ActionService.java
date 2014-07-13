package com.swall.tra.network;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.swall.tra.utils.AccountDatabaseHelper;
import com.swall.tra.utils.NetworkUtils;
import org.apache.http.HttpHeaders;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by pxz on 13-12-13.
 */
public abstract class ActionService {
    protected static Map<String, String> sRequestCookies = new HashMap<String, String>();

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static Map<String,String> sessionCookies = new HashMap<String, String>();
    public static String cookieString = "";

    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     * @param headers Response Headers.
     */
    public static final void saveCookies(Set<Pair<String, String>> headers) {
        // fetch all cookies and save
        for(Pair<String,String> entry : headers){
            if(entry.first.equalsIgnoreCase(SET_COOKIE_KEY)){
                String cookie = entry.second;
                if (cookie.length() > 0) {
                    String[] splitCookie = cookie.split(";");
                    if(splitCookie.length < 0)continue;
                    String[] splitSessionId = splitCookie[0].split("=");
                    if(splitSessionId.length == 0)continue;
                    String theKey = splitSessionId[0].trim();
                    String theValue = "";
                    if(splitSessionId.length > 1){
                        theValue = splitSessionId[1].trim();
                    }
                    sessionCookies.put(theKey,theValue);
                }
            }
            updateCookieString();
            Log.i(NetworkUtils.class.getCanonicalName(), entry.first + entry.second);
        }
    }

    private static void updateCookieString() {
//        StringBuilder builder = new StringBuilder();
//        for(String key:sessionCookies.keySet()){
//            builder.append(key);
//            builder.append("=");
//            builder.append(sessionCookies.get(key));
//            builder.append("; ");
//        }
//        cookieString = builder.toString();
        cookieString =" skey="+sEncodeKey+"; connect.sid="+sSessionId+";";
        sRequestCookies.put("Cookie",cookieString);
    }

    public static final void setCookie(String key,String value) {
        sessionCookies.put(key,value);
    }



    public static Map<String, String> getRequestHeaders(){
        if(sRequestCookies.isEmpty() || TextUtils.isEmpty(sEncodeKey)){
            sRequestCookies.put(HttpHeaders.CACHE_CONTROL,"no-cache");
            sRequestCookies.put("Cookie", cookieString);
        }
        return sRequestCookies;
    }

    public static String getUrlWithSKEY(String url) {
        URI uri = URI.create(url);
        if(url.indexOf('?') != -1){
            url += "?";
        }else{
            url += "&";
        }

        return url + "skey="+sEncodeKey;
    }
    public static void setEncodeKey(String encodeKey,String sessionId){
        sEncodeKey = encodeKey;
        sSessionId = sessionId;
        if(!TextUtils.isEmpty(encodeKey)){
            sRequestCookies.clear();
        }
        updateCookieString();
    }
    protected String TAG;
    protected static String sEncodeKey;
    protected static String sSessionId;
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
