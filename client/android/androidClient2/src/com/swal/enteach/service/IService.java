package com.swal.enteach.service;

import android.os.Bundle;

/**
 * Created by pxz on 13-12-13.
 */
public interface IService {
    public void doAction(int action,Bundle data,IServiceObserver callback);
    public int[] getActionList();
}
