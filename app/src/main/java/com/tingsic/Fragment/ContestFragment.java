package com.tingsic.Fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.tingsic.API.ApiClient;
import com.tingsic.API.ApiInterface;
import com.tingsic.Adapter.ContestAdapter;
import com.tingsic.Listner.OnContestListener;
import com.tingsic.Listner.OnLoadMoreListener;
import com.tingsic.POJO.Contest.Request.ContestRequest;
import com.tingsic.POJO.Contest.Request.Data;
import com.tingsic.POJO.Contest.Request.Request;
import com.tingsic.POJO.Contest.Response.Contest;
import com.tingsic.POJO.Contest.Response.ContestResponse;
import com.tingsic.R;
import com.tingsic.View.LoadingView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContestFragment extends Fragment implements OnLoadMoreListener {
    private static final String TAG = ContestFragment.class.getSimpleName();
    private List<Contest> contestList;
    private ContestAdapter adapter;
    private OnContestListener onContestListener;

    private LoadingView loadingView,loadMore;

    private int page;

    public void setOnContestListener(OnContestListener onContestListener) {
        this.onContestListener = onContestListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contest,container,false);

        Toolbar toolbar = view.findViewById(R.id.contest_toolbar);

        if (getActivity() != null) {
            ((AppCompatActivity)(getActivity())).setSupportActionBar(toolbar);
        }

        //admob banner
        LinearLayout adContainer = view.findViewById(R.id.ad_lout);
        AdView adView = new AdView(getContext());
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(getContext().getString(R.string.admob_banner));

// Initiate a generic request to load it with an ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

// Place the ad view.
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        adContainer.addView(adView, params);


        loadingView = view.findViewById(R.id.loader_contest);
        loadingView.setVisibility(View.GONE);

        loadMore = view.findViewById(R.id.progress_load_more);
        loadMore.setVisibility(View.GONE);

        RecyclerView recyclerView = view.findViewById(R.id.rv_contest);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        contestList = new ArrayList<>();

        adapter = new ContestAdapter(contestList, recyclerView);
        adapter.setOnLoadMoreListener(this);
        adapter.setOnContestListener(onContestListener);

        recyclerView.setAdapter(adapter);

        page = 1;
        loadingView.setVisibility(View.VISIBLE);
        getContestAPI(page);

        return view;
    }

    private void getContestAPI(final int page) {
        //todo pagination is missing boss! Done \/
        ContestRequest contestRequest = new ContestRequest();

        Data data = new Data();
        data.setPage(page);

        Request request = new Request();
        request.setData(data);

        contestRequest.setRequest(request);
        contestRequest.setService("getContest");

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ContestResponse> responseCall = apiInterface.getContestList(contestRequest);
        responseCall.enqueue(new Callback<ContestResponse>() {
            @Override
            public void onResponse(Call<ContestResponse> call, Response<ContestResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getSuccess() == 1) {

                        if (page == 1) {
                            contestList.addAll(response.body().getData());
                            Log.i(TAG, "onResponse: contestList size"+contestList.size());
                            adapter.notifyDataSetChanged();
                        }
                        else {

                            loadMore.setVisibility(View.GONE);
                            contestList.addAll(response.body().getData());
                            adapter.setLoaded();
                            adapter.notifyDataSetChanged();
                        }

                    } else {

                        if (page != 1) {

                            loadMore.setVisibility(View.GONE);

                        }
                    }
                }
                if (page == 1) {
                    loadingView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ContestResponse> call, Throwable t) {
                if (page == 1) {
                    loadingView.setVisibility(View.GONE);
                }
                else {
                    loadMore.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public void onLoadMore() {

        //Log.e(TAG, "onLoadMore: TRUE");
        page++;
        loadMore.setVisibility(View.VISIBLE);
        getContestAPI(page);

    }
}
