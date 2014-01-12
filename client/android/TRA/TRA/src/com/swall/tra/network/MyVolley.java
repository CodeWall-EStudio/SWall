package com.swall.tra.network;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.swall.tra.utils.DiskLruImageCache;
import com.swall.tra.utils.MyImageLoader;

import java.io.File;

/**
 * Created by pxz on 13-12-18.
 */
public class MyVolley {
    private static RequestQueue sRequestQueue;
    private static ImageLoader sImageLoader;

    public static void init(Context context){
        sRequestQueue = Volley.newRequestQueue(context);

        // 使用 1/8 的可用内存为缓存
        int memClass = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        int cacheSize = memClass * 1024 * 1024 / 3;

        int diskCaseSize = cacheSize * 10;

        File f = new File("/mnt/sdcard2");
        File cacheDir = new File("/mnt/sdcard2/tracache");
        if(f.exists()){// 存在第二张sdcard
            if(!cacheDir.exists()){
                cacheDir.mkdirs();
            }
//            Toast.makeText(context,"has 2nd sdcard",Toast.LENGTH_SHORT).show();
//            sImageLoader = new MyImageLoader(sRequestQueue, new BitmapLruCache(cacheSize),new DiskLruImageCache(diskCaseSize,cacheDir));
            sImageLoader = new ImageLoader(sRequestQueue, new BitmapLruCache(cacheSize));
        }else{
            sImageLoader = new ImageLoader(sRequestQueue, new BitmapLruCache(cacheSize));
        }
    }

    public static RequestQueue getRequestQueue(){
        if(sRequestQueue == null){
            throw new IllegalStateException("RequestQueue not initialed");
        }
        return sRequestQueue;
    }

    public static ImageLoader getImageLoader() {
        if( sImageLoader == null ){
            throw new IllegalStateException("sImageLoader not initialed");
        }
        return sImageLoader;
    }

    private static class BitmapLruCache extends LruCache<String,Bitmap> implements ImageLoader.ImageCache {
        public BitmapLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(String key,Bitmap value){
            return value.getRowBytes() * value.getHeight();
        }

        @Override
        public Bitmap getBitmap(String url) {
            return get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            put(url,bitmap);
        }
    }
}