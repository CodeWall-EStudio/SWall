package com.swall.tra2;

import android.app.Activity;
import android.os.Bundle;
import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.swall.tra.R;

/**
 * Created by pxz on 13-12-28.
 */
public class MainActivity2 extends BaseFragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initTabar();
    }

    private void initTabar() {
        ActionBarSherlock actionBar = getSherlock();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("Save")
                .setIcon(R.drawable.btn_audio_record)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add("Search")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add("Refresh")
                .setIcon(R.drawable.btn_audio_record)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }
}