package com.swall.tra.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by pxz on 14-1-12.
 */
public class MyImageLoader extends ImageLoader {
    private final DiskLruImageCache mDiskCache;
    private HandlerThread mWriteFileThread;
    private Handler mWriteFileHandler;

    private static class WriteFileWorker implements Runnable{
        private String mKey;
        private Bitmap mResponse;
        public WriteFileWorker(String key,Bitmap response){
            if(key.toLowerCase().startsWith("http")){
                key = String.valueOf(key.hashCode());
            }
            mKey = key;
            mResponse = response;
        }

        @Override
        public void run() {
            String filePath = Utils.getUrlFileName(mKey);
            File f = new File(filePath);
            if(!f.exists()){
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(filePath);
                mResponse.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public MyImageLoader(RequestQueue queue, ImageCache memoryCache, DiskLruImageCache diskCache) {
        super(queue, memoryCache);
        mDiskCache = diskCache;
        mWriteFileThread = new HandlerThread("writeFile");
        mWriteFileThread.start();
        mWriteFileHandler = new Handler(mWriteFileThread.getLooper());
    }

    @Override
    public boolean isCached(String requestUrl, int maxWidth, int maxHeight) {
        if(!super.isCached(requestUrl, maxWidth, maxHeight)){
            String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);
            return mDiskCache.getBitmap(Utils.md5(cacheKey)) != null;
        }
        return true;
    }


    @Override
    public ImageContainer get(String requestUrl, ImageListener imageListener, int maxWidth, int maxHeight) {





        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);

        // Try to look up the request in the cache of remote images.
        Bitmap cachedBitmap = mCache.getBitmap(cacheKey);
        if(cachedBitmap == null){
            cachedBitmap = mDiskCache.getBitmap(Utils.md5(cacheKey));
            if(cachedBitmap != null){
                Log.i("SWall","--no memory cache,has disk cache !!"+requestUrl);
                mCache.putBitmap(cacheKey,cachedBitmap);
            }else{
                Log.i("Swall","===no memory cache neither disk cache !! "+requestUrl + "\n cache key="+cacheKey);
            }
        }else{
            Log.i("SWall","--has memory cache "+requestUrl);
        }
        if (cachedBitmap != null) {
            // Return the cached bitmap.
            ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, null, null);
            imageListener.onResponse(container, true);
            return container;
        }else{
            Log.i("Swall","no cache");
        }

        // The bitmap did not exist in the cache, fetch it!
        ImageContainer imageContainer =
                new ImageContainer(null, requestUrl, cacheKey, imageListener);

        // Update the caller to let them know that they should use the default bitmap.
        imageListener.onResponse(imageContainer, true);

        // Check to see if a request is already in-flight.
        BatchedImageRequest request = mInFlightRequests.get(cacheKey);
        if (request != null) {
            // If it is, add this request to the list of listeners.
            request.addContainer(imageContainer);
            return imageContainer;
        }

        // The request is not already in flight. Send the new request to the network and
        // track it.
        Request<?> newRequest =
                new ImageRequest(requestUrl, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        onGetImageSuccess(cacheKey, response);
                    }
                }, maxWidth, maxHeight,
                        Bitmap.Config.RGB_565, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onGetImageError(cacheKey, error);
                    }
                });
        Log.i("SWall","new request "+requestUrl+" cachekey "+cacheKey);
        mRequestQueue.add(newRequest);
        mInFlightRequests.put(cacheKey,
                new BatchedImageRequest(newRequest, imageContainer));
        return imageContainer;
    }

    /**
     * Handler for when an image was successfully loaded.
     * @param cacheKey The cache key that is associated with the image request.
     * @param response The bitmap that was returned from the network.
     */
    @Override
    protected void onGetImageSuccess(String cacheKey, Bitmap response) {
        // cache the image that was fetched.
        mDiskCache.putBitmap(Utils.md5(cacheKey), response);
//        mWriteFileHandler.post(new WriteFileWorker(Utils.md5(cacheKey),response));
        super.onGetImageSuccess(cacheKey, response);
        Log.i("SWall","cache "+cacheKey);
    }

    public void onDestroy(){
        mWriteFileThread.getLooper().quit();
    }


}
