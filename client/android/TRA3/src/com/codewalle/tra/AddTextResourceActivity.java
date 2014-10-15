package com.codewalle.tra;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.androidannotations.annotations.BeforeTextChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;

/**
 * Created by xiangzhipan on 14-10-13.
 */
@EActivity(R.layout.activity_add_resource)
public class AddTextResourceActivity extends BaseFragmentActivity {


    @Click({R.id.btnAbort,R.id.btnSave})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAbort:
                finish();
                break;
            case R.id.btnSave:
                Intent intent = new Intent();
                intent.putExtra("text",((EditText)(findViewById(R.id.editText))).getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    @TextChange(R.id.editText)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(TextUtils.isEmpty(s)){
            findViewById(R.id.btnSave).setEnabled(false);
        }else{
            findViewById(R.id.btnSave).setEnabled(true);
        }
    }

}