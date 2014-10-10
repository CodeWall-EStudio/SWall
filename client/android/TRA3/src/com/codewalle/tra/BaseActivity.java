package com.codewalle.tra;

import android.app.Activity;
import android.os.Bundle;
import com.codewalle.tra.Model.TRAApp;
import com.koushikdutta.async.http.AsyncHttpClient;

/**
 * Created by xiangzhipan on 14-9-29.
 */
public class BaseActivity extends Activity {
    protected TRAApp app;
    protected String TAG;

    public void setApp(TRAApp app) {
        this.app = app;
        TAG = getClass().getCanonicalName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setApp((TRAApplication)getApplication());
    }
}
