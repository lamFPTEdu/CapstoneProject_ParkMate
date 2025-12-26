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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.parkmate.android.R;
import com.parkmate.android.adapter.VehicleSelectionAdapter;
import com.parkmate.android.model.Vehicle;
import com.parkmate.android.model.response.AvailableSpotResponse;
import com.parkmate.android.model.response.VehicleResponse;
import com.parkmate.android.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class VehicleSelectionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayoutVehicleTypes;
    private TextInputEditText etReservedFrom;
    private TextInputEditText etAssumedMinutes;
    private TextView tvSelectedDateTime;
    private RecyclerView rvVehicles;
    private TextView tvNoVehicles;
    private MaterialButton btnCheckAvailability;
    private ProgressBar progressBar;
    private androidx.core.widget.NestedScrollView nestedScrollView;

    private VehicleSelectionAdapter vehicleAdapter;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Long parkingLotId;
    private Long selectedVehicleId = null;
    private Calendar selectedDateTime;
    private String currentVehicleType = null; // Current selected vehicle type filter
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    // Pagination for vehicles
    private int currentVehiclePage = 0;
    private boolean isLoadingVehicles = false;
    private boolean isLastVehiclePage = false;
    private static final int VEHICLE_PAGE_SIZE = 10;
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
        tabLayoutVehicleTypes = findViewById(R.id.tabLayoutVehicleTypes);
        etReservedFrom = findViewById(R.id.etReservedFrom);
        etAssumedMinutes = findViewById(R.id.etAssumedMinutes);
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime);
        rvVehicles = findViewById(R.id.rvVehicles);
        tvNoVehicles = findViewById(R.id.tvNoVehicles);
        btnCheckAvailability = findViewById(R.id.btnCheckAvailability);
        progressBar = findViewById(R.id.progressBar);
        nestedScrollView = findViewById(R.id.nestedScrollView);

        toolbar.setNavigationOnClickListener(v -> finish());

        vehicleAdapter = new VehicleSelectionAdapter((vehicle, position) -> {
            onVehicleSelected(vehicle);
        });

        // Set vertical layout manager for vehicles
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvVehicles.setLayoutManager(layoutManager);
        rvVehicles.setAdapter(vehicleAdapter);

        // Add infinite scroll listener to NestedScrollView (not RecyclerView)
        // Because RecyclerView inside NestedScrollView doesn't trigger its own scroll
        // events
        nestedScrollView.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v,
                scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Check if scrolled to bottom
            if (!isLoadingVehicles && !isLastVehiclePage) {
                int childHeight = v.getChildAt(0).getMeasuredHeight();
                int scrollViewHeight = v.getMeasuredHeight();
                int scrollableHeight = childHeight - scrollViewHeight;

                // Load more when within 200dp of bottom
                if (scrollY >= scrollableHeight - 200) {
                    android.util.Log.d("VehicleSelection", "NestedScrollView near bottom, loading more vehicles...");
                    loadMoreVehicles();
                }
            }
        });

        // Setup vehicle type tabs
        setupVehicleTypeTabs();

        selectedDateTime = Calendar.getInstance();
    }

    /**
     * Setup vehicle type tabs
     */
    private void setupVehicleTypeTabs() {
        tabLayoutVehicleTypes.removeAllTabs();

        // Add "All" tab first
        TabLayout.Tab allTab = tabLayoutVehicleTypes.newTab();
        allTab.setText("Tất cả");
        allTab.setTag(null);
        tabLayoutVehicleTypes.addTab(allTab);

        // Add vehicle type tabs
        TabLayout.Tab carTab = tabLayoutVehicleTypes.newTab();
        carTab.setText("Ô tô");
        carTab.setTag("CAR_UP_TO_9_SEATS");
        tabLayoutVehicleTypes.addTab(carTab);

        TabLayout.Tab motorbikeTab = tabLayoutVehicleTypes.newTab();
        motorbikeTab.setText("Xe máy");
        motorbikeTab.setTag("MOTORBIKE");
        tabLayoutVehicleTypes.addTab(motorbikeTab);

        TabLayout.Tab bikeTab = tabLayoutVehicleTypes.newTab();
        bikeTab.setText("Xe đạp");
        bikeTab.setTag("BIKE");
        tabLayoutVehicleTypes.addTab(bikeTab);

        // Tab selection listener
        tabLayoutVehicleTypes.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentVehicleType = (String) tab.getTag();
                // Reset selection when changing tab
                selectedVehicleId = null;
                vehicleAdapter.clearSelection();
                checkFormValidity();
                // Filter vehicles
                filterVehiclesByType();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * Filter vehicles by current selected type
     */
    private void filterVehiclesByType() {
        if (allVehicles.isEmpty()) {
            showNoVehicles();
            return;
        }

        if (currentVehicleType == null) {
            // Show all vehicles
            vehicleAdapter.updateVehicles(allVehicles);
            if (allVehicles.isEmpty()) {
                showNoVehicles();
            } else {
                tvNoVehicles.setVisibility(View.GONE);
                rvVehicles.setVisibility(View.VISIBLE);
            }
        } else {
            // Filter by vehicle type
            List<Vehicle> filteredVehicles = new ArrayList<>();
            for (Vehicle vehicle : allVehicles) {
                if (currentVehicleType.equals(vehicle.getVehicleType())) {
                    filteredVehicles.add(vehicle);
                }
            }
            vehicleAdapter.updateVehicles(filteredVehicles);
            if (filteredVehicles.isEmpty()) {
                tvNoVehicles.setText("Không có xe " + getVehicleTypeName(currentVehicleType) + " nào");
                tvNoVehicles.setVisibility(View.VISIBLE);
                rvVehicles.setVisibility(View.GONE);
            } else {
                tvNoVehicles.setVisibility(View.GONE);
                rvVehicles.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Get display name for vehicle type
     */
    private String getVehicleTypeName(String vehicleType) {
        if (vehicleType == null)
            return "";
        switch (vehicleType) {
            case "CAR_UP_TO_9_SEATS":
                return "ô tô";
            case "MOTORBIKE":
                return "xe máy";
            case "BIKE":
                return "xe đạp";
            default:
                return vehicleType;
        }
    }

    private void setupClickListeners() {
        // Date card click
        View cardDatePicker = findViewById(R.id.cardDatePicker);
        if (cardDatePicker != null) {
            cardDatePicker.setOnClickListener(v -> showDateTimePicker());
        }
        if (tvSelectedDateTime != null) {
            tvSelectedDateTime.setOnClickListener(v -> showDateTimePicker());
        }
        etReservedFrom.setOnClickListener(v -> showDateTimePicker());
        btnCheckAvailability.setOnClickListener(v -> checkAvailableSpots());

        // Add text watcher for etAssumedMinutes
        etAssumedMinutes.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFormValidity();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
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

                                String formattedDate = displayDateFormat.format(selectedDateTime.getTime());
                                etReservedFrom.setText(formattedDate);
                                if (tvSelectedDateTime != null) {
                                    tvSelectedDateTime.setText(formattedDate);
                                }
                                checkFormValidity();
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true).show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(now.getTimeInMillis());
        datePickerDialog.show();
    }

    private void loadVehicles() {
        if (isLoadingVehicles)
            return;

        isLoadingVehicles = true;
        showLoading(true);

        compositeDisposable.add(
                ApiClient.getApiService()
                        .getVehiclesWithFilter(currentVehiclePage, VEHICLE_PAGE_SIZE, "createdAt", "desc", true,
                                parkingLotId)
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
                                                ", Content size: "
                                                + (vehicleResponse.getContent() != null
                                                        ? vehicleResponse.getContent().size()
                                                        : 0));

                                        if (vehicleResponse.getContent() != null
                                                && !vehicleResponse.getContent().isEmpty()) {
                                            // Filter out inactive vehicles (soft deleted)
                                            java.util.List<Vehicle> activeVehicles = new java.util.ArrayList<>();
                                            for (Vehicle vehicle : vehicleResponse.getContent()) {
                                                if (vehicle.isActive()) {
                                                    activeVehicles.add(vehicle);
                                                }
                                            }

                                            android.util.Log.d("VehicleSelection",
                                                    "Active vehicles in this page: " + activeVehicles.size() +
                                                            ", Total vehicles loaded: "
                                                            + (allVehicles.size() + activeVehicles.size()));

                                            if (!activeVehicles.isEmpty()) {
                                                allVehicles.addAll(activeVehicles);
                                                // Filter by current tab selection
                                                filterVehiclesByType();

                                                // Check if this is the last page
                                                isLastVehiclePage = vehicleResponse.isLast();

                                                // Auto-load next page if total vehicles < 5 (not enough to fill screen
                                                // and enable scroll)
                                                if (allVehicles.size() < 5 && !isLastVehiclePage) {
                                                    android.util.Log.d("VehicleSelection",
                                                            "Auto-loading next page because only " + allVehicles.size()
                                                                    + " vehicles loaded");
                                                    // Post delayed to avoid blocking UI
                                                    rvVehicles.postDelayed(() -> loadMoreVehicles(), 300);
                                                }
                                            } else if (currentVehiclePage == 0 && allVehicles.isEmpty()) {
                                                // Only show no vehicles if we've loaded first page and still have no
                                                // active vehicles
                                                // Keep loading next pages to find active vehicles
                                                if (!isLastVehiclePage) {
                                                    loadMoreVehicles();
                                                } else {
                                                    showNoVehicles();
                                                }
                                            }
                                        } else if (currentVehiclePage == 0) {
                                            showNoVehicles();
                                        }
                                    } else {
                                        showError(response.getError() != null ? response.getError()
                                                : "Failed to load vehicles");
                                    }
                                },
                                throwable -> {
                                    isLoadingVehicles = false;
                                    showLoading(false);
                                    android.util.Log.e("VehicleSelection", "Error loading vehicles", throwable);
                                    showError("Network error: " + throwable.getMessage());
                                }));
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
        // Validate assumed minutes
        String assumedMinutesStr = etAssumedMinutes.getText() != null ? etAssumedMinutes.getText().toString() : "";
        if (TextUtils.isEmpty(assumedMinutesStr)) {
            Toast.makeText(this, "Vui lòng nhập thời gian dự kiến", Toast.LENGTH_SHORT).show();
            return;
        }

        int assumedMinutes;
        try {
            assumedMinutes = Integer.parseInt(assumedMinutesStr);
            if (assumedMinutes <= 0) {
                Toast.makeText(this, "Thời gian đỗ phải lớn hơn 0 phút", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate reservation time (must be at least 5 minutes in the future)
        Calendar now = Calendar.getInstance();
        long diffMs = selectedDateTime.getTimeInMillis() - now.getTimeInMillis();
        long diffMinutes = diffMs / (1000 * 60);

        if (diffMinutes < 0) {
            Toast.makeText(this, "Thời gian đặt chỗ đã qua. Vui lòng chọn lại", Toast.LENGTH_SHORT).show();
            return;
        }

        if (diffMinutes < 5) {
            Toast.makeText(this, "Thời gian đặt chỗ phải cách hiện tại ít nhất 5 phút", Toast.LENGTH_SHORT).show();
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
                                }));
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
                                        showError(response.getError() != null ? response.getError()
                                                : "Failed to check availability");
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    android.util.Log.e("VehicleSelection", "Error checking spots", throwable);
                                    showError("Network error: " + throwable.getMessage());
                                }));
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
