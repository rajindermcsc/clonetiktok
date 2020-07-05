package com.tingsic.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.tingsic.API.ApiClient;
import com.tingsic.API.ApiInterface;
import com.tingsic.POJO.service.Auth;
import com.tingsic.POJO.service.Request;
import com.tingsic.POJO.service.Service;
import com.tingsic.POJO.service.data.UserPointData;
import com.tingsic.POJO.userpoints.UserPointsResponse;
import com.tingsic.R;
import com.tingsic.Utils.PrefManager;
import com.tingsic.View.LoadingView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserPointsActivity extends AppCompatActivity {

    private LoadingView loadingView;

    private TextView tvUpload,tvWatch,tvShare,tvTotalPoints,tvReferral;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_points);

        loadingView = findViewById(R.id.loader_user_points);

        tvTotalPoints = findViewById(R.id.tv_total_points);
        tvWatch = findViewById(R.id.tv_watch);
        tvUpload = findViewById(R.id.tv_upload);
        tvShare = findViewById(R.id.tv_share);
        tvReferral = findViewById(R.id.tv_referral);

        tvTotalPoints.setText("0");
        tvWatch.setText("0");
        tvUpload.setText("0");
        tvShare.setText("0");
        tvReferral.setText("0");

        interstitialAd=new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.admoob_interestial));
        AdRequest adRequest1=new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest1);


        //admob banner
        LinearLayout adContainer = findViewById(R.id.ad_lout);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(getString(R.string.admob_banner));

// Initiate a generic request to load it with an ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

// Place the ad view.
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        adContainer.addView(adView, params);



        getPointsAPI();


        TextView tvUserReferralCode = findViewById(R.id.tv_referral_code);
        tvUserReferralCode.setText(PrefManager.getReferralCode(this));
        tvUserReferralCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.app_name), PrefManager.getReferralCode(UserPointsActivity.this));
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(UserPointsActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (interstitialAd.isLoaded()){
            interstitialAd.show();
        }
    }

    private void getPointsAPI() {

        loadingView.setVisibility(View.VISIBLE);

        Service service = new Service();

        final int id = PreferenceManager.getDefaultSharedPreferences(this).getInt("id",-111);
        String token = PreferenceManager.getDefaultSharedPreferences(this).getString("token","null");

        if (id == -111 || token.equals("null")) {
            return;
        }

        Auth auth = new Auth();
        auth.setId(id);
        auth.setToken(token);

        Request request = new Request();

        UserPointData data = new UserPointData();
        data.setUserId(""+id);

        request.setData(data);

        service.setAuth(auth);
        service.setRequest(request);
        service.setService("getPoint");

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<UserPointsResponse> call = apiInterface.getUserPoints(service);
        call.enqueue(new Callback<UserPointsResponse>() {
            @Override
            public void onResponse(Call<UserPointsResponse> call, Response<UserPointsResponse> response) {
                loadingView.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    if (response.body().getSuccess() == 1) {

                        if (response.body().getPoint() != null) {
                            updateChart(response.body());
                        }

                    } else {
                        Toast.makeText(UserPointsActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserPointsResponse> call, Throwable t) {
                loadingView.setVisibility(View.GONE);
                Toast.makeText(UserPointsActivity.this, "failed! Try again", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void updateChart(UserPointsResponse response) {

        tvTotalPoints.setText(""+response.getPoint());

        tvWatch.setText(""+response.getViewPoint());
        tvUpload.setText(""+response.getUplodePoint());
        tvShare.setText(""+response.getSharePoint());
        tvReferral.setText(""+response.getRefPoint());


    }
}
