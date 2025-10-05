package com.parkmate.android;

import android.app.Application;

import com.parkmate.android.utils.TokenManager;
import com.parkmate.android.utils.UserManager;

public class ParkMateApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo TokenManager và UserManager
        TokenManager.init(this);
        UserManager.init(this);
    }
}

