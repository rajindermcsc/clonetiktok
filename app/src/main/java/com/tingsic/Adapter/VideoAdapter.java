package com.tingsic.Adapter;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlWatermarkFilter;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.wang.avi.AVLoadingIndicatorView;
import com.tingsic.API.ApiClient;
import com.tingsic.API.ApiInterface;
import com.tingsic.Fragment.CommentBSFragment;
import com.tingsic.Fragment.LogInBSFragment;
import com.tingsic.Listner.OnLoadMoreListener;
import com.tingsic.POJO.Auth;
import com.tingsic.POJO.LikenShare.Data;
import com.tingsic.POJO.LikenShare.LikenShareResponse;
import com.tingsic.POJO.LikenShare.Request;
import com.tingsic.POJO.LikenShare.Requests.AddLikeRequest;
import com.tingsic.POJO.LikenShare.Requests.ShareRequest;
import com.tingsic.POJO.Video.Video;
import com.tingsic.R;
import com.tingsic.Transformation.CircleTransform;
import com.tingsic.Utils.PreciseCount;
import com.tingsic.View.LikeButton.LikeButton;
import com.tingsic.View.SocialView.SocialTextView;
import com.tingsic.View.SocialView.SocialView;
import com.tingsic.activity.HashTagVideoActivity;
import com.tingsic.activity.UserActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements LikeButton.OnLikeEventListener,
        CommentBSFragment.OnCommentAddListener,
        CommentAdapter.OnCommentRemoveListener {

    private static final String VIDEO_BASE_URL = "https://websoftquality.com/uploads/videos/";
    private static final String PROFIL_URL = "https://websoftquality.com/uploads/profile_pic/";
    private static final int AD_DISPLAY_FREQUENCY = 10;
    private static final int POST_TYPE = 0;
    public static final int AD_TYPE = 1;
    private static final String TAG = VideoAdapter.class.getSimpleName();
    private String[] ids;

    private Context context;
    private List<Video> videos;
    private List<NativeAd> mAdItems;

    private DownloadManager downloadManager;
    private long refid;
    private ArrayList<Long> list = new ArrayList<>();
    private ArrayList<DownloadM> mlist = new ArrayList<>();
    private String filename;

    public List<Video> getData() {
        return videos;
    }

    public static class DownloadM {
        Long refId;
        String fileName;

        public Long getRefId() {
            return refId;
        }

        public void setRefId(Long refId) {
            this.refId = refId;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

    }

    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private InterstitialAd interstitialAd;

    private NativeAdsManager mNativeAdsManager;

    public VideoAdapter(Context context, List<Video> videos, RecyclerView recyclerView) {

        this.context = context;
        this.videos = videos;
        mAdItems = new ArrayList<>();
        this.ids = readFromFile(context).split(",");
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        //AdSettings.addTestDevice("3d4047c6-375d-4189-ae15-9c221f6fb6e0");

        mNativeAdsManager = new NativeAdsManager(context, context.getString(R.string.facebook_ads_native), 1);
        mNativeAdsManager.loadAds();

        interstitialAd = new InterstitialAd(context, context.getString(R.string.facebook_ads_interestial));
        interstitialAd.loadAd();

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = layoutManager.getItemCount();
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void Set_Player(final int currentPage, MyViewHolder viewHolder) {

        final Video item = videos.get(currentPage);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "TikTok"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(VIDEO_BASE_URL + item.getVideoUrl()));

        Log.d("resp", VIDEO_BASE_URL + item.getVideoUrl());


        player.prepare(videoSource);

        player.setRepeatMode(Player.REPEAT_MODE_ALL);


        PlayerView playerView = viewHolder.player;
        playerView.setPlayer(player);


        player.setPlayWhenReady(true);
//        privious_player = player;


//        final RelativeLayout mainlayout = layout.findViewById(R.id.mainlayout);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    super.onFling(e1, e2, velocityX, velocityY);
                    float deltaX = e1.getX() - e2.getX();
                    float deltaXAbs = Math.abs(deltaX);
                    // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
                    if ((deltaXAbs > 100) && (deltaXAbs < 1000)) {
                        if (deltaX > 0) {
                            //   OpenProfile(item, true);
                        }
                    }


                    return true;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    super.onSingleTapUp(e);
                    if (!player.getPlayWhenReady()) {
                        player.setPlayWhenReady(true);
                    } else {
                        player.setPlayWhenReady(false);
                    }


                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    //       Show_video_option(item);

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if (!player.getPlayWhenReady()) {
                        //privious_player.setPlayWhenReady(true);
                    }


//                    if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
//                        Show_heart_on_DoubleTap(item, mainlayout, e);
//                        Like_Video(currentPage, item);
//                    } else {
//                        Toast.makeText(context, "Please Login into app", Toast.LENGTH_SHORT).show();
//                    }
                    return super.onDoubleTap(e);

                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        if (i == AD_TYPE) {

            View inflatedView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.native_ad_unit_test_full, viewGroup, false);
            return new AdHolder(inflatedView);

        } else {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_rv_video, viewGroup, false);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
            return new MyViewHolder(view);

        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder.getItemViewType() == POST_TYPE) {

            //todo undo for ads
            int i = position - ((position + 1) / AD_DISPLAY_FREQUENCY);

            MyViewHolder myViewHolder = (MyViewHolder) viewHolder;

            Integer likeCount = Integer.parseInt(videos.get(i).getTotalLikes());

            if (Arrays.asList(ids).contains(videos.get(i).getVid())) {
                myViewHolder.likeButton.setCurrentlyLiked(true);
            } else {
                myViewHolder.likeButton.setCurrentlyLiked(false);
            }
            myViewHolder.tvVideoDescription.setText(videos.get(i).getDescription());
            myViewHolder.tvVideoDescription.setOnHashtagClickListener(new SocialView.OnClickListener() {
                @Override
                public void onClick(@NonNull SocialView view, @NonNull CharSequence text) {
                    context.startActivity(new Intent(context, HashTagVideoActivity.class).putExtra("hashtag", text.toString()));
                }
            });

            myViewHolder.likeButton.setLikeIcon(myViewHolder.itemView.getContext().getResources().getDrawable(R.drawable.ic_like_red));
            myViewHolder.likeButton.setUnlikeIcon(myViewHolder.itemView.getContext().getResources().getDrawable(R.drawable.ic_like));
            myViewHolder.likeButton.setCount(likeCount);
            myViewHolder.likeButton.setOnLikeEventListener(this);
            myViewHolder.likeButton.setPositon(i);

            //myViewHolder.tvLike.setText(videos.get(i).getTotalLike());
            myViewHolder.tvComment.setText(PreciseCount.from(videos.get(i).getTotalComment()));
            myViewHolder.tvShare.setText(PreciseCount.from(videos.get(i).getTotalShare()));
            myViewHolder.tvVideUsername.setText(context.getString(R.string.user_name, videos.get(i).getUsername()));
            myViewHolder.tvVideoContestName.setText(videos.get(i).getContestTitle());

//            myViewHolder.player.setUp(VIDEO_BASE_URL + videos.get(i).getVideoUrl(), videos.get(i).getVid(), JzvdStd.SCREEN_WINDOW_LIST);
//            myViewHolder.player.setBufferProgress(0);
            if (videos.get(i).getIs_verifed() != null && videos.get(i).getIs_verifed().length() > 0 && videos.get(i).getIs_verifed().equals("1")) {
                myViewHolder.imgIsVarified.setVisibility(View.VISIBLE);
            } else {
                myViewHolder.imgIsVarified.setVisibility(View.GONE);
            }
            String profileUrl = videos.get(i).getProfilepic();
            String thumbUrl = videos.get(i).getThumbUrl();

            int win = Integer.parseInt(videos.get(i).getIsWin());
            if (win == 1) {
                myViewHolder.ivWin.setVisibility(View.VISIBLE);
            } else {
                myViewHolder.ivWin.setVisibility(View.GONE);
            }

            if (profileUrl != null && profileUrl.isEmpty()) {
                Picasso.get().load(R.drawable.blank_profile).transform(new CircleTransform()).into(myViewHolder.ivUser);
            } else {
                Picasso.get().load(PROFIL_URL + profileUrl).placeholder(R.drawable.blank_profile).transform(new CircleTransform()).resize(90, 90).into(myViewHolder.ivUser);
            }

            if (!thumbUrl.isEmpty()) {
                Picasso.get().load(VIDEO_BASE_URL + thumbUrl).into(myViewHolder.getTarget());
            }

            //Set_Player(i, myViewHolder);
            if (i == 0) {
//                myViewHolder.player.startButton.performClick();
            }

            //Disable own profile on video
            if (isUserLoggedIn()) {
                int id = PreferenceManager.getDefaultSharedPreferences(context).getInt("id", -111);
                if (id != -111) {
                    if (videos.get(i).getUserId().equals("" + id)) {
                        myViewHolder.ivUser.setVisibility(View.INVISIBLE);
                    } else {
                        myViewHolder.ivUser.setVisibility(View.VISIBLE);
                    }
                }
            }



            /*
             * Downloading Video
             * */
            final String url = VIDEO_BASE_URL + videos.get(i).getVideoUrl();
            myViewHolder.tvDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (interstitialAd.isAdLoaded()) {
                        interstitialAd.show();
                        interstitialAd.setAdListener(new InterstitialAdListener() {
                            @Override
                            public void onInterstitialDisplayed(Ad ad) {

                            }

                            @Override
                            public void onInterstitialDismissed(Ad ad) {
                                handleDownload(url);
                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {

                            }

                            @Override
                            public void onAdLoaded(Ad ad) {

                            }

                            @Override
                            public void onAdClicked(Ad ad) {

                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {

                            }
                        });
                    } else {
                        handleDownload(url);
                    }

                }
            });


        } else if (viewHolder.getItemViewType() == AD_TYPE) {


            NativeAd ad;

            if (mAdItems.size() > position / AD_DISPLAY_FREQUENCY) {
                ad = mAdItems.get(position / AD_DISPLAY_FREQUENCY);
            } else {
                ad = mNativeAdsManager.nextNativeAd();
                mAdItems.add(ad);
            }

            AdHolder adHolder = (AdHolder) viewHolder;
            adHolder.adChoicesContainer.removeAllViews();

            if (ad != null) {

                adHolder.tvAdTitle.setText(ad.getAdvertiserName());
                adHolder.tvAdBody.setText(ad.getAdBodyText());
                adHolder.tvAdSocialContext.setText(ad.getAdSocialContext());
                adHolder.tvAdSponsoredLabel.setText(ad.getSponsoredTranslation());
                adHolder.btnAdCallToAction.setText(ad.getAdCallToAction());
                adHolder.btnAdCallToAction.setVisibility(
                        ad.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                AdChoicesView adChoicesView = new AdChoicesView(adHolder.itemView.getContext(),
                        ad, true);
                adHolder.adChoicesContainer.addView(adChoicesView, 0);

                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(adHolder.ivAdIcon);
                clickableViews.add(adHolder.mvAdMedia);
                clickableViews.add(adHolder.btnAdCallToAction);
                ad.registerViewForInteraction(
                        adHolder.itemView,
                        adHolder.mvAdMedia,
                        adHolder.ivAdIcon,
                        clickableViews);
            }

            /*AdHolder adHolder = (AdHolder) viewHolder;
            adHolder.setIsRecyclable(false);

            NativeAd nativeAd;

            if (mAdItems.size() > position / AD_DISPLAY_FREQUENCY) {
                nativeAd = mAdItems.get(position / AD_DISPLAY_FREQUENCY);
            } else {
                nativeAd = new NativeAd(context, context.getString(R.string.facebook_ads_native));
                AdSettings.addTestDevice("3d4047c6-375d-4189-ae15-9c221f6fb6e0");
                nativeAd.loadAd(NativeAdBase.MediaCacheFlag.ALL);
                mAdItems.add(nativeAd);
            }

            // Add the Ad view into the ad container.
            LayoutInflater inflater = LayoutInflater.from(context);
            // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
            LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_layout_full, adHolder.adLayout, false);
            adHolder.adLayout.addView(adView);

            // Add the AdOptionsView
            LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
            AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, adHolder.adLayout);
            adChoicesContainer.removeAllViews();
            adChoicesContainer.addView(adOptionsView, 0);

            // Create native UI using the ad metadata.
            AdIconView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
            TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
            MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
            TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
            TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
            TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
            Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

            // Set the Text.
            nativeAdTitle.setText(nativeAd.getAdvertiserName());
            nativeAdBody.setText(nativeAd.getAdBodyText());
            nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
            nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
            nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
            sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

            // Create a list of clickable views
            List<View> clickableViews = new ArrayList<>();
            clickableViews.add(nativeAdTitle);
            clickableViews.add(nativeAdCallToAction);

            // Register the Title and CTA button to listen for clicks.
            nativeAd.registerViewForInteraction(
                    adView,
                    nativeAdMedia,
                    nativeAdIcon,
                    clickableViews);*/
        }

    }


    private void handleDownload(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("" + url));
        String[] filename = url.split("/");
        VideoAdapter.this.filename = filename[filename.length - 1];
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(true);
        request.setTitle(VideoAdapter.this.filename);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/Tingsic/" + ".no_watermark" + VideoAdapter.this.filename);


        refid = downloadManager.enqueue(request);
        DownloadM downloadM = new DownloadM();
        downloadM.setRefId(refid);
        downloadM.setFileName(VideoAdapter.this.filename);

        Log.e("OUTbtnDownload", "" + refid + " " + filename[filename.length - 1]);

        list.add(refid);
        mlist.add(downloadM);
        /*PRDownloader.initialize(context.getApplicationContext());
        DownloadRequest prDownloader = PRDownloader.download(url, Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/Tingsic/", ".no_watermark" + VideoAdapter.this.filename)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                        int prog = (int) ((progress.currentBytes * 100) / progress.totalBytes);


                    }
                });*/

