package com.swall.tra_qq.model;

/**
 * Created by ippan on 13-12-24.
 */
public class AccountInfo {
    public String userName;
    public String password;
    public String showName;
    public String encodeKey;
    private long mTime = 0;

    public AccountInfo(String userName,String password,String showName,String encodeKey){
        this.userName = userName;
        this.password = password;
        this.showName = showName;
        this.encodeKey = encodeKey;
    }

    public void setTime(long time) {
        mTime = time;
    }
    public long getTime(){
        return mTime;
    }
}
