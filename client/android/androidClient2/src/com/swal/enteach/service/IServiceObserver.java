package com.swal.enteach.service;

import android.os.Bundle;

/**
 * Created by pxz on 13-12-13.
 */
public interface IServiceObserver {
    public void onComplete(int action,Bundle data);
    public void onError(int action,Bundle data);
}
