package com.parkmate.android.network;

import com.parkmate.android.BuildConfig;

/**
 * Chứa các hằng số và tiện ích liên quan đến API.
 */
public final class ApiConstants {
    private ApiConstants() {}

    // Timeout (giây) - Tăng lên để xử lý backend chậm
    public static final long CONNECT_TIMEOUT = 60L;
    public static final long READ_TIMEOUT = 60L;
    public static final long WRITE_TIMEOUT = 60L;

    public static String getBaseUrl() {
        return BuildConfig.BASE_URL; // lấy từ buildConfigField
    }
}
