package com.swall.tra.network;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import com.android.volley.*;
import com.swall.tra.TRAApplication;
import com.swall.tra.utils.JSONUtils;
import com.swall.tra.utils.NetworkUtils;
import com.umeng.analytics.MobclickAgent;
import org.apache.http.entity.ContentType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by pxz on 13-12-13.
 */
public class UploadService extends ActionService {

    public UploadService(Context context,ServiceManager manager) {
        super(context, manager);
    }

    @Override
    public void doAction(int action, Bundle data, ActionListener callback) {
        switch (action){
            case ServiceManager.Constants.ACTION_UPLOAD_TEXT:
                uploadText(action,data,callback);
                break;
            case ServiceManager.Constants.ACTION_UPLOAD_PIC:
                uploadResourceExtra(action, data, callback, ServiceManager.Constants.UPLOAD_TYPE_IMAGE);
                break;
            case ServiceManager.Constants.ACTION_UPLOAD_AUDIO:
                uploadResourceExtra(action, data, callback, ServiceManager.Constants.UPLOAD_TYPE_AUDIO);
                break;
            case ServiceManager.Constants.ACTION_UPLOAD_VIDEO:
                uploadResourceExtra(action, data, callback, ServiceManager.Constants.UPLOAD_TYPE_VIDEO);
                break;
            default:
                break;
        }
    }

    private void upload(final int action,int type,String content,String comment,String uid,String activityId,final ActionListener listener){
        RequestQueue rq = MyVolley.getRequestQueue();
        JSONObject object = new JSONObject();
        try {
            object.put("type",type);
            object.put("content",content);
            object.put("comment",comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyJsonObjectRequest jor = new MyJsonObjectRequest(
                Request.Method.POST,
                ServiceManager.Constants.getPostResourceUrl(activityId, uid),
                object,
                new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject response) {
                        MobclickAgent.onError(TRAApplication.getApp().getApplicationContext(),"err_comment_network_success");
                        Log.i("SWall", response.toString());
                        Bundle data = new Bundle();
                        data.putString("result",response.toString());
                        notifyListener(action, data, listener);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        MobclickAgent.onError(TRAApplication.getApp().getApplicationContext(),"err_comment_network_fail");
                        if(error != null && error.networkResponse != null){
                            MobclickAgent.onError(TRAApplication.getApp().getApplicationContext(),"err_comment_network_fail_"+error.networkResponse.statusCode);
                        }else{
                            MobclickAgent.onError(TRAApplication.getApp().getApplicationContext(),"err_comment_network_fail_other");
                        }
                        notifyListener(action, null, listener);
                    }
                }
        );

        rq.add(jor);
        rq.start();
    }

    private void uploadText(final int action,final Bundle data,final ActionListener callback) {
        upload(action, ServiceManager.Constants.UPLOAD_TYPE_TEXT,data.getString("text"),"",data.getString(ServiceManager.Constants.KEY_USER_NAME,"admin"),data.getString("id"),callback);
    }

