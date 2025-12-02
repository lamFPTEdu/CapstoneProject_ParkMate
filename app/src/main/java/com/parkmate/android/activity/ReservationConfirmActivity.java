package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.model.request.HoldReservationRequest;
import com.parkmate.android.model.response.AvailableSpotResponse;
import com.parkmate.android.model.response.HoldReservationResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.utils.ReservationHoldManager;

import java.text.DecimalFormat;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ReservationConfirmActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvAvailableCapacity;
    private TextView tvTotalCapacity;
    private TextView tvInitialCharge;
    private TextView tvStepRate;
    private TextView tvEstimatedTotal;
    private com.google.android.material.card.MaterialCardView cardExpiresAt;
    private TextView tvExpiresAt;
    private MaterialButton btnConfirmReservation;
    private ProgressBar progressBar;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ReservationHoldManager holdManager;
    private DecimalFormat formatter = new DecimalFormat("#,###");

    // Data from intent
    private Long parkingLotId;
    private Long vehicleId;
    private String reservedFrom;
    private int assumedStayMinute;
    private AvailableSpotResponse.Data spotData;

    // Hold data
    private String holdId;
    private boolean isHoldActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_confirm);

        holdManager = new ReservationHoldManager(this);

        // Release any previous hold before starting new reservation
        releasePreviousHold();

        getIntentData();
        initializeViews();
        displaySpotInfo();
        holdReservation();
    }

    private void releasePreviousHold() {
        if (holdManager.hasHold()) {
            String previousHoldId = holdManager.getHoldId();
            compositeDisposable.add(
                    ApiClient.getApiService()
                            .releaseHold(previousHoldId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    response -> {
                                        android.util.Log.d("ReservationConfirm", "Previous hold released");
                                        holdManager.clearHoldId();
                                    },
                                    throwable -> {
                                        android.util.Log.e("ReservationConfirm", "Error releasing previous hold", throwable);
                                        holdManager.clearHoldId();
                                    }
                            )
            );
        }
    }

    private void getIntentData() {
        parkingLotId = getIntent().getLongExtra("PARKING_LOT_ID", -1);
        vehicleId = getIntent().getLongExtra("VEHICLE_ID", -1);
        reservedFrom = getIntent().getStringExtra("RESERVED_FROM");
        assumedStayMinute = getIntent().getIntExtra("ASSUMED_STAY_MINUTE", 0);
        spotData = (AvailableSpotResponse.Data) getIntent().getSerializableExtra("SPOT_DATA");
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvAvailableCapacity = findViewById(R.id.tvAvailableCapacity);
        tvTotalCapacity = findViewById(R.id.tvTotalCapacity);
        tvInitialCharge = findViewById(R.id.tvInitialCharge);
        tvStepRate = findViewById(R.id.tvStepRate);
        tvEstimatedTotal = findViewById(R.id.tvEstimatedTotal);
        cardExpiresAt = findViewById(R.id.cardExpiresAt);
        tvExpiresAt = findViewById(R.id.tvExpiresAt);
        btnConfirmReservation = findViewById(R.id.btnConfirmReservation);
        progressBar = findViewById(R.id.progressBar);

        toolbar.setNavigationOnClickListener(v -> handleBackPress());

        btnConfirmReservation.setOnClickListener(v -> confirmReservation());

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });
    }

    private void displaySpotInfo() {
        tvAvailableCapacity.setText(String.valueOf(spotData.getAvailableCapacity()));
        tvTotalCapacity.setText(String.valueOf(spotData.getTotalCapacity()));

        AvailableSpotResponse.Pricing pricing = spotData.getPricing();
        tvInitialCharge.setText(formatter.format(pricing.getInitialCharge()) + " VNĐ (" + pricing.getInitialDurationMinute() + " phút đầu)");
        tvStepRate.setText(formatter.format(pricing.getStepRate()) + " VNĐ (mỗi " + pricing.getStepMinute() + " phút)");
        tvEstimatedTotal.setText(formatter.format(pricing.getEstimateTotalFee()) + " VNĐ");
    }

    private void holdReservation() {
        showLoading(true);

        HoldReservationRequest request = new HoldReservationRequest(
                parkingLotId,
                vehicleId,
                reservedFrom,
                assumedStayMinute
        );

        compositeDisposable.add(
                ApiClient.getApiService()
                        .holdReservation(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    showLoading(false);
                                    if (response.isSuccess() && response.getData() != null) {
                                        handleHoldSuccess(response.getData());
                                    } else {
                                        showError("Không thể giữ chỗ: " + response.getError());
                                        finish();
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    android.util.Log.e("ReservationConfirm", "Error holding reservation", throwable);
                                    showError("Lỗi mạng: " + throwable.getMessage());
                                    finish();
                                }
                        )
        );
    }

    private void handleHoldSuccess(HoldReservationResponse holdResponse) {
        this.holdId = holdResponse.getHoldId();
        this.isHoldActive = true;

        // Save to manager for global tracking
        holdManager.setHoldId(this.holdId);

        tvExpiresAt.setText("Giữ chỗ đến: " + holdResponse.getExpiresAt());
        cardExpiresAt.setVisibility(View.VISIBLE);

        Toast.makeText(this, holdResponse.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void confirmReservation() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt chỗ")
                .setMessage("Bạn có chắc chắn muốn đặt chỗ này?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    createReservation();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createReservation() {
        if (holdId == null) {
            showError("Lỗi: Không có holdId");
            return;
        }

        showLoading(true);

        // Get initialFee from pricing
        long initialFee = (long) spotData.getPricing().getInitialCharge();

        com.parkmate.android.model.request.ReservationRequest request =
                new com.parkmate.android.model.request.ReservationRequest(
                        vehicleId,
                        parkingLotId,
                        initialFee,
                        reservedFrom,
                        assumedStayMinute,
                        holdId
                );

        compositeDisposable.add(
                ApiClient.getApiService()
                        .createReservation(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    showLoading(false);
                                    if (response.isSuccess() && response.getData() != null) {
                                        handleReservationSuccess(response.getData());
                                    } else {
                                        showError("Không thể tạo đặt chỗ: " + response.getError());
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    android.util.Log.e("ReservationConfirm", "Error creating reservation", throwable);
                                    showError("Lỗi mạng: " + throwable.getMessage());
                                }
                        )
        );
    }

    private void handleReservationSuccess(com.parkmate.android.model.Reservation reservation) {
        // Đặt chỗ thành công, không cần release hold nữa
        isHoldActive = false;
        holdManager.clearHoldId(); // Clear from manager

        Toast.makeText(this, "Đặt chỗ thành công!", Toast.LENGTH_SHORT).show();

        // Navigate to ReservationDetailActivity to show QR code and details
        Intent intent = new Intent(this, ReservationDetailActivity.class);
        intent.putExtra("RESERVATION_ID", reservation.getId());
        intent.putExtra("RESERVATION", reservation);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void releaseHold() {
        if (!isHoldActive || holdId == null) {
            return;
        }

        compositeDisposable.add(
                ApiClient.getApiService()
                        .releaseHold(holdId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    android.util.Log.d("ReservationConfirm", "Hold released successfully");
                                    isHoldActive = false;
                                    holdManager.clearHoldId(); // Clear from manager
                                },
                                throwable -> {
                                    android.util.Log.e("ReservationConfirm", "Error releasing hold", throwable);
                                    // Clear anyway to avoid stuck state
                                    holdManager.clearHoldId();
                                }
                        )
        );
    }

    private void handleBackPress() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đặt chỗ")
                .setMessage("Bạn có chắc chắn muốn hủy? Chỗ đang giữ sẽ được giải phóng.")
                .setPositiveButton("Có", (dialog, which) -> {
                    releaseHold();
                    finish();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnConfirmReservation.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();

        // Release hold if activity is destroyed without completing reservation
        // This handles cases like app crash, task removal, etc.
        if (isHoldActive && holdId != null && !isFinishing()) {
            // Activity is being destroyed but not by normal finish()
            // Release the hold asynchronously without blocking
            new Thread(() -> {
                try {
                    ApiClient.getApiService()
                            .releaseHold(holdId)
                            .blockingSubscribe(
                                    response -> {
                                        android.util.Log.d("ReservationConfirm", "Hold released on destroy");
                                        holdManager.clearHoldId();
                                    },
                                    throwable -> {
                                        android.util.Log.e("ReservationConfirm", "Error releasing hold on destroy", throwable);
                                        holdManager.clearHoldId();
                                    }
                            );
                } catch (Exception e) {
                    // Ignore errors, will be cleaned up on next app start
                    holdManager.clearHoldId();
                }
            }).start();
        } else if (isHoldActive) {
            // Normal finish with hold active, release normally
            releaseHold();
        }
    }
}

