package com.parkmate.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Quản lý lưu trữ và truy xuất JWT (Bearer Token).
 * Đơn giản sử dụng SharedPreferences, có thể nâng cấp lên EncryptedSharedPreferences hoặc DataStore sau.
 */
public final class TokenManager {

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context appContext) {
        this.prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Gọi từ Application.onCreate() để khởi tạo.
     */
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
    }

    public static TokenManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TokenManager chưa được init. Hãy gọi TokenManager.init(context) trong Application.");
        }
        return instance;
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public String getToken() {
        String token = prefs.getString(KEY_ACCESS_TOKEN, null);
        return TextUtils.isEmpty(token) ? null : token;
    }

    public void clearToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply();
    }

    /**
     * Lưu refresh token
     */
    public void saveRefreshToken(String refreshToken) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    /**
     * Lấy refresh token
     */
    public String getRefreshToken() {
        String token = prefs.getString(KEY_REFRESH_TOKEN, null);
        return TextUtils.isEmpty(token) ? null : token;
    }

    /**
     * Xóa refresh token
     */
    public void clearRefreshToken() {
        prefs.edit().remove(KEY_REFRESH_TOKEN).apply();
    }

    /**
     * Xóa tất cả tokens (access + refresh)
     */
    public void clearAllTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply();
    }
}

