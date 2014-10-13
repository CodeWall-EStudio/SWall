package com.codewalle.tra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.codewalle.tra.Model.AccountInfo;
import com.codewalle.tra.Network.RequestError;
import com.codewalle.tra.utils.TRAResponseParser;
import com.codewalle.tra.utils.Utils;
import com.codewalle.tra.widget.IMERelativeLayout;
import com.koushikdutta.async.future.FutureCallback;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiangzhipan on 14-9-29.
 */
@EActivity(R.layout.login)
public class LoginActivity extends BaseActivity implements IMERelativeLayout.onSizeChangedListenner, FutureCallback<String> {

    @ViewById(R.id.login_root)
    IMERelativeLayout mRoot;

    @ViewById(R.id.login_school_name)
    ImageView mSchoolNameView;

    @ViewById(R.id.login_button)
    Button mLoginButton;

    @ViewById(R.id.username)
    EditText mUserName;

    @ViewById(R.id.password)
    EditText mPassword;

    private boolean mIsRequesting;


    @AfterViews
    public void setupBackgroud(){
        mRoot.setBackgroundColor(app.getBgColor());
    }

    @Click
    public void loginButton(){
        try {
            doRequest();
        } catch (JSONException e) {
            updateState(LoginState.FAIL);
            Utils.toast(this,"登录出错");
        }


    }

    private void doRequest() throws JSONException{

        String username = mUserName.getText().toString();
        String password = mPassword.getText().toString();

        updateState(LoginState.REQUESTING);
        app.doLogin(this, username, password, this);
    }


    private void updateState(LoginState loginState) {
        Utils.toast(this, "loginstate " + loginState.toString() + Thread.currentThread().getName());
        switch (loginState){
            case SUCCESS:
                finish();
                startActivity(new Intent(this,MainActivity_.class));
                break;
            case REQUESTING:
                // todo Disabled all buttons and show dialog
                break;
            case TIME_OUT:
            case FAIL:
            default:
                // todo dismiss all dialog and enable buttons
                break;
        }
    }


    @AfterViews
    public void initListeners(){
        mRoot.setOnSizeChangedListenner(this);
    }

    @Override
    public void onSizeChange(boolean isOpen, boolean changed,int preH, int curH) {
        if(!changed)return;
        if(isOpen){
            mSchoolNameView.setImageResource(R.drawable.login_top_image_inputing);
        }else{
            mSchoolNameView.setImageResource(R.drawable.login_top_image_normal);
        }
    }

    @Override
    public void onCompleted(Exception e, String str) {
        JSONObject result = null;
        try {
            Log.i(TAG,"z"+str);
            result = new JSONObject(""+str);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        if(e != null){
            updateState(LoginState.FAIL);
            // TODO network problem?
            Log.e(TAG,e.getMessage(),e );
        }else{
            try {
                Log.i(TAG,"result"+result.toString(4));
            } catch (JSONException e1) {
                e1.printStackTrace();
            };

            Pair<AccountInfo,RequestError> parseResult = TRAResponseParser.parseLogin(result,mUserName.getText().toString(),mPassword.getText().toString());
            AccountInfo accountInfo = parseResult.first;
            RequestError error = parseResult.second;
            if(accountInfo != null){
                app.cacheAccount(accountInfo);
                updateState(LoginState.SUCCESS);
            }else{
                updateState(LoginState.FAIL);
            }
        }
    }
}

