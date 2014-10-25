package com.codewalle.tra;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.codewalle.tra.Model.ActivityComment;
import com.codewalle.tra.Model.ActivityImageComment;
import com.codewalle.tra.Model.ActivityTextComment;
import com.codewalle.tra.Model.TRAInfo;
import com.codewalle.tra.Network.RequestError;
import com.codewalle.tra.adapter.ActivityResourceAdapter;
import com.codewalle.tra.utils.TRAResponseParser;
import com.codewalle.tra.utils.Utils;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import org.androidannotations.annotations.*;

import java.io.File;
import java.util.List;

/**
 * Created by xiangzhipan on 14-10-13.
 */
@EActivity(R.layout.activity_current_joined)
public class CurrentTRAActivity extends BaseFragmentActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final int REQUEST_ID_PHOTO = 0x01;
    private static final int REQUEST_ID_VIDEO = 0x02;
    private static final int REQUEST_ID_TEXT = 0x03;


    @ViewById(R.id.listview)
    ListView mListView;

    @ViewById(R.id.swipeLayout)
    SwipeRefreshLayout mRootLayout;

    @ViewById(R.id.title_tips)
    TextView mTitleTips;


    private Intent mTakePicIntent;
    private Intent mTakeVideoRecordIntent;
    private Intent mWriteTextIntent;
    private TRAInfo mInfo;
    private ActivityResourceAdapter mAdapter;
    private String mPicFilePath;
    private String mVedioFilePath;

    @Click({R.id.btnPhoto,R.id.btnText,R.id.btnRecordVideo,R.id.btnQuit})
    void onSendCommentClick(View v){
        int viewId = v.getId();
        int reqId = -1;
        Intent i = null;
        switch (viewId) {
            case R.id.btnPhoto:
                i = mTakePicIntent;
                mPicFilePath = getUriPath(".jpg");
                if(mPicFilePath == null){
                    Toast.makeText(this, "存储空间不足.", Toast.LENGTH_SHORT).show();
                    return;
                }
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mPicFilePath)));
                reqId = REQUEST_ID_PHOTO;
                break;
            case R.id.btnRecordVideo:
                i = mTakeVideoRecordIntent;
                mVedioFilePath = getUriPath(".3gp");
                if(mVedioFilePath == null){
                    Toast.makeText(this,"存储空间不足.",Toast.LENGTH_SHORT).show();
                    return;
                }
                i.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(mVedioFilePath)));
                reqId = REQUEST_ID_VIDEO;
                break;
            case R.id.btnText:
                i = mWriteTextIntent;
                reqId = REQUEST_ID_TEXT;
                break;
            case R.id.btnQuit:
                quitActivity();
                break;
            default:
                break;

        }
        if(i != null){
            setCollectionButtonEnabled(false);
            try{
                startActivityForResult(i, reqId);
            }catch(Exception e){//某些机器上启动不了摄像头拍照或录像
                if(viewId == R.id.btnPhoto){
                    Toast.makeText(this, R.string.launch_camera_fail, Toast.LENGTH_LONG).show();
                }
                setCollectionButtonEnabled(true);
            }
        }
    }
    public String getUriPath(String postFix){
        File f = new File(Utils.getExternalDir()+"/tra/");
        if(!f.exists()){
            f.mkdir();
        }
        return Utils.getExternalDir()+"/tra/"+System.currentTimeMillis()+postFix;
    }

    private void quitActivity() {
        app.quitActivity(mInfo.id,new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                Intent intent = new Intent(CurrentTRAActivity.this,MainActivity_.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }



    @AfterViews
    public void init(){



        String str = getIntent().getStringExtra("result");
        mAdapter = new ActivityResourceAdapter();
        mInfo = mAdapter.setJSONData(str);
        if(mInfo == null){
            // TODO
            finish();
            return;
        }

        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView listView, int i) {

            }

            @Override
            public void onScroll(AbsListView listView, int i, int i2, int i3) {
                int topRowVerticalPosition =
                        (listView ==null || listView.getChildCount() == 0)?
                                0:
                                listView.getChildAt(0).getTop();
                mRootLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });



        mRootLayout.setOnRefreshListener(this);
        mTakePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mTakePicIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 100);
//        mPicFilePath = Utils.getExternalDir()+"/"+ System.currentTimeMillis()+".jpg";
//        mTakePicIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(mPicFilePath)));


        mTakeVideoRecordIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        mTakeVideoRecordIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 100);
        mTakeVideoRecordIntent.putExtra(MediaStore.EXTRA_SHOW_ACTION_ICONS,true);
//        mVedioFilePath = Utils.getExternalDir()+"/"+ System.currentTimeMillis()+".3gp";
//        mTakeVideoRecordIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(mVedioFilePath)));

        // 录像过程中按home ,再次进入时不需再显示
        mTakePicIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mTakeVideoRecordIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

//        mTakePicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        mTakeVideoRecordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        mTakeAudioRecordIntent = new Intent(this,CollectionAudio.class);
        mWriteTextIntent = new Intent(this,AddTextResourceActivity_.class);
        mWriteTextIntent.putExtra("title",mInfo.title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
//            case R.id.btnDraw:// 涂鸦和拍照是同一类型
            case REQUEST_ID_PHOTO:
                if(resultCode == Activity.RESULT_OK){
                    doUploadImage(data);
                }else{
//                    Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_ID_VIDEO:
                if(resultCode == Activity.RESULT_OK){
                    doUploadVideo(data);
                }else{
//                    Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
                }
                break;
//            case R.id.btnRecordAudio:
//            if(resultCode == Activity.RESULT_OK && data.getExtras() != null){
//                doUpload(data, ServiceManager.Constants.UPLOAD_TYPE_VIDEO, ServiceManager.Constants.ACTION_UPLOAD_AUDIO);
//            }else{
//                Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
//            }
//            break;
            case REQUEST_ID_TEXT:
                if(resultCode == RESULT_OK){
                    doUploadText(data.getStringExtra("text"));
                }
                break;
        }
    }

    private void doUploadVideo(Intent data) {

    }

    private void doUploadText(String text) {
        ActivityComment comment = new ActivityTextComment(text);
        app.postComment(mInfo,comment,new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                refrshCurrentActivity();
            }
        });
    }

    private void doUploadImage(Intent data) {
        ActivityImageComment comment = new ActivityImageComment("",mPicFilePath,"");
        app.postComment(mInfo,comment,new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                refrshCurrentActivity();
            }
        });
    }


    private void setCollectionButtonEnabled(boolean enabled) {

    }

    @Override
    public void onRefresh() {
        refrshCurrentActivity();
    }

    private void refrshCurrentActivity() {
        app.getJoinedActivityInfo(new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                mRootLayout.setRefreshing(false);
                Pair<TRAInfo,RequestError> parsed =  TRAResponseParser.parseJoinedActivity(result, e);
                TRAInfo info = null;
                RequestError error = null;
                if(parsed != null) {
                    info = parsed.first;
                    error = parsed.second;
                }
                mInfo = info;
                mAdapter.setTRAInfo(info);
            }
        });
    }
}