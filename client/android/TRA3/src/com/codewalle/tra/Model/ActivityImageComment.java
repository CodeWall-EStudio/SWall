package com.codewalle.tra.Model;

/**
 * Created by xiangzhipan on 14-10-12.
 */
public class ActivityImageComment extends ActivityComment {
    public String remoteUrl;
    public String localUrl;
    public String text;

    public ActivityImageComment(String remoteUrl, String localUrl, String textComment) {
        super(CommentType.IMAGE);
        this.remoteUrl = remoteUrl;
        this.localUrl = localUrl;
        this.text = text;
    }
}
