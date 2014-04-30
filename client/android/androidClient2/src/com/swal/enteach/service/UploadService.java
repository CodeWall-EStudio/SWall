package com.swal.enteach.service;

import android.os.Bundle;

/**
 * Created by pxz on 13-12-13.
 */
public class UploadService implements IService{
    final String UPLOAD_URL = "http://localhost:8999/upload/";

    @Override
    public void doAction(String action, Bundle data, IServiceObserver callback) {

    }

    @Override
    public int[] getActionList() {
        return new int[]{
                ServiceManager.ACTION_UPLOAD_PIC,
                ServiceManager.ACTION_UPLOAD_TEXT,
                ServiceManager.ACTION_UPLOAD_VIDEO
        };
    }


    private void doUpload(String postUrl,byte[] data){

    }
}
