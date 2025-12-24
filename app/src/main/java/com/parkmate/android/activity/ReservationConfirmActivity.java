package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.model.request.HoldReservationRequest;
import com.parkmate.android.model.response.AvailableSpotResponse;
import com.parkmate.android.model.response.HoldReservationResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.utils.ReservationHoldManager;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ReservationConfirmActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvAvailableCapacity;
    private TextView tvTotalCapacity;
    private TextView tvInitialCharge;
    private TextView tvStepRate;
    private TextView tvEstimatedTotal;
    private LinearLayout llCountdown;
    private TextView tvCountdown;
    private MaterialButton btnConfirmReservation;
    private ProgressBar progressBar;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ReservationHoldManager holdManager;
    private DecimalFormat formatter = new DecimalFormat("#,###");
    private CountDownTimer countDownTimer;

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
                                        android.util.Log.e("ReservationConfirm", "Error releasing previous hold",
                                                throwable);
                                        holdManager.clearHoldId();
                                    }));
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
        btnBack = findViewById(R.id.btnBack);
        tvAvailableCapacity = findViewById(R.id.tvAvailableCapacity);
        tvTotalCapacity = findViewById(R.id.tvTotalCapacity);
        tvInitialCharge = findViewById(R.id.tvInitialCharge);
        tvStepRate = findViewById(R.id.tvStepRate);
        tvEstimatedTotal = findViewById(R.id.tvEstimatedTotal);
        llCountdown = findViewById(R.id.llCountdown);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnConfirmReservation = findViewById(R.id.btnConfirmReservation);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> handleBackPress());
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
        tvInitialCharge.setText(formatter.format(pricing.getInitialCharge()) + " VNĐ ("
                + pricing.getInitialDurationMinute() + " phút đầu)");
        tvStepRate.setText(formatter.format(pricing.getStepRate()) + " VNĐ (mỗi " + pricing.getStepMinute() + " phút)");
        tvEstimatedTotal.setText(formatter.format(pricing.getEstimateTotalFee()) + " VNĐ");
    }

    private void holdReservation() {
        showLoading(true);

        HoldReservationRequest request = new HoldReservationRequest(
                parkingLotId,
                vehicleId,
                reservedFrom,
                assumedStayMinute);

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
                                }));
    }

    private void handleHoldSuccess(HoldReservationResponse holdResponse) {
        this.holdId = holdResponse.getHoldId();
        this.isHoldActive = true;

        // Save to manager for global tracking
        holdManager.setHoldId(this.holdId);

        // Start countdown timer
        startCountdownTimer(holdResponse.getExpiresAt());

        Toast.makeText(this, holdResponse.getMessage(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Start countdown timer from expiresAt time string
     */
    private void startCountdownTimer(String expiresAt) {
        try {
            // Parse the expiresAt time (format: HH:mm:ss or similar)
            // Try multiple formats
            long remainingMillis = parseRemainingTime(expiresAt);

            if (remainingMillis <= 0) {
                // Default to 15 minutes if parsing fails
                remainingMillis = 15 * 60 * 1000;
            }

            llCountdown.setVisibility(View.VISIBLE);

            countDownTimer = new CountDownTimer(remainingMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long minutes = millisUntilFinished / 60000;
                    long seconds = (millisUntilFinished % 60000) / 1000;
                    String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                    tvCountdown.setText(timeText);

                    // Change color when less than 2 minutes
                    if (millisUntilFinished < 2 * 60 * 1000) {
                        llCountdown.setBackgroundResource(R.drawable.bg_countdown_urgent);
                    }
                }

                @Override
                public void onFinish() {
                    tvCountdown.setText("00:00");
                    showHoldExpiredDialog();
                }
            };
            countDownTimer.start();

        } catch (Exception e) {
            android.util.Log.e("ReservationConfirm", "Error parsing expiresAt", e);
            // Default countdown 15 minutes
            startDefaultCountdown();
        }
    }

    /**
     * Parse remaining time from expiresAt string
     */
    private long parseRemainingTime(String expiresAt) {
        try {
            // Try format: "2025-12-23T14:30:00"
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getDefault());
            Date expiryDate = isoFormat.parse(expiresAt);
            if (expiryDate != null) {
                return expiryDate.getTime() - System.currentTimeMillis();
            }
        } catch (ParseException e1) {
            try {
                // Try format: "HH:mm:ss"
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                Date expiryTime = timeFormat.parse(expiresAt);
                if (expiryTime != null) {
                    // Assume same day
                    SimpleDateFormat todayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String today = todayFormat.format(new Date());
                    SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date fullExpiry = fullFormat.parse(today + " " + expiresAt);
                    if (fullExpiry != null) {
                        return fullExpiry.getTime() - System.currentTimeMillis();
                    }
                }
            } catch (ParseException e2) {
                android.util.Log.e("ReservationConfirm", "Cannot parse expiresAt: " + expiresAt);
            }
        }
        return -1;
    }

    private void startDefaultCountdown() {
        llCountdown.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(15 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvCountdown.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("00:00");
                showHoldExpiredDialog();
            }
        };
        countDownTimer.start();
    }

    private void showHoldExpiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hết thời gian giữ chỗ")
                .setMessage("Thời gian giữ chỗ đã hết. Vui lòng thực hiện lại đặt chỗ.")
                .setPositiveButton("Đóng", (dialog, which) -> {
                    releaseHold();
                    finish();
                })
                .setCancelable(false)
                .show();
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

        com.parkmate.android.model.request.ReservationRequest request = new com.parkmate.android.model.request.ReservationRequest(
                vehicleId,
                parkingLotId,
                initialFee,
                reservedFrom,
                assumedStayMinute,
                holdId);

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
                                }));
    }

    private void handleReservationSuccess(com.parkmate.android.model.Reservation reservation) {
        // Stop countdown
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

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
                                }));
    }

    private void handleBackPress() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy đặt chỗ")
                .setMessage("Bạn có chắc chắn muốn hủy? Chỗ đang giữ sẽ được giải phóng.")
                .setPositiveButton("Có", (dialog, which) -> {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
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

        // Cancel countdown timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        compositeDisposable.clear();

        // Release hold if activity is destroyed without completing reservation
        if (isHoldActive && holdId != null && !isFinishing()) {
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
                                        android.util.Log.e("ReservationConfirm", "Error releasing hold on destroy",
                                                throwable);
                                        holdManager.clearHoldId();
                                    });
                } catch (Exception e) {
                    holdManager.clearHoldId();
                }
            }).start();
        } else if (isHoldActive) {
            releaseHold();
        }
    }
}
