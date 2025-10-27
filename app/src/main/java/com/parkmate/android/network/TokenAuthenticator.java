package com.parkmate.android.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.parkmate.android.ParkMateApplication;
import com.parkmate.android.activity.LoginActivity;
import com.parkmate.android.utils.TokenManager;
import com.parkmate.android.utils.UserManager;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Authenticator xử lý khi server trả về 401 Unauthorized (token hết hạn hoặc invalid)
 * Sẽ tự động logout và chuyển về màn hình đăng nhập
 */
public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";
    private static boolean isRedirectingToLogin = false;

    @Nullable
    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        Log.w(TAG, "Received 401 Unauthorized - Token expired or invalid");

        // Tránh redirect nhiều lần
        if (isRedirectingToLogin) {
            return null;
        }

        // Kiểm tra nếu đã thử retry rồi thì không retry nữa
        if (responseCount(response) >= 2) {
            Log.e(TAG, "Already retried authentication, giving up");
            return null;
        }

        // Xử lý logout và chuyển về màn hình đăng nhập
        handleTokenExpired();

        // Không retry request này nữa
        return null;
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

