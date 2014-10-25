package com.codewalle.tra.widget;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.codewalle.tra.BaseFragment;
import com.codewalle.tra.R;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by xiangzhipan on 14-10-13.
 */

@Deprecated
@EFragment(R.layout.activity_list)
public class ExpiredActivitiesFragment extends BaseFragment {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


}
