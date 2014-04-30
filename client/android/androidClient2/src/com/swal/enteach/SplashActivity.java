package com.swal.enteach;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import com.swal.enteach.widget.BounceListView;

public class SplashActivity extends Activity {
    private BounceListView mBounceListView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*
        mBounceListView = (BounceListView)findViewById(R.id.bounceListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1);
        adapter.add("string1");
        adapter.add("haha");
        adapter.add("heihei");
        adapter.add("string1");
        adapter.add("haha");
        adapter.add("heihei");
        adapter.add("string1");
        adapter.add("haha");
        adapter.add("heihei");
        adapter.add("string1");
        adapter.add("haha");
        adapter.add("heihei");
        adapter.add("string1");
        adapter.add("haha");
        adapter.add("heihei");
        adapter.add("string1");
        adapter.add("haha");
        adapter.add("heihei");
        adapter.add("string1");
        adapter.add("haha");
        adapter.add("heihei");
        mBounceListView.setAdapter(adapter);
        */
        startActivity(new Intent(this,MainActivity.class));
    }
}
