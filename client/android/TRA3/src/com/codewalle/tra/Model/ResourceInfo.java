package com.codewalle.tra.Model;

import com.codewalle.tra.utils.JSONUtils;
import org.json.JSONObject;

/**
 * Created by xiangzhipan on 14-10-12.
 */
public class ResourceInfo {

    public static final int RESOURCE_TYPE_TEXT = 0;
    public static final int RESOURCE_TYPE_IMAGE = 1;
    public static final int RESOURCE_TYPE_VIDEO = 2;
    public static final int RESOURCE_TYPE_AUDIO = 3;

    public String user;     // 创建者
    public String activity; // 所属的活动id
    public int type;        // 见上面 RESOURCE_TYPE_*
    public String content;  // 内容，type为1时为文本，其它为链接
    public String comment;  //应该不需要使用
    public long date;        // 创建时间
    public String resourceId;   // id
    public String userName;
    public ResourceInfo(JSONObject object){
        user = JSONUtils.getString(object, "user", "");
        activity = JSONUtils.getString(object,"activity","");
        type = JSONUtils.getInt(object,"type",0);
        content = JSONUtils.getString(object,"content","");
        comment = JSONUtils.getString(object,"comment","");
        date = JSONUtils.getLong(object, "date", 0l);
    }
}