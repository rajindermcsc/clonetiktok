package com.tingsic.Fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.tingsic.API.ApiClient;
import com.tingsic.API.ApiInterface;
import com.tingsic.Adapter.PeopleAdapter;
import com.tingsic.Listner.OnLoadMoreListener;
import com.tingsic.Listner.OnPeopleListener;
import com.tingsic.POJO.People;
import com.tingsic.POJO.Search.Request.Data;
import com.tingsic.POJO.Search.Request.Request;
import com.tingsic.POJO.Search.Request.SearchRequest;
import com.tingsic.POJO.Search.Response.SearchResponse;
import com.tingsic.R;
import com.tingsic.View.LoadingView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment implements OnLoadMoreListener {

    private PeopleAdapter adapter;
    private List<People> peoples;
    private EditText searchView;
    private RecyclerView recyclerView;
    private OnPeopleListener onSearchFragmentListener;

    private LoadingView loadingView;

    private int page;
    private String searchText;

    public void setOnSearchFragmentListener(OnPeopleListener onSearchFragmentListener) {
        this.onSearchFragmentListener = onSearchFragmentListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);

        Toolbar toolbar = view.findViewById(R.id.search_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        searchView = toolbar.findViewById(R.id.searchView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            searchView.setShowSoftInputOnFocus(true);
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

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    page = 1;
                    searchText = v.getText().toString();
                    peoples.clear();
                    searchView.clearFocus();
                    hideSoftKeyboard(getActivity());
                    getSearchAPI(searchText,page);

                }
                return false;
            }
        });

        ImageView ivSearch = view.findViewById(R.id.searchView_search);
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = 1;
                searchText = searchView.getText().toString();
                peoples.clear();
                searchView.clearFocus();
                hideSoftKeyboard(getActivity());
                getSearchAPI(searchText,page);
            }
        });
        ImageView ivClose = view.findViewById(R.id.searchView_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setText("");
                searchView.clearFocus();
                peoples.clear();
                adapter.notifyDataSetChanged();
                hideSoftKeyboard(getActivity());
            }
        });

        loadingView = view.findViewById(R.id.loader_search);
        loadingView.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.rv_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        peoples = new ArrayList<>();
        adapter = new PeopleAdapter(getContext(),peoples,recyclerView);
        adapter.setOnLoadMoreListener(this);

        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new PeopleAdapter.onItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position, People people) {
                //startActivity(new Intent(getContext(),UserActivity.class));
                hideSoftKeyboard(getActivity());
                searchView.clearFocus();
                onSearchFragmentListener.onListClicked(people);
            }
        });


        /*
         * Facebook Ads :- show in bottom
         */


        return view;
    }

    private void getSearchAPI(String searchText, final int page) {
        if (page == 1) {
            loadingView.setVisibility(View.VISIBLE);
        }

        SearchRequest searchRequest = new SearchRequest();

        Data data = new Data();
        data.setPage(page);
        data.setSearchText(searchText);

        Request request = new Request();
        request.setData(data);

        searchRequest.setRequest(request);
        searchRequest.setService("search");


        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<SearchResponse> searchRequestCall = apiInterface.getSearchPeople(searchRequest);
        searchRequestCall.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful()){
                    if (response.body().getSuccess() == 1){
                        if (page==1){
                            peoples.addAll(response.body().getPeople());
                            adapter.setLoaded();
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            peoples.remove(peoples.size()-1);
                            adapter.notifyItemRemoved(peoples.size());
                            peoples.addAll(response.body().getPeople());
                            adapter.setLoaded();
                            adapter.notifyDataSetChanged();
                        }
                    }
                    else {
                        if (page != 1) {
                            peoples.remove(peoples.size()-1);
                            adapter.notifyItemRemoved(peoples.size());
                        }
                    }
                }
                if (page==1) {
                    loadingView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                if (page ==1) {
                    loadingView.setVisibility(View.GONE);
                }
            }
        });
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        if (searchView != null) {
            searchView.clearFocus();
        }
        super.onResume();
    }

    @Override
    public void onLoadMore() {
        peoples.add(null);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(peoples.size()-1);
                page++;
                getSearchAPI(searchText,page);
            }
        });
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
