package com.swall.tra.widget;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ImageView;
import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by pxz on 14-1-11.
 */
public abstract class ImageViewTouchBase extends NetworkImageView {
    /**
     * 基线图片矩阵。
     */
    protected Matrix mBaseMatrix = new Matrix();

    protected Matrix mSuppMatrix = new Matrix();

    /**
     * 显示图片矩阵。
     */
    private final Matrix mDisplayMatrix = new Matrix();

    /**
     * 矩阵数值数组
     */
    private final float[] mMatrixValues = new float[9];

    /**
     * 图片对象。
     */
    protected final RotateBitmap mBitmapDisplayed = new RotateBitmap(null);

    public RotateBitmap getRotateBitmap() {
        return mBitmapDisplayed;
    }

    int mThisWidth = -1;

    int mThisHeight = -1;

    //给个默认值吧，否则调用方自己初始化有问题的话，就具备了无限缩放的能力
    float mMaxZoom = 3F;
    float mMinZoom = 0.5F;

    private boolean bShadow = false;


    public void SetMinZoom(float scale){
        mMinZoom = scale;
    }

    public interface Recycler {
        public void recycle(Bitmap b);
    }


    public void setRecycler(Recycler r) {
        mRecycler = r;
    }

    private Recycler mRecycler;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mThisWidth = right - left;
        mThisHeight = bottom - top;
        Runnable r = mOnLayoutRunnable;
        if (r != null) {
            mOnLayoutRunnable = null;
            r.run();
        }
        if (mBitmapDisplayed.getBitmap() != null) {
            getProperBaseMatrix(mBitmapDisplayed, mBaseMatrix);
            setImageMatrix(getImageViewMatrix());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        // && !event.isCanceled()) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getScale() > 1.0f) {
                zoomTo(1.0f);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public void setShadow(boolean b)
    {
        bShadow = b;
    }


    protected Handler mHandler = new Handler();




    private RectF tmpRect = new RectF();
    protected RectF getShownRect()
    {
        return tmpRect;
    }

    public void onDraw(Canvas c)
    {
        try{
            super.onDraw(c);
            if (mBitmapDisplayed != null && mBitmapDisplayed.getBitmap() != null)
            {
                Paint p = new Paint();
                Matrix m = getImageViewMatrix();
                tmpRect.set(0, 0, mBitmapDisplayed.getBitmap().getWidth(),
                        mBitmapDisplayed.getBitmap().getHeight());
                m.mapRect(tmpRect);

                //QLog.v("svenxu", tmpRect.left + " " + tmpRect.top + " " + tmpRect.right + " " + tmpRect.bottom);

                if(bShadow)
                {
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(1);
                    int tc = 0xff212121;
                    tmpRect.right--;
                    for (int i = 0;i < 5;i++)
                    {
                        tmpRect.left--;
                        tmpRect.top--;
                        tmpRect.right++;
                        tmpRect.bottom++;
                        p.setColor(tc);
                        c.drawRoundRect(tmpRect,i,i,p);
                        int ttt = ((5 - i) << 16 | (5 - i) << 8 | (5 - i));
                        tc +=ttt;
                    }
                }
            }
        }
        catch(RuntimeException re)
        {
            re.printStackTrace();
        }
    }


    @Override
    public void setImageBitmap(Bitmap bitmap) {
        setImageBitmap(bitmap, 0);
    }

    public RectF getDisplayRect(){
        Matrix m = getImageViewMatrix();
        RectF r = new RectF(0, 0, 0, 0);
        if(mBitmapDisplayed.getBitmap() != null)
        {
//			QLog.d("sven", QLog.CLR, "image width: " + mBitmapDisplayed.getBitmap().getWidth()
//					+ " image height: " + mBitmapDisplayed.getBitmap().getHeight());
            r.set(0, 0, mBitmapDisplayed.getBitmap().getWidth(),
                    mBitmapDisplayed.getBitmap().getHeight());
        }
        m.mapRect(r);
        return r;
    }

    public int getImageWidth(){
        return mBitmapDisplayed.getBitmap().getWidth();
    }

    public int getImageHeight(){
        return mBitmapDisplayed.getBitmap().getHeight();
    }

    private void setImageBitmap(Bitmap bitmap, int rotation) {
        super.setImageBitmap(bitmap);
        Drawable d = getDrawable();
        if (d != null) {
            d.setDither(true);
        }


        Bitmap old = mBitmapDisplayed.getBitmap();
        mBitmapDisplayed.setBitmap(bitmap);
        mBitmapDisplayed.setRotation(rotation);

        if (old != null && old != bitmap && mRecycler != null) {
            mRecycler.recycle(old);
        }
    }

    public void clear() {
        setImageBitmapResetBase(null, true);
    }

    private Runnable mOnLayoutRunnable = null;

    public void setImageBitmapResetBase(final Bitmap bitmap,
                                        final boolean resetSupp) {
        setImageRotateBitmapResetBase(new RotateBitmap(bitmap), resetSupp);
    }

    public void setImageRotateBitmapResetBase(final RotateBitmap bitmap,
                                              final boolean resetSupp) {
        final int viewWidth = getWidth();

        if (viewWidth <= 0) {
            mOnLayoutRunnable = new Runnable() {
                public void run() {
                    setImageRotateBitmapResetBase(bitmap, resetSupp);
                }
            };
            return;
        }

        if (bitmap.getBitmap() != null) {
            getProperBaseMatrix(bitmap, mBaseMatrix);
            setImageBitmap(bitmap.getBitmap(), bitmap.getRotation());
        } else {
            mBaseMatrix.reset();
            setImageBitmap(null);
        }

        if (resetSupp) {
            mSuppMatrix.reset();
        }
        setImageMatrix(getImageViewMatrix());
        mMaxZoom = maxZoom();
    }

    protected void center(boolean horizontal, boolean vertical) {
        if (mBitmapDisplayed.getBitmap() == null) {
            return;
        }

        Matrix m = getImageViewMatrix();

        RectF rect = new RectF(0, 0, mBitmapDisplayed.getBitmap().getWidth(),
                mBitmapDisplayed.getBitmap().getHeight());

        m.mapRect(rect);

        float height = rect.height();
        float width = rect.width();

        float deltaX = 0, deltaY = 0;

        if (vertical) {
            int viewHeight = getHeight();
            if (height < viewHeight) {
                deltaY = (viewHeight - height) / 2 - rect.top;
            } else if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < viewHeight) {
                deltaY = getHeight() - rect.bottom;
            }
        }

        if (horizontal) {
            int viewWidth = getWidth();
            if (width < viewWidth) {
                deltaX = (viewWidth - width) / 2 - rect.left;
            } else if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < viewWidth) {
                deltaX = viewWidth - rect.right;
            }
        }

