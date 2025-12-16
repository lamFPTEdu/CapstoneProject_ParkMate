package com.parkmate.android.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.parkmate.android.ParkMateApplication;
import com.parkmate.android.activity.LoginActivity;
import com.parkmate.android.utils.TokenManager;
import com.parkmate.android.utils.UserManager;
import com.parkmate.android.model.request.RefreshTokenRequest;
import com.parkmate.android.model.response.RefreshTokenResponse;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Authenticator xử lý khi server trả về 401 Unauthorized (token hết hạn hoặc invalid)
 * Sẽ tự động thử refresh token trước, nếu refresh thất bại mới logout
 */
public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";
    private static boolean isRedirectingToLogin = false;
    private static boolean isRefreshing = false;

    @Nullable
    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        Log.w(TAG, "Received 401 Unauthorized - Token expired or invalid");

        // Tránh redirect nhiều lần
        if (isRedirectingToLogin) {
            return null;
        }

        // Kiểm tra nếu đã thử retry rồi thì không retry nữa
        if (responseCount(response) >= 3) {
            Log.e(TAG, "Already retried authentication multiple times, giving up");
            handleTokenExpired();
            return null;
        }

        // Tránh refresh đồng thời nhiều lần
        synchronized (TokenAuthenticator.class) {
            if (isRefreshing) {
                Log.d(TAG, "Already refreshing token, waiting...");
                try {
                    Thread.sleep(1000); // Đợi refresh hoàn tất
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String newToken = TokenManager.getInstance().getToken();
                if (newToken != null) {
                    return response.request().newBuilder()
                            .header("Authorization", "Bearer " + newToken)
                            .build();
                }
                return null;
            }

            isRefreshing = true;
        }

        try {
            // Thử refresh token
            String refreshToken = TokenManager.getInstance().getRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) {
                Log.w(TAG, "No refresh token available, logout required");
                handleTokenExpired();
                return null;
            }

            Log.d(TAG, "Attempting to refresh access token...");
            RefreshTokenResponse refreshResponse = refreshTokenSync(refreshToken);

            if (refreshResponse != null &&
                refreshResponse.getData() != null &&
                refreshResponse.getData().getAccessToken() != null) {

                String newAccessToken = refreshResponse.getData().getAccessToken();
                String newRefreshToken = refreshResponse.getData().getRefreshToken();

                Log.d(TAG, "✅ Token refreshed successfully!");

                // Lưu token mới
                TokenManager.getInstance().saveToken(newAccessToken);
                if (newRefreshToken != null) {
                    TokenManager.getInstance().saveRefreshToken(newRefreshToken);
                }

                // Retry request với token mới
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newAccessToken)
                        .build();
            } else {
                Log.e(TAG, "Failed to refresh token - invalid response");
                handleTokenExpired();
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while refreshing token", e);
            handleTokenExpired();
            return null;
        } finally {
            synchronized (TokenAuthenticator.class) {
                isRefreshing = false;
            }
        }
    }

    /**
     * Synchronous refresh token call (chỉ dùng trong Authenticator)
     */
    private RefreshTokenResponse refreshTokenSync(String refreshToken) {
        try {
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
            RefreshTokenResponse response =
                ApiClient.getApiService().refreshToken(request).blockingGet();

            if (response != null && response.getSuccess() != null && response.getSuccess()) {
                return response;
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error calling refresh API", e);
            return null;
        }
    }

    /**
     * Đếm số lần response 401 để tránh retry vô hạn
     */
    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    /**
     * Xử lý khi token hết hạn: xóa token, user info và chuyển về màn hình đăng nhập
     */
    private void handleTokenExpired() {
        if (isRedirectingToLogin) {
            return;
        }

        isRedirectingToLogin = true;

        try {
            // Xóa token và thông tin user
            TokenManager.getInstance().clearToken();
            UserManager.getInstance().clearUserInfo();

            Log.d(TAG, "Cleared token and user info due to 401");

            // Lấy context từ Application
            Context context = ParkMateApplication.getInstance();
            if (context != null) {
                // Chuyển về màn hình đăng nhập
                Intent intent = new Intent(context, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                               Intent.FLAG_ACTIVITY_CLEAR_TASK |
                               Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("SESSION_EXPIRED", true);
                context.startActivity(intent);

                Log.d(TAG, "Redirected to LoginActivity due to token expiration");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling token expiration", e);
        } finally {
            // Reset flag sau 2 giây
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                isRedirectingToLogin = false;
            }, 2000);
        }
    }
}

