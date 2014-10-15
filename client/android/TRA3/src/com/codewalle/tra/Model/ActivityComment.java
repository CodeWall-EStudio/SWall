package com.codewalle.tra.Model;

/**
 * Created by xiangzhipan on 14-10-11.
 */
public abstract class ActivityComment {

    public static String getTypeString(CommentType type) {
        switch(type){
            case TEXT:
                return "0";
            case IMAGE:
                return "1";
            case VIDEO:
                return "2";
            default:
                return "-1";
        }
    }

    public static enum CommentType{
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO
    }
    private final CommentType mType;
    public ActivityComment(CommentType type){
        this.mType = type;
    }
    public CommentType getType(){
        return mType;
    }
}
