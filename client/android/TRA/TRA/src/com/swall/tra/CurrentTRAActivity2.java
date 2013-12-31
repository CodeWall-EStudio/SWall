package com.swall.tra;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.swall.tra.model.TRAInfo;
import com.swall.tra.network.ActionListener;
import com.swall.tra.network.ServiceManager;
import com.swall.tra.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pxz on 13-12-25.
 */
public class CurrentTRAActivity2 extends TabFrame implements View.OnClickListener {
    private ActionListener mUploadListener;
    private AlertDialog mDialog;


    @Override
    public View onCreateView(LayoutInflater inflater) {
        mView = inflater.inflate(R.layout.activity_current_tra,null);



        initViews();
        initClickListeners();
        initIntents();


        setState(STATE_LOADING);
        fetchCurrentActivity();

        mUploadListener = new ActionListener(getActivity()) {
            @Override
            public void onReceive(int action, Bundle data) {
                // TODO
                Toast.makeText(getActivity(),"上传完成",Toast.LENGTH_SHORT).show();
                fetchCurrentActivity();
            }
        };


        return mView;
    }


    private static final int STATE_LOADING = 0;
    private static final int STATE_NO_ACTIVITY = 1;
    private static final int STATE_HAS_ACTIVITY = 2;
    private Intent mTakePicIntent;
    private Intent mTakeVideoRecordIntent;
    private Intent mWriteTextIntent;
    private Intent mAvailableListIntent;
    private boolean isToolShow;


    private View mBtnQuit;

    private View mCollectionButtons;
    private View mLoading;
    private View noContentButton;
    private TextView mName;
    private TextView mTime;
    private TextView mDesc;
    private View mDescWrapper;
    private TRAInfo mInfo;


    private void fetchCurrentActivity() {

        app.doAction(ServiceManager.Constants.ACTION_GET_CURRENT_ACTIVITY_INFO,defaultRequestData,new ActionListener(getActivity()) {
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
        mName.setOnClickListener(this);
    }

    private void initIntents(){
        mTakePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mTakeVideoRecordIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        mTakeAudioRecordIntent = new Intent(this,CollectionAudio.class);
//        mWriteTextIntent = new Intent(this,AddTextActivity.class);

        mAvailableListIntent = new Intent(getActivity(),AvailableActivitiesActivity.class);
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
                showTextDialog();
                break;
            case R.id.btn_goto_available_list:
                i = mAvailableListIntent;
                break;
            case R.id.tra_name:
                i = new Intent(getActivity(),ResourceListActivity.class);
                i.putExtra("result",mInfo.toString());
                break;
            case R.id.btnQuit:
                if(mInfo == null)break;
                defaultRequestData.putString("id", mInfo.id);
                app.doAction(ServiceManager.Constants.ACTION_QUIT_ACTIVITY,defaultRequestData,new ActionListener(getActivity()) {
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
                startActivityForResult(i, viewId);
            }catch(Exception e){//某些机器上启动不了摄像头拍照或录像
                if(viewId == R.id.btnPhoto){
                    Toast.makeText(getActivity(), R.string.launch_camera_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showTextDialog() {
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
        }
        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        mDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Update Status")
                .setMessage(mInfo.title)
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        defaultRequestData.putString("text",value.toString());
                        defaultRequestData.putString("id", mInfo.id);
                        app.doAction(ServiceManager.Constants.ACTION_UPLOAD_TEXT, defaultRequestData, mUploadListener);
                        mDialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }


    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case R.id.btnDraw:// 涂鸦和拍照是同一类型
            case R.id.btnPhoto:
                if(resultCode == Activity.RESULT_OK && data.getExtras() != null){
                    doUpload(data, ServiceManager.Constants.UPLOAD_TYPE_IMAGE, ServiceManager.Constants.ACTION_UPLOAD_PIC);
                }else{
                    Toast.makeText(getActivity(),"cancel",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnRecordVideo:
                if(resultCode == Activity.RESULT_OK){
                    doUpload(data, ServiceManager.Constants.UPLOAD_TYPE_VIDEO, ServiceManager.Constants.ACTION_UPLOAD_VIDEO);
                }else{
                    Toast.makeText(getActivity(),"cancel",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnRecordAudio:
                if(resultCode == Activity.RESULT_OK && data.getExtras() != null){
                    doUpload(data, ServiceManager.Constants.UPLOAD_TYPE_VIDEO, ServiceManager.Constants.ACTION_UPLOAD_AUDIO);
                }else{
                    Toast.makeText(getActivity(),"cancel",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_goto_available_list:
                if(resultCode == Activity.RESULT_OK){//加入成功，拉一次数据
                    fetchCurrentActivity();
                }
                return true;
            case R.id.tra_name:
                break;
            case R.id.btnText:
                if(data != null){
                    app.doAction(ServiceManager.Constants.ACTION_UPLOAD_TEXT,data.getExtras(),mUploadListener);
                }
                return true;
        }
        return false;
    }

    private void doUpload(Uri videoUri, int uploadTypeVideo, int actionUploadVideo) {
        defaultRequestData.putString("id",mInfo.id);
        defaultRequestData.putParcelable("data",videoUri);

    }

    private void doUpload(Intent data, int uploadTypeVideo,int action) {
        Parcelable dataToTranfer = null;
        switch (action){
            case  ServiceManager.Constants.ACTION_UPLOAD_VIDEO:
                dataToTranfer = data.getData();
                break;
            case ServiceManager.Constants.ACTION_UPLOAD_PIC:
                dataToTranfer = (Bitmap) data.getExtras().get("data");
                break;
            case ServiceManager.Constants.ACTION_UPLOAD_AUDIO:// TODO
                return;
        }
        defaultRequestData.putString("id",mInfo.id);
        defaultRequestData.putParcelable("data",dataToTranfer);
        app.doAction(action,defaultRequestData,mUploadListener);
    }
}
