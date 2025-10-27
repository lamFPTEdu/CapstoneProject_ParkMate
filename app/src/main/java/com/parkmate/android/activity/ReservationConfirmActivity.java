package com.parkmate.android.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.model.Reservation;
import com.parkmate.android.model.request.ReservationRequest;
import com.parkmate.android.repository.ReservationRepository;
import com.parkmate.android.repository.WalletRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Activity xác nhận đặt chỗ
 */
public class ReservationConfirmActivity extends AppCompatActivity {

    private static final String TAG = "ReservationConfirm";

    // Views
    private MaterialToolbar toolbar;
    private TextView tvParkingLotName;
    private TextView tvFloorInfo;
    private TextView tvAreaInfo;
    private TextView tvSpotName;
    private TextView tvVehiclePlate;
    private TextView tvVehicleType;
    private TextView tvReservationFee;
    private TextView tvWalletBalance;
    private TextView tvReservedFrom;
    private TextView tvReservedUntil;
    private CardView cvTopUp;
    private MaterialButton btnConfirm;
    private ProgressBar progressBar;

    // Data từ Intent
    private Long parkingLotId;
    private Long floorId;
    private Long areaId;
    private Long spotId;
    private Long vehicleId;
    private String parkingLotName;
    private String floorName;
    private String areaName;
    private String spotName;
    private String vehiclePlate;
    private String vehicleType;

    // Pricing information
    private Integer initialCharge;
    private Integer stepRate;
    private Integer stepMinute;
    private Integer initialDurationMinute;
    private String pricingRuleName;
    private int reservationFee; // Calculated from initialCharge

    // Repositories
    private WalletRepository walletRepository;
    private ReservationRepository reservationRepository;
    private CompositeDisposable compositeDisposable;

