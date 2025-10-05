package com.parkmate.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Quản lý lưu trữ và truy xuất thông tin user đã đăng nhập
 */
public final class UserManager {

    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USERNAME = "username"; // Thêm key cho username

    private static UserManager instance;
    private final SharedPreferences prefs;

    private UserManager(Context appContext) {
        this.prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Gọi từ Application.onCreate() để khởi tạo.
     */
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
    }

    public static UserManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UserManager chưa được init. Hãy gọi UserManager.init(context) trong Application.");
        }
        return instance;
    }

    /**
     * Lưu thông tin user sau khi đăng nhập thành công
     */
    public void saveUserInfo(String userId, String email, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        if (userId != null) editor.putString(KEY_USER_ID, userId);
        if (email != null) editor.putString(KEY_USER_EMAIL, email);
        if (name != null) editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        String name = prefs.getString(KEY_USER_NAME, null);
        return TextUtils.isEmpty(name) ? "Người dùng ParkMate" : name;
    }

    public void setUsername(String username) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    /**
     * Xóa toàn bộ thông tin user (khi logout)
     */
    public void clearUserInfo() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_NAME)
                .remove(KEY_USERNAME) // Xóa username khi logout
                .apply();
    }

    /**
     * Kiểm tra xem có user đã đăng nhập hay chưa
     */
    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(getUserId()) || !TextUtils.isEmpty(getUserEmail());
    }
}
