package com.example.SWall.services;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by pxz on 13-12-13.
 */
public class UploadService extends ActionService {
    final String UPLOAD_URL = "http://localhost:8999/upload/";

    public UploadService(Context context,ServiceManager manager) {
        super(context, manager);
    }

    @Override
    public void doAction(int action, Bundle data, ActionListener callback) {

    }

    @Override
    public int[] getActionList() {
        return new int[]{
                ServiceManager.Constants.ACTION_UPLOAD_PIC,
                ServiceManager.Constants.ACTION_UPLOAD_TEXT,
                ServiceManager.Constants.ACTION_UPLOAD_VIDEO
        };
    }


    private void doUpload(String postUrl,byte[] data){

    }
}
