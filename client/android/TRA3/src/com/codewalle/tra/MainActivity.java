package com.codewalle.tra;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.SlidingDrawer;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import org.androidannotations.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xiangzhipan on 14-8-30.
 */

@EActivity(com.codewalle.tra.R.layout.main)
public class MainActivity extends BaseFragmentActivity {


    @ViewById(R.id.tabhost)
    TabHost mTabHost;

    @ViewById(R.id.viewpager)
    ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;


    @AfterViews
    public void initTabs(){
        mTabHost.setup();
        if(mTabHost == null || mViewPager == null){
            return;
        }

        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
        mTabsAdapter.addTab(mTabHost.newTabSpec("home").setIndicator(getString(R.string.available_activities)),
                AvailableActivitiesFragment_.class, null);
        mTabsAdapter.addTab(mTabHost.newTabSpec("home").setIndicator(getString(R.string.available_activities)),
                AvailableActivitiesFragment_.class, null);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i("AAA","onPause");
    }


    @UiThread
    void test(){

    }

    public static class TabsAdapter extends FragmentPagerAdapter
            implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            @SuppressWarnings("unused")
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
            updateTabBackground(mTabHost);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            updateTabBackground(mTabHost);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        @Override
        public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
            updateTabBackground(mTabHost);
            // 需要侧滑
//			if (position == 0) {
//				((MainActivity) mContext).mSlidingLayout.setCanSlide(false, true);
//			} else {
//				((MainActivity) mContext).mSlidingLayout.setCanSlide(false, false);
//			}
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
            // 需要侧滑
//			if (position == 0) {
//				((MainActivity) mContext).mSlidingLayout.setCanSlide(false, true);
//			} else {
//				((MainActivity) mContext).mSlidingLayout.setCanSlide(false, false);
//			}
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        /**
         * 修改tabhost中tab的文字颜色和背景图片
         * @param tabHost
         */
        private void updateTabBackground(final TabHost tabHost) {

            if(tabHost.getTabWidget() == null)return;

            for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
                View view = tabHost.getTabWidget().getChildAt(i);
                TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP ,14);
                TextPaint paint = tv.getPaint();
                paint.setTypeface(Typeface.SANS_SERIF);
                if (tabHost.getCurrentTab() == i) {
                    //选中后的背景
//                    tv.setTextColor(AppData.getContext().getResources().getColorStateList(R.color.home_tab_selected));
//                    view.setBackgroundResource(R.drawable.tabhost_selected_bg);
                } else {
                    //非选择的背景
//                    tv.setTextColor(AppData.getContext().getResources().getColorStateList(R.color.home_tab_unselected));
//                    view.setBackgroundResource(R.drawable.tabhost_unselected_bg);
                }
            }
        }
    }
}