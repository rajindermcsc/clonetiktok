package com.tingsic.activity;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tingsic.API.ApiClient;
import com.tingsic.API.ApiInterface;
import com.tingsic.Adapter.HashTagAdapter;
import com.tingsic.Fragment.VideoPostFragment;
import com.tingsic.Listner.OnVideoListener;
import com.tingsic.POJO.Video.Video;
import com.tingsic.POJO.hastag.HashTagVideoResponse;
import com.tingsic.R;
import com.tingsic.View.LoadingView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HashTagVideoActivity extends AppCompatActivity implements OnVideoListener {

    private static final String TAG = HashTagVideoActivity.class.getSimpleName();
    private String hashTag;
    public HashTagAdapter adapter = new HashTagAdapter();
    private ImageView ivBack;
    private LoadingView loadingView;
    private RecyclerView rvPosts;
    private LinearLayout lnr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hash_tag_video);
        adapter.setVideoListener(this);
        initView();
        initAdapter();
        initListener();
    }

    private void initListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initView() {
        hashTag = getIntent().getStringExtra("hashtag");
        ivBack = findViewById(R.id.iv_back);
        loadingView = findViewById(R.id.loading);
        rvPosts = findViewById(R.id.rvPosts);
        lnr = findViewById(R.id.lnr);
        TextView tvHashTag = findViewById(R.id.tv_hastag);
        tvHashTag.setText("#".concat(hashTag));
    }

    private void initAdapter() {
        loadingView.setVisibility(View.VISIBLE);
        rvPosts.setVisibility(View.GONE);
        rvPosts.setAdapter(adapter);
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        JsonObject mainJson = new JsonObject();
        JsonObject jsonObject = new JsonObject();
        JsonObject object = new JsonObject();
        object.addProperty("hashtag", hashTag);
        object.addProperty("page", "1");
        jsonObject.add("data", object);
        mainJson.add("request", jsonObject);
        mainJson.addProperty("service", "getHashtagvideo");
        //Log.e(TAG, "initAdapter: " + new Gson().toJson(mainJson));
        Call<HashTagVideoResponse> userResponseCall = apiInterface.getHashTagVideo(mainJson);
        userResponseCall.enqueue(new Callback<HashTagVideoResponse>() {
            @Override
            public void onResponse(Call<HashTagVideoResponse> call, Response<HashTagVideoResponse> response) {
                System.out.println("--------------------------AAAAAAAAAAAAAAAA-------------------");
                System.out.println(response.body().getVideos());
                if (response.body() != null) {
                    HashTagVideoResponse hashTagVideoResponse = response.body();
                    if (hashTagVideoResponse.getVideos() != null) {
                        adapter.updateData(hashTagVideoResponse.getVideos());
                        loadingView.setVisibility(View.GONE);
                        rvPosts.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<HashTagVideoResponse> call, Throwable t) {
                loadingView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onVideoSelected(int position, List<Video> videos) {
        lnr.setVisibility(View.GONE);
        VideoPostFragment fragment = new VideoPostFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putBoolean("isshow",true);
        fragment.setArguments(bundle);
        fragment.setVideos(videos);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        ft.addToBackStack("ProfileFragment");
        ft.replace(R.id.frame, fragment, "VideoPostFragment").commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            lnr.setVisibility(View.VISIBLE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
