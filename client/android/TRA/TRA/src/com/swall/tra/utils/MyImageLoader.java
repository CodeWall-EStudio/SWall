package com.swall.tra.utils;

import android.graphics.Bitmap;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;

/**
 * Created by pxz on 14-1-12.
 */
public class MyImageLoader extends ImageLoader {
    private final ImageCache mDiskCache;
    private final ImageCache mMemoryCache;

    public MyImageLoader(RequestQueue queue, ImageCache memoryCache, ImageCache diskCache) {
        super(queue, memoryCache);
        mMemoryCache = memoryCache;
        mDiskCache = diskCache;
    }

    @Override
    public boolean isCached(String requestUrl, int maxWidth, int maxHeight) {
        if(!super.isCached(requestUrl, maxWidth, maxHeight)){
            String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);
            return mDiskCache.getBitmap(cacheKey) != null;
        }
        return true;
    }


    @Override
    public ImageContainer get(String requestUrl, ImageListener imageListener, int maxWidth, int maxHeight) {

        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);

        // Try to look up the request in the cache of remote images.
        Bitmap cachedBitmap = mMemoryCache.getBitmap(cacheKey);
        if(cachedBitmap == null){
            cachedBitmap = mDiskCache.getBitmap(cacheKey);
        }
        if (cachedBitmap != null) {
            // Return the cached bitmap.
            ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, null, null);
            imageListener.onResponse(container, true);
            return container;
        }
        return super.get(requestUrl, imageListener, maxWidth, maxHeight);
    }

    /**
     * Handler for when an image was successfully loaded.
     * @param cacheKey The cache key that is associated with the image request.
     * @param response The bitmap that was returned from the network.
     */
    @Override
    protected void onGetImageSuccess(String cacheKey, Bitmap response) {
        // cache the image that was fetched.
        mDiskCache.putBitmap(cacheKey, response);
        super.onGetImageSuccess(cacheKey,response);
    }

    protected static String getCacheKey(String url, int maxWidth, int maxHeight) {
        return String.valueOf(url.hashCode());
    }
}