/*
        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {

            }

            @Override
            public void onError(Error error) {
                Delete_file_no_watermark();
//                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
//                        Functions.cancel_determinent_loader();
            }


        });*/
        Toast.makeText(context, "Downloading...", Toast.LENGTH_LONG).show();
    }

    public void Applywatermark(final NotificationManager notificationManager, final int m, final NotificationCompat.Builder builder, final String mfileName) {

        Bitmap myLogo = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.logo)).getBitmap();
        Bitmap bitmap_resize = Bitmap.createScaledBitmap(myLogo, 75, 75, false);
        GlWatermarkFilter filter = new GlWatermarkFilter(bitmap_resize, GlWatermarkFilter.Position.LEFT_TOP);
        new GPUMp4Composer(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/Tingsic/" + ".no_watermark" + mfileName,
                Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/Tingsic/" + mfileName)
                .filter(filter)

                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                        Log.d("resp", "" + (int) (progress * 100));
                        // Functions.Show_loading_progress((int)((progress*100)/2)+50);

                    }

                    @Override
                    public void onCompleted() {

                        if (notificationManager != null) {
                            notificationManager.notify(m/*Notification id*/, builder.build());
                        }
                        Delete_file_no_watermark(mfileName);

                    }

                    @Override
                    public void onCanceled() {
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {

                        Log.d("resp", exception.toString());

                        try {

                            Delete_file_no_watermark(mfileName);
                            //  Functions.cancel_determinent_loader();
                            //Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {

                        }

                    }
                })
                .start();
    }


    public void Delete_file_no_watermark(String mfileName) {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/Tingsic/" + ".no_watermark" + mfileName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        //  Jzvd.releaseAllVideos();
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    @Override
    public int getItemViewType(int position) {
        //todo add ads here undo comments
        return POST_TYPE;
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public void onLikeClicked(LikeButton androidLikeButton) {

        if (isUserLoggedIn()) {
            int likeCount = androidLikeButton.getCount();
            likeCount = likeCount + 1;
            androidLikeButton.setCount(likeCount);
            addLikeAPI(androidLikeButton.getPositon());
        } else {
            androidLikeButton.performClick();
            showLogInFragment();
        }

    }

    @Override
    public void onUnlikeClicked(LikeButton androidLikeButton) {

        if (isUserLoggedIn()) {

            int likeCount = androidLikeButton.getCount();
            likeCount = likeCount - 1;
            androidLikeButton.setCount(likeCount);

            String[] ids = readFromFile(context).split(",");
            List<String> list = new ArrayList<>(Arrays.asList(ids));
            list.remove(videos.get(androidLikeButton.getPositon()).getVid());
            ids = list.toArray(new String[]{});
            Log.e("Straing", "onUnlikeClicked: " + stringArrayToString(ids));
            writeToFile(stringArrayToString(ids), context);

        }

    }

    @Override
    public void onCommentAdded(int position) {
        int count = Integer.parseInt(videos.get(position).getTotalComment());
        videos.get(position).setTotalComment("" + (count + 1));
        notifyItemChanged(position);
    }

    @Override
    public void onCommentRemoved(int position) {
        int count = Integer.parseInt(videos.get(position).getTotalComment());
        videos.get(position).setTotalComment("" + (count - 1));
        notifyItemChanged(position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Target {

        RenderScript rs = RenderScript.create(context);
        private PlayerView player;
        //private CheckBox tvLike;
        private TextView tvComment, tvShare, tvDownload, tvVideUsername, tvVideoContestName;
        private SocialTextView tvVideoDescription;
        private ImageView ivUser, ivWin;
        private LikeButton likeButton;
        private ImageView imgIsVarified;
        private AVLoadingIndicatorView indicatorView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            player = itemView.findViewById(R.id.playerview);
            indicatorView = itemView.findViewById(R.id.avi);
            indicatorView.show();
//            player.titleTextView.setVisibility(View.GONE);
//            player.progressBar.setVisibility(View.GONE);
//            player.currentTimeTextView.setVisibility(View.GONE);

            //tvLike = itemView.findViewById(R.id.btnLike);
            tvComment = itemView.findViewById(R.id.btnComment);
            tvShare = itemView.findViewById(R.id.btnshare);
            tvDownload = itemView.findViewById(R.id.btndownload);
            tvVideUsername = itemView.findViewById(R.id.tv_video_username);
            imgIsVarified = itemView.findViewById(R.id.imgIsVarified);
            tvVideoContestName = itemView.findViewById(R.id.tv_video_contest_name);

            ivUser = itemView.findViewById(R.id.iv_video_user);
            ivWin = itemView.findViewById(R.id.iv_win);

            likeButton = itemView.findViewById(R.id.likeButton);
            tvVideoDescription = itemView.findViewById(R.id.tv_video_description);

            tvComment.setOnClickListener(this);
            tvShare.setOnClickListener(this);
            ivUser.setOnClickListener(this);

        }

        private Target getTarget() {
            return this;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            //todo undo for ads
            int index = position - ((position + 1) / AD_DISPLAY_FREQUENCY);
            switch (v.getId()) {
                case R.id.btnComment:
                    handleComment(index);
                    break;
                case R.id.btnshare:
//                    if (player.currentState == Jzvd.CURRENT_STATE_PLAYING) {
//                        player.startButton.performClick();
//                    }
                    initBranch(index);
                    break;
                case R.id.iv_video_user:
                    handleUserProfile(index);
                    break;

            }
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//            player.thumbImageView.setImageBitmap(bitmap);
            //Background blur
            Bitmap bitmapOriginal = bitmap.copy(bitmap.getConfig(), true);

            if (bitmapOriginal.getWidth() >= bitmapOriginal.getHeight()) {

                bitmapOriginal = Bitmap.createBitmap(
                        bitmapOriginal,
                        bitmapOriginal.getWidth() / 2 - bitmapOriginal.getHeight() / 2,
                        0,
                        9 * bitmapOriginal.getWidth() / 16,
                        bitmapOriginal.getHeight()
                );

            }

            final Allocation input = Allocation.createFromBitmap(rs, bitmapOriginal); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(23f);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmapOriginal);

            Drawable drawable = new BitmapDrawable(context.getResources(), bitmapOriginal);
            drawable.setAlpha(100);
//            player.setBackgroundBlur(drawable);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

    /*private static class AdHolder extends RecyclerView.ViewHolder {

        NativeAdLayout adLayout;

        AdHolder(View view) {
            super(view);

            adLayout = view.findViewById(R.id.native_banner_ad_container);

        }
    }*/

    private static class AdHolder extends RecyclerView.ViewHolder {
        MediaView mvAdMedia;
        AdIconView ivAdIcon;
        TextView tvAdTitle;
        TextView tvAdBody;
        TextView tvAdSocialContext;
        TextView tvAdSponsoredLabel;
        Button btnAdCallToAction;
        LinearLayout adChoicesContainer;

        AdHolder(View view) {
            super(view);

            mvAdMedia = (MediaView) view.findViewById(R.id.native_ad_media);
            tvAdTitle = (TextView) view.findViewById(R.id.native_ad_title);
            tvAdBody = (TextView) view.findViewById(R.id.native_ad_body);
            tvAdSocialContext = (TextView) view.findViewById(R.id.native_ad_social_context);
            tvAdSponsoredLabel = (TextView) view.findViewById(R.id.native_ad_sponsored_label);
            btnAdCallToAction = (Button) view.findViewById(R.id.native_ad_call_to_action);
            ivAdIcon = (AdIconView) view.findViewById(R.id.native_ad_icon);
            adChoicesContainer = (LinearLayout) view.findViewById(R.id.ad_choices_container);

        }
    }

    private void handleComment(int position) {
        if (isUserLoggedIn()) {
            showCommentFragment(position);
        } else {
            showLogInFragment();
        }
    }

    private void handleUserProfile(int position) {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra("userId", videos.get(position).getUserId());
        context.startActivity(intent);
    }

    private void showCommentFragment(int position) {
        int id = PreferenceManager.getDefaultSharedPreferences(context).getInt("id", -111);
        String profilePic = PreferenceManager.getDefaultSharedPreferences(context).getString("profilePic", "");

        Bundle bundle = new Bundle();
        bundle.putString("totalComment", videos.get(position).getTotalComment());
        bundle.putString("vid", videos.get(position).getVid());
        bundle.putString("user_id", "" + id);
        bundle.putString("profilePic", profilePic);
        CommentBSFragment fragment = new CommentBSFragment();
        fragment.setOnCommentAddListener(this, position);
        fragment.setOnCommentRemoveListener(this, position);
        fragment.setArguments(bundle);
        fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), fragment.getTag());
    }

    private void showLogInFragment() {
        LogInBSFragment fragment = new LogInBSFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isProfileActivity", false);
        fragment.setArguments(bundle);
        fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), fragment.getTag());
    }

    private boolean isUserLoggedIn() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("isLoggedIn", false);
    }

    private void addLikeAPI(int position) {

        final int pos = position;

        AddLikeRequest likeRequest = new AddLikeRequest();

        Auth auth = new Auth();
        int id = PreferenceManager.getDefaultSharedPreferences(context).getInt("id", -111);
        String token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "null");

        if (id == -111 || token.equals("null")) {
            showLogInFragment();
            return;
        }

        auth.setToken(token);
        auth.setId(id);

        Data data = new Data();
        data.setVideoId(videos.get(position).getVid());
        data.setType("likes");
        data.setCount("1");
        data.setUserId(videos.get(position).getUserId());

        Request request = new Request();
        request.setData(data);

        likeRequest.setAuth(auth);
        likeRequest.setRequest(request);
        likeRequest.setService("SaveLikes");

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<LikenShareResponse> responseCall = apiInterface.addLike(likeRequest);
        responseCall.enqueue(new Callback<LikenShareResponse>() {
            @Override
            public void onResponse(Call<LikenShareResponse> call, Response<LikenShareResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getSuccess() == 1) {
                        String[] ids = readFromFile(context).split(",");
                        Log.e("IDS", "onResponse: " + stringArrayToString(ids));
                        List<String> list = new ArrayList<>(Arrays.asList(ids));
                        list.add(videos.get(pos).getVid());
                        Set<String> stringSet = new HashSet<>(list);
                        ids = stringSet.toArray(new String[]{});
                        Log.e("Straing", "onResponse: " + stringArrayToString(ids));
                        writeToFile(stringArrayToString(ids), context);
                    }
                }
            }

            @Override
            public void onFailure(Call<LikenShareResponse> call, Throwable t) {

            }
        });

    }

    private void getShareAPI(int position) {

        final int pos = position;

        ShareRequest shareRequest = new ShareRequest();

        Data data = new Data();
        data.setVideoId(videos.get(position).getVid());
        data.setType("share");
        data.setCount("1");
        data.setUserId(videos.get(position).getUserId());

        Request request = new Request();
        request.setData(data);

        shareRequest.setRequest(request);
        shareRequest.setService("SaveShare");

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<LikenShareResponse> responseCall = apiInterface.saveShare(shareRequest);
        responseCall.enqueue(new Callback<LikenShareResponse>() {
            @Override
            public void onResponse(Call<LikenShareResponse> call, Response<LikenShareResponse> response) {
                Log.i("VideoAdapter", "onResponse: " + response.code());
                if (response.isSuccessful()) {
                    if (response.body().getSuccess() == 1) {
                        int share = Integer.parseInt(videos.get(pos).getTotalShare());
                        videos.get(pos).setTotalShare("" + (share + 1));
                        notifyItemChanged(pos);
                    }
                }
            }

            @Override
            public void onFailure(Call<LikenShareResponse> call, Throwable t) {

            }
        });

    }

    private void initBranch(final int position) {

        //todo change desktop url

        Video video = videos.get(position);

        String vUrl = video.getVideoUrl();
        video.setVideoUrl(vUrl);
        String url = video.getThumbUrl();
        video.setThumbUrl(url);

        Gson gson = new Gson();
        String json = gson.toJson(video);

        String title = video.getContestTitle();

        Log.i("ShareJson", "Json Object: " + json);
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("content/12345")
                .setTitle("Tingsic")
                .setContentImageUrl(url)
                .setContentDescription("Check out this new video")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("data", json));

        LinkProperties lp = new LinkProperties()
                .setFeature("sharing")
                .setCampaign(title)
                .setStage("Tingsic user")
                .addControlParameter("$desktop_url", "https://websoftquality.com/playstore.php")
                .addControlParameter("custom", "data")
                .addControlParameter("custom_random", Long.toString(Calendar.getInstance().getTimeInMillis()));

        ShareSheetStyle ss = new ShareSheetStyle(context, title, title)
                .setCopyUrlStyle(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_send), "Copy", "Added to clipboard")
                .setMoreOptionStyle(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_more), "Show more")
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK_MESSENGER)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FLICKR)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.WHATS_APP)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.GMAIL)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.GOOGLE_DOC)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.HANGOUT)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.INSTAGRAM)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.PINTEREST)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.SNAPCHAT)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.TWITTER)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.WECHAT)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.EMAIL)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.MESSAGE)
                .setAsFullWidthStyle(true)
                .setSharingTitle("Share With");

        buo.showShareSheet((Activity) context, lp, ss, new Branch.BranchLinkShareListener() {
            @Override
            public void onShareLinkDialogLaunched() {
            }

            @Override
            public void onShareLinkDialogDismissed() {
            }

            @Override
            public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {
                Log.i("Branch", "onLinkShareResponse: SharingLink: " + sharedLink);
            }

            @Override
            public void onChannelSelected(String channelName) {

                Log.i("Branch", "onChannelSelected: " + channelName);
                getShareAPI(position);

            }
        });
    }

    private void writeToFile(String text, Context context) {
        try {
            OutputStreamWriter stream = new OutputStreamWriter(context.openFileOutput("like.txt", Context.MODE_PRIVATE));
            stream.write(text);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("VideoAdapter", "writeToFile: failed");
        }
    }

    private String readFromFile(Context context) {
        String data = "";

        try {

            InputStream stream = context.openFileInput("like.txt");


            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(streamReader);
            String recievedData;

            StringBuilder builder = new StringBuilder();

            while ((recievedData = reader.readLine()) != null) {
                builder.append(recievedData);
            }

            stream.close();

            data = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    private static String stringArrayToString(String[] stringArray) {
        StringBuilder sb = new StringBuilder();
        for (String element : stringArray) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(element);
        }
        return sb.toString();
    }

    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long refrenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Log.i("INrefrenceId", "" + refrenceId);

//            if (list.isEmpty()) {
            File file;

            Uri path;
            for (int i = 0; i < mlist.size(); i++) {
                if (mlist.get(i).getRefId().equals(refrenceId)) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                                + File.separator + "/Tingsic/" + mlist.get(i).getFileName());
                        path = FileProvider.getUriForFile(context, "com.tingsic.provider", file);
                    } else {
                        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + "/Tingsic/" + mlist.get(i).getFileName());
                        path = Uri.fromFile(file);
                    }


                    String channelid = context.getString(R.string.default_notification_channel_id);
                    Log.i("Path", "" + path.toString());
                    Intent open = new Intent(Intent.ACTION_VIEW);
                    open.setDataAndType(path, "video/*");
                    open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, open, 0);
                    Log.i("INSIDE refrenceId", "" + refrenceId);
                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelid)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(pendingIntent)
                            .setContentTitle(filename)
                            .setContentText("Download Complete")
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setSound(defaultSoundUri)
                            .setAutoCancel(true);
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(channelid, "Tingsic Notification", NotificationManager.IMPORTANCE_HIGH);
                        assert notificationManager != null;
                        notificationManager.createNotificationChannel(channel);
                    }
                    Applywatermark(notificationManager, m, builder, mlist.get(i).getFileName());
//                    if (notificationManager != null) {
//                        notificationManager.notify(m/*Notification id*/, builder.build());
//                    }

//            }
                    list.remove(refrenceId);
                    mlist.remove(mlist.get(i));
                }
            }

        }
    };

    public void onDestroy() {
        if (!mlist.isEmpty()) {
            for (int i = 0; i < mlist.size(); i++) {
                downloadManager.remove(mlist.get(i).getRefId());
                Delete_file_no_watermark(mlist.get(i).getFileName());
            }
        }
        interstitialAd.destroy();
        context.unregisterReceiver(onComplete);
    }

}
