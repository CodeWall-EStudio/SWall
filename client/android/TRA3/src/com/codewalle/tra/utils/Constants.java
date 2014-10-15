package com.codewalle.tra.utils;

import java.net.URLEncoder;

/**
 * Created by xiangzhipan on 14-9-29.
 */
public class Constants {
    // request types

    public static enum REQUEST_TYPE {
        LOGIN,
        GET_ALL_ACTIVITIES
    }

    // keys
    public static final class KEYS{
        public static final String LOGIN_USERNAME = "username";
        public static final String LOGIN_PASSWORD = "password";
    }

    // values
    public static final class VALUES{
        public static final String LOGIN_METHOD = /*C_BEGIN_LOGIN_METHOD*/"POST"/*C_END*/;
        public static final String DATA_URL_PREFIX = /*C_BEGIN_DATA_URL*/"http://ydmedia.hylc-edu.cn/"/*C_END*/;
        public static final String FILE_SERVER_URL = /*C_BEGIN_FS_URL*/"http://ydszone.hylc-edu.cn/"/*C_END*/;
        public static final String LOGIN_URL = /*C_BEGIN_LOGIN_URL*/"http://ydszone.hylc-edu.cn/"/*C_END*/;
    }

    public static String getLoginUrl(String username,String password){
        return /*C_BEGIN_LOGIN_URL_SOURCE*/VALUES.LOGIN_URL + "api/user/login?" + Math.random()/*C_END*/;
    }

    public static String getActivityListUrl(String userName, boolean isActive, boolean isJoined, int index) {
        String url =  VALUES.DATA_URL_PREFIX + String.format("activities?uid=%s&status=%s&index=%d&authorize=%s&t=%d",
            URLEncoder.encode(userName),
            (isActive ? "active" : "closed"),
            index,
            isJoined?"joined":"available",
            System.currentTimeMillis());

        return url;
    }

    public static String getJoinUrl(String userName,String id){
        String url = VALUES.DATA_URL_PREFIX + String.format("activities/%s/participators?uid=%s&t=%d",id,URLEncoder.encode(userName),
                System.currentTimeMillis());
        return url;
    }

    public static String getQuitUrl(String uid,String aid){
        String url = VALUES.DATA_URL_PREFIX + String.format("activities/%s/participators/%s?uid=%s&t=%d",
                aid,
                URLEncoder.encode(uid),
                URLEncoder.encode(uid),
                System.currentTimeMillis());
        return url;
    }

    public static String getPostUrl(String uid,String aid) {
        String url = VALUES.DATA_URL_PREFIX + String.format("activities/%s/resources?uid=%s&t=%d",aid,URLEncoder.encode(uid),
                System.currentTimeMillis());
        return url;
    }

    public static String getUploadUrl() {
        return VALUES.FILE_SERVER_URL + String.format("/upload/?t=%d",System.currentTimeMillis());
    }
    public static String getDownloadUrl(String fid) {
        return VALUES.FILE_SERVER_URL +"api/media/download?fileId="+fid;
    }
}
