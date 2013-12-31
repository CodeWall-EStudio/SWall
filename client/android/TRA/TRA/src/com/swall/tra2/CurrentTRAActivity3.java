package com.swall.tra2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListFragment;
import com.swall.tra.*;
import com.swall.tra.adapter.ActivityResourceAdapter;
import com.swall.tra.model.ResourceInfo;
import com.swall.tra.model.TRAInfo;
import com.swall.tra.network.ActionListener;
import com.swall.tra.network.ServiceManager;
import com.swall.tra.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pxz on 13-12-28.
 */
public class CurrentTRAActivity3 extends BaseFragmentActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final int REQUEST_ID_PHOTO = 0x01;
    private static final int REQUEST_ID_VIDEO = 0x02;
    private static final int REQUEST_ID_TEXT = 0x03;
    ActivityResourceAdapter mAdapter = new ActivityResourceAdapter();
    private ListView mListView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_tra3);
        mListView = (ListView)findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);

        mBtnQuit = findViewById(R.id.btnQuit);



        initClickListeners();
        initIntents();


        String str = getIntent().getStringExtra("result");


        mInfo = mAdapter.setJSONData(str);
        if(mInfo == null){
            fetchCurrentActivity();
        }else{

            // header view
            View headerView = getLayoutInflater().inflate(R.layout.tra_info_detail,null,false);
            TextView name = (TextView)headerView.findViewById(R.id.tra_name);
            TextView time = (TextView)headerView.findViewById(R.id.tra_time);
            TextView desc = (TextView)headerView.findViewById(R.id.tra_intro);
            name.setText(mInfo.title);
            time.setText(mInfo.getTimeFormated());
            desc.setText(mInfo.getAllDesc());
            mListView.addHeaderView(headerView);
        }


        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mUploadListener = new ActionListener(this) {
            @Override
            public void onReceive(int action, Bundle data) {
                // TODO
                Toast.makeText(CurrentTRAActivity3.this, "上传完成", Toast.LENGTH_SHORT).show();
                fetchCurrentActivity();
            }
        };



    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ResourceInfo info = (ResourceInfo)parent.getAdapter().getItem(position);
        if(info.type == ServiceManager.Constants.UPLOAD_TYPE_VIDEO){
            Uri uri = Uri.parse(info.content);
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "video/3gp");
            intent.putExtra(Intent.EXTRA_TITLE, "新媒体教研");
            startActivity(intent);
        }else if(info.type == ServiceManager.Constants.UPLOAD_TYPE_IMAGE){
            String url = info.content;
            if(url.startsWith("http")){
                Intent i = new Intent(this,ImageViewActivity.class);
                i.putExtra("url",url);
                startActivity(i);
            }else{
                // do nothing
            }

        }
    }


    private ActionListener mUploadListener;
    private AlertDialog mDialog;




    private static final int STATE_LOADING = 0;
    private static final int STATE_NO_ACTIVITY = 1;
    private static final int STATE_HAS_ACTIVITY = 2;
    private Intent mTakePicIntent;
    private Intent mTakeVideoRecordIntent;
    private Intent mWriteTextIntent;
    private Intent mAvailableListIntent;


    private View mBtnQuit;
    private TRAInfo mInfo;


    private void fetchCurrentActivity() {

        app.doAction(ServiceManager.Constants.ACTION_GET_CURRENT_ACTIVITY_INFO,defaultRequestData,new ActionListener(this) {
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
            return;
        }
        try {
            JSONObject object = new JSONObject(str);
            if(object.has("r")){
                object = JSONUtils.getJSONObject(object, "r", new JSONObject());
                JSONArray array = JSONUtils.getJSONArray(object,"activities",new JSONArray());
                if(array.length() < 1){
                    return;
                }else{
                    object = JSONUtils.arrayGetJSONObject(array,0);
                }
            }
            mInfo = new TRAInfo(object);
            mAdapter.setTRAInfo(mInfo);
        } catch (JSONException e) {
        }
    }



    private void initClickListeners() {
//        findViewById(R.id.btnDraw).setOnClickListener(this);
        findViewById(R.id.btnPhoto).setOnClickListener(this);
//        findViewById(R.id.btnRecordAudio).setOnClickListener(this);
        findViewById(R.id.btnRecordVideo).setOnClickListener(this);
        findViewById(R.id.btnText).setOnClickListener(this);

        mBtnQuit.setOnClickListener(this);
    }

    private void initIntents(){
        mTakePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mTakePicIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 100);


        mTakeVideoRecordIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // 录像过程中按home ,再次进入时不需再显示
        mTakePicIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mTakeVideoRecordIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

//        mTakePicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        mTakeVideoRecordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        mTakeAudioRecordIntent = new Intent(this,CollectionAudio.class);
//        mWriteTextIntent = new Intent(this,AddTextActivity.class);

//        mAvailableListIntent = new Intent(this,AvailableActivitiesActivity.class);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        int reqId = -1;
        Intent i = null;
        switch (viewId) {
            case R.id.btnPhoto:
                i = mTakePicIntent;
                reqId = REQUEST_ID_PHOTO;
                break;
            case R.id.btnRecordVideo:
                i = mTakeVideoRecordIntent;
                reqId = REQUEST_ID_VIDEO;
                break;
            case R.id.btnText:
                showTextDialog();
                reqId = REQUEST_ID_TEXT;
                break;
            case R.id.btn_goto_available_list:
                i = mAvailableListIntent;
                break;
            case R.id.tra_name:
                i = new Intent(this,ResourceListActivity.class);
                i.putExtra("result",mInfo.toString());
                break;
            case R.id.btnQuit:
                confirmQuit();
                break;
            default:
                break;

        }
        if(i != null){
            try{
                startActivityForResult(i, reqId);
            }catch(Exception e){//某些机器上启动不了摄像头拍照或录像
                if(viewId == R.id.btnPhoto){
                    Toast.makeText(this, R.string.launch_camera_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void gotoMainActivity() {
        Intent i = new Intent(this, com.swall.tra.MainActivity2.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void showTextDialog() {
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
        }
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        mDialog  = new AlertDialog.Builder(this)
                .setTitle("添加文字评论")
                .setMessage(mInfo.title)
                .setView(input)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        defaultRequestData.putString("text",value.toString());
                        defaultRequestData.putString("id", mInfo.id);
                        app.doAction(ServiceManager.Constants.ACTION_UPLOAD_TEXT, defaultRequestData, mUploadListener);
                        mDialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
//            case R.id.btnDraw:// 涂鸦和拍照是同一类型
            case REQUEST_ID_PHOTO:
                if(resultCode == Activity.RESULT_OK && data.getExtras() != null){
                    doUpload(data, ServiceManager.Constants.UPLOAD_TYPE_IMAGE, ServiceManager.Constants.ACTION_UPLOAD_PIC);
                }else{
                    Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_ID_VIDEO:
                if(resultCode == Activity.RESULT_OK){
                    doUpload(data, ServiceManager.Constants.UPLOAD_TYPE_VIDEO, ServiceManager.Constants.ACTION_UPLOAD_VIDEO);
                }else{
                    Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
                }
                break;
//            case R.id.btnRecordAudio:
//            if(resultCode == Activity.RESULT_OK && data.getExtras() != null){
//                doUpload(data, ServiceManager.Constants.UPLOAD_TYPE_VIDEO, ServiceManager.Constants.ACTION_UPLOAD_AUDIO);
//            }else{
//                Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
//            }
//            break;
            case R.id.btn_goto_available_list:
                if(resultCode == Activity.RESULT_OK){//加入成功，拉一次数据
                    fetchCurrentActivity();
                }
                break;
            case R.id.tra_name:
                break;
            case REQUEST_ID_TEXT:
                if(data != null){
                    app.doAction(ServiceManager.Constants.ACTION_UPLOAD_TEXT,data.getExtras(),mUploadListener);
                }
                break;
        }
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

    AlertDialog mQuitConfirmDialog;
    @Override
    public void onBackPressed() {
        confirmQuit();
    }

    private void confirmQuit() {

        if(mQuitConfirmDialog == null){
            mQuitConfirmDialog = new AlertDialog.Builder(this)
                    .setTitle("是否退出本活动?")
                    .setCancelable(false)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mQuitConfirmDialog.dismiss();
                        }
                    })
                    .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mQuitConfirmDialog.dismiss();
                            quitActivity();
                        }
                    })
                    .create();
        }
        mQuitConfirmDialog.show();
    }

    private void quitActivity() {
        if(mInfo == null)return;
        defaultRequestData.putString("id", mInfo.id);
        app.doAction(ServiceManager.Constants.ACTION_QUIT_ACTIVITY,defaultRequestData,new ActionListener(this) {
            @Override
            public void onReceive(int action, Bundle data) {
//                        fetchCurrentActivity();
                gotoMainActivity();

            }
        });
    }
}