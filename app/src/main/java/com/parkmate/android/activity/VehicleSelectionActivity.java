package com.parkmate.android.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.parkmate.android.R;
import com.parkmate.android.adapter.VehicleSelectionAdapter;
import com.parkmate.android.model.Vehicle;
import com.parkmate.android.model.response.AvailableSpotResponse;
import com.parkmate.android.model.response.VehicleResponse;
import com.parkmate.android.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class VehicleSelectionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etReservedFrom;
    private TextInputEditText etAssumedMinutes;
    private RecyclerView rvVehicles;
    private TextView tvNoVehicles;
    private MaterialButton btnCheckAvailability;
    private ProgressBar progressBar;

    private VehicleSelectionAdapter vehicleAdapter;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Long parkingLotId;
    private Long selectedVehicleId = null;
    private Calendar selectedDateTime;
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    // Pagination for vehicles
    private int currentVehiclePage = 0;
    private boolean isLoadingVehicles = false;
    private boolean isLastVehiclePage = false;
    private static final int VEHICLE_PAGE_SIZE = 4;
    private final java.util.List<Vehicle> allVehicles = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_selection);

        parkingLotId = getIntent().getLongExtra("PARKING_LOT_ID", -1);
        if (parkingLotId == -1) {
            Toast.makeText(this, "Invalid parking lot ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        loadVehicles();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etReservedFrom = findViewById(R.id.etReservedFrom);
        etAssumedMinutes = findViewById(R.id.etAssumedMinutes);
        rvVehicles = findViewById(R.id.rvVehicles);
        tvNoVehicles = findViewById(R.id.tvNoVehicles);
        btnCheckAvailability = findViewById(R.id.btnCheckAvailability);
        progressBar = findViewById(R.id.progressBar);

        toolbar.setNavigationOnClickListener(v -> finish());

        vehicleAdapter = new VehicleSelectionAdapter((vehicle, position) -> {
            onVehicleSelected(vehicle);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvVehicles.setLayoutManager(layoutManager);
        rvVehicles.setAdapter(vehicleAdapter);

        // Add infinite scroll listener for vehicles
        rvVehicles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // Load more when user scrolled to last 2 items
                if (!isLoadingVehicles && !isLastVehiclePage && dy > 0) {
                    if ((visibleItemCount + firstVisibleItemPosition + 2) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        android.util.Log.d("VehicleSelection", "Loading more vehicles...");
                        loadMoreVehicles();
                    }
                }
            }
        });

        selectedDateTime = Calendar.getInstance();
    }

    private void setupClickListeners() {
        etReservedFrom.setOnClickListener(v -> showDateTimePicker());
        btnCheckAvailability.setOnClickListener(v -> checkAvailableSpots());

        // Add text watcher for etAssumedMinutes
        etAssumedMinutes.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFormValidity();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void showDateTimePicker() {
        Calendar now = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Show time picker
                    new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDateTime.set(Calendar.MINUTE, minute);
                                selectedDateTime.set(Calendar.SECOND, 0);

                                etReservedFrom.setText(displayDateFormat.format(selectedDateTime.getTime()));
                                checkFormValidity();
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true
                    ).show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(now.getTimeInMillis());
        datePickerDialog.show();
    }

    private void loadVehicles() {
        if (isLoadingVehicles) return;

        isLoadingVehicles = true;
        showLoading(true);

        compositeDisposable.add(
                ApiClient.getApiService()
                        .getVehiclesWithFilter(currentVehiclePage, VEHICLE_PAGE_SIZE, "createdAt", "asc", true, parkingLotId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    isLoadingVehicles = false;
                                    showLoading(false);

                                    if (response.isSuccess() && response.getData() != null) {
                                        VehicleResponse vehicleResponse = response.getData();
                                        android.util.Log.d("VehicleSelection", "Page " + currentVehiclePage +
                                            " - Total elements: " + vehicleResponse.getTotalElements() +
                                            ", Total pages: " + vehicleResponse.getTotalPages() +
                                            ", Is last: " + vehicleResponse.isLast() +
                                            ", Content size: " + (vehicleResponse.getContent() != null ? vehicleResponse.getContent().size() : 0));

                                        if (vehicleResponse.getContent() != null && !vehicleResponse.getContent().isEmpty()) {
                                            // Filter out inactive vehicles (soft deleted)
                                            java.util.List<Vehicle> activeVehicles = new java.util.ArrayList<>();
                                            for (Vehicle vehicle : vehicleResponse.getContent()) {
                                                if (vehicle.isActive()) {
                                                    activeVehicles.add(vehicle);
                                                }
                                            }

                                            android.util.Log.d("VehicleSelection", "Active vehicles in this page: " + activeVehicles.size() +
                                                ", Total vehicles loaded: " + (allVehicles.size() + activeVehicles.size()));

                                            if (!activeVehicles.isEmpty()) {
                                                allVehicles.addAll(activeVehicles);
                                                vehicleAdapter.updateVehicles(allVehicles);

                                                // Check if this is the last page
                                                isLastVehiclePage = vehicleResponse.isLast();

                                                tvNoVehicles.setVisibility(View.GONE);
                                                rvVehicles.setVisibility(View.VISIBLE);

                                                // Auto-load next page if total vehicles < 5 (not enough to fill screen and enable scroll)
                                                if (allVehicles.size() < 5 && !isLastVehiclePage) {
                                                    android.util.Log.d("VehicleSelection", "Auto-loading next page because only " + allVehicles.size() + " vehicles loaded");
                                                    // Post delayed to avoid blocking UI
                                                    rvVehicles.postDelayed(() -> loadMoreVehicles(), 300);
                                                }
                                            } else if (currentVehiclePage == 0) {
                                                showNoVehicles();
                                            }
                                        } else if (currentVehiclePage == 0) {
                                            showNoVehicles();
                                        }
                                    } else {
                                        showError(response.getError() != null ? response.getError() : "Failed to load vehicles");
                                    }
                                },
                                throwable -> {
                                    isLoadingVehicles = false;
                                    showLoading(false);
                                    android.util.Log.e("VehicleSelection", "Error loading vehicles", throwable);
                                    showError("Network error: " + throwable.getMessage());
                                }
                        )
        );
    }

    private void loadMoreVehicles() {
        if (!isLoadingVehicles && !isLastVehiclePage) {
            currentVehiclePage++;
            loadVehicles();
        }
    }

    private void onVehicleSelected(Vehicle vehicle) {
        // Check if vehicle is disabled
        if (vehicle.isHasSubscriptionInThisParkingLot() || vehicle.isInReservation()) {
            String message = vehicle.isHasSubscriptionInThisParkingLot()
                ? "Xe này đã có vé tháng tại bãi xe này"
                : "Xe này đang trong phiên đặt chỗ khác";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        selectedVehicleId = vehicle.getId();
        checkFormValidity();
    }

    private void checkFormValidity() {
        String reservedFrom = etReservedFrom.getText() != null ? etReservedFrom.getText().toString() : "";
        String assumedMinutes = etAssumedMinutes.getText() != null ? etAssumedMinutes.getText().toString() : "";

        boolean isValid = !TextUtils.isEmpty(reservedFrom)
                && !TextUtils.isEmpty(assumedMinutes)
                && selectedVehicleId != null;

        btnCheckAvailability.setEnabled(isValid);
    }

    private void checkAvailableSpots() {
        String assumedMinutesStr = etAssumedMinutes.getText() != null ? etAssumedMinutes.getText().toString() : "";
        if (TextUtils.isEmpty(assumedMinutesStr)) {
            Toast.makeText(this, "Vui lòng nhập thời gian dự kiến", Toast.LENGTH_SHORT).show();
            return;
        }

        int assumedMinutes;
        try {
            assumedMinutes = Integer.parseInt(assumedMinutesStr);
            if (assumedMinutes <= 0) {
                Toast.makeText(this, "Thời gian phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedVehicleId == null) {
            Toast.makeText(this, "Vui lòng chọn xe", Toast.LENGTH_SHORT).show();
            return;
        }

        String reservedFrom = apiDateFormat.format(selectedDateTime.getTime());

        // Get vehicle type from selected vehicle ID
        showLoading(true);
        compositeDisposable.add(
                ApiClient.getApiService()
                        .getVehicleById(selectedVehicleId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.isSuccess() && response.getData() != null) {
                                        Vehicle vehicle = response.getData();
                                        checkAvailableSpotsWithVehicle(vehicle, reservedFrom, assumedMinutes);
                                    } else {
                                        showLoading(false);
                                        showError("Không thể lấy thông tin xe");
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    android.util.Log.e("VehicleSelection", "Error getting vehicle", throwable);
                                    showError("Network error: " + throwable.getMessage());
                                }
                        )
        );
    }

    private void checkAvailableSpotsWithVehicle(Vehicle vehicle, String reservedFrom, int assumedMinutes) {
        compositeDisposable.add(
                ApiClient.getApiService()
                        .checkAvailableSpots(parkingLotId, reservedFrom, assumedMinutes, vehicle.getVehicleType())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    showLoading(false);
                                    if (response.isSuccess() && response.getData() != null) {
                                        handleAvailableSpotResponse(response.getData());
                                    } else {
                                        showError(response.getError() != null ? response.getError() : "Failed to check availability");
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    android.util.Log.e("VehicleSelection", "Error checking spots", throwable);
                                    showError("Network error: " + throwable.getMessage());
                                }
                        )
        );
    }

    private void handleAvailableSpotResponse(AvailableSpotResponse.Data data) {
        if (data.getAvailableCapacity() <= 0) {
            // No spots available
            new AlertDialog.Builder(this)
                    .setTitle("Hết chỗ")
                    .setMessage("Rất tiếc, bãi xe đã hết chỗ trong khung giờ bạn chọn.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            // Navigate directly to confirmation
            navigateToConfirmation(data);
        }
    }

    private void navigateToConfirmation(AvailableSpotResponse.Data data) {
        String reservedFrom = apiDateFormat.format(selectedDateTime.getTime());
        String assumedMinutesStr = etAssumedMinutes.getText() != null ? etAssumedMinutes.getText().toString() : "";
        int assumedMinutes = Integer.parseInt(assumedMinutesStr);

        Intent intent = new Intent(this, ReservationConfirmActivity.class);
        intent.putExtra("PARKING_LOT_ID", parkingLotId);
        intent.putExtra("VEHICLE_ID", selectedVehicleId);
        intent.putExtra("RESERVED_FROM", reservedFrom);
        intent.putExtra("ASSUMED_STAY_MINUTE", assumedMinutes);
        intent.putExtra("SPOT_DATA", data);

        startActivity(intent);
    }

    private void showNoVehicles() {
        rvVehicles.setVisibility(View.GONE);
        tvNoVehicles.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCheckAvailability.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

