package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.model.UserSubscription;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.utils.SubscriptionHoldManager;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SubscriptionSummaryActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvSpotName;
    private TextView tvStartDate;
    private TextView tvPackageInfo;
    private TextView tvPrice;
    private MaterialButton btnConfirm;
    private ProgressBar progressBar;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private SubscriptionHoldManager holdManager;

    private long parkingLotId;
    private long vehicleId;
    private long packageId;
    private String packageName;
    private long packagePrice;
    private String startDate;
    private Long spotId; // Changed to Long (nullable) - for motorbike/bike this will be null
    private String spotName;

    private boolean isSpotHeld = false;
    private boolean requiresSpot = false; // Flag to check if this subscription requires spot selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_summary);

        holdManager = new SubscriptionHoldManager(this);

        // Release any previously held spot before starting new subscription
        releasePreviousHeldSpot();

        parkingLotId = getIntent().getLongExtra("PARKING_LOT_ID", -1);
        vehicleId = getIntent().getLongExtra("VEHICLE_ID", -1);
        packageId = getIntent().getLongExtra("PACKAGE_ID", -1);
        packageName = getIntent().getStringExtra("PACKAGE_NAME");
        packagePrice = getIntent().getLongExtra("PACKAGE_PRICE", 0);
        startDate = getIntent().getStringExtra("START_DATE");

        // Get spotId - có thể null cho xe máy/xe đạp
        long spotIdExtra = getIntent().getLongExtra("SPOT_ID", -1);
        spotId = (spotIdExtra != -1) ? spotIdExtra : null;
        spotName = getIntent().getStringExtra("SPOT_NAME");

        // Check if this subscription requires spot (car) or not (motorbike/bike)
        requiresSpot = (spotId != null);

        // Validate required fields - spotId không bắt buộc
        if (parkingLotId == -1 || vehicleId == -1 || packageId == -1 || startDate == null) {
            Toast.makeText(this, "Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set default values for nullable fields
        if (packageName == null || packageName.isEmpty()) {
            packageName = "Gói đăng ký";
        }
        if (requiresSpot && (spotName == null || spotName.isEmpty())) {
            spotName = "Spot #" + spotId;
        }

        initializeViews();
        setupToolbar();
        setupBackPressHandler();
        displaySummary();

        // Hold spot chỉ khi subscription yêu cầu spot (car)
        if (requiresSpot) {
            holdSpot();
        }
    }

    private void releasePreviousHeldSpot() {
        if (holdManager.hasHeldSpot()) {
            long previousSpotId = holdManager.getHeldSpot();
            compositeDisposable.add(
                    ApiClient.getApiService()
                            .releaseHoldSpot(previousSpotId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    response -> holdManager.clearHeldSpot(),
                                    throwable -> holdManager.clearHeldSpot()
                            )
            );
        }
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                releaseSpotAndFinish();
            }
        });
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvSpotName = findViewById(R.id.tvSpotName);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvPackageInfo = findViewById(R.id.tvPackageInfo);
        tvPrice = findViewById(R.id.tvPrice);
        btnConfirm = findViewById(R.id.btnConfirm);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Xác nhận đăng ký");
        }
        toolbar.setNavigationOnClickListener(v -> {
            releaseSpotAndFinish();
        });
    }

    private void displaySummary() {
        // Display spot name - chỉ hiển thị nếu có spot
        if (requiresSpot && spotId != null) {
            tvSpotName.setVisibility(View.VISIBLE);
            if (spotName != null && !spotName.isEmpty()) {
                tvSpotName.setText(String.format("Chỗ: %s", spotName));
            } else {
                tvSpotName.setText(String.format("Chỗ: Spot #%d", spotId));
            }
        } else {
            // Ẩn thông tin spot cho xe máy/xe đạp
            tvSpotName.setVisibility(View.GONE);
        }

        // Display start date
        if (startDate != null && !startDate.isEmpty()) {
            tvStartDate.setText(String.format("Ngày bắt đầu: %s", startDate));
        } else {
            tvStartDate.setText("Ngày bắt đầu: N/A");
        }

        // Display package info
        if (packageName != null && !packageName.isEmpty()) {
            tvPackageInfo.setText(String.format("Gói: %s", packageName));
        } else {
            tvPackageInfo.setText("Gói đăng ký");
        }

        // Display price
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
        tvPrice.setText(String.format("%sđ", formatter.format(packagePrice)));

        btnConfirm.setOnClickListener(v -> createSubscription());
    }

    private void holdSpot() {
        showLoading(true);

        compositeDisposable.add(
                ApiClient.getApiService()
                        .holdSpot(spotId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    showLoading(false);
                                    if (response.isSuccess() && response.getData() != null && response.getData()) {
                                        isSpotHeld = true;
                                        holdManager.setHeldSpot(spotId);
                                        Toast.makeText(this, "Đã giữ chỗ thành công", Toast.LENGTH_SHORT).show();
                                    } else {
                                        showError("Không thể giữ chỗ: " + (response.getError() != null ? response.getError() : "Unknown error"));
                                        finish();
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    showError("Lỗi: " + throwable.getMessage());
                                    finish();
                                }
                        )
        );
    }

    private void createSubscription() {
        showLoading(true);

        // Format startDate - chỉ thêm time nếu chưa có
        String formattedStartDate = startDate;
        if (!startDate.contains("T")) {
            formattedStartDate = startDate + "T00:00:00";
        }

        Map<String, Object> request = new HashMap<>();
        request.put("ownedByMe", true);
        request.put("vehicleId", vehicleId);
        request.put("subscriptionPackageId", packageId);
        request.put("parkingLotId", parkingLotId);

        // Chỉ gửi assignedSpotId nếu có (car), không gửi cho motorbike/bike
        if (requiresSpot && spotId != null) {
            request.put("assignedSpotId", spotId);
        }

        request.put("startDate", formattedStartDate);

        compositeDisposable.add(
                ApiClient.getApiService()
                        .createUserSubscription(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    showLoading(false);
                                    if (response.isSuccess() && response.getData() != null) {
                                        UserSubscription subscription = response.getData();
                                        navigateToSuccess(subscription);
                                    } else {
                                        showError(response.getError() != null ? response.getError() : "Không thể tạo đăng ký");
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    showError("Lỗi: " + throwable.getMessage());
                                }
                        )
        );
    }

    private void releaseSpotAndFinish() {
        // Chỉ release spot nếu có spot và đã được held
        if (requiresSpot && isSpotHeld && spotId != null) {
            compositeDisposable.add(
                    ApiClient.getApiService()
                            .releaseHoldSpot(spotId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    response -> {
                                        holdManager.clearHeldSpot();
                                        finish();
                                    },
                                    throwable -> {
                                        holdManager.clearHeldSpot();
                                        finish();
                                    }
                            )
            );
        } else {
            finish();
        }
    }

    private void navigateToSuccess(UserSubscription subscription) {
        // Clear held spot since subscription is created successfully
        holdManager.clearHeldSpot();

        Intent intent = new Intent(this, SubscriptionSuccessActivity.class);
        intent.putExtra("SUBSCRIPTION", subscription);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnConfirm.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();

        // Release held spot if activity is destroyed without completing subscription
        // This handles cases like app crash, task removal, etc.
        // Chỉ release nếu có spot (car)
        if (requiresSpot && isSpotHeld && spotId != null && !isFinishing()) {
            // Activity is being destroyed but not by normal finish()
            // Release the spot asynchronously without blocking
            new Thread(() -> {
                try {
                    ApiClient.getApiService()
                            .releaseHoldSpot(spotId)
                            .blockingSubscribe(
                                    response -> holdManager.clearHeldSpot(),
                                    throwable -> holdManager.clearHeldSpot()
                            );
                } catch (Exception e) {
                    // Ignore errors, will be cleaned up on next app start
                }
            }).start();
        }
    }
}

