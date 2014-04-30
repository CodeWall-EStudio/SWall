package com.swal.enteach;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.swal.enteach.utils.BaseActivity;

/**
 * Created by pxz on 13-12-14.
 */
public class AvailableTRAActivity extends BaseActivity {
    private ListView mListView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available);
        mListView = (ListView)findViewById(R.id.availabel_list_view);

        ArrayAdapter<String> sampleAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1);
        for(int i=0;i<1000;++i){
            sampleAdapter.add("hits ... dsf dsf "+i);
        }
        mListView.setAdapter(sampleAdapter);
    }
}