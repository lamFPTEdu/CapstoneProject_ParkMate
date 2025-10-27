package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ParkingLotDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivParkingImage;
    private TextView tvParkingName;
    private TextView tvStatusBadge;
    private TextView tvAddress;
    private TextView tvOperatingHours;
    private TextView tvCapacity;
    private RecyclerView rvFloors;
    private ProgressBar progressBar;

    private FloorAdapter floorAdapter;
    private ParkingRepository parkingRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Long parkingLotId;
    private String parkingLotName;

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
        rvFloors = findViewById(R.id.rvFloors);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        floorAdapter = new FloorAdapter(new ArrayList<>(), this::onFloorClick);
        rvFloors.setLayoutManager(new LinearLayoutManager(this));
        rvFloors.setAdapter(floorAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
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

        // Set floors
        if (parkingLot.getParkingFloors() != null) {
            floorAdapter.updateData(parkingLot.getParkingFloors());
        }
    }

    private void onFloorClick(ParkingLotDetailResponse.ParkingFloor floor) {
        Intent intent = new Intent(this, FloorDetailActivity.class);
        intent.putExtra("parking_lot_id", parkingLotId);
        intent.putExtra("floor_id", floor.getId());
        intent.putExtra("floor_name", floor.getFloorName());
        intent.putExtra("parking_lot_name", parkingLotName);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            rvFloors.setVisibility(View.GONE);
        } else {
            rvFloors.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
