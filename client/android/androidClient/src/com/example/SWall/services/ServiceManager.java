package com.example.SWall.services;

import android.content.Context;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by pxz on 13-12-13.
 */
public class ServiceManager {
    public static final int ACTION_UPLOAD_PIC = 0x01;
    public static final int ACTION_UPLOAD_TEXT = 0x02;
    public static final int ACTION_UPLOAD_VIDEO = 0x03;
    WeakReference<Context> appContextRef;
    SparseArray<IService> actionMap;


    public ServiceManager(Context context){
        appContextRef = new WeakReference<Context>(context);
        actionMap = new SparseArray<IService>();
    }

    /**
     *  注册 service
     * @param services
     * @return
     */
    public boolean registerServices(List<IService> services){
        return  registerServices(services.toArray(new IService[services.size()]));
    }

    public boolean registerServices(IService... services){
        for(IService service : services){
            int[] actions = service.getActionList();
            for(int action : actions){
                if(actionMap.indexOfKey(action) != -1){
                    // 同一个action 不应该被多个service注册
                    return false;
                }
                actionMap.put(action,service);
            }

        }
        return true;
    }
}
