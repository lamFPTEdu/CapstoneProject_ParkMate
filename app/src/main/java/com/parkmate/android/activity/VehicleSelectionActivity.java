package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.adapter.VehicleSelectionAdapter;
import com.parkmate.android.model.Vehicle;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class VehicleSelectionActivity extends AppCompatActivity {

    private static final String TAG = "VehicleSelectionActivity";

    private MaterialToolbar toolbar;
    private TextView tvVehicleTypeRequired;
    private TextView tvNoVehicles;
    private RecyclerView rvVehicles;
    private MaterialButton btnContinue;
    private ProgressBar progressBar;

    private VehicleSelectionAdapter vehicleAdapter;
    private ApiService apiService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    // Data from previous screen
    private Long parkingLotId;
    private Long floorId;
    private Long areaId;
    private Long spotId;
    private String parkingLotName;
    private String floorName;
    private String areaName;
    private String spotName;
    private String vehicleTypeRequired;

    // Pricing information
    private Integer initialCharge;
    private Integer stepRate;
    private Integer stepMinute;
    private Integer initialDurationMinute;
    private String pricingRuleName;

    private Vehicle selectedVehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vehicle_selection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        apiService = ApiClient.getApiService();

        // Get data from intent
        getIntentData();

        if (vehicleTypeRequired == null) {
            showError("Không xác định được loại xe yêu cầu");
            finish();
            return;
        }

        // Set vehicle type required text
        tvVehicleTypeRequired.setText("Khu vực yêu cầu: " + getVehicleTypeDisplayName(vehicleTypeRequired));

        loadUserVehicles();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvVehicleTypeRequired = findViewById(R.id.tvVehicleTypeRequired);
        tvNoVehicles = findViewById(R.id.tvNoVehicles);
        rvVehicles = findViewById(R.id.rvVehicles);
        btnContinue = findViewById(R.id.btnContinue);
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

    private void getIntentData() {
        parkingLotId = getIntent().getLongExtra("parking_lot_id", -1);
        floorId = getIntent().getLongExtra("floor_id", -1);
        areaId = getIntent().getLongExtra("area_id", -1);
        spotId = getIntent().getLongExtra("spot_id", -1);
        parkingLotName = getIntent().getStringExtra("parking_lot_name");
        floorName = getIntent().getStringExtra("floor_name");
        areaName = getIntent().getStringExtra("area_name");
        spotName = getIntent().getStringExtra("spot_name");
        vehicleTypeRequired = getIntent().getStringExtra("vehicle_type");

        // Get pricing data
        initialCharge = getIntent().getIntExtra("initial_charge", 0);
        stepRate = getIntent().getIntExtra("step_rate", 0);
        stepMinute = getIntent().getIntExtra("step_minute", 0);
        initialDurationMinute = getIntent().getIntExtra("initial_duration_minute", 0);
        pricingRuleName = getIntent().getStringExtra("pricing_rule_name");
    }

    private void setupRecyclerView() {
        vehicleAdapter = new VehicleSelectionAdapter(this, new ArrayList<>(), this::onVehicleClick);
        rvVehicles.setLayoutManager(new LinearLayoutManager(this));
        rvVehicles.setAdapter(vehicleAdapter);
    }

    private void setupClickListeners() {
        btnContinue.setOnClickListener(v -> {
            if (selectedVehicle != null) {
                proceedToBooking();
            } else {
                showError("Vui lòng chọn xe");
            }
        });
    }

    private void loadUserVehicles() {
        showLoading(true);

        // Gọi API giống VehicleActivity - lấy tất cả xe của user với ownedByMe=true
        compositeDisposable.add(
            apiService.getVehicles(0, 100, "createdAt", "desc", true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        showLoading(false);
                        Log.d(TAG, "API Response success: " + response.isSuccess());

                        if (response.isSuccess() && response.getData() != null) {
                            List<Vehicle> allVehicles = response.getData().getContent();
                            Log.d(TAG, "Total vehicles from API: " + allVehicles.size());
                            Log.d(TAG, "Vehicle type required: " + vehicleTypeRequired);

                            // Log tất cả xe
                            for (Vehicle v : allVehicles) {
                                Log.d(TAG, "Vehicle: " + v.getLicensePlate() +
                                    ", Type: " + v.getVehicleType() +
                                    ", Active: " + v.isActive());
                            }

                            List<Vehicle> compatibleVehicles = filterVehiclesByType(allVehicles);
                            Log.d(TAG, "Compatible vehicles after filter: " + compatibleVehicles.size());

                            if (compatibleVehicles.isEmpty()) {
                                showNoVehiclesMessage();
                            } else {
                                displayVehicles(compatibleVehicles);
                            }
                        } else {
                            showError("Không thể lấy danh sách xe");
                        }
                    },
                    error -> {
                        showLoading(false);
                        Log.e(TAG, "Error loading vehicles: " + error.getMessage(), error);
                        showError("Lỗi khi tải danh sách xe: " + error.getMessage());
                    }
                )
        );
    }

    private List<Vehicle> filterVehiclesByType(List<Vehicle> allVehicles) {
        List<Vehicle> filtered = new ArrayList<>();
        for (Vehicle vehicle : allVehicles) {
            Log.d(TAG, "Filtering vehicle: " + vehicle.getLicensePlate() +
                ", Active: " + vehicle.isActive() +
                ", Type: " + vehicle.getVehicleType() +
                ", Required: " + vehicleTypeRequired +
                ", Match: " + vehicleTypeRequired.equals(vehicle.getVehicleType()));

            // Lọc theo: xe đang hoạt động VÀ đúng loại xe yêu cầu
            if (vehicle.isActive() && vehicleTypeRequired.equals(vehicle.getVehicleType())) {
                filtered.add(vehicle);
                Log.d(TAG, "Vehicle added to filtered list: " + vehicle.getLicensePlate());
            }
        }
        return filtered;
    }

    private void displayVehicles(List<Vehicle> vehicles) {
        Log.d(TAG, "displayVehicles called with " + vehicles.size() + " vehicles");
        tvNoVehicles.setVisibility(View.GONE);
        rvVehicles.setVisibility(View.VISIBLE);
        vehicleAdapter.updateData(vehicles);
    }

    private void showNoVehiclesMessage() {
        tvNoVehicles.setVisibility(View.VISIBLE);
        rvVehicles.setVisibility(View.GONE);
        btnContinue.setEnabled(false);

        tvNoVehicles.setText(String.format(
            "Bạn chưa có xe %s phù hợp.\n\nVui lòng thêm xe trong phần Quản lý xe để tiếp tục đặt chỗ.",
            getVehicleTypeDisplayName(vehicleTypeRequired)
        ));
    }

    private void onVehicleClick(Vehicle vehicle) {
        selectedVehicle = vehicle;
        vehicleAdapter.setSelectedVehicle(vehicle);
        btnContinue.setEnabled(true);
    }

    private void proceedToBooking() {
        // Chuyển sang màn hình xác nhận đặt chỗ
        Intent intent = new Intent(this, ReservationConfirmActivity.class);
        intent.putExtra("parking_lot_id", parkingLotId);
        intent.putExtra("floor_id", floorId);
        intent.putExtra("area_id", areaId);
        intent.putExtra("spot_id", spotId);
        intent.putExtra("parking_lot_name", parkingLotName);
        intent.putExtra("floor_name", floorName);
        intent.putExtra("area_name", areaName);
        intent.putExtra("spot_name", spotName);
        intent.putExtra("vehicle_id", selectedVehicle.getId());
        intent.putExtra("vehicle_plate", selectedVehicle.getLicensePlate());
        intent.putExtra("vehicle_type", selectedVehicle.getVehicleType());

        // Add pricing data
        intent.putExtra("initial_charge", initialCharge);
        intent.putExtra("step_rate", stepRate);
        intent.putExtra("step_minute", stepMinute);
        intent.putExtra("initial_duration_minute", initialDurationMinute);
        intent.putExtra("pricing_rule_name", pricingRuleName);

        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            rvVehicles.setVisibility(View.GONE);
            tvNoVehicles.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
        compositeDisposable.dispose();
    }
}
