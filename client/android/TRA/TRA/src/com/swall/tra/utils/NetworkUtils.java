package com.swall.tra.utils;

import android.graphics.Bitmap;
import com.android.volley.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.android.volley.toolbox.HttpHeaderParser;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pxz on 13-12-25.
 */
public class NetworkUtils {
    public static class MultipartRequest extends Request<String> {

        private HttpEntity mEntity;
        private static final String FILE_PART_NAME = "file";
        private static final String STRING_PART_NAME = "text";

        private final Response.Listener<String> mListener;

        public MultipartRequest(String url,byte[] data,ContentType contentType,Response.Listener<String> listener, Response.ErrorListener errorListener){
            super(Method.POST, url, errorListener);
            mListener = listener;

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            String fileName = "x.jpg";
            if(contentType.getMimeType().indexOf("3gp")!=-1){
                fileName = "x.3gp";
            }
            builder.addBinaryBody(FILE_PART_NAME,data, contentType,fileName);
            builder.setBoundary("--swallatra");
            mEntity = builder.build();

        }

        public MultipartRequest(String url, Response.ErrorListener errorListener, Response.Listener<String> listener, File file, String stringPart)
        {
            super(Method.POST, url, errorListener);

            mListener = listener;
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody(FILE_PART_NAME,file);

            mEntity = builder.build();
        }


        @Override
        public String getBodyContentType()
        {
            return mEntity.getContentType().getValue();
        }

        @Override
        public byte[] getBody() throws AuthFailureError
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try
            {
                mEntity.writeTo(bos);
            }
            catch (IOException e)
            {
                VolleyLog.e("IOException writing to ByteArrayOutputStream");
            }
            return bos.toByteArray();
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response)
        {
            try {
                String string =
                        new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return Response.success(string,
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            }
        }

        @Override
        protected void deliverResponse(String response)
        {
            mListener.onResponse(response);
        }
    }

    public static byte[] bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
}
