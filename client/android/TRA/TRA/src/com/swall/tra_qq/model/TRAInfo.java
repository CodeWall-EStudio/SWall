package com.swall.tra_qq.model;

/**
 * Created by pxz on 13-12-14.
 */

import android.text.format.Time;
import com.swall.tra_qq.utils.DateUtil;
import com.swall.tra_qq.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * TRA : Teaching and Researching Activity
 */
public class TRAInfo {
    public JSONObject profilesObject;
    public int type;
    private String mJsonString;
    private String mResourceDesc; //  xx人参与，10条文字，5张图片，3段视频，4段录音
    public ArrayList<ResourceInfo> resources = new ArrayList<ResourceInfo>();
    public String creator;

    public List<String> invitedUsers = new ArrayList<String>();//可为空
    public boolean isPublic;    // 是否公开课

    public List<String> participators = new ArrayList<String>();     // 参与者
    public boolean activeStatus;    // 是否开放中的活动
    public String title;
    public String desc;
    public long beginDate;
    public long createDate;
    public String teacher;
    public String grade;
    public String _class;
    public String subject;
    public String domain;

    public String id;
    private String mTimeFormatted;
    private boolean mJoined;
    private String mAllDesc;
    private String mLongDesc;
    private long postParticipatorLength;
/*
{
      "users": {
        "creator": "oscar",
        "invitedUsers": [
          "*"
        ],
        "participators": []
      },
      "resources": [],
      "active": true,
      "info": {
        "title": "三個戴錶重要思想",
        "desc": null,
        "type": 1,
        "date": "2014-05-07T11:02:06.758Z",
        "createDate": "2013-12-19T13:42:15.227Z",
        "teacher": "雷鋒",
        "grade": "3",
        "class": "2",
        "subject": "三個戴錶重要思想",
        "domain": "毛克思理論"
      },
      "_id": "52b2f7b7dcd66b942728d8b4"
    }
*/

    /**
     *
     * @param activitiesObject
     */

    public TRAInfo(JSONObject activitiesObject){

        profilesObject = JSONUtils.getJSONObject(activitiesObject,"profiles",new JSONObject());

        mJoined = false;

        JSONObject userObject = JSONUtils.getJSONObject(activitiesObject,"users",new JSONObject());

        creator = JSONUtils.getString(userObject,"creator","admin");
        JSONArray participatorArray = JSONUtils.getJSONArray(userObject,"participators",new JSONArray());
        participators = JSONUtils.JSONStringArrayToStringArray(participatorArray);
        JSONArray invitedUserArray = JSONUtils.getJSONArray(userObject,"invitedUsers",new JSONArray());
        invitedUsers = JSONUtils.JSONStringArrayToStringArray(invitedUserArray);

        JSONArray resourceArray = JSONUtils.getJSONArray(activitiesObject, "resources", new JSONArray());
        for(int i=0;i<resourceArray.length();++i){
            JSONObject resourceObject = JSONUtils.arrayGetJSONObject(resourceArray,i);
            if(resourceObject != null){
                resources.add(new ResourceInfo(resourceObject));
            }
        }

        activeStatus = JSONUtils.getBoolean(activitiesObject,"active",true);
        id = JSONUtils.getString(activitiesObject, "_id", "");

        JSONObject infoObject = JSONUtils.getJSONObject(activitiesObject,"info",new JSONObject());

        title = JSONUtils.getString(infoObject,"title","公开课");
        desc = JSONUtils.getString(infoObject,"desc","");
        createDate = JSONUtils.getLong(infoObject, "createDate", 0l);
        beginDate = JSONUtils.getLong(infoObject, "date", (new Time().toMillis(false) / 1000));;
        teacher = JSONUtils.getString(infoObject, "teacher", "待定");
        _class = JSONUtils.getString(infoObject,"class","");
        grade = JSONUtils.getString(infoObject,"grade","");
//        _class = JSONUtils.getInt(infoObject,"class",1);
//        grade = JSONUtils.getInt(infoObject,"grade",1);
        subject = JSONUtils.getString(infoObject,"subject","");
        domain = JSONUtils.getString(infoObject, "domain", "");
        type = JSONUtils.getInt(infoObject,"type",1);



        mJsonString = activitiesObject.toString();
        mTimeFormatted = DateUtil.getDisplayTime(beginDate, true);

        // init resrouce desc
        mResourceDesc=participators.size()+"人参与 ";
        mResourceDesc += caculateResourceDesc(resources," ");

        mLongDesc = mTimeFormatted+" "+mResourceDesc;


        initAllDesc();

    }

