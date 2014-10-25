package com.codewalle.tra.Model;

import android.app.Application;
import android.content.Context;
import com.codewalle.tra.LoginActivity;
import com.codewalle.tra.utils.Constants;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.ion.future.ResponseFuture;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by xiangzhipan on 14-9-29.
 */
public interface TRAApp {


    // TODO 支持多帐号时可用
//    public List<AccountInfo> getCachedAccounts();
    public AccountInfo getCachedAccount();
    public void cacheAccount(AccountInfo account);

    com.koushikdutta.async.future.Future<JSONObject> doJSONRequest(Constants requestType,
                                                                   AsyncHttpRequest request,
                                                                   AsyncHttpClient.JSONObjectCallback callback);

    boolean isAutoLogin();

    Future doLogin(String username, String password, FutureCallback callback);
    Future getJoinedActivityInfo(FutureCallback callback);
    Future getActivityInfo(String activityId,FutureCallback callback);
    Future getTRAList(boolean expired, FutureCallback callback);
    Future joinActivity(String activityId,FutureCallback callback);
    Future quitActivity(String actiivtyId,FutureCallback callback);
    Future postComment(TRAInfo info, ActivityComment comment, FutureCallback callback);

    int getBgColor();
}
