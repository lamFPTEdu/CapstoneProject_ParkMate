package com.parkmate.android.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Retrofit client cho GraphHopper API
 * FREE: 500 requests/day, không cần credit card
 */
public class GraphHopperApiClient {
    private static final String BASE_URL = "https://graphhopper.com/api/1/";
    private static GraphHopperApiService apiService;

    public static GraphHopperApiService getApiService() {
        if (apiService == null) {
            // Logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // OkHttp client - GraphHopper server nhanh, timeout ngắn hơn
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            // Retrofit
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();

            apiService = retrofit.create(GraphHopperApiService.class);
        }

        return apiService;
    }
}

