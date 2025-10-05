package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.utils.TokenManager; // thêm import

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo TokenManager (idempotent – chỉ thực hiện lần đầu)
        TokenManager.init(getApplicationContext());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

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