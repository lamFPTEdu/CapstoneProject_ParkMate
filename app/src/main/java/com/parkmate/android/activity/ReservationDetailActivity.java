package com.parkmate.android.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.model.Reservation;
import com.parkmate.android.utils.ImageUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Activity hiển thị chi tiết reservation và QR code
 */
public class ReservationDetailActivity extends AppCompatActivity {

    private static final String TAG = "ReservationDetail";

    // Views
    private MaterialToolbar toolbar;
    private ImageView ivQrCode;
    private TextView tvReservationId;
    private TextView tvStatus;
    private TextView tvReservationFee;
    private TextView tvReservedFrom;
    private TextView tvSpotInfo;
    private TextView tvVehicleLicensePlate;
    private TextView tvVehicleType;
    private MaterialButton btnDone;
    private ProgressBar progressBar;

    private Reservation reservation;

    // API Service (not needed anymore since API returns complete data)
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reservation_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        compositeDisposable = new CompositeDisposable();

        getDataFromIntent();
        initViews();
        setupToolbar();

        // API response đã bao gồm đầy đủ thông tin
        displayReservationInfo();
        displayQrCode();
        setupClickListeners();
    }

    private void getDataFromIntent() {
        reservation = (Reservation) getIntent().getSerializableExtra("RESERVATION");
        if (reservation == null) {
            // Fallback to old key
            reservation = (Reservation) getIntent().getSerializableExtra("reservation");
        }
        if (reservation == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đặt chỗ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivQrCode = findViewById(R.id.ivQrCode);
        tvReservationId = findViewById(R.id.tvReservationId);
        tvStatus = findViewById(R.id.tvStatus);
        tvReservationFee = findViewById(R.id.tvReservationFee);
        tvReservedFrom = findViewById(R.id.tvReservedFrom);
        tvSpotInfo = findViewById(R.id.tvSpotInfo);
        tvVehicleLicensePlate = findViewById(R.id.tvVehicleLicensePlate);
        tvVehicleType = findViewById(R.id.tvVehicleType);
        btnDone = findViewById(R.id.btnDone);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayReservationInfo() {
        if (reservation == null) return;

        // Hiển thị ID
        tvReservationId.setText("" + reservation.getId());

        // Hiển thị trạng thái với màu background đúng
        tvStatus.setText(reservation.getStatusDisplayName());
        setStatusBackground(reservation.getStatus());

        // Hiển thị phí (sử dụng totalFee hoặc initialFee)
        int displayFee = reservation.getTotalFee() > 0 ? reservation.getTotalFee() : reservation.getInitialFee();
        tvReservationFee.setText(String.format("%,dđ", displayFee));

        // Hiển thị thời gian (từ - đến)
        String timeRange = formatDateTimeRange(reservation.getReservedFrom(), reservation.getReservedUntil());
        tvReservedFrom.setText(timeRange);

        // Hiển thị thông tin bãi đỗ - API mới trả về parkingLotName
        String spotInfo;
        if (reservation.getParkingLotName() != null && !reservation.getParkingLotName().isEmpty()) {
            spotInfo = reservation.getParkingLotName();
        } else {
            // Fallback nếu không có tên
            spotInfo = "Bãi đỗ ID: " + reservation.getParkingLotId();
        }
        tvSpotInfo.setText(spotInfo);

        // Hiển thị thông tin xe - API mới đã trả về vehicleLicensePlate và vehicleType
        if (reservation.getVehicleLicensePlate() != null && !reservation.getVehicleLicensePlate().isEmpty()) {
            tvVehicleLicensePlate.setText(reservation.getVehicleLicensePlate());
        } else {
            tvVehicleLicensePlate.setText("N/A");
        }

        if (reservation.getVehicleType() != null && !reservation.getVehicleType().isEmpty()) {
            tvVehicleType.setText(getVehicleTypeDisplayName(reservation.getVehicleType()));
        } else {
            tvVehicleType.setText("N/A");
        }
    }

    private void setStatusBackground(String status) {
        if (status == null) return;

        int backgroundColor;
        switch (status) {
            case "PENDING":
                backgroundColor = 0xFFFF9800; // Orange - Đặt rồi nhưng chưa vào bãi
                break;
            case "ACTIVE":
                backgroundColor = 0xFF4CAF50; // Green - Xe đang trong bãi
                break;
            case "COMPLETED":
                backgroundColor = 0xFF2196F3; // Blue - Xe ra khỏi bãi hoàn thành
                break;
            case "CANCELLED":
                backgroundColor = 0xFFF44336; // Red - Booking bị hủy
                break;
            case "EXPIRED":
                backgroundColor = 0xFF9E9E9E; // Gray - Hết hạn
                break;
            default:
                backgroundColor = 0xFF9E9E9E; // Gray
                break;
        }

        tvStatus.setBackgroundColor(backgroundColor);
    }

    private void displayQrCode() {
        if (reservation == null || reservation.getQrCode() == null) {
            Toast.makeText(this, "Không có mã QR", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Decode Base64 thành Bitmap trên background thread
        new Thread(() -> {
            String qrCodeBase64 = reservation.getQrCode();
            Bitmap qrCodeBitmap = ImageUtils.decodeBase64ToBitmap(qrCodeBase64);

            // Update UI trên main thread
            runOnUiThread(() -> {
                showLoading(false);

                if (qrCodeBitmap != null) {
                    ivQrCode.setImageBitmap(qrCodeBitmap);
                    Log.d(TAG, "QR code hiển thị thành công");
                } else {
                    Toast.makeText(this, "Không thể hiển thị QR code", Toast.LENGTH_SHORT).show();
                    ivQrCode.setImageResource(R.drawable.ic_qr_code_placeholder);
                }
            });
        }).start();
    }

    private void setupClickListeners() {
        btnDone.setOnClickListener(v -> {
            // Chuyển về màn hình Danh sách đặt chỗ và clear toàn bộ back stack
            // Sau đó khi bấm back sẽ về ProfileActivity
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent listIntent = new Intent(this, ReservationListActivity.class);

            startActivity(profileIntent);
            startActivity(listIntent);
            finish();
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        ivQrCode.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private String getVehicleTypeDisplayName(String vehicleType) {
        if (vehicleType == null) return "Không xác định";

        switch (vehicleType) {
            case "MOTORBIKE":
                return "Xe máy";
            case "BIKE":
                return "Xe đạp";
            case "CAR_UP_TO_9_SEATS":
                return "Ô tô (đến 9 chỗ)";
            case "OTHER":
                return "Khác";
            default:
                return vehicleType;
        }
    }

    private String formatDateTimeRange(String fromDateTime, String untilDateTime) {
        if (fromDateTime == null || fromDateTime.isEmpty()) {
            return "Chưa xác định";
        }

        try {
            // API response format: "2024-07-01 10:00:00"
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date dateFrom = apiFormat.parse(fromDateTime);

            if (dateFrom != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
                String fromStr = displayFormat.format(dateFrom);

                // Nếu có thời gian kết thúc
                if (untilDateTime != null && !untilDateTime.isEmpty()) {
                    Date dateUntil = apiFormat.parse(untilDateTime);
                    if (dateUntil != null) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        String untilStr = timeFormat.format(dateUntil);
                        return fromStr + " → " + untilStr;
                    }
                }

                return fromStr;
            }
        } catch (ParseException e) {
            Log.e(TAG, "Lỗi parse datetime range: " + e.getMessage());
        }

        return fromDateTime;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }
}
