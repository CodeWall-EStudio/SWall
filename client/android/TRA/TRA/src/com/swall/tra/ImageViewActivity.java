package com.swall.tra;

import android.app.Activity;
import android.os.Bundle;
import com.android.volley.toolbox.NetworkImageView;
import com.swall.tra.network.MyVolley;
import com.swall.tra_demo.R;

/**
 * Created by pxz on 13-12-25.
 */
public class ImageViewActivity extends BaseFragmentActivity {
    private NetworkImageView mImageVIew;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        hideQuitButton();
        mImageVIew = (NetworkImageView)findViewById(R.id.imageview);
        mImageVIew.setImageUrl(getIntent().getStringExtra("url"), MyVolley.getImageLoader());
    }
}