package com.codewalle.tra.Model;

/**
 * Created by xiangzhipan on 14-10-12.
 */
public class ActivityVideoComment extends ActivityComment {
    public String localUrl;
    public String remoteUrl;
    public String text;

    public ActivityVideoComment(String remoteUrl,String localUrl,String text) {
        super(CommentType.VIDEO);
        this.remoteUrl = remoteUrl;
        this.localUrl = localUrl;
        this.text = text;
    }
}
