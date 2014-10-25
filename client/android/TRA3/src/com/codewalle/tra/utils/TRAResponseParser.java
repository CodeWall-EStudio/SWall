package com.codewalle.tra.utils;

import android.accounts.Account;
import android.util.Pair;
import com.codewalle.tra.Model.AccountInfo;
import com.codewalle.tra.Model.TRAInfo;
import com.codewalle.tra.Network.RequestError;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by xiangzhipan on 14-10-5.
 */
public class TRAResponseParser {
    public static Pair<AccountInfo,RequestError> parseLogin(JSONObject account,String username,String password) {
        Pair<AccountInfo,RequestError> result;
        if(account == null){
            result = new Pair<AccountInfo, RequestError>(null,new RequestError(RequestError.ErrorType.NO_JSON,""));
        }else {
            long error = account.optLong("err", 0);
            String msg = account.optString("msg","");
            JSONObject resultObj = account.optJSONObject("result");

            if(error != 0){
                result = new Pair<AccountInfo, RequestError>(null,new RequestError(error,msg));
            }else {

                AccountInfo accountInfo = null;

                try {
                    accountInfo = new AccountInfo(
                            username,
                            password,
                            resultObj.getString("nick"),
                            resultObj.getString("skey"),
                            resultObj.optString("session",""),//userId??
                            true // TODO 应试允许设置是否自动登录
                    );
                    result = new Pair<AccountInfo, RequestError>(accountInfo,null);
                } catch (JSONException e) {
                    e.printStackTrace();
                    result = new Pair<AccountInfo, RequestError>(null, new RequestError(RequestError.ErrorType.NO_JSON,""));
                }
            }
        }

        return result;
    }

    public static Pair<TRAInfo,RequestError> parseJoinedActivity(JsonObject result, Exception e) {
/*
{
  "c": 0,
  "r": {
    "activities": [
      {
        "_id": "543cf0971742c6844d1c64ec",
        "active": true,
        "info": {
          "type": 1,
          "date": 1413366180000,
          "createDate": 1413279895961,
          "title": "æµè¯",
          "desc": "è",
          "teacher": "test",
          "grade": "ä¸åå¹´çº§",
          "class": "ä¸åç­çº§",
          "subject": "ä¸åå­¦ç§",
          "domain": "sfd",
          "link": ""
        },
        "resources": [],
        "users": {
          "creator": "xzone_admin",
          "invitedUsers": [
            "xzone_admin"
          ],
          "participators": [
            "xzone_admin"
          ]
        }
      }
    ],
    "profiles": {},
    "more": false
  }
}
*/
        if(result == null){
            // 网络原因获取失败，需重新获取
            return new Pair<TRAInfo,RequestError>(null,new RequestError(RequestError.ErrorType.SERVER_5XX,"."));
        }else{
            int code = result.get("c").getAsInt();
            if(code != 0){
                // TODO
                return new Pair<TRAInfo, RequestError>(null,new RequestError(RequestError.ErrorType.NO_JSON,"authorize fail?"));
            }else{
                JsonObject r = result.get("r").getAsJsonObject();

                JsonArray activities = r.getAsJsonArray("activities");
                if(activities.size() != 0){
                    try {
                        // 环境问题。。TODO

                        JsonObject profiles = new JsonObject();
                        try {
                            profiles = r.getAsJsonObject("profiles");
                        }catch(RuntimeException e2){}

                        TRAInfo info = new TRAInfo(activities.get(0).getAsJsonObject(),profiles);
                        return new Pair<TRAInfo, RequestError>(info,null);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        // TODO
                        return new Pair<TRAInfo, RequestError>(null,new RequestError(RequestError.ErrorType.NO_JSON,""));
                    }
                } else {
                    return new Pair<TRAInfo,RequestError>(null,new RequestError(RequestError.ErrorType.NORMAL,""));
                }
            }
        }

    }
}
