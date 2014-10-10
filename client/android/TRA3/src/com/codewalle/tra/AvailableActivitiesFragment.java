package com.codewalle.tra;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.androidannotations.annotations.EFragment;

/**
 * Created by xiangzhipan on 14-10-3.
 */

@EFragment(R.layout.activity_list)
public class AvailableActivitiesFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("AAA", "oncreateview");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("AAA","onActivityCreated");
    }
}
