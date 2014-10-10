package com.codewalle.tra.Network;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpResponse;
import org.json.JSONObject;

/**
 * Created by xiangzhipan on 14-9-29.
 */
public abstract class MyJSONObjectCallback extends AsyncHttpClient.JSONObjectCallback {
    @Override
    public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {

    }
}
