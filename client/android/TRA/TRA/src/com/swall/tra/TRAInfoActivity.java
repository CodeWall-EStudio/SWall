package com.swall.tra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.swall.tra.adapter.ActivityResourceAdapter;
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
public class TRAInfoActivity extends BaseActivity implements View.OnClickListener {

    private static final int STATE_LOADING = 0;
    private static final int STATE_NO_ACTIVITY = 1;
    private static final int STATE_HAS_ACTIVITY = 2;
    private Intent mTakePicIntent;
    private Intent mTakeVideoRecordIntent;
    private Intent mWriteTextIntent;
    private boolean isToolShow;

    private ActionListener mUploadListener = new ActionListener(TRAInfoActivity.this) {
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
    private View mJoinButton;
    private TRAInfo mInfo;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tra_info);

        initViews();
        initClickListeners();
        initIntents();

        setState(STATE_LOADING);

        if(!jumpFromList()){
            //fetchCurrentActivity();
        }
    }

    // TODO 此方法名有问题
    private boolean jumpFromList() {
        Intent intent = getIntent();
        if(intent != null){
            Bundle data = intent.getExtras();
            dealWithData(data);
        }
        return true;

    }

    private void fetchCurrentActivity() {

        app.doAction(Constants.ACTION_GET_CURRENT_ACTIVITY_INFO,defaultRequestData,new ActionListener(this) {
            @Override
            public void onReceive(int action, Bundle data) {
                dealWithData(data);
            }
        });
    }

    protected void dealWithData(Bundle data) {
        if(data == null){
            setState(STATE_NO_ACTIVITY);
            return;
        }
        String str = data.getString("result");
        try {
            JSONObject object = new JSONObject(str);
            if(object.has("r")){
                object = JSONUtils.getJSONObject(object,"r",new JSONObject());
                JSONArray array = JSONUtils.getJSONArray(object,"activities",new JSONArray());
                if(array.length() < 1){
                    setState(STATE_NO_ACTIVITY);
                    return;
                }else{
                    object = JSONUtils.arrayGetJSONObject(array,0);
                }
            }
            TRAInfo info = new TRAInfo(object);
            initTRAInfo(info);
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
                mJoinButton.setVisibility(View.GONE);
                break;
            case STATE_NO_ACTIVITY:
                noContentButton.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);
                
                mCollectionButtons.setVisibility(View.GONE);
                mDescWrapper.setVisibility(View.GONE);
                mJoinButton.setVisibility(View.GONE);
                break;
            case STATE_HAS_ACTIVITY:
                noContentButton.setVisibility(View.GONE);
                mLoading.setVisibility(View.GONE);
                mDescWrapper.setVisibility(View.VISIBLE);

                if(mInfo.participators.indexOf(currentAccount.userName) >= 0){
                    mCollectionButtons.setVisibility(View.VISIBLE);
                    mJoinButton.setVisibility(View.GONE);;
                }else{
                    mCollectionButtons.setVisibility(View.GONE);
                    mJoinButton.setVisibility(View.VISIBLE);
                }
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
        this.mInfo = info;
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
        mJoinButton.setOnClickListener(this);
    }

    private void initIntents(){
        mTakePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mTakeVideoRecordIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        mTakeAudioRecordIntent = new Intent(this,CollectionAudio.class);
//        mWriteTextIntent = new Intent(this,AddTextActivity.class);


    }


    private void initViews() {
        mBtnQuit = findViewById(R.id.btnQuit);
        mCollectionButtons = findViewById(R.id.collection_panel_wrapper);
        mLoading = findViewById(R.id.loading);
        noContentButton = findViewById(R.id.btn_goto_available_list);
        mDescWrapper = findViewById(R.id.tra_info_detail_wrapper);
        mJoinButton = findViewById(R.id.join_button);


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
            case R.id.join_button:
                i = new Intent(this,JoinActivityIntent.class);
                i.putExtra("id",mInfo.id);
                break;
            default:
                break;

        }
        if(i != null){
            try{
                startActivityForResult(i, viewId);
            }catch(Exception e){//某些机器上启动不了摄像头拍照或录像
                Toast.makeText(this,R.string.launch_camera_fail,Toast.LENGTH_LONG).show();
            }
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
            case R.id.join_button:
                if(resultCode == RESULT_OK){
                    SharedPreferences sp = getApplicationContext().getSharedPreferences("current",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("result",mInfo.toString());
                    editor.commit();
                    Log.i("SWall", "result:"+sp.getString("result", ""));
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            case R.id.btnText:
                if(data == null)return;
                app.doAction(Constants.ACTION_UPLOAD_TEXT,data.getExtras(),mUploadListener);
                break;
        }
    }
}