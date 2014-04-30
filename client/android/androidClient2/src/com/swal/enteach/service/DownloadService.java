package com.swal.enteach.service;

import android.os.Bundle;

/**
 * Created by pxz on 13-12-13.
 */
public class DownloadService implements IService{
    @Override
    public void doAction(String action, Bundle data, IServiceObserver callback) {

    }

    @Override
    public int[] getActionList() {
        return new int[0];
    }
}
