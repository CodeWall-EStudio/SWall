package com.swall.tra.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by pxz on 14-1-17.
 */
public class TouchableNetworkImageView extends NetworkImageView {
    public TouchableNetworkImageView(Context context) {
        super(context);
    }

    public TouchableNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
