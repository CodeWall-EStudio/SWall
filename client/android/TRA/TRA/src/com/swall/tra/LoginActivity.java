package com.swall.tra;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import com.swall.tra.model.AccountInfo;
import com.swall.tra.network.ActionListener;
import com.swall.tra.network.ServiceManager;
import com.swall.tra.widget.InputMethodRelativeLayout;

/**
 * Created by pxz on 13-12-28.
 */
public class LoginActivity extends BaseFragmentActivity implements View.OnClickListener, InputMethodRelativeLayout.onSizeChangedListenner {

    ActionListener listener = new ActionListener(LoginActivity.this) {
        @Override
        public void onReceive(int action, Bundle data) {

            switch (action){
                case ServiceManager.Constants.ACTION_LOGIN:
                    dismissLoginProgressDialog();

                    if(data != null && data.getBoolean(ServiceManager.Constants.KEY_STATUS,false)){
                        /*
                        SharedPreferences prefs = getSharedPreferences(SettingActivity.PRE_NAME, Context.MODE_PRIVATE);
                        boolean autoLogin =prefs.getBoolean("auto_login", true);
                        mLoginData.putBoolean(ServiceManager.Constants.KEY_AUT_LOGIN,autoLogin);
                        app.doAction(ServiceManager.Constants.ACTION_UPDATE_ACCOUNT,mLoginData,null);
                        */

                        String userName = mEtUserName.getText().toString();
                        String pwd = mEtPassword.getText().toString();
                        app.updateCurrentAccount(new AccountInfo(userName,pwd),true);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        Log.i(TAG, "login success");
                        finish();
                    }else{
                        enableLoginActoins();

                        //mEtPassword.setText("");
                        //mEtUserName.setText("");
                        findViewById(R.id.login_button).setEnabled(true);
                    }
                    break;
                case ServiceManager.Constants.ACTION_GET_ACCOUNTS:
                    if(data == null)return;

                    updateAccounts(data);

                    break;
            }
        }
    };


    private Bundle mLoginData;
    private AlertDialog mProgressDialog;
    private InputMethodRelativeLayout mRootLayout;
    private ImageView mSchoolNameView;


    private void updateAccounts(Bundle data) {
        String name = data.getString(ServiceManager.Constants.KEY_USER_NAME);
        String pwd = data.getString(ServiceManager.Constants.KEY_PASSWORD);
        if(!"".equals(name) && TextUtils.isEmpty(mEtUserName.getText().toString())){
            mEtUserName.setText(name);
            mEtPassword.setText(pwd);
        }
    }


    private EditText mEtUserName;
    private EditText mEtPassword;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        app.addObserver(ServiceManager.Constants.ACTION_LOGIN, listener);
        app.addObserver(ServiceManager.Constants.ACTION_GET_ACCOUNTS,listener);

        mEtUserName = (EditText)findViewById(R.id.username);
        mEtPassword = (EditText)findViewById(R.id.password);
        mRootLayout = (InputMethodRelativeLayout)findViewById(R.id.root);
        mSchoolNameView = (ImageView)findViewById(R.id.login_school_name);

        mRootLayout.setOnSizeChangedListenner(this);
        findViewById(R.id.login_button).setOnClickListener(this);

        mProgressDialog = new AlertDialog.Builder(this)
                .setTitle("登录中，请稍候...")
                .setCancelable(false)
                .create();

        app.doAction(ServiceManager.Constants.ACTION_GET_ACCOUNTS,null,null);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        app.removeObserver(listener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_button:
                // hide soft keyboard
                ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                String userName = mEtUserName.getText().toString();
                String pwd = mEtPassword.getText().toString();

                mLoginData = new Bundle();
                if(TextUtils.isEmpty(userName) || TextUtils.isEmpty(pwd))return;
                mLoginData.putString(ServiceManager.Constants.KEY_USER_NAME, userName);
                mLoginData.putString(ServiceManager.Constants.KEY_PASSWORD, pwd);
                app.doAction(ServiceManager.Constants.ACTION_LOGIN,mLoginData,listener);

                disableLoginActions();
                showLoginProgressDialog();
                break;
        }
    }

    private void showLoginProgressDialog() {
        // TODO
        mProgressDialog.show();
    }
    private void dismissLoginProgressDialog(){
        // TODO
        mProgressDialog.dismiss();
    }

    private void enableLoginActoins() {
        //findViewById(R.id.login_status).setVisibility(View.GONE);
        findViewById(R.id.login_button).setEnabled(true);
    }
    private void disableLoginActions() {

        //findViewById(R.id.login_status).setVisibility(View.VISIBLE);
        findViewById(R.id.login_button).setEnabled(false);
    }

    @Override
    public void onSizeChange(boolean isOpen, int preH, int curH) {
        if(isOpen){
            mSchoolNameView.setImageResource(R.drawable.login_school_input);
        }else{
            mSchoolNameView.setImageResource(R.drawable.login_school_normal);
        }
    }
}