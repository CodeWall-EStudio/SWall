package com.codewalle.tra;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import org.androidannotations.annotations.EFragment;

/**
 * Created by xiangzhipan on 14-10-3.
 */
public class BaseFragment extends Fragment {
    protected TRAApplication app;
    protected String TAG;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = TRAApplication.getApplication();
        TAG = getClass().getCanonicalName();
    }
}
