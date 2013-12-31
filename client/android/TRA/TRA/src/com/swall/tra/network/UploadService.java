package com.swall.tra.network;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.swall.tra.utils.NetworkUtils;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
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

    private void upload(final int action,int type,String content,String uid,String activityId,final ActionListener listener){
        RequestQueue rq = MyVolley.getRequestQueue();
        JSONObject object = new JSONObject();
        try {
            object.put("type",type);
            object.put("content",content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.POST,
                ServiceManager.Constants.getPostResourceUrl(activityId, uid),
                object,
                new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("SWall", response.toString());
                        Bundle data = new Bundle();
                        data.putString("result",response.toString());
                        notifyListener(action, data, listener);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        notifyListener(action, null, listener);
                    }
                }
        );

        rq.add(jor);
        rq.start();
    }

    private void uploadText(final int action,final Bundle data,final ActionListener callback) {
        upload(action, ServiceManager.Constants.UPLOAD_TYPE_TEXT,data.getString("text"),data.getString(ServiceManager.Constants.KEY_USER_NAME,"admin"),data.getString("id"),callback);
    }

    private void uploadResourceExtra(final int action, final Bundle data, final ActionListener listener,final int type){
        byte[] bytes = null;
        ContentType contentType = ContentType.MULTIPART_FORM_DATA;
        switch(action){
            case ServiceManager.Constants.ACTION_UPLOAD_PIC:
                Bitmap bitmap = (Bitmap)data.get("data");
                bytes = NetworkUtils.bitmap2Bytes(bitmap);
                contentType = ContentType.create("imag/jpg");
                break;
            case ServiceManager.Constants.ACTION_UPLOAD_VIDEO:
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileInputStream fis;
                try {
                    Uri uri = (Uri)data.get("data");
                    fis = new FileInputStream(new File(getFilePathFromContentUri(uri, contextRef.get().getContentResolver())));
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
        doUpload(ServiceManager.Constants.URL_PREFIX + "resources",
                bytes,
                new ActionListener(null) {//此处不应该用ActionListener
                    @Override
                    public void onReceive(int action, Bundle data2) {
                        boolean error = true;
                        if(action == 0 && data2 != null){
                            String jsonString = data2.getString("data");
                            try {
                                JSONArray arr = new JSONArray(jsonString);
                                if(arr.length() > 0){
                                    JSONObject object = arr.getJSONObject(0);
                                    String filePath = object.getString("filename");
                                    String fullFilePath = ServiceManager.Constants.getUploadedFilPath(filePath);
                                    upload(action,
                                            type,
                                            fullFilePath,
                                            data.getString(ServiceManager.Constants.KEY_USER_NAME,"admin"),
                                            data.getString("id"),
                                            listener
                                            );
                                    error = false;

                                }
                            } catch (JSONException e) {
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


    private void doUpload(String postUrl,byte[] data, final ActionListener listener,ContentType contentType){
        NetworkUtils.MultipartRequest req =  new NetworkUtils.MultipartRequest(
                postUrl,
                data,
                contentType,
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
