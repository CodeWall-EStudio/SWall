package com.codewalle.tra.Model;

/**
 * Created by xiangzhipan on 14-10-12.
 */
public class ActivityTextComment extends ActivityComment{
    public String text;

    public ActivityTextComment(String text) {
        super(CommentType.TEXT);
        this.text = text;
    }
}
