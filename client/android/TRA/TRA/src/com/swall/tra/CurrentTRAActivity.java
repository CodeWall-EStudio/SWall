package com.swall.tra;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.swall.tra.model.TRAInfo;
import com.swall.tra.network.ActionListener;
import com.swall.tra.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.swall.tra.network.ServiceManager.Constants;

/**
 * Created by pxz on 13-12-24.
 */
public class CurrentTRAActivity extends BaseActivity implements View.OnClickListener {

    private static final int STATE_LOADING = 0;
    private static final int STATE_NO_ACTIVITY = 1;
    private static final int STATE_HAS_ACTIVITY = 2;
    private Intent mTakePicIntent;
    private Intent mTakeVideoRecordIntent;
    private Intent mWriteTextIntent;
    private Intent mAvailableListIntent;
    private boolean isToolShow;


    private ActionListener mQuitListener = new ActionListener(this) {
        @Override
        public void onReceive(int action, Bundle data) {

        }
    };

    private ActionListener mUploadListener = new ActionListener(this) {
        @Override
        public void onReceive(int action, Bundle data) {
            // TODO
        }
    };
    private View mBtnQuit;

    private View mCollectionButtons;
    private View mLoading;
    private View noContentButton;
    private TextView mName;
    private TextView mTime;
    private TextView mDesc;
    private View mDescWrapper;
    private TRAInfo mInfo;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_tra);

        initViews();
        initClickListeners();
        initIntents();

        setState(STATE_LOADING);

        if(!jumpFromList()){
            fetchCurrentActivity();
        }
    }

    // TODO 此方法名有问题
    private boolean jumpFromList() {
        Bundle data = getIntent().getExtras();
        if(data != null && data.containsKey("result")){
            String result = "";
            if(data!=null)result = data.getString("result","");
            dealWithData(result);
            return true;
        }
        return false;

    }

    private void fetchCurrentActivity() {

        app.doAction(Constants.ACTION_GET_CURRENT_ACTIVITY_INFO,defaultRequestData,new ActionListener(this) {
            @Override
            public void onReceive(int action, Bundle data) {
                String result = "";
                if(data!=null)result = data.getString("result","");
                dealWithData(result);
            }
        });
    }

    private void dealWithData(String str) {
        if(TextUtils.isEmpty(str)){
            setState(STATE_NO_ACTIVITY);
            return;
        }
        try {
            JSONObject object = new JSONObject(str);
            if(object.has("r")){
                object = JSONUtils.getJSONObject(object, "r", new JSONObject());
                JSONArray array = JSONUtils.getJSONArray(object,"activities",new JSONArray());
                if(array.length() < 1){
                    setState(STATE_NO_ACTIVITY);
                    return;
                }else{
                    object = JSONUtils.arrayGetJSONObject(array,0);
                }
            }
            mInfo = new TRAInfo(object);
            initTRAInfo(mInfo);
            setState(STATE_HAS_ACTIVITY);
        } catch (JSONException e) {
            setState(STATE_NO_ACTIVITY);
        }
    }


    private void setState(int state){
        switch(state){
            case STATE_LOADING:
                noContentButton.setVisibility(View.GONE);
                mLoading.setVisibility(View.VISIBLE);

                mCollectionButtons.setVisibility(View.GONE);
                mDescWrapper.setVisibility(View.GONE);
                break;
            case STATE_NO_ACTIVITY:
                noContentButton.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);

                mCollectionButtons.setVisibility(View.GONE);
                mDescWrapper.setVisibility(View.GONE);
                break;
            case STATE_HAS_ACTIVITY:
                noContentButton.setVisibility(View.GONE);
                mLoading.setVisibility(View.GONE);

                mCollectionButtons.setVisibility(View.VISIBLE);
                mDescWrapper.setVisibility(View.VISIBLE);
                break;
        }
    }
    private void showGotoListButton() {

        noContentButton.setVisibility(View.VISIBLE);
    }


    private void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
        noContentButton.setVisibility(View.GONE);
    }
    private void hideLoading(){
        mLoading.setVisibility(View.GONE);
    }


    private void initTRAInfo(TRAInfo info) {

        mName.setText(info.title);
        mTime.setText(info.getTimeFormated());
        mDesc.setText(info.getAllDesc());

    }


    private void initClickListeners() {
        findViewById(R.id.btnDraw).setOnClickListener(this);
        findViewById(R.id.btnPhoto).setOnClickListener(this);
        findViewById(R.id.btnRecordAudio).setOnClickListener(this);
        findViewById(R.id.btnRecordVideo).setOnClickListener(this);
        findViewById(R.id.btnText).setOnClickListener(this);

        mBtnQuit.setOnClickListener(this);
        noContentButton.setOnClickListener(this);
    }

    private void initIntents(){
        mTakePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mTakeVideoRecordIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        mTakeAudioRecordIntent = new Intent(this,CollectionAudio.class);
//        mWriteTextIntent = new Intent(this,AddTextActivity.class);

        mAvailableListIntent = new Intent(this,AvailableActivitiesActivity.class);
    }


    private void initViews() {
        mBtnQuit = findViewById(R.id.btnQuit);
        mCollectionButtons = findViewById(R.id.collection_panel_wrapper);
        mLoading = findViewById(R.id.loading);
        noContentButton = findViewById(R.id.btn_goto_available_list);
        mDescWrapper = findViewById(R.id.tra_info_detail_wrapper);


        mName = (TextView)findViewById(R.id.tra_name);
        mTime = (TextView)findViewById(R.id.tra_time);
        mDesc = (TextView)findViewById(R.id.tra_intro);

    }


    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        Intent i = null;
        switch (viewId) {
            case R.id.btnPhoto:
                i = mTakePicIntent;
                break;
            case R.id.btnRecordVideo:
                i = mTakeVideoRecordIntent;
                break;
            case R.id.btnText:
                i = mWriteTextIntent;
                break;
            case R.id.btn_goto_available_list:
                i = mAvailableListIntent;
                break;
            case R.id.btnQuit:
                if(mInfo == null)break;
                defaultRequestData.putString("id", mInfo.id);
                app.doAction(Constants.ACTION_QUIT_ACTIVITY,defaultRequestData,new ActionListener(this) {
                    @Override
                    public void onReceive(int action, Bundle data) {
                        fetchCurrentActivity();
                    }
                });
                break;
            default:
                break;

        }
        if(i != null){
            try{
                getParent().startActivityForResult(i, viewId);
            }catch(Exception e){//某些机器上启动不了摄像头拍照或录像
                if(viewId == R.id.btnPhoto){
                    Toast.makeText(this, R.string.launch_camera_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getApplicationContext().getSharedPreferences("current",MODE_PRIVATE);
        String result = sp.getString("result","");
        if(!result.equals("")){
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("result");
            editor.commit();
            fetchCurrentActivity();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case R.id.btnPhoto:
                if(resultCode == RESULT_OK){
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                }
                break;
            case R.id.btnDraw:
                break;
            case R.id.btnRecordAudio:
                break;
            case R.id.btnRecordVideo:
                break;
            case R.id.btn_goto_available_list:
                if(resultCode == RESULT_OK){//加入成功，拉一次数据
                    fetchCurrentActivity();
                }
                break;
            case R.id.btnText:
                if(data == null)return;
                defaultRequestData.putString("text","helle world");
                app.doAction(Constants.ACTION_UPLOAD_TEXT,defaultRequestData,mUploadListener);
                break;
        }
    }
}