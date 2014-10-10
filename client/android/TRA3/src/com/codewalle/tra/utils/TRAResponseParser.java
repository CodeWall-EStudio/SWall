package com.codewalle.tra.utils;

import android.accounts.Account;
import android.util.Pair;
import com.codewalle.tra.Model.AccountInfo;
import com.codewalle.tra.Network.RequestError;
import org.json.JSONException;
import org.json.JSONObject;

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
}
