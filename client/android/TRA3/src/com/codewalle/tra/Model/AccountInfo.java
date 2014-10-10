package com.codewalle.tra.Model;

/**
 * Created by xiangzhipan on 14-9-29.
 */
public class AccountInfo {
    public String userName;
    public String password;
    public String showName;
    public String encodeKey;
    public String sessionId;
    public boolean autoLogin;
    private long mTime;


    public AccountInfo(String userName, String password, String showName, String encodeKey, String sessionId,boolean autoLogin){
        this.userName = userName;
        this.password = password;
        this.showName = showName;
        this.encodeKey = encodeKey;
        this.sessionId = sessionId;
        this.autoLogin = autoLogin;
    }

    public void setTime(long time) {
        mTime = time;
    }
    public long getTime(){
        return mTime;
    }
}
