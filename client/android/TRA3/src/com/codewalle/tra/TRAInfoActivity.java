package com.codewalle.tra;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.codewalle.tra.Model.TRAInfo;
import com.codewalle.tra.utils.JSONUtils;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiangzhipan on 14-10-14.
 */
@EActivity(R.layout.activity_info)
public class TRAInfoActivity extends BaseFragmentActivity {
    private TRAInfo mInfo;



    @AfterViews
    void processData() {
        Bundle data = getIntent().getExtras();
        if(data == null){
//            setState(STATE_NO_ACTIVITY);
            Log.i(TAG,"data=null");
            finish();
            return;
        }
        String str = data.getString("result");
        try {
            JSONObject object = new JSONObject(str);
            Log.i(TAG,str);
            if(object.has("r")){
                object = JSONUtils.getJSONObject(object, "r", new JSONObject());
                JSONArray array = JSONUtils.getJSONArray(object,"activities",new JSONArray());
                if(array.length() < 1){
//                    setState(STATE_NO_ACTIVITY);
                    finish();
                    return;
                }else{
                    object = JSONUtils.arrayGetJSONObject(array,0);
                }
            }
            TRAInfo info = new TRAInfo(object);
            initTRAInfo(info);
//            setState(STATE_HAS_ACTIVITY);
        } catch (JSONException e) {
//            setState(STATE_NO_ACTIVITY);
            finish();
        }
    }
    private void initTRAInfo(TRAInfo info) {
        this.mInfo = info;
        mName.setText(info.title);
        mTimeAndCreator.setText(info.getTimeAndCreatorDesc());//(info.getTimeFormated());
        mDesc.setText(info.getAllDesc());
        if(mInfo.activeStatus){
            mJoinButton.setVisibility(View.VISIBLE);
            mViewResourcesButton.setVisibility(View.GONE);
        }else{
            mJoinButton.setVisibility(View.GONE);
            mViewResourcesButton.setVisibility(View.VISIBLE);
        }

    }

    @ViewById(R.id.tra_name)
    TextView mName;
    @ViewById(R.id.tra_time_and_creator)
    TextView mTimeAndCreator;
    @ViewById(R.id.tra_intro)
    TextView mDesc;
    @ViewById(R.id.btn_join)
    View mJoinButton;
    @ViewById(R.id.btn_view_resource)
    View mViewResourcesButton;


    @Click({R.id.btn_join,R.id.btn_back,R.id.btn_view_resource})
    void onClicked(View v){
        int viewId = v.getId();
        Intent i = null;
        switch (viewId) {
            case R.id.btn_join:
                showJoiningDialog();
                sendJoin(mInfo.id);
                break;
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_view_resource:
                gotoResourceActivity(mInfo);
                break;
            default:
                break;

        }
        if(i != null){
            try{
                startActivityForResult(i, viewId);
            }catch(Exception e){//某些机器上启动不了摄像头拍照或录像
//                Toast.makeText(this, R.string.launch_camera_fail, Toast.LENGTH_LONG).show();
            }
        }    }

    private void gotoResourceActivity(TRAInfo mInfo) {

    }

    private void sendJoin(String id) {
        app.joinActivity(id,new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if (result != null && result.has("c")) {
                    if (result.get("c").getAsInt() == 0) {

                        Log.i(TAG, result.toString());

                        Intent i = new Intent(TRAInfoActivity.this, CurrentTRAActivity_.class);
                        i.putExtra("result", mInfo.toString());

                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                        return;
                    }
                }

                if (e != null)
                    Log.d(TAG, e.getMessage(), e);
            }
        });
    }

    private void showJoiningDialog() {

    }
}