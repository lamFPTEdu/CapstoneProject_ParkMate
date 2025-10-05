package com.parkmate.android.repository;

import android.util.Log;

import com.parkmate.android.model.request.RegisterRequest;
import com.parkmate.android.model.request.EmptyJsonBody;
import com.parkmate.android.model.response.OtpVerifyResponse;
import com.parkmate.android.model.response.RegisterResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;
import com.parkmate.android.model.request.LoginRequest;
import com.parkmate.android.model.response.LoginResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.HttpException;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final ApiService apiService;

    public AuthRepository() { this.apiService = ApiClient.getApiService(); }

    public Single<OtpVerifyResponse> verifyOtp(String email, String otp) {
        String code = otp == null ? null : otp.trim();
        Log.d(TAG, "Verify OTP (single PUT) code=" + code + ", emailHint=" + email);
        return apiService.verifyOtp(code, EmptyJsonBody.INSTANCE)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Verify OTP HTTP " + he.code() + " msg=" + he.message());
                    } else {
                        Log.e(TAG, "Verify OTP error: " + err.getMessage(), err);
                    }
                });
    }

    public Single<RegisterResponse> register(RegisterRequest request) {
        return apiService.register(request);
    }

    // Login
    public Single<LoginResponse> login(LoginRequest request) {
        return apiService.login(request);
    }
}
