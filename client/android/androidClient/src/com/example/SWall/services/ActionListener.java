package com.example.SWall.services;

import android.os.Bundle;

/**
 * Created by pxz on 13-12-13.
 */
public interface ActionListener {

    // 使用 Bundle 最大的坏处是调用方需要知道 key
    // 使用具体的类对象则编码更加烦琐
    // TODO 后续如果 key 越来越多，需考虑重构为具体的类
    public void onComplete(int action, Bundle data);
    public void onError(int action, Bundle data);
}
