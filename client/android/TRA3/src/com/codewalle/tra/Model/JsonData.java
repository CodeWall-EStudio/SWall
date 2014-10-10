package com.codewalle.tra.Model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiangzhipan on 14-10-5.
 */
public class JsonData {
    JSONObject mJsonObject;
    String mOriginString;
    boolean mIsValidated;
    public JsonData(String str){
        mOriginString = str;
        try {
            mJsonObject = new JSONObject(str);
            mIsValidated = true;
        } catch (JSONException e) {
            e.printStackTrace();
            mIsValidated = false;
            mJsonObject = null;
        }
    }

    public String geString(){
        return mOriginString;
    }

    public JSONObject getJSON(){
        return mJsonObject;
    }

    public boolean isValidate(){
        return mIsValidated;
    }
}
