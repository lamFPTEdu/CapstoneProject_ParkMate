package com.parkmate.android.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.adapter.FloorAdapter;
import com.parkmate.android.model.response.ParkingLotDetailResponse;
import com.parkmate.android.repository.ParkingRepository;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ParkingLotDetailActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private ImageButton btnBack;
    private ImageView ivParkingImage;
    private TextView tvParkingName;
    private TextView tvStatusBadge;
    private TextView tvAddress;
    private TextView tvOperatingHours;
    private TextView tvCapacity;
    private Button btnGetDirections;
    private Button btnReserveSpot;
    private ProgressBar progressBar;
    private ParkingRepository parkingRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Long parkingLotId;
    private String parkingLotName;
    private Double parkingLotLatitude;
    private Double parkingLotLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parking_lot_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupRecyclerView();
        setupClickListeners();

        parkingRepository = new ParkingRepository();

        // Get data from intent
        parkingLotId = getIntent().getLongExtra("parking_lot_id", -1);
        parkingLotName = getIntent().getStringExtra("parking_lot_name");

        if (parkingLotId == -1) {
            showError("Invalid parking lot ID");
            finish();
            return;
        }

        loadParkingLotDetail();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        ivParkingImage = findViewById(R.id.ivParkingImage);
        tvParkingName = findViewById(R.id.tvParkingName);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvAddress = findViewById(R.id.tvAddress);
        tvOperatingHours = findViewById(R.id.tvOperatingHours);
        tvCapacity = findViewById(R.id.tvCapacity);
        btnGetDirections = findViewById(R.id.btnGetDirections);
        btnReserveSpot = findViewById(R.id.btnReserveSpot);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        // Removed floor selection
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnGetDirections.setOnClickListener(v -> openDirections());
        btnReserveSpot.setOnClickListener(v -> openVehicleSelection());
    }

    private void openVehicleSelection() {
        Intent intent = new Intent(this, VehicleSelectionActivity.class);
        intent.putExtra("PARKING_LOT_ID", parkingLotId);
        startActivity(intent);
    }

    private void loadParkingLotDetail() {
        showLoading(true);

        compositeDisposable.add(
            parkingRepository.getParkingLotDetail(parkingLotId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        showLoading(false);
                        if (response.isSuccess() && response.getData() != null) {
                            displayParkingLotDetail(response.getData());
                        } else {
                            String errorMsg = response.getError() != null ? response.getError() : "Failed to load parking lot details";
                            showError(errorMsg);
                        }
                    },
                    throwable -> {
                        showLoading(false);
                        android.util.Log.e("ParkingLotDetail", "Error loading parking lot detail", throwable);

                        // Hiển thị thông báo lỗi chi tiết hơn
                        String errorMessage = "Network error";
                        if (throwable instanceof com.google.gson.JsonSyntaxException) {
                            errorMessage = "Data format error. Please contact support.";
                            android.util.Log.e("ParkingLotDetail", "JSON parsing error: " + throwable.getMessage());
                        } else if (throwable instanceof java.io.IOException) {
                            errorMessage = "Connection error. Please check your internet.";
                        } else if (throwable instanceof retrofit2.HttpException) {
                            retrofit2.HttpException httpException = (retrofit2.HttpException) throwable;
                            errorMessage = "Server error: " + httpException.code();
                            try {
                                String errorBody = httpException.response().errorBody().string();
                                android.util.Log.e("ParkingLotDetail", "HTTP error body: " + errorBody);
                            } catch (Exception e) {
                                // Ignore
                            }
                        }

                        showError(errorMessage);
                    }
                )
        );
    }

    private void displayParkingLotDetail(ParkingLotDetailResponse.ParkingLotDetail parkingLot) {
        tvParkingName.setText(parkingLot.getName());
        tvAddress.setText(parkingLot.getFullAddress());

        // Save location for directions
        parkingLotLatitude = parkingLot.getLatitude();
        parkingLotLongitude = parkingLot.getLongitude();

        // Set status badge
        tvStatusBadge.setText(parkingLot.getStatus());
        if ("ACTIVE".equals(parkingLot.getStatus())) {
            tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active);
        } else {
            tvStatusBadge.setBackgroundResource(R.drawable.bg_status_inactive);
        }

        // Set operating hours
        tvOperatingHours.setText(parkingLot.getOperatingHours());

        // Set capacity info
        if (parkingLot.getLotCapacity() != null && !parkingLot.getLotCapacity().isEmpty()) {
            int totalCapacity = parkingLot.getLotCapacity().stream()
                .mapToInt(ParkingLotDetailResponse.Capacity::getCapacity)
                .sum();
            tvCapacity.setText("Total capacity: " + totalCapacity + " vehicles");
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Mở chỉ đường từ vị trí hiện tại đến bãi xe
     * Quay về HomeActivity và vẽ route trên map
     */
    private void openDirections() {
        android.util.Log.d("ParkingLotDetail", "╔════════════════════════════════════════╗");
        android.util.Log.d("ParkingLotDetail", "║   OPEN DIRECTIONS CLICKED             ║");
        android.util.Log.d("ParkingLotDetail", "╚════════════════════════════════════════╝");

        Toast.makeText(this, "Đang mở chỉ đường...", Toast.LENGTH_SHORT).show();

        if (parkingLotLatitude == null || parkingLotLongitude == null) {
            android.util.Log.e("ParkingLotDetail", "ERROR: No coordinates - Lat: " + parkingLotLatitude + ", Lng: " + parkingLotLongitude);
            showError("Không có thông tin vị trí bãi xe");
            return;
        }

        android.util.Log.d("ParkingLotDetail", "Coordinates OK - Lat: " + parkingLotLatitude + ", Lng: " + parkingLotLongitude);

        // Kiểm tra permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.d("ParkingLotDetail", "Requesting location permission...");
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        android.util.Log.d("ParkingLotDetail", "Permission OK, opening HomeActivity");
        // Mở HomeActivity với thông tin destination để vẽ route
        openHomeActivityWithRoute();
    }

    /**
     * Mở HomeActivity và truyền thông tin để vẽ route
     */
    private void openHomeActivityWithRoute() {
        android.util.Log.d("ParkingLotDetail", "Creating intent to HomeActivity");
        android.util.Log.d("ParkingLotDetail", "  - destination_lat: " + parkingLotLatitude);
        android.util.Log.d("ParkingLotDetail", "  - destination_lng: " + parkingLotLongitude);
        android.util.Log.d("ParkingLotDetail", "  - destination_name: " + parkingLotName);

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("destination_lat", parkingLotLatitude);
        intent.putExtra("destination_lng", parkingLotLongitude);
        intent.putExtra("destination_name", parkingLotName);

        // Clear stack và quay về Home
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        android.util.Log.d("ParkingLotDetail", "Starting HomeActivity...");
        startActivity(intent);
        android.util.Log.d("ParkingLotDetail", "Finishing ParkingLotDetailActivity");
        finish(); // Đóng ParkingLotDetailActivity
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, mở HomeActivity với route
                openHomeActivityWithRoute();
            } else {
                // Permission denied
                showError("Cần quyền truy cập vị trí để chỉ đường");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
