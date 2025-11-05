package com.parkmate.android.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.model.response.ApiResponse;
import com.parkmate.android.model.response.UserProfileResponse;
import com.parkmate.android.network.ApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AccountQrActivity extends AppCompatActivity {

    private static final String TAG = "AccountQrActivity";
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    private ImageView ivQrCode;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private MaterialButton btnSaveQrCode;
    private MaterialButton btnShareQrCode;
    private ProgressBar progressBar;

    private CompositeDisposable compositeDisposable;
    private Bitmap qrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_qr);

        setupToolbar();
        compositeDisposable = new CompositeDisposable();

        initViews();
        setupClickListeners();
        loadUserProfile();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Mã QR tài khoản");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        ivQrCode = findViewById(R.id.ivQrCode);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnSaveQrCode = findViewById(R.id.btnSaveQrCode);
        btnShareQrCode = findViewById(R.id.btnShareQrCode);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        if (btnSaveQrCode != null) {
            btnSaveQrCode.setOnClickListener(v -> saveQrCodeToGallery());
        }

        if (btnShareQrCode != null) {
            btnShareQrCode.setOnClickListener(v -> shareQrCode());
        }
    }

    private void loadUserProfile() {
        showLoading(true);

        compositeDisposable.add(
            ApiClient.getApiService().getCurrentUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::handleUserProfileSuccess,
                    this::handleUserProfileError
                )
        );
    }

    private void handleUserProfileSuccess(ApiResponse<UserProfileResponse> response) {
        showLoading(false);

        if (response != null && response.isSuccess() && response.getData() != null) {
            displayUserProfile(response.getData());
        } else {
            String errorMsg = response != null ? response.getMessage() : "Không thể tải thông tin người dùng";
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleUserProfileError(Throwable throwable) {
        showLoading(false);
        Log.e(TAG, "Error loading user profile", throwable);
        Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void displayUserProfile(UserProfileResponse profile) {
        // Display user info
        if (tvUserName != null && profile.getFullName() != null) {
            tvUserName.setText(profile.getFullName());
        }

        if (tvUserEmail != null && profile.getAccount() != null && profile.getAccount().getEmail() != null) {
            tvUserEmail.setText(profile.getAccount().getEmail());
        }

        // Display QR code
        if (profile.getQrCode() != null && !profile.getQrCode().isEmpty()) {
            displayQrCode(profile.getQrCode());
        } else {
            Toast.makeText(this, "Không có mã QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayQrCode(String qrCodeBase64) {
        try {
            // Remove "data:image/png;base64," prefix if exists
            String base64Image = qrCodeBase64;
            if (qrCodeBase64.contains(",")) {
                base64Image = qrCodeBase64.split(",")[1];
            }

            // Decode base64 to bitmap
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            qrCodeBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (qrCodeBitmap != null && ivQrCode != null) {
                ivQrCode.setImageBitmap(qrCodeBitmap);
            } else {
                Toast.makeText(this, "Không thể hiển thị mã QR", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying QR code", e);
            Toast.makeText(this, "Lỗi hiển thị mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
            String fileName = "ParkMate_QR_" + System.currentTimeMillis() + ".png";
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

    private void shareQrCode() {
        if (qrCodeBitmap == null) {
            Toast.makeText(this, "Không có mã QR để chia sẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Save to cache directory
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "parkmate_qr.png");

            FileOutputStream stream = new FileOutputStream(file);
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // Get URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", file);

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Mã QR tài khoản ParkMate của tôi");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã QR qua"));
        } catch (IOException e) {
            Log.e(TAG, "Error sharing QR code", e);
            Toast.makeText(this, "Lỗi chia sẻ mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (ivQrCode != null) {
            ivQrCode.setVisibility(show ? View.GONE : View.VISIBLE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }
}

