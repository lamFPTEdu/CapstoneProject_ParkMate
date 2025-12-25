package com.parkmate.android.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.parkmate.android.R;
import com.parkmate.android.utils.TokenManager;

/**
 * SplashActivity - Màn hình khởi động app
 *
 * Trong thời gian hiển thị splash (3 giây), hệ thống thực hiện:
 * 1. Khởi tạo TokenManager và các service cần thiết
 * 2. Request notification permission (Android 13+)
 * 3. Kiểm tra token đăng nhập còn hạn không
 * 4. Auto-login nếu có token hợp lệ → HomeActivity
 * 5. Chuyển đến LoginActivity nếu chưa đăng nhập
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long SPLASH_DELAY_MS = 1000; // 1 giây

    private Handler handler;
    private Runnable navigationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo TokenManager (idempotent – chỉ thực hiện lần đầu)
        TokenManager.init(getApplicationContext());

        setContentView(R.layout.activity_splash);

        // Setup edge-to-edge display (use only our helper, not AndroidX EdgeToEdge)
        com.parkmate.android.utils.EdgeToEdgeHelper.setupEdgeToEdge(this);

        // Request notification permission for Android 13+
        requestNotificationPermission();

        // Initialize handler
        handler = new Handler(Looper.getMainLooper());

        // Start initialization and auto-navigation after delay
        startSplashTimer();
    }

    /**
     * Request notification permission for Android 13+
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS },
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Bắt đầu timer splash và thực hiện các tác vụ khởi tạo
     */
    private void startSplashTimer() {
        Log.d(TAG, "Splash screen started, initializing app...");

        // Thực hiện các tác vụ initialization trong background
        performInitialization();

        // Sau SPLASH_DELAY_MS giây, chuyển màn hình
        navigationRunnable = new Runnable() {
            @Override
            public void run() {
                navigateToNextScreen();
            }
        };
        handler.postDelayed(navigationRunnable, SPLASH_DELAY_MS);
    }

    /**
     * Thực hiện các tác vụ khởi tạo trong thời gian splash
     * - Có thể thêm: Preload data, Check app version, Load config, etc.
     */
    private void performInitialization() {
        // TODO: Có thể thêm các tác vụ khởi tạo khác ở đây
        // Ví dụ:
        // - Khởi tạo Firebase Analytics
        // - Preload user settings
        // - Check for app updates
        // - Load remote config

        Log.d(TAG, "App initialization completed");
    }

    /**
     * Kiểm tra trạng thái đăng nhập và điều hướng đến màn hình phù hợp
     *
     * GUEST MODE ENABLED:
     * - Luôn chuyển đến HomeActivity (cho cả logged in và guest)
     * - Guest có thể browse app mà không cần đăng nhập
     * - Các chức năng quan trọng sẽ yêu cầu đăng nhập khi sử dụng
     */
    private void navigateToNextScreen() {
        // Kiểm tra xem user đã đăng nhập chưa
        String token = TokenManager.getInstance().getToken();
        boolean isLoggedIn = token != null && !token.isEmpty();

        if (isLoggedIn) {
            Log.d(TAG, "✓ Valid token found - User is logged in");
        } else {
            Log.d(TAG, "⚠ No token found - User will browse as GUEST");
        }

        // Luôn chuyển đến HomeActivity (GUEST MODE ENABLED)
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Không cho phép back về splash screen
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup handler để tránh memory leak
        if (handler != null && navigationRunnable != null) {
            handler.removeCallbacks(navigationRunnable);
        }
    }
}