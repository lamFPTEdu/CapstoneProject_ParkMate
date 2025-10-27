package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.parkmate.android.adapter.AreaAdapter;
import com.parkmate.android.model.response.ParkingFloorDetailResponse;
import com.parkmate.android.repository.ParkingRepository;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FloorDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvFloorName;
    private TextView tvAvailableSpots;
    private TextView tvVehicleTypes;
    private RecyclerView rvAreas;
    private ProgressBar progressBar;

    private AreaAdapter areaAdapter;
    private ParkingRepository parkingRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Long floorId;
    private Long parkingLotId;
    private String floorName;
    private String parkingLotName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_floor_detail);

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
        floorId = getIntent().getLongExtra("floor_id", -1);
        parkingLotId = getIntent().getLongExtra("parking_lot_id", -1);
        floorName = getIntent().getStringExtra("floor_name");
        parkingLotName = getIntent().getStringExtra("parking_lot_name");

        if (floorId == -1) {
            showError("Invalid floor ID");
            finish();
            return;
        }

        // Set initial data
        tvFloorName.setText(floorName);

        loadFloorDetail();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.ivNavigation);
        tvFloorName = findViewById(R.id.tvFloorName);
        tvAvailableSpots = findViewById(R.id.tvAvailableSpots);
        tvVehicleTypes = findViewById(R.id.tvVehicleTypes);
        rvAreas = findViewById(R.id.rvAreas);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        areaAdapter = new AreaAdapter(new ArrayList<>(), this::onAreaClick);
        rvAreas.setLayoutManager(new LinearLayoutManager(this));
        rvAreas.setAdapter(areaAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadFloorDetail() {
        showLoading(true);

        compositeDisposable.add(
            parkingRepository.getParkingFloorDetail(floorId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        showLoading(false);
                        if (response.isSuccess() && response.getData() != null) {
                            displayFloorDetail(response.getData());
                        } else {
                            showError("Failed to load floor details");
                        }
                    },
                    throwable -> {
                        showLoading(false);
                        showError("Network error: " + throwable.getMessage());
                    }
                )
        );
    }

    private void displayFloorDetail(ParkingFloorDetailResponse.FloorData floor) {
        // Chuyển thẳng sang SpotSelectionActivity
        Intent intent = new Intent(this, SpotSelectionActivity.class);
        intent.putExtra("parking_lot_id", parkingLotId);
        intent.putExtra("floor_id", floorId);
        intent.putExtra("floor_name", floorName);
        intent.putExtra("parking_lot_name", parkingLotName);

        startActivity(intent);
        finish(); // Close FloorDetailActivity
    }

    private void onAreaClick(ParkingFloorDetailResponse.Area area) {
        Intent intent = new Intent(this, SpotSelectionActivity.class);
        intent.putExtra("parking_lot_id", parkingLotId);
        intent.putExtra("floor_id", floorId);
        intent.putExtra("area_id", area.getId());
        intent.putExtra("area_name", area.getName());
        intent.putExtra("floor_name", floorName);
        intent.putExtra("parking_lot_name", parkingLotName);
        intent.putExtra("vehicle_type", area.getVehicleType());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            rvAreas.setVisibility(View.GONE);
        } else {
            rvAreas.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getVehicleTypeShortName(String vehicleType) {
        switch (vehicleType) {
            case "CAR_UP_TO_9_SEATS": return "Car";
            case "MOTORBIKE": return "Motorbike";
            case "BIKE": return "Bike";
            default: return vehicleType;
        }
    }

    private String getVehicleTypeIcon(String vehicleType) {
        switch (vehicleType) {
            case "CAR_UP_TO_9_SEATS": return "🚗";
            case "MOTORBIKE": return "🏍️";
            case "BIKE": return "🚲";
            default: return "🚗";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
