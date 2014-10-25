package com.swall.tra.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.swall.tra.TRAApplication;
import com.swall.tra.model.AccountInfo;
import static com.swall.tra.network.ServiceManager.Constants;

import com.swall.tra.utils.JSONUtils;
import com.swall.tra.utils.NetworkUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pxz on 13-12-13.
 */
public class LoginService extends ActionService {
    private ArrayList<ActionListener> tmpListenerList = new ArrayList<ActionListener>(5);
    private boolean isLoginProcessing = false;

    public LoginService(Context context,ServiceManager manager){
        super(context,manager);
    }

    @Override
    public void doAction(int action, Bundle data, ActionListener listener) {
        switch (action){
            case Constants.ACTION_AUTO_LOGIN:
                doAutoLogin(data,listener);
                break;
            case Constants.ACTION_LOGIN:
                doLogin(data,listener);
                break;
            case Constants.ACTION_GET_ACCOUNTS:
                doGetAccounts(data,listener);
                break;
            case Constants.ACTION_UPDATE_ACCOUNT:
                doUpdateAccount(data,listener);
                break;
        }
    }

    private void doUpdateAccount(final Bundle data,final ActionListener listener){
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean autoLogin = data.getBoolean(Constants.KEY_AUT_LOGIN,false);
                String name = data.getString(Constants.KEY_USER_NAME,"");
                String pwd = data.getString(Constants.KEY_PASSWORD,"");


                SharedPreferences sp = contextRef.get().getSharedPreferences(TAG, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                if(autoLogin){
                    editor.putString(Constants.KEY_AUTO_LOGIN_ACCOUNT, name);
                }else{
                    editor.remove(Constants.KEY_AUTO_LOGIN_ACCOUNT);
                }
                editor.commit();

                getDBHelper().updateAccountInfo(name,pwd);
            }
        }).start();
        */
        boolean autoLogin = data.getBoolean(Constants.KEY_AUT_LOGIN,false);
        String name = data.getString(Constants.KEY_USER_NAME,"");
        String pwd = data.getString(Constants.KEY_PASSWORD,"");
        String showName = data.getString(TRAApplication.KEY_SHOW_NAME,"");
        String encodeKey = data.getString(TRAApplication.KEY_ENCODE_KEY,"");
        String sessionId = data.getString(TRAApplication.KEY_SESION_ID);
        TRAApplication app = TRAApplication.getApp();
        app.updateCurrentAccount(new AccountInfo(name, pwd,showName,encodeKey, sessionId),autoLogin);
    }

    private void doGetAccounts(Bundle data, final ActionListener listener) {
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AccountInfo> accounts = getDBHelper().getAccountInfoList();

                // 先只传一个帐号吧，多帐号需后面再看
                final Bundle data = new Bundle();
                //data.putParcelableArrayList(Constants.KEY_SAVED_ACCOUNTS,accounts); 需让 AccountInfo 实现 Parcelable
                if(accounts.size() > 0){
                    AccountInfo accountInfo = accounts.get(0);
                    data.putString(Constants.KEY_USER_NAME,accountInfo.userName);
                    data.putString(Constants.KEY_PASSWORD,accountInfo.password);
                }
                notifyListener(Constants.ACTION_GET_ACCOUNTS,data,listener);
            }
        }).start();
        */
        TRAApplication app = TRAApplication.getApp();
        AccountInfo accountInfo = app.getCachedAccount();
        final Bundle result = new Bundle();
        result.putString(Constants.KEY_USER_NAME,accountInfo.userName);
        result.putString(Constants.KEY_PASSWORD,accountInfo.password);
        notifyListener(Constants.ACTION_GET_ACCOUNTS, result,listener);
    }

    private void doAutoLogin(Bundle data, ActionListener listener) {
        Context context = contextRef.get();
        SharedPreferences sp = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        String autoLoginUserName = sp.getString(Constants.KEY_AUTO_LOGIN_ACCOUNT, "");
        if(autoLoginUserName.length() != 0){
            // 1. 以此帐号在本地登录

            Bundle response = new Bundle();
            response.putString(Constants.KEY_USER_NAME,autoLoginUserName);
            notifyListener(Constants.ACTION_AUTO_LOGIN,response,listener);
            // 2. TODO 向服务器查询密码是否正确，以应对密码修改的情况
            // doAction(Constants.ACTION_CHECK_PASSWORD,null,null);
        }else{
            notifyListener(Constants.ACTION_AUTO_LOGIN,new Bundle(),listener);
        }
    }

    @Override
    public int[] getActionList() {
        return new int[]{
                Constants.ACTION_AUTO_LOGIN,
                Constants.ACTION_LOGIN,
                Constants.ACTION_GET_ACCOUNTS,// FIXME 如果后续需要多个帐号登录，可使用此 action
                Constants.ACTION_LOGOUT,
                Constants.ACTION_CHECK_PASSWORD, // TODO
                Constants.ACTION_UPDATE_ACCOUNT
        };
    }


    private void doLogin(Bundle data,final ActionListener listener){
        final String userName = data.getString(Constants.KEY_USER_NAME);
        final String password = data.getString(Constants.KEY_PASSWORD);
        if(TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)){
            if(listener != null){
                listener.onReceive(Constants.ACTION_LOGIN, null);
            }
            return;
        }

        if(listener != null && !tmpListenerList.contains(listener)){
            tmpListenerList.add(listener);
        }
        if(!isLoginProcessing){

            RequestQueue rq = MyVolley.getRequestQueue();

            Map<String,String> params = new HashMap<String,String>();
            params.put("loginName",userName);
            params.put("password",password);
            params.put("name",userName);
            params.put("pwd",password);
            params.put("json","true");
            int method = Request.Method.POST;

//            if(Constants.getCurEnv() == Constants.ENV_TEST){
                // 演示环境用的是PUT
//                method = Request.Method.PUT;
//            }


            NetworkUtils.StringRequestWithParams request  = new NetworkUtils.StringRequestWithParams(
                    method,
                    Constants.getLoginUrl(userName),params,
                    new Response.Listener<String>() {
                        public void onResponse(String response) {
                            Bundle result = new Bundle();
                            result.putString(TRAApplication.KEY_USER_NAME,userName);
                            result.putString(TRAApplication.KEY_USER_PASSWORD,password);
                            boolean success = true;
                            JSONObject o = null;
                            try {
                                /*
                                {"resultMsg":"","resultObject":{"email":null,"userId":"6ad7bae4-6c87-4bf3-a7cd-50f6bb6b8b50","encodeKey":"F3B14EA32A2226DEE7338A2EB44C672981FA33A6755D16ACC1E4783BECF419F3C3233795DD70209BBC4C403589741636D76FFE1EA73815345C51300D55C4228AF6016962C862351008A8403C0E359E18","gender":0,"userName":"æ½ç¥¥æº"},"success":true}
                                */
                                /*
                                {
  "err": 0,
  "result": {
    "skey": "8b8a6d982d405267b7bcb8eaf77fc762",
    "userId": "539654ff05bf294863d12610",
    "name": "xzone_admin",
    "nick": "è¶çº§ç®¡çå"
  }
}

                                * */
                                o = new JSONObject(response);
                                if(o != null){
                                    JSONObject resultObject = o;
                                    String showName = "",encodeKey = "", session = "";

                                    if(Constants.getCurEnv() == Constants.ENV_TEST) {
                                        // 71环境
                                        resultObject = JSONUtils.getJSONObject(o, "result", new JSONObject());
                                        showName = JSONUtils.getString(resultObject, "nick", "");
                                        encodeKey = JSONUtils.getString(resultObject, "skey", "");
                                    } else {
                                        // 演示环境，多了个session
                                        showName = JSONUtils.getString(resultObject, "nick", "");
                                        encodeKey = JSONUtils.getString(resultObject, "skey", "");
                                        session = JSONUtils.getString(resultObject, "session", "");
                                    }
                                    if(TextUtils.isEmpty(encodeKey)){
                                        success = false;
                                    }else{
                                        result.putString(TRAApplication.KEY_SHOW_NAME,showName);
                                        result.putString(TRAApplication.KEY_ENCODE_KEY,encodeKey);
                                        result.putLong(TRAApplication.KEY_LOGIN_TIME,System.currentTimeMillis());
                                        result.putString(TRAApplication.KEY_SESION_ID,session);
                                    }
                                }else{
                                    success =false;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                success = false;
                            }
                            result.putBoolean(ServiceManager.Constants.KEY_STATUS,success);
                            notifyListener(Constants.ACTION_LOGIN,result,tmpListenerList);
                            tmpListenerList.clear();

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO 分别处理网络错误
                            notifyListener(Constants.ACTION_LOGIN,null,tmpListenerList);
                            tmpListenerList.clear();
                        }
                    }
            );
            rq.add(request);
            rq.start();
//            new LoginAsyncTask().execute(userName,password);
        }
    }
}
