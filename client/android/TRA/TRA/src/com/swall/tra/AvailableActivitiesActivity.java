package com.swall.tra;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.swall.tra.adapter.ActivitiesListAdapter;
import com.swall.tra.model.TRAInfo;
import com.swall.tra.network.ActionListener;
import com.swall.tra.network.ServiceManager;
import com.swall.tra.utils.JSONUtils;
import com.swall.tra2.BaseFragmentActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pxz on 13-12-24.
 */
public class AvailableActivitiesActivity extends BaseFragmentActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String KEY_NAME = "name";
    private static final String KEY_CLASS = "class";
    private static final String KEY_COUNT = "count";
    private static final String KEY_RESOURCES = "resouces";
    private static final String KEY_JOINED = "joined";
    private static final int REQUEST_ITEM = 0;
    private PullToRefreshListView mPullToRefreshListView;
    private ActivitiesListAdapter mAdapter;



    private ListView mListView;

    private ActionListener listListener = new ActionListener(AvailableActivitiesActivity.this) {
        @Override
        public void onReceive(int action, Bundle data) {
            removeLoading();
            if(data == null){
                // TODO 处理不同错误类型给出提示
                Toast.makeText(AvailableActivitiesActivity.this,"获取数据失败",Toast.LENGTH_SHORT).show();
                return;
            }
            String str = data.getString("result");
            try {
                JSONObject object = new JSONObject(str);
                int code = object.getInt("c");
                switch(code){
                    case 0:
                        JSONObject resultObject = JSONUtils.getJSONObject(object,"r",new JSONObject());

                        JSONArray array = JSONUtils.getJSONArray(resultObject, "activities", new JSONArray());
                        mAdapter.setJSONData(array);
                        break;
                    default:
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // TODO
                Log.e("SWall",TAG+"listListener",e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_available_list);
        mPullToRefreshListView = (PullToRefreshListView)findViewById(R.id.availableListView);
        mAdapter = new ActivitiesListAdapter(this);
        mListView = mPullToRefreshListView.getRefreshableView();
        mListView.setAdapter(mAdapter);

        mListView.setEmptyView(null);
        mListView.setOnItemClickListener(this);

        //mAdapter.setJSONData(EXAMPLE);
        fetchData();
    }

    private void fetchData() {

        app.doAction(ServiceManager.Constants.ACTION_GET_AVAILABLE_ACTIVITIES, defaultRequestData, listListener);
        showLoading();
    }

    private void showLoading() {
    }
    private void removeLoading(){
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override public void onDestroy(){
        super.onDestroy();
        app.removeObserver(listListener);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        TRAInfo info = (TRAInfo)adapterView.getAdapter().getItem(position);
        Intent i = new Intent(this,TRAInfoActivity.class);
        Bundle bundle = new Bundle();
        Log.i("JSON", info.toString());
        bundle.putString("result", info.toString());
        i.putExtras(bundle);
        startActivityForResult(i, REQUEST_ITEM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ITEM && resultCode == RESULT_OK){
            // 加入活动成功
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
        }
    }
}