package com.tingsic.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.wang.avi.AVLoadingIndicatorView;
import com.tingsic.API.ApiClient;
import com.tingsic.API.ApiInterface;
import com.tingsic.Adapter.VideoAdapter;
import com.tingsic.Listner.OnLoadMoreListener;
import com.tingsic.POJO.Auth;
import com.tingsic.POJO.Video.Request.Following.Data;
import com.tingsic.POJO.Video.Request.Following.FollowingVideoRequest;
import com.tingsic.POJO.Video.Request.Following.Request;
import com.tingsic.POJO.Video.Video;
import com.tingsic.POJO.Video.VideoResponse;
import com.tingsic.POJO.service.Service;
import com.tingsic.POJO.service.ServiceResponse;
import com.tingsic.POJO.service.data.VideoCountData;
import com.tingsic.R;
import com.tingsic.View.LoadingView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowingVideoFragment extends Fragment implements RecyclerView.OnChildAttachStateChangeListener, OnLoadMoreListener, Player.EventListener {

    private static final String TAG = PopularVideoFragment.class.getSimpleName();
    private LoadingView loadingView;
    private TextView tvError;
    private VideoAdapter adapter;
    List<Video> videos;

    private int page;
    private InterstitialAd interstitialAd;

    int currentPage = -1;
    LinearLayoutManager layoutManager;
    int swipe_count = 0;
    private boolean is_visible_to_user;
    com.facebook.ads.InterstitialAd fbinterstitialAd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rv_popular);
        loadingView = view.findViewById(R.id.list_loading);
        tvError = view.findViewById(R.id.tv_internet_error);

        interstitialAd = new InterstitialAd(getContext());
        interstitialAd.setAdUnitId(getString(R.string.admoob_interestial));
        fbinterstitialAd = new com.facebook.ads.InterstitialAd(getContext(), "YOUR_PLACEMENT_ID");
        fbinterstitialAd.loadAd();
        loadAdmobIntAd();
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                loadAdmobIntAd();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                loadAdmobIntAd();
            }

        });
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        //layoutManager.setItemPrefetchEnabled(true);
        recyclerView.setLayoutManager(layoutManager);

        videos = new ArrayList<>();

        adapter = new VideoAdapter(getActivity(), videos, recyclerView);
        adapter.setOnLoadMoreListener(this);
        recyclerView.setAdapter(adapter);
        page = 1;

        if (isUserLoggedIn()) {
            getVideoAPI(1);
            tvError.setVisibility(View.GONE);
        } else {
            loadingView.setVisibility(View.GONE);
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.err_must_login));
            showLogInFragment();
        }

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnChildAttachStateChangeListener(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //here we find the current item number
                final int scrollOffset = recyclerView.computeVerticalScrollOffset();
                final int height = recyclerView.getHeight();
                int page_no = scrollOffset / height;

                if (page_no != currentPage) {
                    currentPage = page_no;
                    View layout = layoutManager.findViewByPosition(currentPage);
                    PlayerView playerView = layout.findViewById(R.id.playerview);
                    Release_Privious_Player();
                    Set_Player(currentPage);

                }
            }
        });
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                Jzvd.releaseAllVideos();
//                int pos = layoutManager.findFirstVisibleItemPosition();
//
//                Log.i("Current", "Position: " + layoutManager.findFirstVisibleItemPosition());
//                if (layoutManager.findViewByPosition(pos) != null) {
//                    if (pos % 5 == 0) {
//                        if (layoutManager.getItemViewType(layoutManager.findViewByPosition(pos)) != VideoAdapter.AD_TYPE) {
////                            Jzvd jzvd = layoutManager.findViewByPosition(pos).findViewById(R.id.player);
////                            if (jzvd != null) {
////                                countVideoView(videos.get(pos).getVid());
////                                //  jzvd.startButton.performClick();
////                                displayAd();
////                            }
//                        }
//                    } else {
//                        if (layoutManager.getItemViewType(layoutManager.findViewByPosition(pos)) != VideoAdapter.AD_TYPE) {
////                            Jzvd jzvd = layoutManager.findViewByPosition(pos).findViewById(R.id.player);
////                            if (jzvd != null) {
////                                countVideoView(videos.get(pos).getVid());
////                                jzvd.startButton.performClick();
////                            }
//                        }
//                    }
//                }
//            }
//        });
        Log.i(TAG, "onCreateView: CAlled");
        return view;
    }

    private void displayAd() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else if (fbinterstitialAd.isAdLoaded()){
            fbinterstitialAd.show();
        }
    }

    private void loadAdmobIntAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }
    SimpleExoPlayer privious_player;

    public void Release_Privious_Player() {
        if (privious_player != null) {
            privious_player.removeListener(this);
            privious_player.release();
        }
    }

    private static final String VIDEO_BASE_URL = "http://tingsic.com/uploads/videos/";
    private AVLoadingIndicatorView indicatorView;
    public void Set_Player(final int currentPage) {

        final Video item = adapter.getData().get(currentPage);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(),
                Util.getUserAgent(getContext(), getString(R.string.app_name)), new DefaultBandwidthMeter());

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(VIDEO_BASE_URL + item.getVideoUrl()));

        Log.d("resp", VIDEO_BASE_URL + item.getVideoUrl());


        player.prepare(videoSource);

        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(this);

        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        View layout = layoutManager.findViewByPosition(currentPage);
        final PlayerView playerView = layout.findViewById(R.id.playerview);
        indicatorView = layout.findViewById(R.id.avi);

        playerView.setPlayer(player);


        player.setPlayWhenReady(true);
        privious_player = player;


