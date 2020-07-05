package com.tingsic.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tingsic.FourChamp;
import com.tingsic.Fragment.FollowingFragment;
import com.tingsic.Fragment.SearchFragment;
import com.tingsic.Fragment.UserFragment;
import com.tingsic.Fragment.VideoPostFragment;
import com.tingsic.Listner.OnFollowerListener;
import com.tingsic.Listner.OnPeopleListener;
import com.tingsic.Listner.OnVideoListener;
import com.tingsic.POJO.People;
import com.tingsic.POJO.Video.Video;
import com.tingsic.R;

import java.util.List;

public class SearchActivity extends AppCompatActivity implements OnPeopleListener, OnFollowerListener, OnVideoListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SearchFragment fragment = new SearchFragment();
        fragment.setOnSearchFragmentListener(this);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.add(R.id.frag_search_activty, fragment,"SearchFragment").commit();

    }

    @Override
    public void onListClicked(People people) {
        UserFragment fragment = new UserFragment();
        fragment.setOnFollowerListener(this);
        fragment.setOnVideoListener(this);
        Bundle bundle = new Bundle();
        bundle.putString("userId",people.getUserId());
        fragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        ft.addToBackStack("SearchFragment");
        ft.add(R.id.frag_search_activty, fragment, "UserFragment").commit();

        changeTabColor(false);
    }

    @Override
    public void onFollowerSelected(String userId) {
        FollowingFragment fragment = new FollowingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId",userId);
        fragment.setArguments(bundle);
        fragment.setOnPeopleListener(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        ft.addToBackStack("SearchFragment");
        ft.add(R.id.frag_search_activty, fragment, "FollowingFragment").commit();

        changeTabColor(false);
    }

    @Override
    public void onVideoSelected(int position, List<Video> videos) {
        VideoPostFragment fragment = new VideoPostFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position",position);
        fragment.setArguments(bundle);
        fragment.setVideos(videos);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        ft.addToBackStack("SearchFragment");
        ft.add(R.id.frag_search_activty, fragment, "VideoPostFragment").commit();

        changeTabColor(true);
    }

    private void changeTabColor(boolean transparent) {
        Intent intent = new Intent("change_color_of_tab");
        intent.putExtra("transparent",transparent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm != null) {
            int backStackCount = fm.getBackStackEntryCount();
            if (backStackCount>0) {
                String tag = fm.getBackStackEntryAt(backStackCount-1).getName();
                if (tag != null && !tag.equals("VideoPostFragment")) {
                    changeTabColor(false);
                }
                super.onBackPressed();
            }
            else {
                this.getParent().onBackPressed();
            }
        }
    }

    @Override
    protected void onResume() {

        boolean isResumed = FourChamp.isIsParentResumed();

        if (!isResumed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FragmentManager fm = getSupportFragmentManager();
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStackImmediate();
            }
            Log.i("TAG","count"+fm.getBackStackEntryCount());

        }

        FourChamp.setIsParentResumed(false);

        Log.i("TAG","count: "+isResumed);

        super.onResume();

    }


}
