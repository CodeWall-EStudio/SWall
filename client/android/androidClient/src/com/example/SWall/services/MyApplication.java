package com.example.SWall.services;

import android.app.Application;

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

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
