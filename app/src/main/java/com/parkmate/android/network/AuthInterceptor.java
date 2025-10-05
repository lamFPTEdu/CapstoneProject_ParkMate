package com.parkmate.android.network;

import com.parkmate.android.utils.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor tự động gắn header Authorization: Bearer <token> nếu có token.
 */
public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // Bỏ qua attach token cho các endpoint public (permitAll) để tránh 401 nếu token cũ/invalid
        String path = original.url().encodedPath();
        if (isPublicAuthEndpoint(path)) {
            return chain.proceed(original);
        }

        String token = null;
        try {
            token = TokenManager.getInstance().getToken();
        } catch (IllegalStateException ignored) {
            // TokenManager chưa init => không crash, tiếp tục request không token
        }

        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }

        // Đảm bảo không bị double prefix
        if (token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }

        Request newRequest = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(newRequest);
    }

    private boolean isPublicAuthEndpoint(String path) {
        if (path == null) return false;
        // Các endpoint không cần gửi Authorization
        return path.endsWith("/auth/register")
                || path.endsWith("/auth/login")
                || path.endsWith("/auth/verify")
                || path.endsWith("/auth/forgot-password")
                || path.endsWith("/auth/reset-password");
    }
}
