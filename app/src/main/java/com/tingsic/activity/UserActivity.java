package com.tingsic.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tingsic.Fragment.FollowingFragment;
import com.tingsic.Fragment.UserFragment;
import com.tingsic.Fragment.VideoPostFragment;
import com.tingsic.Listner.OnFollowerListener;
import com.tingsic.Listner.OnPeopleListener;
import com.tingsic.Listner.OnVideoListener;
import com.tingsic.POJO.People;
import com.tingsic.POJO.Video.Video;
import com.tingsic.R;

import java.util.List;

public class UserActivity extends AppCompatActivity implements OnVideoListener, OnFollowerListener, OnPeopleListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Log.e("TAG", "onCreate: haan haige");
        String userId = getIntent().getStringExtra("userId");

        UserFragment userFragment = new UserFragment();
        userFragment.setOnVideoListener(this);
        userFragment.setOnFollowerListener(this);

        Bundle bundle = new Bundle();
        bundle.putString("userId",userId);
        userFragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.add(R.id.frag_user_activty, userFragment,"UserFragment").commit();

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
        ft.addToBackStack("UserFragment");
        ft.add(R.id.frag_user_activty, fragment).commit();
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
        ft.addToBackStack("UserFragment");
        ft.add(R.id.frag_user_activty, fragment, "FollowingFragment").commit();
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
        ft.addToBackStack("FollowingFragment");
        ft.add(R.id.frag_user_activty, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
