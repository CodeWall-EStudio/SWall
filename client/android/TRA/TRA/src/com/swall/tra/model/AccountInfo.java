package com.swall.tra.model;

/**
 * Created by ippan on 13-12-24.
 */
public class AccountInfo {
    public String userName;
    public String password;
    public String showName;
    public String encodeKey;
    public String sessionId;
    private long mTime;

    public AccountInfo(String userName, String password, String showName, String encodeKey, String sessionId){
        this.userName = userName;
        this.password = password;
        this.showName = showName;
        this.encodeKey = encodeKey;
        this.sessionId = sessionId;
    }

    public void setTime(long time) {
        mTime = time;
    }
    public long getTime(){
        return mTime;
    }
}
