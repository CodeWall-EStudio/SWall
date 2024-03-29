package com.swall.tra.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import com.swall.tra_demo.R;
import com.swall.tra.model.ResourceInfo;
import com.swall.tra.model.TRAInfo;
import com.swall.tra.network.ActionService;
import com.swall.tra.network.MyVolley;
import com.swall.tra.network.ServiceManager;
import com.swall.tra.utils.DateUtil;
import com.swall.tra.utils.JSONUtils;
import com.swall.tra.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pxz on 13-12-24.
 */
public class ActivityResourceAdapter extends BaseAdapter {

    private TRAInfo mInfo;

    @Override
    public int getCount() {
        return mInfo.resources.size();
    }

    @Override
    public Object getItem(int position) {
        // 倒序获取
        int size = mInfo.resources.size();
        int p2 = size - position - 1;
        if(p2 < 0 || p2 >= size) return null;
        return mInfo.resources.get(p2);
    }

    @Override
    public long getItemId(int position) {
        //ResourceInfo item = (ResourceInfo)getItem(position);
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.item_resource,parent,false);
        }

        ViewHolder holder = (ViewHolder)convertView.getTag();
        if(holder == null){
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        holder.update((ResourceInfo)getItem(position));


        return convertView;
    }

    public TRAInfo setJSONData(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            TRAInfo info = new TRAInfo(obj);
            setTRAInfo(info);
            return info;
        } catch (JSONException e) {
            // do nothing?
            Log.e("JSON", "ee", e);
            // TODO report
        }
        return null;
    }

    public TRAInfo setTRAInfo(TRAInfo info) {
        mInfo = info;
        notifyDataSetChanged();
        return mInfo;
    }

    public TRAInfo setJSONData(JSONObject json) {
        TRAInfo info = new TRAInfo(json);
        setTRAInfo(info);
        return info;
    }


    private class ViewHolder {
        private static final int ITEM_TYPE_TEXT = 0;
        private final TextView name;
        private final TextView dateTime;
        private final NetworkImageView imageView;
        private final TextView textView;
        private final TextView commentTextView;
//        private final View joined;

        public ViewHolder(View convertView) {
            name = (TextView) convertView.findViewById(R.id.item_resource_owner);
            textView = (TextView)convertView.findViewById(R.id.item_resource_text);
            commentTextView = (TextView)convertView.findViewById(R.id.item_resource_comment);
            imageView = (NetworkImageView)convertView.findViewById(R.id.item_resource_image);
            dateTime = (TextView)convertView.findViewById(R.id.item_resource_time);
//            joined = convertView.findViewById(R.id.tra_joined);
        }
/*
[
      {
        "_id": "52b665e1dcd66b942728d8b7",
        "user": "irjr",
        "activity": "52b2f7b7dcd66b942728d8b4",
        "type": 0,
        "content": "我是一句评论",
        "comment": null,
        "date": "2013-12-22T04:09:05.573Z"
      },
      {
        "_id": "52b665e1dcd66b942728d8b7",
        "user": "irjr",
        "activity": "52b2f7b7dcd66b942728d8b4",
        "type": 1,
        "content": "http://w.qq.com/icon.png",
        "comment": null,
        "date": "2013-12-22T04:09:05.573Z"
      },
      {
        "_id": "52b665e1dcd66b942728d8b7",
        "user": "irjr",
        "activity": "52b2f7b7dcd66b942728d8b4",
        "type": 2,
        "content": "http://www.abc.com/vedio.mp4",
        "comment": null,
        "date": "2013-12-22T04:09:05.573Z"
      },
      {
        "_id": "52b665e1dcd66b942728d8b7",
        "user": "irjr",
        "activity": "52b2f7b7dcd66b942728d8b4",
        "type": 3,
        "content": "http://www.abc.com/audio.mp3",
        "comment": null,
        "date": "2013-12-22T04:09:05.573Z"
      }
    ]
*/

        public void update(ResourceInfo item) {
            JSONObject profile = JSONUtils.getJSONObject(mInfo.profilesObject, item.user, new JSONObject());
            String showName = JSONUtils.getString(profile,"nick",item.user);
            name.setText(showName);
            dateTime.setText(DateUtil.getDisplayTime(item.date,true));


            if(item.type  == ITEM_TYPE_TEXT){
                textView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                commentTextView.setVisibility(View.GONE);
                textView.setText(item.content);
            }else if(item.type == ServiceManager.Constants.UPLOAD_TYPE_IMAGE){
                if(!TextUtils.isEmpty(item.content) && item.content.startsWith("http")){
                    textView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    commentTextView.setVisibility(View.VISIBLE);
                    if(ServiceManager.Constants.getCurEnv() == ServiceManager.Constants.ENV_PUBLISH) {
                        imageView.setDefaultImageResId(R.drawable.icon);
                    }else{
                        imageView.setDefaultImageResId(R.drawable.icon);
                    }
                    //如果本地存在，拿本地图片，否则从网络获取
                    //File file = new File(Utils.getUrlFileName(item.content));
                    //if(file.exists()){
                    //    imageView.setImageURI(Uri.fromFile(file));
                    //    Log.i("SWall","local image");
                    //}else{
                        imageView.setImageUrl(item.content/*ActionService.getUrlWithSKEY(item.content)*/, MyVolley.getImageLoader());
                    //}
                }else{
                    textView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    commentTextView.setVisibility(View.GONE);
                    textView.setText("数据错误");
                }
            }else if(item.type == ServiceManager.Constants.UPLOAD_TYPE_VIDEO){
                textView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                commentTextView.setVisibility(View.GONE);
                textView.setText("发表了视频"+item.content+(TextUtils.isEmpty(item.comment)?"":"并评论:\n"+item.comment));
            }
            commentTextView.setText(TextUtils.isEmpty(item.comment)?"发表了图片":("发表了图片并评论："+item.comment));

        }
    }
}
