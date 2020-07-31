package com.tingsic.activity;

import android.content.res.Resources;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
//import androidx.fragment.app.FragmentPagerAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tingsic.Fragment.DiscoverSoundListFragment;
import com.tingsic.Fragment.FavouriteSoundFragment;
import com.tingsic.R;
import com.tingsic.View.NonSwipeViewPager;

public class SoundListActivity extends AppCompatActivity implements View.OnClickListener {

    protected TabLayout tablayout;

    protected NonSwipeViewPager pager;

    private ViewPagerAdapter adapter;
    private LinearLayout ll1, ll2;
    private TextView tv1, tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_list);

//        tablayout = (TabLayout) findViewById(R.id.groups_tab);
        pager = findViewById(R.id.viewpager);
        ll1 = findViewById(R.id.ll1);
        ll2 = findViewById(R.id.ll2);
        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        pager = findViewById(R.id.viewpager);
        pager.setOffscreenPageLimit(2);
        pager.setPagingEnabled(false);
        // Note that we are passing childFragmentManager, not FragmentManager
        adapter = new ViewPagerAdapter(getResources(), getSupportFragmentManager());
        pager.setAdapter(adapter);
        //tablayout.setupWithViewPager(pager);

        ll1.setOnClickListener(this);
        ll2.setOnClickListener(this);
        findViewById(R.id.Goback).setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Goback:
                onBackPressed();
                break;
            case R.id.ll1:
                tv1.setBackground(getDrawable(R.drawable.bg_gray_5));
                tv2.setBackground(getDrawable(R.drawable.bg_black_5));
                pager.setCurrentItem(0);
                break;
            case R.id.ll2:
                tv2.setBackground(getDrawable(R.drawable.bg_gray_5));
                tv1.setBackground(getDrawable(R.drawable.bg_black_5));
                pager.setCurrentItem(1);
                break;
        }
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {


        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


        public ViewPagerAdapter(final Resources resources, FragmentManager fm) {

            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new DiscoverSoundListFragment();
                    break;
                case 1:
                    result = new FavouriteSoundFragment();
                    break;
                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            switch (position) {
                case 0:
                    return "Discover";
                case 1:
                    return "My Favorites";

                default:
                    return null;

            }


        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }


        /**
         * Get the Fragment by position
         *
         * @param position tab position of the fragment
         * @return
         */


        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }


    }

}