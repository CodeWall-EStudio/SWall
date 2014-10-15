package com.codewalle.tra;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.codewalle.tra.Model.TRAInfo;
import com.codewalle.tra.adapter.ActivitiesListAdapter;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

/**
 * Created by xiangzhipan on 14-10-3.
 */

@EFragment(R.layout.activity_list)
public class TRAListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {


    private ActivitiesListAdapter mAdapter;


    @ViewById(R.id.root)
    SwipeRefreshLayout rootLayout;

    @ViewById(R.id.listview)
    ListView listView;
    private boolean mIsExpired;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        Log.i("AAA","bundle ?"+(args==null?"null":(""+args.getBoolean("expired"))));

        if(args != null){
            mIsExpired = args.getBoolean("expired",false);
        }else{
            mIsExpired = false;
        }
        Log.i("AAA", "onActivityCreated "+mIsExpired);

        getTRAList();
        rootLayout.setOnRefreshListener(this);

        mAdapter = new ActivitiesListAdapter();
        listView.setAdapter(mAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        getTRAList();
    }

    void getTRAList(){
        rootLayout.setRefreshing(true);
        app.getTRAList(!mIsExpired,new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if (result != null) {
                    Log.d(TAG, result.toString());
                    JsonObject resultObject = result.getAsJsonObject("r");

                    mAdapter.setJSONData(resultObject);
                } else {
                    Log.d(TAG, "err", e);
                }


                rootLayout.setRefreshing(false);
            }
        });
    }


    @ItemClick(R.id.listview)
    void onItemClicked(TRAInfo info){
        Intent i = new Intent(getActivity(),TRAInfoActivity_.class);
        Bundle bundle = new Bundle();
        Log.i("JSON", info.toString());
        bundle.putString("result", info.toString());
        bundle.putBoolean("joinable", !mIsExpired);
        i.putExtras(bundle);
        startActivity(i);
    }

    @Override
    public void onRefresh() {
        getTRAList();
    }
}
