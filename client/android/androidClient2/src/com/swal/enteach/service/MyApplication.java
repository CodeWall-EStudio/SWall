package com.swal.enteach.service;

import android.app.Application;
import android.os.Bundle;

/**
 * Created by pxz on 13-12-13.
 */
public class MyApplication extends Application {
    private ServiceManager mServiceManager;
    private LoginService mLoginService;
    private DownloadService mDownloadService;
    private UploadService mUploadService;

    @Override
    public void onCreate() {
        super.onCreate();
        initServices();
    }

    private void initServices() {
        mServiceManager = new ServiceManager(this);

        mLoginService = new LoginService();
        mDownloadService = new DownloadService();
        mUploadService = new UploadService();


        mServiceManager.registerServices(
                mLoginService,
                mDownloadService,
                mUploadService
        );
    }

    public void doAction(String action,Bundle data){

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
