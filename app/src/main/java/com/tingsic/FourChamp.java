package com.tingsic;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.tingsic.R;

import io.branch.referral.Branch;

public class FourChamp extends Application {

    public static final String VIDEO_BASE_URL = "http://tingsic.com/uploads/videos/";

    private static boolean isParentResumed = false;

    public static boolean isIsParentResumed() {
        return isParentResumed;
    }

    public static void setIsParentResumed(boolean isParentResumed) {
        FourChamp.isParentResumed = isParentResumed;
    }

    private static FourChamp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        AnalyticsTrackers.initialize(this);
        AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
        AudienceNetworkAds.initialize(this);
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Initialize the Branch object
        Branch.getAutoInstance(this);
    }

    public static synchronized FourChamp getInstance() {
        return mInstance;
    }

    public synchronized Tracker getGoogleAnalyticsTracker() {
        AnalyticsTrackers analyticsTrackers = AnalyticsTrackers.getInstance();
        return analyticsTrackers.get(AnalyticsTrackers.Target.APP);
    }

    /***
     * Tracking screen view
     *
     * @param screenName screen name to be displayed on GA dashboard
     */
    public void trackScreenView(String screenName) {
        Tracker t = getGoogleAnalyticsTracker();

        // Set screen name.
        t.setScreenName(screenName);

        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());

        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }

    /***
     * Tracking exception
     *
     * @param e exception to be tracked
     */
    public void trackException(Exception e) {
        if (e != null) {
            Tracker t = getGoogleAnalyticsTracker();

            t.send(new HitBuilders.ExceptionBuilder()
                    .setDescription(
                            new StandardExceptionParser(this, null)
                                    .getDescription(Thread.currentThread().getName(), e))
                    .setFatal(false)
                    .build()
            );
        }
    }

    /***
     * Tracking event
     *
     * @param category event category
     * @param action   action of the event
     * @param label    label
     */
    public void trackEvent(String category, String action, String label) {
        Tracker t = getGoogleAnalyticsTracker();

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }

}
