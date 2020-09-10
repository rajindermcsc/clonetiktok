package com.tingsic.activity;

import android.content.Intent;
import android.os.Build;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.tingsic.FourChamp;
import com.tingsic.Fragment.NotificationsFragment;
import com.tingsic.R;

public class NotificationActivity extends AppCompatActivity  {
    private static final String TAG = "Notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contest);

        NotificationsFragment notificationFragment = new NotificationsFragment();
//        notificationFragment.setOnContestListener(this);


        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.add(R.id.frag_contest_activty, notificationFragment,"NotificationFragment").commit();



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

    private void changeTabColor(boolean transparent) {
        Intent intent = new Intent("change_color_of_tab");
        intent.putExtra("transparent",transparent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onResume() {

        boolean isResumed = FourChamp.isIsParentResumed();

        if (!isResumed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FragmentManager fm = getSupportFragmentManager();
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStackImmediate();
            }
            //Log.e("TAG","count"+fm.getBackStackEntryCount());

        }

        FourChamp.setIsParentResumed(false);

        //Log.e("TAG","count: "+isResumed);

        super.onResume();

    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.cancelAll();
//    }



}