    // Thời gian đặt chỗ
    private Calendar selectedDateTime;
    private Calendar selectedUntilDateTime;
    private double walletBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reservation_confirm);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        walletRepository = new WalletRepository();
        reservationRepository = new ReservationRepository();
        compositeDisposable = new CompositeDisposable();

        // Khởi tạo thời gian mặc định: hiện tại + 30 phút
        selectedDateTime = Calendar.getInstance();
        selectedDateTime.add(Calendar.MINUTE, 30);

        // Khởi tạo thời gian kết thúc mặc định: 2 giờ sau thời gian bắt đầu
        selectedUntilDateTime = (Calendar) selectedDateTime.clone();
        selectedUntilDateTime.add(Calendar.HOUR_OF_DAY, 1);

        getDataFromIntent();
        initViews();
        setupToolbar();
        displayReservationInfo();
        loadWalletBalance();
        setupClickListeners();
    }

    private void getDataFromIntent() {
        parkingLotId = getIntent().getLongExtra("parking_lot_id", 0);
        floorId = getIntent().getLongExtra("floor_id", 0);
        areaId = getIntent().getLongExtra("area_id", 0);
        spotId = getIntent().getLongExtra("spot_id", 0);
        vehicleId = getIntent().getLongExtra("vehicle_id", 0);
        parkingLotName = getIntent().getStringExtra("parking_lot_name");
        floorName = getIntent().getStringExtra("floor_name");
        areaName = getIntent().getStringExtra("area_name");
        spotName = getIntent().getStringExtra("spot_name");
        vehiclePlate = getIntent().getStringExtra("vehicle_plate");
        vehicleType = getIntent().getStringExtra("vehicle_type");

        // Nhận dữ liệu giá cả từ Intent
        initialCharge = getIntent().getIntExtra("initial_charge", 0);
        stepRate = getIntent().getIntExtra("step_rate", 0);
        stepMinute = getIntent().getIntExtra("step_minute", 0);
        initialDurationMinute = getIntent().getIntExtra("initial_duration_minute", 0);
        pricingRuleName = getIntent().getStringExtra("pricing_rule_name");

        // Tính toán reservationFee dựa trên initialCharge
        reservationFee = initialCharge != null ? initialCharge : 0;
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvParkingLotName = findViewById(R.id.tvParkingLotName);
        tvFloorInfo = findViewById(R.id.tvFloorInfo);
        tvAreaInfo = findViewById(R.id.tvAreaInfo);
        tvSpotName = findViewById(R.id.tvSpotName);
        tvVehiclePlate = findViewById(R.id.tvVehiclePlate);
        tvVehicleType = findViewById(R.id.tvVehicleType);
        tvReservationFee = findViewById(R.id.tvReservationFee);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvReservedFrom = findViewById(R.id.tvReservedFrom);
        tvReservedUntil = findViewById(R.id.tvReservedUntil);
        cvTopUp = findViewById(R.id.cvTopUp);
        btnConfirm = findViewById(R.id.btnConfirm);
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
        // Hiển thị thông tin bãi xe
        tvParkingLotName.setText(parkingLotName);
        tvFloorInfo.setText(floorName);
        tvAreaInfo.setText(areaName);
        tvSpotName.setText(spotName);

        // Hiển thị thông tin xe
        tvVehiclePlate.setText(vehiclePlate);
        tvVehicleType.setText(getVehicleTypeDisplayName(vehicleType));

        // Hiển thị phí
        tvReservationFee.setText(String.format("%,dđ", reservationFee));

        // Hiển thị thời gian mặc định
        updateReservedFromDisplay();
        updateReservedUntilDisplay();
    }

    private void loadWalletBalance() {
        compositeDisposable.add(
            walletRepository.getMyWallet()
                .subscribe(
                    response -> {
                        if (response.isSuccess() && response.getData() != null) {
                            walletBalance = response.getData().getBalance();
                            tvWalletBalance.setText(String.format("%,dđ", (int) walletBalance));

                            // Kiểm tra số dư
                            if (walletBalance < reservationFee) {
                                btnConfirm.setEnabled(false);
                                cvTopUp.setVisibility(View.VISIBLE);
                            } else {
                                btnConfirm.setEnabled(true);
                                cvTopUp.setVisibility(View.GONE);
                            }
                        }
                    },
                    error -> {
                        Log.e(TAG, "Không thể tải số dư ví: " + error.getMessage());
                        Toast.makeText(this, "Không thể tải số dư ví", Toast.LENGTH_SHORT).show();
                    }
                )
        );
    }

    private void setupClickListeners() {
        // Chọn thời gian bắt đầu
        tvReservedFrom.setOnClickListener(v -> showDateTimePickerForStart());

        // Chọn thời gian kết thúc
        tvReservedUntil.setOnClickListener(v -> showDateTimePickerForEnd());

        // Nạp tiền
        cvTopUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, WalletActivity.class);
            startActivity(intent);
        });

        // Xác nhận đặt chỗ
        btnConfirm.setOnClickListener(v -> confirmReservation());
    }

    private void showDateTimePickerForStart() {
        Calendar now = Calendar.getInstance();

        // Show Date Picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Show Time Picker sau khi chọn ngày
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (timeView, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);

                        // Tự động cập nhật thời gian kết thúc (2 giờ sau thời gian bắt đầu)
                        selectedUntilDateTime = (Calendar) selectedDateTime.clone();
                        selectedUntilDateTime.add(Calendar.HOUR_OF_DAY, 2);

                        updateReservedFromDisplay();
                        updateReservedUntilDisplay();
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    true
                );
                timePickerDialog.show();
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );

        // Không cho chọn ngày trong quá khứ
        datePickerDialog.getDatePicker().setMinDate(now.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showDateTimePickerForEnd() {
        // Thời gian kết thúc phải sau thời gian bắt đầu
        Calendar minTime = (Calendar) selectedDateTime.clone();

        // Show Date Picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedUntilDateTime.set(Calendar.YEAR, year);
                selectedUntilDateTime.set(Calendar.MONTH, month);
                selectedUntilDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Show Time Picker sau khi chọn ngày
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (timeView, hourOfDay, minute) -> {
                        selectedUntilDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedUntilDateTime.set(Calendar.MINUTE, minute);

                        // Kiểm tra thời gian kết thúc phải sau thời gian bắt đầu
                        if (selectedUntilDateTime.before(selectedDateTime) ||
                            selectedUntilDateTime.equals(selectedDateTime)) {
                            Toast.makeText(this,
                                "Thời gian rời phải sau thời gian đến",
                                Toast.LENGTH_SHORT).show();
                            // Reset về giá trị mặc định (2 giờ sau thời gian bắt đầu)
                            selectedUntilDateTime = (Calendar) selectedDateTime.clone();
                            selectedUntilDateTime.add(Calendar.HOUR_OF_DAY, 2);
                        }

                        updateReservedUntilDisplay();
                    },
                    selectedUntilDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedUntilDateTime.get(Calendar.MINUTE),
                    true
                );
                timePickerDialog.show();
            },
            selectedUntilDateTime.get(Calendar.YEAR),
            selectedUntilDateTime.get(Calendar.MONTH),
            selectedUntilDateTime.get(Calendar.DAY_OF_MONTH)
        );

        // Không cho chọn ngày trước thời gian bắt đầu
        datePickerDialog.getDatePicker().setMinDate(minTime.getTimeInMillis());
        datePickerDialog.show();
    }

    private void updateReservedFromDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        tvReservedFrom.setText(sdf.format(selectedDateTime.getTime()));

        // Tính lại giá khi thay đổi thời gian
        calculateAndUpdateFee();
    }

    private void updateReservedUntilDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        tvReservedUntil.setText(sdf.format(selectedUntilDateTime.getTime()));

        // Tính lại giá khi thay đổi thời gian
        calculateAndUpdateFee();
    }

    /**
     * Tính toán phí đặt chỗ dựa trên pricing rule và thời gian đã chọn
     * Logic:
     * - Tính tổng số phút từ reservedFrom đến reservedUntil
     * - Nếu <= initialDurationMinute: phí = initialCharge
     * - Nếu > initialDurationMinute: phí = initialCharge + ((số phút vượt / stepMinute) * stepRate)
     */
    private void calculateAndUpdateFee() {
        if (selectedDateTime == null || selectedUntilDateTime == null) {
            return;
        }

        // Tính tổng số phút giữa 2 thời điểm
        long durationMillis = selectedUntilDateTime.getTimeInMillis() - selectedDateTime.getTimeInMillis();
        long durationMinutes = durationMillis / (60 * 1000); // Convert to minutes

        // Kiểm tra thời gian hợp lệ
        if (durationMinutes <= 0) {
            Toast.makeText(this, "Thời gian rời phải sau thời gian đến", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tính phí dựa trên pricing rule
        int calculatedFee = initialCharge != null ? initialCharge : 0;

        if (initialDurationMinute != null && initialDurationMinute > 0) {
            if (durationMinutes > initialDurationMinute) {
                // Tính số phút vượt quá thời gian ban đầu
                long extraMinutes = durationMinutes - initialDurationMinute;

                // Tính số bước (làm tròn lên)
                if (stepMinute != null && stepMinute > 0 && stepRate != null) {
                    long steps = (long) Math.ceil((double) extraMinutes / stepMinute);
                    calculatedFee += (int) (steps * stepRate);
                }
            }
        }

        // Cập nhật giá hiển thị
        reservationFee = calculatedFee;
        tvReservationFee.setText(String.format(Locale.getDefault(), "%,dđ", reservationFee));

        // Kiểm tra lại số dư ví
        checkWalletBalance();

        // Hiển thị thông tin chi tiết về thời gian đặt
        long hours = durationMinutes / 60;
        long minutes = durationMinutes % 60;
        String durationText = hours > 0
            ? String.format(Locale.getDefault(), "%d giờ %d phút", hours, minutes)
            : String.format(Locale.getDefault(), "%d phút", minutes);

        Log.d(TAG, String.format("Thời gian đặt: %s (%d phút) - Phí: %,dđ",
            durationText, durationMinutes, reservationFee));
    }

    /**
     * Kiểm tra số dư ví có đủ để thanh toán không
     */
    private void checkWalletBalance() {
        if (walletBalance < reservationFee) {
            btnConfirm.setEnabled(false);
            cvTopUp.setVisibility(View.VISIBLE);
        } else {
            btnConfirm.setEnabled(true);
            cvTopUp.setVisibility(View.GONE);
        }
    }

    private void confirmReservation() {
        showLoading(true);

        // Format thời gian theo ISO 8601 với 'T' như backend expect: "2025-10-22T11:07:50"
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String reservedFrom = apiFormat.format(selectedDateTime.getTime());
        String reservedUntil = apiFormat.format(selectedUntilDateTime.getTime());

        // Tạo request với cả thời gian bắt đầu và kết thúc
        ReservationRequest request = new ReservationRequest(
            vehicleId,
            parkingLotId,
            spotId,
            reservationFee,
            reservedFrom,
            reservedUntil
        );

        compositeDisposable.add(
            reservationRepository.createReservation(request)
                .subscribe(
                    response -> {
                        showLoading(false);
                        if (response.isSuccess() && response.getData() != null) {
                            Toast.makeText(this, "Đặt chỗ thành công!", Toast.LENGTH_SHORT).show();

                            // Chuyển sang màn hình hiển thị QR code
                            Reservation reservation = response.getData();
                            Intent intent = new Intent(this, ReservationDetailActivity.class);
                            intent.putExtra("reservation", reservation);

                            // API đã trả về parkingLotName và spotName trong response
                            // Không cần truyền thêm thông tin nữa

                            startActivity(intent);
                            finish();
                        } else {
                            String errorMsg = response.getError() != null ? response.getError() : "Đặt chỗ thất bại";
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        showLoading(false);
                        Log.e(TAG, "Lỗi đặt chỗ: " + error.getMessage(), error);
                        Toast.makeText(this, "Đặt chỗ thất bại: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                )
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnConfirm.setEnabled(!show);
    }

    private String getVehicleTypeDisplayName(String vehicleType) {
        if (vehicleType == null) return "";
        switch (vehicleType) {
            case "CAR_UP_TO_9_SEATS": return "Ô tô (dưới 9 chỗ)";
            case "MOTORBIKE": return "Xe máy";
            case "BIKE": return "Xe đạp";
            default: return vehicleType;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload wallet balance khi quay lại từ WalletActivity
        loadWalletBalance();
    }
}
