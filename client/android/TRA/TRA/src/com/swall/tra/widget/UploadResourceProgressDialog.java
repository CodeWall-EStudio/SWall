package com.swall.tra.widget;

import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import com.swall.tra.CurrentTRAActivity;
import com.swall.tra.CurrentTRAActivity;
import com.swall.tra_qq.R;

/**
 * Created by pxz on 14-1-12.
 */
public class UploadResourceProgressDialog extends Dialog implements View.OnClickListener {
    private int action;
    private String filePath;
    private String commentText;
    private TextView mPostiveButton;
    private EditText mEditText;


    public UploadResourceProgressDialog(CurrentTRAActivity currentTRAActivity, int action, String filePath) {
        super(currentTRAActivity);
        placeToBottom();
        setupContentView();
        setAction(action);
        setFilePath(filePath);
    }
    private void placeToBottom() {
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.width = WindowManager.LayoutParams.FILL_PARENT;
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }
    protected void setupContentView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.upload_resource_progress_dialog);
        mEditText = (EditText)findViewById(R.id.editText);
        findViewById(R.id.btn_negative).setOnClickListener(this);
        mPostiveButton = (TextView) findViewById(R.id.btn_positive);
        mPostiveButton.setOnClickListener(this);
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCommentText() {
        return commentText;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.btn_positive:
                commentText = mEditText.getText().toString();
                dismiss();
                break;
            case R.id.btn_negative:
                cancel();
                break;
        }
    }
}
