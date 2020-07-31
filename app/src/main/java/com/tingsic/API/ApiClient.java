package com.tingsic.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
//    private static final String BASE_URL = "http://tiktukreward.xitijapp.com/";
//    private static final String BASE_URL = "http://tingsic.com/";
    private static final String BASE_URL = "http://tingsic.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        /*HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);*/
        final OkHttpClient client = new OkHttpClient().newBuilder()/*.addInterceptor(interceptor)*/.connectTimeout(2, TimeUnit.MINUTES).writeTimeout(2,TimeUnit.MINUTES).readTimeout(2,TimeUnit.MINUTES).build();
        if (retrofit==null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}