package com.swall.tra.network;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.android.volley.*;
import com.swall.tra.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pxz on 13-12-19.
 */
public class DataService extends ActionService{
    public DataService(Context context, ServiceManager serviceManager) {
        super(context,serviceManager);
    }

    @Override
    public void doAction(final int action, final Bundle data, final ActionListener listener) {
        switch (action){
            case ServiceManager.Constants.ACTION_GET_UPDATES:
                break;
            case ServiceManager.Constants.ACTION_GET_AVAILABLE_ACTIVITIES:
                getActivities(action, data, listener, true);
                break;
            case  ServiceManager.Constants.ACTION_GET_CURRENT_ACTIVITY_INFO:
                getCurrentActivityInfo(action,data,listener);
                break;
            case ServiceManager.Constants.ACTION_GET_EXPIRED_ACTIVITIES:
                getActivities(action, data, listener, false);
                break;
            case ServiceManager.Constants.ACTION_JION_ACTIVITY:
                joinActivity(action,data,listener);
                break;
            case ServiceManager.Constants.ACTION_QUIT_ACTIVITY:
                quitActivity(action,data,listener);
                break;
            default:
                super.doAction(action,data,listener);
                break;
        }
    }

    private void quitActivity(final int action,final  Bundle data, final ActionListener listener) {
        RequestQueue rq = MyVolley.getRequestQueue();

        MyJsonObjectRequest request  = new MyJsonObjectRequest(
                Request.Method.DELETE,
                ServiceManager.Constants.getQuitUrl(data.getString(ServiceManager.Constants.KEY_USER_NAME), data.getString("id")),null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(!response.has("c") && JSONUtils.getInt(response, "c", -1) == 0){
                            notifyListener(action,null);
                            return;
                        }
                        Bundle result = new Bundle();
                        result.putString("result",response.toString());
                        notifyListener(action,result,listener);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO 分别处理网络错误
                        notifyListener(action,null,listener);
                    }
                }
        );
        rq.add(request);
        rq.start();
    }

    private void joinActivity(final int action,final Bundle data,final ActionListener listener) {
        RequestQueue rq = MyVolley.getRequestQueue();

        MyJsonObjectRequest request  = new MyJsonObjectRequest(
                Request.Method.POST,
                ServiceManager.Constants.getJoinUrl(data.getString(ServiceManager.Constants.KEY_USER_NAME), data.getString("id")),null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(!response.has("c")){
                            notifyListener(action,null);
                            return;
                        }
                        Bundle result = new Bundle();
                        result.putString("result",response.toString());
                        notifyListener(action,result,listener);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO 分别处理网络错误
                        notifyListener(action,null,listener);
                    }
                }
        );
        rq.add(request);
        rq.start();
    }


    MyJsonObjectRequest availableActivityRequest;
    private void getActivities(final int action, final Bundle data, final ActionListener listener, final boolean active) {
        if(availableActivityRequest != null && availableActivityRequest.hasHadResponseDelivered()){
            // TODO
            //throw new Error("deal with me...");
        }
        RequestQueue rq = MyVolley.getRequestQueue();
        availableActivityRequest  = new MyJsonObjectRequest(
                ServiceManager.Constants.getActivitiesListUrl(data.getString(ServiceManager.Constants.KEY_USER_NAME), active, data.getInt("index", 0), false),null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(!response.has("r") ||  !response.has("c")){
                            notifyListener(action,null);
                            return;
                        }
                        Bundle result = new Bundle();
                        result.putString("result",response.toString());
                        notifyListener(action,result,listener);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO 分别处理网络错误
                        notifyListener(action,null,listener);
                    }
                }
        );
        rq.add(availableActivityRequest);
        rq.start();
    }

    private void getCurrentActivityInfo(final int action, final Bundle data, final ActionListener listener) {
        RequestQueue rq = MyVolley.getRequestQueue();
        MyJsonObjectRequest request  = new MyJsonObjectRequest(
                ServiceManager.Constants.getActivitiesListUrl(data.getString(ServiceManager.Constants.KEY_USER_NAME), true, data.getInt("index", 0), true),null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(!response.has("r") ||  !response.has("c")){
                            notifyListener(action,null);
                            return;
                        }
                        Bundle result = new Bundle();
                        result.putString("result",response.toString());
                        notifyListener(action,result,listener);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO 分别处理网络错误

                        String str = error.toString();
                        Log.e(TAG,action+" error:"+str+" "+error.getMessage()+error.getLocalizedMessage(),error);
                        try{
                            JSONObject response = new JSONObject(str);
                            if(!response.has("r") ||  !response.has("c")){
                                notifyListener(action,null,listener);
                                return;
                            }
                            Bundle result = new Bundle();
                            result.putString("result",response.toString());
                            notifyListener(action,result,listener);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            notifyListener(action,null,listener);
                        }
                    }
                }
        );
        rq.add(request);
        rq.start();
    }


    @Override
    public int[] getActionList() {
        return new int[]{
                ServiceManager.Constants.ACTION_GET_UPDATES,
                ServiceManager.Constants.ACTION_GET_CURRENT_ACTIVITY_INFO,
                ServiceManager.Constants.ACTION_GET_AVAILABLE_ACTIVITIES,
                ServiceManager.Constants.ACTION_GET_EXPIRED_ACTIVITIES,
                ServiceManager.Constants.ACTION_JION_ACTIVITY,
                ServiceManager.Constants.ACTION_QUIT_ACTIVITY
        };
    }
}
