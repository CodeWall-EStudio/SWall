package com.swal.enteach.service;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
    SparseArray<ArrayList<IServiceObserver>> observers;


    public ServiceManager(Context context){
        appContextRef = new WeakReference<Context>(context);
        actionMap = new SparseArray<IService>();
        observers = new SparseArray<ArrayList<IServiceObserver>>();
    }

    /**
     *  注册 service
     * @param services
     * @return
     */
    public boolean registerServices(List<IService> services){
        return  registerServices(services.toArray(new IService[services.size()]));
    }

    /**
     * 注册 service
     * @param services
     * @return
     */
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

    /**
     * 执行
     * @param action
     * @param data
     */
    public void doAction(int action,Bundle data){

    }

    /**
     * 添加action 监听者
     * @param action
     * @param observer
     */
    public void addObserver(int action,IServiceObserver observer){
        ArrayList<IServiceObserver> services = observers.get(action);
        if(null == services ){
            services = new ArrayList<IServiceObserver>();
            observers.put(action,services);
        }

        // 避免重复被添加
        if(!services.contains(observer)){
            services.add(observer);
        }
    }

    /**
     * 添加 action 监听者
     * @param actions
     * @param observer
     */
    public void addObserver(int[] actions,IServiceObserver observer){
        for(int action : actions){
            addObserver(action,observer);
        }
    }

    /**
     * 删除此observer某个监听
     * @param action
     * @param observer
     */
    public void removeObserver(int action,IServiceObserver observer){
        ArrayList<IServiceObserver> services = observers.get(action);
        if(null != services && services.contains(observer)){
            services.remove(observer);
        }
    }

    /**
     * 删除此observer的所有监听
     * @param observer
     */
    public void removeObserver(IServiceObserver observer){
        // TODO
        throw new Error("not implement yet");
    }
}
