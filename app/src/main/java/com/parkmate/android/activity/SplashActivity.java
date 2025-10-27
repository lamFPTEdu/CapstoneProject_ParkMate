package com.parkmate.android.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.utils.TokenManager; // thêm import

public class SplashActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo TokenManager (idempotent – chỉ thực hiện lần đầu)
        TokenManager.init(getApplicationContext());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Setup edge-to-edge display
        com.parkmate.android.utils.EdgeToEdgeHelper.setupEdgeToEdge(this);

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        // Find the button by its ID
        MaterialButton splashButton = findViewById(R.id.splashButton);
        // Set click listener for the button
        splashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to navigate to RegisterActivity
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                // Optional: finish the splash activity so user can't go back to it
                finish();
            }
        });
    }
}