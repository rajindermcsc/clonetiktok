package com.tingsic.activity;

import android.Manifest;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.adapters.TabHostBindingAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.tingsic.FourChamp;
import com.tingsic.Fragment.LogInBSFragment;
import com.tingsic.R;
import com.tingsic.Utils.DisplayHelper;

public class MainActivity extends TabActivity implements TabHost.OnTabChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AnimationDrawable drawable;
    private static final int WRITE_PERMISSION = 372;
    private static int CAMERA = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        getWindow().getDecorView().setSystemUiVisibility(flags);

        initView();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, WRITE_PERMISSION);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(tabColorReceiver, new IntentFilter("change_color_of_tab"));
        LocalBroadcastManager.getInstance(this).registerReceiver(videoLoaderReceiver, new IntentFilter("load_more"));
    }

    private void initView() {

        TabHost tab = findViewById(android.R.id.tabhost);
        tab.setOnTabChangedListener(this);
        tab.setup();
//        LocalActivityManager mLocalActivityManager = new LocalActivityManager(this, false);
//        tab.setup(mLocalActivityManager);

        if (DisplayHelper.hasNavBar(this)) {

//            FrameLayout layout = findViewById(android.R.id.tabcontent);
//
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            layoutParams.addRule(RelativeLayout.ABOVE, android.R.id.tabs);
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//
//            layout.setLayoutParams(layoutParams);
//
//            tab.setBackgroundColor(Color.BLACK);

        }

        if (isUserLoggedIn()) {

            boolean isShared = getIntent().getBooleanExtra("shared", false);

            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("shared", isShared);
            intent.putExtra("video", getIntent().getStringExtra("video"));

            TabHost.TabSpec specHome = tab.newTabSpec("home");
            specHome.setIndicator("", getResources().getDrawable(R.drawable.ic_home));
            specHome.setContent(intent);


            TabHost.TabSpec specSearch = tab.newTabSpec("search");
            specSearch.setIndicator("", getResources().getDrawable(R.drawable.ic_search));
            specSearch.setContent(new Intent(this, SearchActivity.class));


//            TabHost.TabSpec specAdd = tab.newTabSpec("add");
//            specAdd.setIndicator("", getResources().getDrawable(R.drawable.ic_add));
//            specAdd.setContent(intent);


            TabHost.TabSpec specNotifications = tab.newTabSpec("notification");
            specNotifications.setIndicator("", getResources().getDrawable(R.drawable.ic_notifications));
            specNotifications.setContent(new Intent(this, NotificationActivity.class));

//
//        TabHost.TabSpec specBadge = tab.newTabSpec("contest");
//        specBadge.setIndicator("", getResources().getDrawable(R.drawable.ic_badge_white));
//        specBadge.setContent(new Intent(this, ContestActivity.class));


            TabHost.TabSpec specUser = tab.newTabSpec("user");
            specUser.setIndicator("", getResources().getDrawable(R.drawable.ic_user));
            specUser.setContent(new Intent(this, ProfileActivity.class));

            tab.addTab(specHome);
            tab.addTab(specSearch);
//            tab.addTab(specAdd);
            tab.addTab(specNotifications);
            tab.addTab(specUser);

            tab.getTabWidget().getChildAt(0).setBackground(null);
            tab.getTabWidget().getChildAt(1).setBackground(null);
            tab.getTabWidget().getChildAt(2).setBackground(null);
            tab.getTabWidget().getChildAt(3).setBackground(null);
//            tab.getTabWidget().getChildAt(4).setBackground(null);

            Log.i(TAG, "initView: strip" + tab.getTabWidget().isStripEnabled());


            View view = findViewById(R.id.laxman_rekha);
            drawable = (AnimationDrawable) getResources().getDrawable(R.drawable.loading_bar);
            view.setBackgroundColor(getResources().getColor(R.color.transparent_white_60));
            drawable.setEnterFadeDuration(700);
            drawable.setExitFadeDuration(600);
            drawable.stop();

            boolean isProfile = getIntent().getBooleanExtra("isProfile", false);
            if (isProfile) {
                String id = getIntent().getStringExtra("id");
                Intent pIntent = new Intent(this, UserActivity.class);
                pIntent.putExtra("userId", id);
                startActivity(pIntent);
            }
        }
        else {

            boolean isShared = getIntent().getBooleanExtra("shared", false);

            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("shared", isShared);
            intent.putExtra("video", getIntent().getStringExtra("video"));

            TabHost.TabSpec specHome = tab.newTabSpec("home");
            specHome.setIndicator("", getResources().getDrawable(R.drawable.ic_home));
            specHome.setContent(intent);


            TabHost.TabSpec specSearch = tab.newTabSpec("search");
            specSearch.setIndicator("", getResources().getDrawable(R.drawable.ic_search));
            specSearch.setContent(new Intent(this, SearchActivity.class));




            TabHost.TabSpec specNotifications = tab.newTabSpec("notification");
            specNotifications.setIndicator("", getResources().getDrawable(R.drawable.ic_notifications));
            specNotifications.setContent(new Intent(this, NotificationActivity.class));

//
//        TabHost.TabSpec specBadge = tab.newTabSpec("contest");
//        specBadge.setIndicator("", getResources().getDrawable(R.drawable.ic_badge_white));
//        specBadge.setContent(new Intent(this, ContestActivity.class));


            TabHost.TabSpec specUser = tab.newTabSpec("user");
            specUser.setIndicator("", getResources().getDrawable(R.drawable.ic_user));
            specUser.setContent(new Intent(this, ProfileActivity.class));

            tab.addTab(specHome);
            tab.addTab(specSearch);
            tab.addTab(specNotifications);
            tab.addTab(specUser);

            tab.getTabWidget().getChildAt(0).setBackground(null);
            tab.getTabWidget().getChildAt(1).setBackground(null);
            tab.getTabWidget().getChildAt(2).setBackground(null);
            tab.getTabWidget().getChildAt(3).setBackground(null);

            Log.i(TAG, "initView: strip" + tab.getTabWidget().isStripEnabled());


            View view = findViewById(R.id.laxman_rekha);
            drawable = (AnimationDrawable) getResources().getDrawable(R.drawable.loading_bar);
            view.setBackgroundColor(getResources().getColor(R.color.transparent_white_60));
            drawable.setEnterFadeDuration(700);
            drawable.setExitFadeDuration(600);
            drawable.stop();

            boolean isProfile = getIntent().getBooleanExtra("isProfile", false);
            if (isProfile) {
                String id = getIntent().getStringExtra("id");
                Intent pIntent = new Intent(this, UserActivity.class);
                pIntent.putExtra("userId", id);
                startActivity(pIntent);
            }
        }

    }


    @Override
    public void onTabChanged(String tabId) {
        TabWidget widget = findViewById(android.R.id.tabs);
        View view = findViewById(R.id.laxman_rekha);
        if (tabId.equals("home")) {
            widget.setBackgroundColor(Color.TRANSPARENT);
            view.setVisibility(View.VISIBLE);
        } else {
            widget.setBackgroundColor(Color.BLACK);
            view.setVisibility(View.GONE);
        }

        if (tabId.equals("add")){
            widget.setCurrentTab(0);

            if (isUserLoggedIn()) {
                //change 30may
//                startActivityForResult(new Intent(MainActivity.this, VideoRecorderActivity.class), CAMERA);



            }
        }
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK && requestCode == CAMERA) {
//            setDefaultTab(2);
//        }
//    }

    private boolean isUserLoggedIn() {
        boolean logIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isLoggedIn", false);

        return logIn;
    }

    private BroadcastReceiver tabColorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TabWidget widget = findViewById(android.R.id.tabs);
            View view = findViewById(R.id.laxman_rekha);

            boolean isTransaparent = intent.getBooleanExtra("transparent", false);

            boolean hasNav = DisplayHelper.hasNavBar(MainActivity.this);

            if (isTransaparent) {
                widget.setBackgroundColor(Color.TRANSPARENT);
                view.setVisibility(View.VISIBLE);
            } else {
                widget.setBackgroundColor(Color.BLACK);
                view.setVisibility(View.GONE);
            }

        }
    };

    private BroadcastReceiver videoLoaderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            View view = findViewById(R.id.laxman_rekha);
            boolean isLoading = intent.getBooleanExtra("isLoading", false);
            if (isLoading) {
                view.setBackground(drawable);
                drawable.start();
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.transparent_white_60));
                drawable.stop();
            }
        }
    };

    @Override
    public void onBackPressed() {

        TabHost tab = findViewById(android.R.id.tabhost);

        int position = tab.getCurrentTab();

        if (position != 0) {
            tab.setCurrentTab(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //Unregister the broadcast
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tabColorReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(videoLoaderReceiver);

        super.onDestroy();
    }

    @Override
    protected void onResume() {

        Log.i(TAG, "onResume: true");

        FourChamp.setIsParentResumed(true);

        super.onResume();

        if (drawable != null && !drawable.isRunning())
            drawable.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (drawable != null && !drawable.isRunning())
            drawable.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

}
