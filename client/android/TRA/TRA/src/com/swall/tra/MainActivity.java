package com.swall.tra;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.swall.tra.network.ActionListener;
import com.swall.tra.network.ServiceManager;
import com.swall.tra.utils.JSONUtils;
import com.umeng.update.UmengUpdateAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ippan on 13-12-24.
 */
public class MainActivity extends BaseFragmentActivity  implements TabHost.TabContentFactory,  TabHost.OnTabChangeListener {
    private static final int RETRY_FETCH_CURRENT = 1;
    private TabHost mTabHost;
    private Map<String,TabFrame> mTabFrames = new HashMap<String,TabFrame>();
    private ProgressDialog mProgressDialog;
    private boolean mTabInited = false;
    private int mRetryCount = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(android.os.Build.VERSION.SDK_INT >= 11){
            getWindow().setFlags(0x01000000, 0x01000000);//开启硬件加速
        }
        setContentView(R.layout.main);

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);


        showProgressDialog();



        setBackConfirm(true);
    }

    private void showProgressDialog() {
/*
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle("正在拉取数据...");
        mProgressDialog.show();
*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCurrentActivity();
    }

    private void fetchCurrentActivity() {

        boolean sent = app.doAction(ServiceManager.Constants.ACTION_GET_CURRENT_ACTIVITY_INFO,defaultRequestData,new ActionListener(this) {
            @Override
            public void onReceive(int action, Bundle data) {
                if(isFinishing()){
                    return;
                }
                dismissProgressDialog();


                String result = "";
                if(data == null){
                    // 网络原因获取失败，需重新获取
                    retryFetchCurrentActivity();
                    return;
                }
                result = data.getString("result");
                try {
                    JSONObject object = new JSONObject(result);
                    if(JSONUtils.getInt(object,"c",-1) == 0){
                        if(gotoCurrentTRAInfo(object)){
                            return;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 没获取到数据，初始化列表
                initTabs();
            }
        });
        if(!sent){

        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case RETRY_FETCH_CURRENT:
                    fetchCurrentActivity();
                    break;
            }
        }
    };
    private void retryFetchCurrentActivity() {
        mRetryCount ++ ;
        if(mRetryCount >= 4){
            Toast.makeText(this,"网络不可用，请检查网络连接",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Toast.makeText(this,"网络连接有问题，3秒后重连..."+mRetryCount,Toast.LENGTH_SHORT).show();
        mHandler.sendEmptyMessageDelayed(RETRY_FETCH_CURRENT,3000);
    }

    private void dismissProgressDialog() {
//        mProgressDialog.dismiss();
    }

    private boolean gotoCurrentTRAInfo(JSONObject object) {
        JSONObject resultObject = JSONUtils.getJSONObject(object,"r",null);
        if(resultObject != null){
            JSONObject profiles = JSONUtils.getJSONObject(resultObject,"profiles",new JSONObject());
            JSONArray activities = JSONUtils.getJSONArray(resultObject,"activities",new JSONArray());
            if(activities.length() > 0){
                JSONObject activity = JSONUtils.arrayGetJSONObject(activities,0);
                if(activity == null){
                    return false;
                }
                try {
                    activity.put("profiles",profiles);
                } catch (JSONException e) {
                    Log.e(TAG,"put profile failed ");
                }
                Intent i = new Intent(this, CurrentTRAActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("result",activity.toString());
                startActivity(i);
                finish();
                return true;
            }
        }
        return false;
    }

    private void initTabs() {

        if(!mTabInited){
            mTabInited = true;
        }else{
            return;
        }
        mTabHost.setup();
        mTabHost.setOnTabChangedListener(this);

        //mTabHost.addTab(getTabSpec(CollectionFrame.class,R.string.collection_tab_name));
        //mTabHost.addTab(getTabSpec(TimelineFrame.class,R.string.timeline_tab_name));
        mTabHost.addTab(getTabSpec(AvailableFrame.class,R.string.tab_current_activities));
        mTabHost.addTab(getTabSpec(ExpiredActivitiesFrame.class,R.string.tab_expired_activities));
        //mTabHost.addTab(getTabSpec(SettingActivity2.class,R.string.tab_settings));

    }

    private TabHost.TabSpec getTabSpec(Class<? extends TabFrame> tabFrameClass,int resourceId){
        return getTabSpec(tabFrameClass, getApplicationContext().getResources().getString(resourceId));
    }

    private TabHost.TabSpec getTabSpec(Class<? extends TabFrame> tabFrameClass,String indicator) {
        TabHost.TabSpec tabSpec = mTabHost.newTabSpec(tabFrameClass.getName());
        tabSpec.setIndicator(indicator);
        tabSpec.setContent(this);
        tabSpec.setIndicator(getIndicatorView(indicator));
        return tabSpec;
    }

    private View getIndicatorView(String indicator) {
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.nav_tab_item,null,false);
        TextView tv = (TextView) v.findViewById(R.id.textView);
        tv.setText(indicator);
        return v;
    }

    @Override
    public void onTabChanged(String classTag) {
    }

    @Override
    public View createTabContent(String classTag) {
        TabFrame tf = null;
        try {
            tf = (TabFrame)Class.forName(classTag).newInstance();
            tf.setActivity(this);
            mTabFrames.put(classTag, tf);
            return tf.onCreateView(getLayoutInflater());

        } catch (Exception e){
            // do nothing
        }

        return null;
    }

    private TabFrame getCurrentTab(){
        String tag = mTabHost.getCurrentTabTag();
        return mTabFrames.get(tag);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        getCurrentTab().onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(RETRY_FETCH_CURRENT);
        for(TabFrame tf:mTabFrames.values()){
            tf.onDestroy();
        }
    }
}