    private void uploadResourceExtra(final int action, final Bundle data, final ActionListener listener,final int type){
        byte[] bytes = null;
        String filePath = data.getString("filePath");
        final String comment = data.getString("comment");
        ContentType contentType = ContentType.MULTIPART_FORM_DATA;
        switch(action){
            case ServiceManager.Constants.ACTION_UPLOAD_PIC:
                /*
                Bitmap bitmap = (Bitmap)data.get("data");
                bytes = NetworkUtils.bitmap2Bytes(bitmap);
                */
                contentType = ContentType.create("imag/jpg");
                break;
            case ServiceManager.Constants.ACTION_UPLOAD_VIDEO:
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileInputStream fis;
                try {
                    Uri uri = (Uri)data.get("data");
//                    fis = new FileInputStream(new File(getFilePathFromContentUri(uri, contextRef.get().getContentResolver())));
                    fis = new FileInputStream(new File(filePath));
                    byte[] buf = new byte[1024];
                    int n;
                    while (-1 != (n = fis.read(buf)))
                        baos.write(buf, 0, n);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bytes = baos.toByteArray();
                contentType = ContentType.create("video/3gp");
                break;

        }

        doUpload(ServiceManager.Constants.getUploadUrl(),
                data.getString("id"),
                comment,
                bytes,
                filePath,
                data.getString("activityTime"),
                data.getString("activityName"),
                new ActionListener(null) {//此处不应该用ActionListener
                    @Override
                    public void onReceive(int action, Bundle data2) {
                        boolean error = true;
                        if(action == 0 && data2 != null){
                            String jsonString = data2.getString("data");
                            // 处理php notice..
                            jsonString = jsonString.trim();
                            if(jsonString.indexOf("<") == 0){
                                String s = jsonString;
                                s = s.replaceAll("\r\n","");
                                s = s.replaceAll("\n","");
                                jsonString = s.replaceAll("\\<div[^>]*>.*</div>","");
                            }


                            /*
                            *{"code":"200","msg":"\u4e0a\u4f20\u6210\u529f!","data":{"fid":292,"jsonrpc":"2.0","error":{"code":0,"message":"\u4e0a\u4f20\u6210\u529f!"}},"elapsed_time":"0.3787","memory_usage":"3.71MB","profiler":"{profiler}"}
                            * */

                            try {

                                JSONObject object = new JSONObject(jsonString);

                                /*

                                {
  "code": 200,
  "msg": "ok",
  "data": {
    "fid": "5325d685622b683f2428f385"
  }
}
                                */
                                // 两边cgi返回格式不一样。。
                                boolean iscodewalle = ServiceManager.Constants.FILE_SERVER_URL.indexOf("codewalle") != -1;
                                String fid;
                                JSONObject resultData = JSONUtils.getJSONObject(object, "data", new JSONObject());
//                                if(iscodewalle){
                                    fid = JSONUtils.getString(resultData,"fid","-1");
//                                }else{
//                                    fid = ""+JSONUtils.getLong(resultData,"fid",-1);
//                                }
                                  
                                if(!"-1".equals(fid)){
                                    String fullFilePath = ServiceManager.Constants.getDownloadUrl(fid);
                                    upload(action,
                                            type,
                                            fullFilePath,
                                            comment,
                                            data.getString(ServiceManager.Constants.KEY_USER_NAME,"admin"),
                                            data.getString("id"),
                                            listener
                                    );
                                    error = false;
                                }else{
                                    MobclickAgent.onEvent(TRAApplication.getApp().getApplicationContext(), "err_upload_fail_but_connected");
                                }

                            } catch (JSONException e) {
                                MobclickAgent.onEvent(TRAApplication.getApp().getApplicationContext(), "err_upload_fail_json");
                                e.printStackTrace();
                            }
                        }
                        if(error){
                            notifyListener(action,null,listener);
                        }
                    }
                },
                contentType);

    }

    @Override
    public int[] getActionList() {
        return new int[]{
                ServiceManager.Constants.ACTION_UPLOAD_PIC,
                ServiceManager.Constants.ACTION_UPLOAD_TEXT,
                ServiceManager.Constants.ACTION_UPLOAD_VIDEO,
                ServiceManager.Constants.ACTION_UPLOAD_AUDIO
        };
    }


    private void doUpload(String postUrl,
                          String activityId,
                          String comment,
                          byte[] data,
                          String filePath,
                          String activityTime,
                          String activityName,
                          final ActionListener listener,
                          ContentType contentType){

        /*
        Map<String,String> params = new HashMap<String,String>(3);
        params.put("media","1");
        params.put("name", "noname");
        params.put("encodeKey", sEncodeKey);
        params.put("file","test");
        NetworkUtils.StringRequestWithParams request = new NetworkUtils.StringRequestWithParams(
                Request.Method.POST,
                postUrl,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Bundle data = new Bundle();
                        data.putString("data",response);
                        listener.onReceive(0,data);
                    }
                },
                new Response.ErrorListener(){

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onReceive(-1,null);
                    }
                  
        );
        */

        File file = null;
        if(filePath != null){
            file = new File(filePath);
        }

        NetworkUtils.MultipartRequest req =  new NetworkUtils.MultipartRequest(
                postUrl,
                activityId,
                sEncodeKey,
                data,
                file,
                contentType,
                activityTime,
                activityName,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        MobclickAgent.onEvent(TRAApplication.getApp().getApplicationContext(),
                                "err_upload_success_200");
                        Bundle data = new Bundle();
                        data.putString("data",response);
                        listener.onReceive(0,data);
                    }
                },
                new Response.ErrorListener(){

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MobclickAgent.onEvent(TRAApplication.getApp().getApplicationContext(),
                                "err_upload_fail_netwrk");
                        if(error != null && error.networkResponse != null ){
                            MobclickAgent.onEvent(TRAApplication.getApp().getApplicationContext(),
                                    "err_upload_fail_netwrk_"+error.networkResponse.statusCode);
                        }else{
                            MobclickAgent.onEvent(TRAApplication.getApp().getApplicationContext(),
                                    "err_upload_fail_netwrk_other");
                        }
                        listener.onReceive(-1,null);
                    }
                }
        );

        RequestQueue rq = MyVolley.getRequestQueue();
        rq.add(req);
        rq.start();
    } 
    
    // TODO move to util
    static String getFilePathFromContentUri(Uri selectedVideoUri,
                                     ContentResolver contentResolver) {
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }
}
