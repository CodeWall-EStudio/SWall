package com.codewalle.tra.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.widget.RelativeLayout;
import com.codewalle.tra.utils.Utils;

import java.lang.ref.WeakReference;

/**
 * Created by pxz on 13-12-14.
 */
public class IMERelativeLayout extends RelativeLayout {

    protected WeakReference<onSizeChangedListenner> mOnSizeChangedListennerRef;
    private int widthMeasureSpec;
    private int heightMeasureSpec;

    private int screenWidth;
    private int screenHeight;

    public IMERelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
    }

    public IMERelativeLayout(Context context, AttributeSet attrs,
                                     int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean isOpen = false;

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(mOnSizeChangedListennerRef == null) return;
        onSizeChangedListenner onSizeChangedListenner = mOnSizeChangedListennerRef.get();
        if (onSizeChangedListenner != null) {
            boolean oldOpenStatus = isOpen;
            if(w == oldw && oldw != 0 && oldh != 0){
                if (h < oldh){
                    if(Math.abs(h-screenHeight) > Utils.dp2px(150, getResources())){
                        isOpen = true;
                    }else{
                        isOpen = false;
                    }
                }else if(Math.abs(h-oldh) > Utils.dp2px(150, getResources())){
                    // 如果高度变大，并且变化大于一定的数值，则认为键盘已弹回
                    isOpen = false;
                }else{
                    // 否则键盘状态未变
                }
                onSizeChangedListenner.onSizeChange(isOpen,oldOpenStatus!=isOpen, oldh, h);
                measure(widthMeasureSpec-w+getWidth(), heightMeasureSpec-h+getHeight());
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        this.widthMeasureSpec = widthMeasureSpec;
        this.heightMeasureSpec = heightMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public interface onSizeChangedListenner{
        void onSizeChange(boolean isOpen, boolean changed,int preH, int curH);
    }

    public void setOnSizeChangedListenner(onSizeChangedListenner l){
        this.mOnSizeChangedListennerRef = new WeakReference<onSizeChangedListenner>(l);
    }

}