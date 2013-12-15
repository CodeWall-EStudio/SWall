package com.example.SWall.services;

import android.os.Bundle;

/**
 * Created by pxz on 13-12-13.
 */
public interface ActionService {
    public void doAction(int action, Bundle data, ActionListener callback);
    public int[] getActionList();
}
