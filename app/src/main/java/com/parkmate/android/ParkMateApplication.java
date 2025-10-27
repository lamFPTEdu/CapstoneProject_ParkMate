package com.parkmate.android;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.parkmate.android.utils.TokenManager;
import com.parkmate.android.utils.UserManager;

public class ParkMateApplication extends Application {

    private static ParkMateApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Khởi tạo TokenManager và UserManager bất đồng bộ để tránh ANR
        // Sử dụng Handler để post lên main thread nhưng không block
        new Handler(Looper.getMainLooper()).post(() -> {
            TokenManager.init(this);
            UserManager.init(this);
        });
    }

    public static ParkMateApplication getInstance() {
        return instance;
    }
}

