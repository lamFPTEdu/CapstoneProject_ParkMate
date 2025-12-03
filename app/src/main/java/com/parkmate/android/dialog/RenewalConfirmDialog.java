package com.parkmate.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.model.UserSubscription;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog xác nhận gia hạn gói đăng ký
 */
public class RenewalConfirmDialog extends Dialog {

    public interface OnRenewalConfirmListener {
        void onConfirm();
        void onCancel();
    }

    private final UserSubscription subscription;
    private final OnRenewalConfirmListener listener;

    private ImageView iconRenew;
    private TextView tvTitle, tvMessage, tvParkingLotName, tvVehicleInfo, tvPackageInfo, tvEndDate, tvPrice;
    private MaterialButton btnCancel, btnConfirm;

    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public RenewalConfirmDialog(@NonNull Context context, UserSubscription subscription, OnRenewalConfirmListener listener) {
        super(context);
        this.subscription = subscription;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_renewal_confirm);

        // Set dialog window properties
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Set dialog width to 90% of screen width
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        initViews();
        setupListeners();
        populateData();
    }

    private void initViews() {
        iconRenew = findViewById(R.id.iconRenew);
        tvTitle = findViewById(R.id.tvTitle);
        tvMessage = findViewById(R.id.tvMessage);
        tvParkingLotName = findViewById(R.id.tvParkingLotName);
        tvVehicleInfo = findViewById(R.id.tvVehicleInfo);
        tvPackageInfo = findViewById(R.id.tvPackageInfo);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvPrice = findViewById(R.id.tvPrice);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirm();
            }
            dismiss();
        });
    }

    private void populateData() {
        // Parking lot name
        tvParkingLotName.setText(subscription.getParkingLotName());

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

        // End date
        String endDate = formatDate(subscription.getEndDate());
        tvEndDate.setText(String.format("Hết hạn: %s", endDate));

        // Price
        tvPrice.setText(String.format("%sđ/tháng", formatter.format(subscription.getPaidAmount())));
    }

    private String getVehicleTypeDisplay(String vehicleType) {
        if (vehicleType == null) return "";
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

