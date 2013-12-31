package com.swall.tra;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import com.swall.tra.adapter.ActivitiesListAdapter;
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
public class ExpiredActivitiesActivity extends BaseFragmentActivity {
    private ListView mListView;
    private ActivitiesListAdapter mAdapter;
    private ActionListener listListener = new ActionListener(ExpiredActivitiesActivity.this) {
        @Override
        public void onReceive(int action, Bundle data) {
            if(data == null){
                // TODO
                // showEmptyOrShowError()
                return;
            }
            String str = data.getString("result");
            Log.i("SWall", TAG + " " + str);
            try {
                JSONObject obj = new JSONObject(str);
                JSONObject resultObject = JSONUtils.getJSONObject(obj, "r", new JSONObject());

                JSONArray array = JSONUtils.getJSONArray(resultObject, "activities", new JSONArray());
                mAdapter.setJSONData(array);
            } catch (JSONException e) {
                // DO nothing
                e.printStackTrace();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expired_list);
        mListView = (ListView)findViewById(R.id.expiredlist);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new String[]{
//                "sdfsdfsdf",
//                "sdfsdf",
//                "sdfsdgdf"
//        });
        mAdapter = new ActivitiesListAdapter(this);
        mListView.setAdapter(mAdapter);


        app.doAction(ServiceManager.Constants.ACTION_GET_EXPIRED_ACTIVITIES, defaultRequestData, listListener);

    }
}