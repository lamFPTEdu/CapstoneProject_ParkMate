package com.parkmate.android.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.model.UserSubscription;
import com.parkmate.android.utils.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SubscriptionSuccessActivity extends AppCompatActivity {

    private static final String TAG = "SubscriptionSuccess";
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    private MaterialToolbar toolbar;
    private ImageView ivQrCode;
    private ImageButton btnSaveQr;
    private TextView tvSubscriptionId;
    private TextView tvStatus;
    private TextView tvPackageName;
    private TextView tvParkingLotName;
    private TextView tvSpotName;
    private TextView tvPeriod;
    private TextView tvPrice;
    private TextView tvDaysRemaining;
    private MaterialButton btnDone;
    private ProgressBar progressBar;

    private UserSubscription subscription;
    private Bitmap qrCodeBitmap;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_success);

        subscription = (UserSubscription) getIntent().getSerializableExtra("SUBSCRIPTION");
        if (subscription == null) {
            Toast.makeText(this, "Không có dữ liệu đăng ký", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupBackPressHandler();
        displaySubscriptionData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        ivQrCode = findViewById(R.id.ivQrCode);
        btnSaveQr = findViewById(R.id.btnSaveQr);
        tvSubscriptionId = findViewById(R.id.tvSubscriptionId);
        tvStatus = findViewById(R.id.tvStatus);
        tvPackageName = findViewById(R.id.tvPackageName);
        tvParkingLotName = findViewById(R.id.tvParkingLotName);
        tvSpotName = findViewById(R.id.tvSpotName);
        tvPeriod = findViewById(R.id.tvPeriod);
        tvPrice = findViewById(R.id.tvPrice);
        tvDaysRemaining = findViewById(R.id.tvDaysRemaining);
        btnDone = findViewById(R.id.btnDone);
        progressBar = findViewById(R.id.progressBar);

        // Setup save button click listener
        btnSaveQr.setOnClickListener(v -> saveQrCodeToGallery());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết vé tháng");
        }
        toolbar.setNavigationOnClickListener(v -> navigateToSubscriptionList());
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToSubscriptionList();
            }
        });
    }

    private void displaySubscriptionData() {
        // Subscription ID
        tvSubscriptionId.setText(String.format("#%d", subscription.getId()));

        // Status - with Vietnamese label and appropriate background
        String status = subscription.getStatus();
        tvStatus.setText(getStatusDisplay(status));
        setStatusBackground(status);

        // Package Name
        tvPackageName.setText(subscription.getSubscriptionPackageName());

        // Parking Lot
        tvParkingLotName.setText(subscription.getParkingLotName());

        // Spot Name
        tvSpotName.setText(subscription.getAssignedSpotName());

        // Period
        String period = formatPeriod(subscription.getStartDate(), subscription.getEndDate());
        tvPeriod.setText(period);

        // Price
        tvPrice.setText(String.format("%sđ", formatter.format(subscription.getPaidAmount())));

        // Days Remaining - show if available
        if (subscription.getDaysRemaining() > 0) {
            tvDaysRemaining.setVisibility(View.VISIBLE);
            tvDaysRemaining.setText(String.format("%d ngày", subscription.getDaysRemaining()));
        } else {
            tvDaysRemaining.setVisibility(View.GONE);
        }

        // QR Code
        displayQrCode(subscription.getQrCode());

        // Done button - Navigate to Subscription List
        btnDone.setOnClickListener(v -> navigateToSubscriptionList());
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING_PAYMENT":
                return "Chờ thanh toán";
            case "ACTIVE":
                return "Đang trong bãi";
            case "INACTIVE":
                return "Ngoài bãi";
            case "EXPIRED":
                return "Hết hạn";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private void setStatusBackground(String status) {
        switch (status) {
            case "PENDING_PAYMENT":
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "ACTIVE":
                tvStatus.setBackgroundResource(R.drawable.bg_status_active);
                break;
            case "INACTIVE":
                tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
                break;
            case "EXPIRED":
                tvStatus.setBackgroundResource(R.drawable.bg_status_expired);
                break;
            case "CANCELLED":
                tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                break;
            default:
                tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
                break;
        }
    }

    private String formatPeriod(String startDate, String endDate) {
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date start = apiFormat.parse(startDate);
            Date end = apiFormat.parse(endDate);

            if (start != null && end != null) {
                return String.format("%s - %s",
                        displayFormat.format(start),
                        displayFormat.format(end));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dates: " + e.getMessage());
        }

        return String.format("%s - %s", startDate, endDate);
    }

    private void displayQrCode(String qrCodeBase64) {
        if (qrCodeBase64 == null || qrCodeBase64.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Không có mã QR", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                qrCodeBitmap = ImageUtils.decodeBase64ToBitmap(qrCodeBase64);
                runOnUiThread(() -> {
                    if (qrCodeBitmap != null) {
                        ivQrCode.setImageBitmap(qrCodeBitmap);
                        ivQrCode.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Không thể tải mã QR", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error decoding QR code: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải mã QR", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveQrCodeToGallery() {
        if (qrCodeBitmap == null) {
            Toast.makeText(this, "Không có mã QR để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check permission for Android 10 and below
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
                return;
            }
        }

        saveImageToGallery();
    }

    private void saveImageToGallery() {
        try {
            String fileName = "ParkMate_Subscription_QR_" + subscription.getId() + "_" + System.currentTimeMillis() + ".png";
            OutputStream outputStream;
            Uri imageUri;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 and above
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ParkMate");

                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (imageUri != null) {
                    outputStream = getContentResolver().openOutputStream(imageUri);
                } else {
                    Toast.makeText(this, "Không thể tạo file", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Android 9 and below
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File parkMateDir = new File(picturesDir, "ParkMate");
                if (!parkMateDir.exists()) {
                    parkMateDir.mkdirs();
                }

                File imageFile = new File(parkMateDir, fileName);
                outputStream = new FileOutputStream(imageFile);
                imageUri = Uri.fromFile(imageFile);

                // Notify gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(imageUri);
                sendBroadcast(mediaScanIntent);
            }

            // Save bitmap to output stream
            if (outputStream != null) {
                qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
                Toast.makeText(this, "Đã lưu mã QR vào thư viện", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving QR code", e);
            Toast.makeText(this, "Lỗi lưu mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageToGallery();
            } else {
                Toast.makeText(this, "Cần quyền truy cập bộ nhớ để lưu ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToSubscriptionList() {
        // Navigate to Profile first, then to List to create proper back stack
        // This ensures: List -> Back -> Profile
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent listIntent = new Intent(this, UserSubscriptionListActivity.class);

        // Start Profile, then immediately start List
        startActivities(new Intent[]{profileIntent, listIntent});
        finish();
    }
}

