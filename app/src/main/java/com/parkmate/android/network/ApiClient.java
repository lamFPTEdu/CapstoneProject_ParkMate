package com.parkmate.android.network;

import static com.parkmate.android.network.ApiConstants.CONNECT_TIMEOUT;
import static com.parkmate.android.network.ApiConstants.READ_TIMEOUT;
import static com.parkmate.android.network.ApiConstants.WRITE_TIMEOUT;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Khởi tạo Retrofit + OkHttpClient dùng Singleton.
 */
public final class ApiClient {

    private static volatile Retrofit retrofitInstance;

    private ApiClient() {}

    public static Retrofit getRetrofit() {
        if (retrofitInstance == null) {
            synchronized (ApiClient.class) {
                if (retrofitInstance == null) {
                    retrofitInstance = new Retrofit.Builder()
                            .baseUrl(ApiConstants.getBaseUrl())
                            .client(buildOkHttpClient())
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                            .build();
                }
            }
        }
        return retrofitInstance;
    }

    public static ApiService getApiService() {
        return getRetrofit().create(ApiService.class);
    }

    private static OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                // Thêm header Accept chung
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("Accept", "application/json")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    }
                })
                // Thêm interceptor gắn Authorization Bearer nếu có token
                .addInterceptor(new AuthInterceptor())
                // Thêm Authenticator xử lý khi token hết hạn (401)
                .authenticator(new TokenAuthenticator());

        // Logging (có thể cân nhắc chỉ bật ở debug bằng BuildConfig.DEBUG)
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(logging);

        return builder.build();
    }
}
