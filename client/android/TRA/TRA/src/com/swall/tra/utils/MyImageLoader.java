package com.swall.tra.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.swall.tra.network.ActionService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by pxz on 14-1-12.
 */
public class MyImageLoader extends ImageLoader {

    public MyImageLoader(RequestQueue queue, ImageCache memoryCache) {
        super(queue, memoryCache);
    }

    /**
     * Issues a bitmap request with the given URL if that image is not available
     * in the cache, and returns a bitmap container that contains all of the data
     * relating to the request (as well as the default image if the requested
     * image is not available).
     * @param requestUrl The url of the remote image
     * @param imageListener The listener to call when the remote image is loaded
     * @param maxWidth The maximum width of the returned image.
     * @param maxHeight The maximum height of the returned image.
     * @return A container object that contains all of the properties of the request, as well as
     *     the currently available image (default if remote is not loaded).
     */
    @Override
    public ImageContainer get(String requestUrl, ImageListener imageListener,
                              int maxWidth, int maxHeight) {
        // only fulfill requests that were initiated from the main thread.
        throwIfNotOnMainThread();

        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);

        // Try to look up the request in the cache of remote images.
        Bitmap cachedBitmap = mCache.getBitmap(cacheKey);
        if (cachedBitmap != null) {
            // Return the cached bitmap.
            ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, null, null);
            imageListener.onResponse(container, true);
            return container;
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
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return ActionService.getRequestHeaders();
                    }
                };

        mRequestQueue.add(newRequest);
        mInFlightRequests.put(cacheKey,
                new BatchedImageRequest(newRequest, imageContainer));
        return imageContainer;
    }
}