        postTranslate(deltaX, deltaY);
        setImageMatrix(getImageViewMatrix());
    }

    public ImageViewTouchBase(Context context) {
        super(context);
        init();
    }

    public ImageViewTouchBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScaleType(ImageView.ScaleType.MATRIX);
    }

    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    protected float getScale(Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_X);
    }

    public float getScale() {
        return getScale(mSuppMatrix);
    }

    private void getProperBaseMatrix(RotateBitmap bitmap, Matrix matrix) {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float w = bitmap.getWidth();
        float h = bitmap.getHeight();
        matrix.reset();

        float widthScale = Math.min(viewWidth / w, 3.0f);
        float heightScale = Math.min(viewHeight / h, 3.0f);
        float scale = Math.min(widthScale, heightScale);

        matrix.postConcat(bitmap.getRotateMatrix());
        matrix.postScale(scale, scale);

        matrix.postTranslate((viewWidth - w * scale) / 2F, (viewHeight - h
                * scale) / 2F);
    }

    protected Matrix getImageViewMatrix() {
        mDisplayMatrix.set(mBaseMatrix);
        mDisplayMatrix.postConcat(mSuppMatrix);
        return mDisplayMatrix;
    }

    static final float SCALE_RATE = 1.15F;

    protected float maxZoom() {
        if (mBitmapDisplayed.getBitmap() == null) {
            return 1F;
        }

        float fw = (float) mBitmapDisplayed.getWidth() / (float) mThisWidth;
        float fh = (float) mBitmapDisplayed.getHeight() / (float) mThisHeight;
        float max = Math.max(fw, fh) * 4;
        if (max < 1) {
            max = 3f;
        }
        return max;
    }

    public float zoomBy(float scale){

        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        mSuppMatrix.postScale(scale, scale, cx, cy);
        setImageMatrix(getImageViewMatrix());
        center(true, true);
        return getScale();
    }

    protected float zoomTo(float scale, float centerX, float centerY) {
        if (scale > mMaxZoom) {
            scale = mMaxZoom;
        }else if(scale < mMinZoom){
            scale = mMinZoom;
        }

        float oldScale = getScale();
        float deltaScale = scale / oldScale;

        mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
        setImageMatrix(getImageViewMatrix());
        center(true, true);
        return scale;
    }

    protected void zoomTo(final float scale, final float centerX,
                          final float centerY, final float durationMs) {
        final float incrementPerMs = (scale - getScale()) / durationMs;
        final float oldScale = getScale();
        final long startTime = System.currentTimeMillis();

        mHandler.post(new Runnable() {
            public void run() {
                long now = System.currentTimeMillis();
                float currentMs = Math.min(durationMs, now - startTime);
                float target = oldScale + (incrementPerMs * currentMs);
                zoomTo(target, centerX, centerY);

                if (currentMs < durationMs) {
                    mHandler.post(this);
                }
            }
        });
    }

    public float zoomTo(float scale) {
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        return zoomTo(scale, cx, cy);
    }

    public void zoomToPoint(float scale, float pointX, float pointY) {
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        panBy(cx - pointX, cy - pointY);
        zoomTo(scale, cx, cy);
    }

    public void zoomIn() {
        zoomIn(SCALE_RATE);
    }

    public void zoomOut() {
        zoomOut(SCALE_RATE);
    }

    protected void zoomIn(float rate) {
        if (getScale() >= mMaxZoom) {
            return; // Don't let the user zoom into the molecular level.
        }
        if (mBitmapDisplayed.getBitmap() == null) {
            return;
        }

        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        mSuppMatrix.postScale(rate, rate, cx, cy);
        setImageMatrix(getImageViewMatrix());
        center(true, true);
    }

    /**
     * 缩小
     *
     * @param rate
     * <br>
     */
    protected void zoomOut(float rate) {
        if (mBitmapDisplayed.getBitmap() == null) {
            return;
        }

        if (getScale()/rate < mMinZoom) {
            rate = getScale() / mMinZoom;
        }

        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        // Zoom out to at most 1x.
        Matrix tmp = new Matrix(mSuppMatrix);
        tmp.postScale(1F / rate, 1F / rate, cx, cy);

        if (getScale(tmp) > 0.5F) {
            mSuppMatrix.postScale(1F / rate, 1F / rate, cx, cy);
        }
        setImageMatrix(getImageViewMatrix());
        center(true, true);
    }

    protected void postTranslate(float dx, float dy) {
        mSuppMatrix.postTranslate(dx, dy);
    }

    public void panBy(float dx, float dy) {
        postTranslate(dx, dy);
        setImageMatrix(getImageViewMatrix());
    }

    public float getMaxZoom()
    {
        return mMaxZoom;
    }
}
