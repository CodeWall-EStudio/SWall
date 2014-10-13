package com.codewalle.tra.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.widget.Toast;
import com.codewalle.tra.LoginActivity;

import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by pxz on 14-1-8.
 */
public class Utils {
    public static final Charset US_ASCII = Charset.forName("US-ASCII");;
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    private static String sLoginUrl;


    public static String[] getStorageDirectories()
    {
        String[] dirs = null;
        BufferedReader bufReader = null;
        try {
            bufReader = new BufferedReader(new FileReader("/proc/mounts"));
            ArrayList<String> list = new ArrayList<String>();
            String line;
            while ((line = bufReader.readLine()) != null) {
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String s = tokens.nextToken();
                    s = tokens.nextToken(); // Take the second token, i.e. mount point

                    if (s.equals(Environment.getExternalStorageDirectory().getPath())) {
                        list.add(s);
                    }
                    else if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure") && !line.contains("/mnt/asec") && !line.contains("/mnt/obb") && !line.contains("/dev/mapper") && !line.contains("tmpfs")) {
                            list.add(s);
                        }
                    }
                }
            }

            dirs = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                dirs[i] = list.get(i);
            }
        }
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
        finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                }
                catch (IOException e) {}
            }

            return dirs;
        }
    }

    public static String getExternalDir(){
        return Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    }

    /*
    public static boolean shouldCachePic(){
        SharedPreferences sp = TRAApplication.getApp().getSharedPreferences(SettingActivity.PRE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean("save_images",false);
    }
    */
    public static String getCacheDir(){
        String rootDir = "/mnt/sdcard";
        // TODO
        /*
        SharedPreferences sp = TRAApplication.getApp().getSharedPreferences(SettingActivity.PRE_NAME, Context.MODE_PRIVATE);

        String saveTarget = sp.getString("save_images_target","1");// 1 内置 2 外置
        if(saveTarget.equals("2")){
            rootDir += "2";
        }
        */

        String cacheDir = rootDir + "/TRACache/";
        File f = new File(cacheDir);
        if(!f.exists()){
            f.mkdir();
        }
        return cacheDir;
    }


    public static boolean hasSDCard() {
        return android.os.Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean has2SDCard() {
        final File f = new File("/mnt/sdcard2/TRACache");
        if(!f.exists()){
            return f.mkdir();
        }
        return true;
    }


    public static String getUrlFileName(String url){
        return getCacheDir()+md5(url)+".jpg";
    }
    public static String md5(String source){
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(source.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            return source.replaceAll("[^\\w]", "-");
        } catch (UnsupportedEncodingException e) {
            return source.replaceAll("[^\\w]", "-");
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }


    static String readFully(Reader reader) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, count);
            }
            return writer.toString();
        } finally {
            reader.close();
        }
    }

    /**
     * Deletes the contents of {@code dir}. Throws an IOException if any file
     * could not be deleted, or if {@code dir} is not a readable directory.
     */
    static void deleteContents(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("not a readable directory: " + dir);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }

    static void closeQuietly(/*Auto*/Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    public static void clearFileCache() {
        File cacheFile = new File(getCacheDir());
        if(cacheFile.exists() && cacheFile.isDirectory()){
            String[] children = cacheFile.list();
            for (int i = 0; i < children.length; i++) {
                new File(cacheFile, children[i]).delete();
            }
        }

    }

    public static String getLoginUrl() {
        return sLoginUrl;
    }

    private static Toast sToast;
    public static void toast(Context context, String s) {
        if(sToast != null)sToast.cancel();
        sToast = Toast.makeText(context,s,Toast.LENGTH_LONG);
        sToast.show();
    }

    public static int dp2px(int dp, Resources resources) {
        return (int)(dp * resources.getDisplayMetrics().density + 0.5f);
    }
}
