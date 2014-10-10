package com.codewalle.tra.Network;

/**
 * Created by xiangzhipan on 14-10-10.
 */
public class RequestError {
    public RequestError(ErrorType errorType, String msg) {
        this.errorType = errorType;
        this.msg = msg;
    }

    public static enum ErrorType{
        USER_OR_PASSWORD_NOT_MATCH,
        SERVER_5XX,
        SERVER_4XX,
        NO_JSON
    }
    private ErrorType errorType;
    private long err;
    private String msg;
    public RequestError(long err, String msg){
        // TODO cast long to enum ErrorType
        this.err = err;
        this.msg = msg;
    }
    public ErrorType getErrorType(){
        return errorType;
    }
    public String getMsg(){
        return msg;
    }
}
