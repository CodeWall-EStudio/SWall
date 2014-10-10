package com.codewalle.tra.Model;

/**
 * Created by xiangzhipan on 14-9-29.
 */
public class AccountManager {
    TRAApp app;

    static AccountManager accountManager = new AccountManager();
    public static AccountManager getInstance(){
        return accountManager;
    }

    public void setContext(TRAApp app){
        this.app = app;
    }

    public void getAccount(){

    }

}
