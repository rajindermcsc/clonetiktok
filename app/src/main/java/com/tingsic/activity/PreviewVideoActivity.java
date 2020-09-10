package com.tingsic.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlFilterGroup;
import com.daasuu.gpuv.player.GPUPlayerView;
import com.daasuu.gpuv.player.PlayerScaleType;
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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.tingsic.Adapter.FilterAdapter;
import com.tingsic.Filter.FilterType;
import com.tingsic.Helper.Functions;
import com.tingsic.R;
import com.tingsic.Utils.Variables;
import com.tingsic.View.MovieWrapperView;
import com.tingsic.async.MergeVideoAudio;

import java.util.List;

public class PreviewVideoActivity extends AppCompatActivity implements Player.EventListener {
    String video_url;

    GPUPlayerView gpuPlayerView;

    public static int select_postion = 0;

    final List<FilterType> filterTypes = FilterType.createFilterList();
    FilterAdapter adapter;
    RecyclerView recylerview;
    String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video);


        select_postion = 0;
        Intent intent=getIntent();
        type=intent.getStringExtra("type");
        if (type.equalsIgnoreCase("selected")){
            video_url =intent.getStringExtra("video_path");
            //Log.e("TAG", "onCreateurl: "+video_url);
        }
        else {

            video_url = Variables.outputfile2;
            //Log.e("TAG", "onCreateur: "+video_url);
        }



        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

            }
        });


        findViewById(R.id.next_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equalsIgnoreCase("selected")){
                    Save_Video(video_url, Variables.output_filter_file);
                }
                else {

                    Save_Video(Variables.outputfile2, Variables.output_filter_file);
                }
            }
        });


        Set_Player(video_url);


        recylerview = findViewById(R.id.recylerview);

        adapter = new FilterAdapter(this, filterTypes, new FilterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, FilterType item) {
                select_postion = postion;
                gpuPlayerView.setGlFilter(FilterType.createGlFilter(filterTypes.get(postion), getApplicationContext()));
                adapter.notifyDataSetChanged();
            }
        });
        recylerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recylerview.setAdapter(adapter);

    }


    // this function will set the player to the current video
    SimpleExoPlayer player;

    public void Set_Player(String path) {


        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Tingsic"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(path));


        player.prepare(videoSource);

        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(this);
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        player.setPlayWhenReady(true);


        gpuPlayerView = new GPUPlayerView(this);
        gpuPlayerView.setPlayerScaleType(PlayerScaleType.RESIZE_NONE);

        gpuPlayerView.setSimpleExoPlayer(player);
        gpuPlayerView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).addView(gpuPlayerView);

        gpuPlayerView.onResume();

    }


    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player
    @Override
    protected void onStop() {
        super.onStop();

        if (player != null) {
            player.setPlayWhenReady(false);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (player != null) {
            player.setPlayWhenReady(true);
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (player != null) {
            player.setPlayWhenReady(true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.removeListener(PreviewVideoActivity.this);
            player.release();
            player = null;
        }
    }


    // this function will add the filter to video and save that same video for post the video in post video screen
    ProgressDialog progressDialog;

    public void Save_Video(String srcMp4Path, final String destMp4Path) {
        //Log.e("TAG", "Save_Video: "+srcMp4Path);
        //Log.e("TAG", "Save_Video: "+destMp4Path);

        Functions.Show_determinent_loader(this,false,false);

        new GPUMp4Composer(srcMp4Path, destMp4Path)
                .filter(new GlFilterGroup(FilterType.createGlFilter(filterTypes.get(select_postion), getApplicationContext())))
                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {
                        Functions.Show_loading_progress((int)(progress*100));
                    }

                    @Override
                    public void onCompleted() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Functions.cancel_determinent_loader();
                                Intent intent = new Intent(PreviewVideoActivity.this, PostVideoActivity.class);
                                intent.putExtra("srcMp4Path",  Variables.output_filter_file);
                                intent.putExtra("destMp4Path", Variables.output_filter_file2);
                                startActivityForResult(intent, 270);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                finish();

                            }
                        });


                    }

                    @Override
                    public void onCanceled() {

                        Functions.cancel_determinent_loader();
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {
                        Functions.cancel_determinent_loader();
                        Toast.makeText(PreviewVideoActivity.this, "Try Again", Toast.LENGTH_SHORT).show();

                    }
                })
                .start();
    }


//    public void GotopostScreen() {
//
//        /*Intent intent =new Intent(PreviewVideoActivity.this,PostVideoActivity.class);
//        //intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
//        //startActivity(intent);
//        startActivityForResult(intent,270);
//        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);*/
//        Intent intent = new Intent();
//        intent.putExtra("video_path", Variables.output_filter_file);
//        setResult(RESULT_OK, intent);
//        finish();
//
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Log.e("TAGPreview", "onActivityResult: "+requestCode);
        if (requestCode == 270) {
            if (resultCode == RESULT_OK) {
                //Log.e("TAGPreview", "onActivityResult@: "+requestCode);
                Intent intent = new Intent();
                intent.putExtra("video_path", data.getStringExtra("video_path"));
                setResult(RESULT_OK, intent);
                finish();

            }
        }

    }

    // Bottom all the function and the Call back listener of the Expo player
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


    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

    }


}
