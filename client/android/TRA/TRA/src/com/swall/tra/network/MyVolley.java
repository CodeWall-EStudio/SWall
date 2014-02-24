package com.swall.tra.network;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.swall.tra.utils.DiskLruImageCache;
import com.swall.tra.utils.MyImageLoader;
import com.swall.tra.utils.Utils;

import java.io.*;

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
        int cacheSize = memClass * 1024 * 1024 / 8;

/*
        int diskCaseSize = cacheSize * 800;
        File f = new File("/mnt/sdcard2");
        File cacheDir = new File("/mnt/sdcard2/tracache");
        if(f.exists()){
            if(!cacheDir.exists()){
                cacheDir.mkdirs();
            }
            Toast.makeText(context,"has 2nd sdcard",Toast.LENGTH_SHORT).show();
            sImageLoader = new MyImageLoader(sRequestQueue, new BitmapLruCache(cacheSize),new DiskLruImageCache(diskCaseSize,cacheDir));
        }else{
            f = new File("/mnt/sdcard");
            if(f.exists()){
                cacheDir = new File("/mnt/sdcard/tracache");
                if(!cacheDir.exists())
                cacheDir.mkdir();

                sImageLoader = new MyImageLoader(sRequestQueue, new BitmapLruCache(cacheSize),new DiskLruImageCache(diskCaseSize,cacheDir));
            }else{
                sImageLoader = new ImageLoader(sRequestQueue, new BitmapLruCache(cacheSize));
            }
        }

*/
        sImageLoader = new MyImageLoader(sRequestQueue, new BitmapLruCache(cacheSize));
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
        private static final int IO_BUFFER_SIZE = 8 * 1024;

        public BitmapLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(String key,Bitmap value){
            return value.getRowBytes() * value.getHeight();
        }

        @Override
        public Bitmap getBitmap(String url) {
            Bitmap bitmap = get(url);
            if(bitmap == null){
                try {
                    bitmap = getBitmapFromFile(url);
                    if(bitmap != null){
                        putBitmapFromFileToMemory(url,bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        private void putBitmapFromFileToMemory(String url, Bitmap bitmap) {
            putBitmap(url, bitmap);
        }

        private synchronized Bitmap getBitmapFromFile(String url) throws IOException {
            File file = new File(Utils.getUrlFileName(url));
            Bitmap bitmap = null;
            if(file.exists()){
                final InputStream in = new FileInputStream(file);
                if ( in != null ) {
                    final BufferedInputStream buffIn =
                            new BufferedInputStream( in, 8*1024 );

                    // 读取文件，有可能会内存不足
                    try{
                        BitmapFactory.Options options=new BitmapFactory.Options();
                        options.inJustDecodeBounds = false;
                        options.inSampleSize = 15;
                        options.inPurgeable = true;
                        bitmap = BitmapFactory.decodeStream(buffIn,null,options);
                    }catch(OutOfMemoryError error){
                        Log.i("SWall", "read file oom " + url);
                    }
                    buffIn.close();
                }
                in.close();
            }
            return bitmap;
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            putBitmapToFile(url,bitmap);
            put(url,bitmap);
        }

        private synchronized void putBitmapToFile(String url, Bitmap bitmap) {
            File file = new File(Utils.getUrlFileName(url));
            if(file.exists())return;
            OutputStream out = null;
            FileOutputStream fos = null;
            try {
                fos =  new FileOutputStream(file);
                out = new BufferedOutputStream( fos, IO_BUFFER_SIZE );
                boolean isSuccess = bitmap.compress( Bitmap.CompressFormat.JPEG, 80, out );
                Log.i("SWall","write to file success "+isSuccess + url + file.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if ( out != null ) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if( fos != null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}