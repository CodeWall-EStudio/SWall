package com.swall.tra_qq.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.swall.tra_qq.TRAApplication;
import com.swall.tra_qq.model.AccountInfo;
import static com.swall.tra_qq.network.ServiceManager.Constants;

import com.swall.tra_qq.utils.NetworkUtils;

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
        TRAApplication app = TRAApplication.getApp();
        app.updateCurrentAccount(new AccountInfo(name, pwd,showName,encodeKey), autoLogin);
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
                Constants.ACTION_UPDATE_ACCOUNT,
                Constants.ACTION_LOGIN_WITH_QQ
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

            NetworkUtils.StringRequestWithParams request  = new NetworkUtils.StringRequestWithParams(
                    Request.Method.POST,
                    Constants.getLoginUrl(),params,
                    new Response.Listener<String>() {
                        public void onResponse(String response) {
                            Bundle result = new Bundle();
                            result.putString(TRAApplication.KEY_USER_NAME,userName);
                            result.putString(TRAApplication.KEY_USER_PASSWORD,password);
                            boolean success = true;
                            result.putString(TRAApplication.KEY_SHOW_NAME,"QQ登录用户");
                            result.putString(TRAApplication.KEY_ENCODE_KEY,"-");
                            result.putLong(TRAApplication.KEY_LOGIN_TIME,System.currentTimeMillis());

//                            JSONObject o = null;
//                            try {
                                // 以下为非QQ登录的情况
//                                o = new JSONObject(response);
//                                if(o != null){
//                                    JSONObject resultObject = JSONUtils.getJSONObject(o, Constants.KEY_LOGIN_RESULT_OBJECT, new JSONObject());
//                                    String showName = JSONUtils.getString(resultObject,"userName","");
//                                    String encodeKey = JSONUtils.getString(resultObject,"encodeKey","");
//                                    if(TextUtils.isEmpty(encodeKey)){
//                                        success = false;
//                                    }else{
//                                        result.putString(TRAApplication.KEY_SHOW_NAME,showName);
//                                        result.putString(TRAApplication.KEY_ENCODE_KEY,encodeKey);
//                                        result.putLong(TRAApplication.KEY_LOGIN_TIME,System.currentTimeMillis());
//                                    }
//                                }else{
//                                    success =false;
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                success = false;
//                            }
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