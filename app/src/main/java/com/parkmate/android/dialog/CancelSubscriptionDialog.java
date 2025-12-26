package com.parkmate.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.model.UserSubscription;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog xác nhận hủy gói đăng ký vé tháng
 */
public class CancelSubscriptionDialog extends Dialog {

    public interface OnCancelSubscriptionListener {
        void onConfirmCancel(String reason);

        void onBack();
    }

    private final UserSubscription subscription;
    private final OnCancelSubscriptionListener listener;

    private TextView tvParkingLotName, tvVehicleInfo, tvPackageInfo, tvDateRange;
    private TextView tvNoRefundWarning;
    private com.google.android.material.card.MaterialCardView cardNoRefundWarning;
    private com.google.android.material.card.MaterialCardView cardRefundEligible;
    private MaterialButton btnBack, btnConfirmCancel;

    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public CancelSubscriptionDialog(@NonNull Context context, UserSubscription subscription,
            OnCancelSubscriptionListener listener) {
        super(context);
        this.subscription = subscription;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_cancel_subscription);

        // Set dialog window properties
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Set dialog width to 90% of screen width
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        setupListeners();
        populateData();
    }

    private void initViews() {
        tvParkingLotName = findViewById(R.id.tvParkingLotName);
        tvVehicleInfo = findViewById(R.id.tvVehicleInfo);
        tvPackageInfo = findViewById(R.id.tvPackageInfo);
        tvDateRange = findViewById(R.id.tvDateRange);
        cardNoRefundWarning = findViewById(R.id.cardNoRefundWarning);
        tvNoRefundWarning = findViewById(R.id.tvNoRefundWarning);
        cardRefundEligible = findViewById(R.id.cardRefundEligible);
        btnBack = findViewById(R.id.btnBack);
        btnConfirmCancel = findViewById(R.id.btnConfirmCancel);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBack();
            }
            dismiss();
        });

        btnConfirmCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmCancel("Người dùng tự hủy");
            }
            dismiss();
        });
    }

    private void populateData() {
        // Parking lot name
        String parkingLotName = subscription.getParkingLotName();
        tvParkingLotName.setText(parkingLotName != null ? parkingLotName : "Bãi đỗ xe");

        // Vehicle info
        String vehicleTypeDisplay = getVehicleTypeDisplay(subscription.getVehicleType());
        tvVehicleInfo.setText(String.format("%s • %s",
                subscription.getVehicleLicensePlate(),
                vehicleTypeDisplay));

        // Package info
        String packageName = subscription.getSubscriptionPackageName();
        if (packageName != null && !packageName.isEmpty()) {
            tvPackageInfo.setText(packageName);
        } else {
            tvPackageInfo.setText("Gói đăng ký");
        }

        // Date range
        String startDate = formatDate(subscription.getStartDate());
        String endDate = formatDate(subscription.getEndDate());
        tvDateRange.setText(String.format("%s - %s", startDate, endDate));

        // Show no-refund warning based on status
        updateRefundWarning();
    }

    private void updateRefundWarning() {
        boolean hasBeenUsed = subscription.isHasBeenUsed();
        boolean passedHalfPeriod = subscription.isPassedHalfPeriod();

        if (hasBeenUsed && passedHalfPeriod) {
            // Both conditions - strongest warning
            cardNoRefundWarning.setVisibility(View.VISIBLE);
            cardRefundEligible.setVisibility(View.GONE);
            tvNoRefundWarning.setText("⚠️ Bạn sẽ KHÔNG được hoàn tiền do đã sử dụng và đã qua nửa thời hạn!");
        } else if (hasBeenUsed) {
            // Has been used
            cardNoRefundWarning.setVisibility(View.VISIBLE);
            cardRefundEligible.setVisibility(View.GONE);
            tvNoRefundWarning.setText("⚠️ Bạn sẽ KHÔNG được hoàn tiền do gói đã được sử dụng!");
        } else if (passedHalfPeriod) {
            // Passed half period
            cardNoRefundWarning.setVisibility(View.VISIBLE);
            cardRefundEligible.setVisibility(View.GONE);
            tvNoRefundWarning.setText("⚠️ Bạn sẽ KHÔNG được hoàn tiền do đã qua nửa thời hạn!");
        } else {
            // Eligible for refund - show green card
            cardNoRefundWarning.setVisibility(View.GONE);
            cardRefundEligible.setVisibility(View.VISIBLE);
        }
    }

    private String getVehicleTypeDisplay(String vehicleType) {
        if (vehicleType == null)
            return "";
        switch (vehicleType) {
            case "CAR_UP_TO_9_SEATS":
                return "Ô tô";
            case "MOTORBIKE":
                return "Xe máy";
            case "BIKE":
                return "Xe đạp";
            default:
                return vehicleType;
        }
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }
        try {
            Date date = inputDateFormat.parse(dateString);
            if (date != null) {
                return outputDateFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }
}
