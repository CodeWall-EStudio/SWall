package com.codewalle.tra.utils;

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
        public static final String DATA_URL_PREFIX = /*C_BEGIN_DATA_URL_PREFIX*/"http://yqmedia.hylc-edu.cn/"/*C_END*/;
        public static final String FILE_SERVER_URL = /*C_BEGIN_FILE_SERVER_URL*/"http://yqszone.hylc-edu.cn/"/*C_END*/;
        public static final String LOGIN_URL = /*C_BEING_LOGIN_URL*/"http://szone.hylc-edu.cn/"/*C_END*/;
    }

    public static String getLoginUrl(String username,String password){
        return /*C_BEGIN_LOGIN_URL_SOURCE*/VALUES.LOGIN_URL + "api/user/login?" + Math.random()/*C_END*/;
    }


}