//        final RelativeLayout mainlayout = layout.findViewById(R.id.mainlayout);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

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
                        privious_player.setPlayWhenReady(true);
                    } else {
                        privious_player.setPlayWhenReady(false);
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
                        privious_player.setPlayWhenReady(true);
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


        swipe_count++;
        if (swipe_count > 4) {
            displayAd();
            swipe_count = 0;
        }


    }
    private void getVideoAPI(final int page) {
        FollowingVideoRequest videoRequest = new FollowingVideoRequest();

        Request request = new Request();

        Auth auth = new Auth();
        int id = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("id", -111);
        String token = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("token", "null");

        if (id == -111 || token.equals("null")) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.err_must_login));
            showLogInFragment();
            return;
        }

        auth.setToken(token);
        auth.setId(id);

        Data data = new Data();
        data.setUserId("" + id);
        data.setPage(page);

        request.setData(data);

        videoRequest.setService("getContestfollowingvideo");
        videoRequest.setAuth(auth);
        videoRequest.setRequest(request);


        Log.d(TAG, "getVideoAPI() returned: " + videoRequest.toString());
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<VideoResponse> videoResponseCall = apiInterface.getFollowingVideos(videoRequest);

        videoResponseCall.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {

                Log.i(TAG, "onResponse: " + response.code());
                if (response.isSuccessful()) {
                    if (response.body().getSuccess() == 1) {
                        videos.addAll(response.body().getVideos());
                        adapter.notifyDataSetChanged();
                        adapter.setLoaded();
                        if (page != 1) {
                            Intent intent = new Intent("load_more");
                            intent.putExtra("isLoading", false);
                            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                        }
                        Log.i(TAG, "onResponse: " + videos.size());
                    } else {
                        Log.e(TAG, "onResponse: no success");
                        if (page != 1) {
                            Intent intent = new Intent("load_more");
                            intent.putExtra("isLoading", false);
                            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                        }
                    }
                }
                if (page == 1) {
                    loadingView.setVisibility(View.GONE);
                    if (videos.size() == 0) {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText(R.string.no_video_yet);
                    } else {
                        tvError.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                final CharSequence CONNECTEXCEPTION = "java.net.ConnectException";
                if (page == 1) {
                    loadingView.setVisibility(View.GONE);
                    if (t.toString().contains(CONNECTEXCEPTION)) {
                        tvError.setText(R.string.please_connect_to_internet);
                        tvError.setVisibility(View.VISIBLE);
                    }
                } else {
                    Intent intent = new Intent("load_more");
                    intent.putExtra("isLoading", false);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                }
                Log.e(TAG, "onFailure: " + t.getMessage() + " " + t.toString());
            }
        });
    }

    private void countVideoView(String vId) {

        Service service = new Service();

        com.tingsic.POJO.service.Auth auth = new com.tingsic.POJO.service.Auth();

        int id = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("id", -111);
        String token = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("token", "null");

        String contestId = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("contest_id", "1");

        if (id == -111 || token.equals("null")) {
            Log.i(TAG, "addVideoAPI: returned");
            return;
        }

        auth.setId(id);
        auth.setToken(token);

        VideoCountData data = new VideoCountData();
        data.setUserId("" + id);
        data.setContId(contestId);
        data.setVideoId(vId);

        com.tingsic.POJO.service.Request request = new com.tingsic.POJO.service.Request();
        request.setData(data);

        service.setAuth(auth);
        service.setRequest(request);
        service.setService("videoview");

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ServiceResponse> call = apiInterface.countView(service);
        call.enqueue(new Callback<ServiceResponse>() {
            @Override
            public void onResponse(Call<ServiceResponse> call, Response<ServiceResponse> response) {
            }

            @Override
            public void onFailure(Call<ServiceResponse> call, Throwable t) {
            }
        });

    }

    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
//        Log.i("this", "is being called detached " + JZMediaManager.getCurrentUrl());
//        Jzvd jzvd = view.findViewById(R.id.player);
//        if (jzvd != null && jzvd.jzDataSource.containsTheUrl(JZMediaManager.getCurrentUrl())) {
//            Jzvd currentJzvd = JzvdMgr.getCurrentJzvd();
//            if (currentJzvd != null && currentJzvd.currentScreen != Jzvd.SCREEN_WINDOW_FULLSCREEN) {
//                Jzvd.releaseAllVideos();
//            }
//        }
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) adapter.onDestroy();
        if (privious_player != null) {
            privious_player.release();
        }
    }

    @Override
    public void onLoadMore() {
        Log.e(TAG, "onLoadMore: TRUE");
        Intent intent = new Intent("load_more");
        intent.putExtra("isLoading", true);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        page++;
        getVideoAPI(page);
    }

    private boolean isUserLoggedIn() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isLoggedIn", false);
    }

    private void showLogInFragment() {
        LogInBSFragment fragment = new LogInBSFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isProfileActivity", false);
        fragment.setArguments(bundle);
        fragment.show((getActivity()).getSupportFragmentManager(), fragment.getTag());
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            indicatorView.setVisibility(View.VISIBLE);
        } else if (playbackState == Player.STATE_READY) {
            indicatorView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }
    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player
    @Override
    public void onResume() {
        super.onResume();
        if ((privious_player != null && is_visible_to_user) && !is_fragment_exits()) {
            privious_player.setPlayWhenReady(true);
        }
    }

    public boolean is_fragment_exits() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm.getBackStackEntryCount() == 0) {
            return false;
        } else {
            return true;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (privious_player != null) {
            privious_player.setPlayWhenReady(false);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (privious_player != null) {
            privious_player.setPlayWhenReady(false);
        }
    }
}