package com.swal.enteach;

import android.app.Activity;
import android.os.Bundle;


import java.util.ArrayList;
import java.util.List;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

/**
 * Created by pxz on 13-12-14.
 */
public class MainActivity extends Activity implements TabHost.TabContentFactory {
    List<View> mTabContentViews;//视图列表

    Context mContext = null;

    LocalActivityManager mLocalActivityManager = null;

    TabHost mTabHost = null;

    private ViewPager mPager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mLocalActivityManager = new LocalActivityManager(this, true);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        /*在一个Activity的一部分中显示其他Activity”要用到LocalActivityManagerity
        作用体现在manager获取View：localActivityManager.startActivity(String, Intent).getDecorView()*/

        mTabHost = (TabHost) findViewById(R.id.tabhost);
        mTabHost.setup();
        mTabHost.setup(mLocalActivityManager);//实例化THost

        mContext =MainActivity.this;

        mPager = (ViewPager) findViewById(R.id.viewpager);//ViewPager


        //加入3个子Activity
        Intent i1 = new Intent(mContext, AvailableTRAActivity.class);
        Intent i2 = new Intent(mContext, ExpiredTRAActivity.class);
        Intent i3 = new Intent(mContext, SettingActivity.class);

        mTabContentViews = new ArrayList<View>();  //实例化listViews
        mTabContentViews.add(mLocalActivityManager.startActivity("T1", i1).getDecorView());
        mTabContentViews.add(mLocalActivityManager.startActivity("T2", i2).getDecorView());
        mTabContentViews.add(mLocalActivityManager.startActivity("T3", i3).getDecorView());

        RelativeLayout tabIndicator1 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.tabwidget, null);
        //tabIndicator1从一个layout文件获取view（即单个选项卡）再在大布局里显示
        TextView tvTab1 = (TextView)tabIndicator1.findViewById(R.id.tv_title);
        //id是tabIndicator1的
        tvTab1.setText("Tab1");

        RelativeLayout tabIndicator2 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.tabwidget, null);
        TextView tvTab2 = (TextView)tabIndicator2.findViewById(R.id.tv_title);
        tvTab2.setText("Tab2");

        RelativeLayout tabIndicator3 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.tabwidget, null);
        TextView tvTab3 = (TextView)tabIndicator3.findViewById(R.id.tv_title);
        tvTab3.setText("Tab3");

        mTabHost.addTab(mTabHost.newTabSpec("A").setIndicator(tabIndicator1).setContent(this));
        //TabSpec的名字A，B，C才是各个tab的Id
        mTabHost.addTab(mTabHost.newTabSpec("B").setIndicator(tabIndicator2).setContent(this));
        mTabHost.addTab(mTabHost.newTabSpec("C").setIndicator(tabIndicator3).setContent(this));


        //为tabhost设置监听
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
                    @Override
                    public void onTabChanged(String tabId) {
                        if ("A".equals(tabId)) {
                            mPager.setCurrentItem(0);//在tabhost的监听改变Viewpager
                        }
                        if ("B".equals(tabId)) {
                            mPager.setCurrentItem(1);
                        }
                        if ("C".equals(tabId)) {
                            mPager.setCurrentItem(2);
                        }
                    }
                });

            }
        });

        //为ViewPager适配和设置监听
        mPager.setAdapter(new MyPageAdapter(mTabContentViews));
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mTabHost.setCurrentTab(position);//在Viewpager改变tabhost
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public View createTabContent(String tag) {
        View v = mTabContentViews.get(0);
        return v;
    }

    private class MyPageAdapter extends PagerAdapter {

        private List<View> list;

        private MyPageAdapter(List<View> list) {
            this.list = list;
        }
        @Override
        public void destroyItem(ViewGroup view, int position, Object arg2) {
            ViewPager pViewPager = ((ViewPager) view);
            pViewPager.removeView(list.get(position));
        }
        @Override
        public void finishUpdate(View arg0) {
        }
        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            ViewPager pViewPager = ((ViewPager) view);
            pViewPager.addView(list.get(position));
            return list.get(position);
        }
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }
        @Override
        public Parcelable saveState() {
            return null;
        }
        @Override
        public void startUpdate(View arg0) {
        }
    }
}