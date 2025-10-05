package com.parkmate.android.network;

import com.parkmate.android.BuildConfig;

/**
 * Chứa các hằng số và tiện ích liên quan đến API.
 */
public final class ApiConstants {
    private ApiConstants() {}

    // Timeout (giây)
    public static final long CONNECT_TIMEOUT = 30L;
    public static final long READ_TIMEOUT = 30L;
    public static final long WRITE_TIMEOUT = 30L;

    public static String getBaseUrl() {
        return BuildConfig.BASE_URL; // lấy từ buildConfigField
    }
}