    private void initAllDesc() {
        // init all desc



        StringBuilder sb = new StringBuilder();
        if(type == 1){
            sb.append("活动类型 : 公开课\n");// 不是1要怎么表示 ?
        }
        String resourceDesc = caculateResourceDesc(resources,"\n\t\t");
        sb.append(String.format(
                "活动简介 : %s " +
                        "\n\n出课教师 : %s " +
                        "\n学科 : %s " +
                        "\n年级 : %s" +
                        "\n班级 : %s " +
                        "\n课题 : %s \n" +
                        "\n当前在线用户 : %d 位" +
                        "\n参与用户总数 : %d 位\n" +
                        "\n资源 : \n\t\t%s",
                desc,
                teacher,
                subject,
                grade,
                _class,
//                getNumberString(grade),
//                getNumberString(grade,_class),

                domain,
                participators.size(),
                postParticipatorLength,
                resourceDesc
        ));
        mAllDesc = sb.toString();
    }

    private String getNumberString(int grade, int aClass) {
        return getNumberString(grade)+"年级"+getNumberString(aClass)+"班";
    }

    // 后台是数字，前端要显示汉字...
    static Map<Integer, String> valueMap = new HashMap<Integer, String>();

    private String getNumberString(int d) {
        if(valueMap.isEmpty()){
            valueMap.put(1, "一");
            valueMap.put(2, "二");
            valueMap.put(3, "三");
            valueMap.put(4, "四");
            valueMap.put(5, "五");
            valueMap.put(6, "六");
            valueMap.put(7, "七");
            valueMap.put(8, "八");
            valueMap.put(9, "九");
        }
        if(d<10 && d>0){
            return valueMap.get(d);
        }
        return ""+d;
    }

    private String caculateResourceDesc(ArrayList<ResourceInfo> resources,String seperator) {
        HashSet<String> tempHash = new HashSet<String>();

        int texts=0;
        int images = 0;
        int videos = 0;
        int audios = 0;
        for(ResourceInfo info:resources){
            tempHash.add(info.user);
            switch (info.type){
                case ResourceInfo.RESOURCE_TYPE_AUDIO:
                    audios++;
                    break;
                case ResourceInfo.RESOURCE_TYPE_TEXT:
                    texts ++;
                    break;
                case ResourceInfo.RESOURCE_TYPE_IMAGE:
                    images++;
                    break;
                case ResourceInfo.RESOURCE_TYPE_VIDEO:
                    videos++;
                    break;
                default:
                    // TODO report unknow type
                    break;
            }
        }
        StringBuilder builder = new StringBuilder(8);

        if(texts != 0){
            builder.append(texts);
            builder.append("条文字"+seperator);
        }
        if(images!= 0){
            builder.append(images);
            builder.append("张图片"+seperator);
        }
        if(videos != 0){
            builder.append(videos);
            builder.append("段视频"+seperator);
        }
        if(audios != 0){
            builder.append(audios);
            builder.append("段视频"+seperator);
        }
        String result = builder.toString();
        if(texts + images + videos + audios != 0){
            result = result.substring(0,result.length()-(seperator.length()));
        }

        postParticipatorLength = tempHash.size();
        return result;

    }

    @Override
    public String toString(){
        return mJsonString;
    }


    public String getResourceDesc(){// xx人参与，10条文字，5张图片，3段视频，4段录音
        return mResourceDesc;
    }

    public String getTimeFormated() {
        return mTimeFormatted;
    }

    public void setJoined(boolean joined) {
        this.mJoined = joined;
    }

    public boolean ismJoined(){
        return mJoined;
    }

    public String getAllDesc() {
        return mAllDesc;
    }

    public String getLongDesc() {
        return mLongDesc;
    }

    public String getTimeAndCreatorDesc() {
        String res = "活动开始时间："+getTimeFormated()+"\n";
        res += "活动创建人：" + getCreator();
        return res;
    }

    private String getCreator() {
        return getShowName(creator);
    }

    private String getShowName(String userName) {
        JSONObject profile = JSONUtils.getJSONObject(profilesObject, userName, new JSONObject());
        String showName = JSONUtils.getString(profile,"nick",userName);
        return showName;
    }
}
