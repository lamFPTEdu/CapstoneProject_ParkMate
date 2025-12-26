package com.parkmate.android.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.parkmate.android.R;
import com.parkmate.android.adapter.SubscriptionPackageAdapter;
import com.parkmate.android.adapter.SubscriptionVehicleAdapter;
import com.parkmate.android.model.response.ParkingLotResponse;
import com.parkmate.android.model.SubscriptionPackage;
import com.parkmate.android.model.Vehicle;
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

public class SubscriptionSelectionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayoutVehicleTypes;
    private RecyclerView rvPackages;
    private RecyclerView rvVehicles;
    private TextInputEditText etStartDate;
    private TextView tvSelectedDate;
    private MaterialButton btnContinue;
    private ProgressBar progressBar;
    private TextView tvNoPackages;
    private TextView tvNoVehicles;
    private androidx.core.widget.NestedScrollView nestedScrollView;

    private SubscriptionPackageAdapter packageAdapter;
    private SubscriptionVehicleAdapter vehicleAdapter;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ParkingLotResponse.ParkingLot parkingLot;
    private List<SubscriptionPackage> allPackages = new ArrayList<>();
    private List<Vehicle> allVehicles = new ArrayList<>();

    private Long selectedPackageId = null;
    private SubscriptionPackage selectedPackage = null; // Store selected package object
    private Long selectedVehicleId = null;
    private String selectedVehiclePlate = null; // Store selected vehicle plate number
    private String selectedStartDate = null;
    private String currentVehicleType = null; // Current selected vehicle type from tab

    // Pagination for vehicles
    private int currentVehiclePage = 0;
    private boolean isLoadingVehicles = false;
    private boolean isLastVehiclePage = false;
    private static final int VEHICLE_PAGE_SIZE = 10;

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_selection);

        parkingLot = (ParkingLotResponse.ParkingLot) getIntent().getSerializableExtra("PARKING_LOT");
        if (parkingLot == null) {
            Toast.makeText(this, "Không có thông tin bãi xe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupClickListeners();
        loadPackages();
        loadVehicles();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayoutVehicleTypes = findViewById(R.id.tabLayoutVehicleTypes);
        rvPackages = findViewById(R.id.rvPackages);
        rvVehicles = findViewById(R.id.rvVehicles);
        etStartDate = findViewById(R.id.etStartDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnContinue = findViewById(R.id.btnContinue);
        progressBar = findViewById(R.id.progressBar);
        tvNoPackages = findViewById(R.id.tvNoPackages);
        tvNoVehicles = findViewById(R.id.tvNoVehicles);
        nestedScrollView = findViewById(R.id.nestedScrollView);

        packageAdapter = new SubscriptionPackageAdapter(pkg -> {
            if (pkg != null) {
                selectedPackageId = pkg.getId();
                selectedPackage = pkg;
                filterVehiclesByPackage(pkg);
            } else {
                // Deselected - reset package and hide vehicles
                selectedPackageId = null;
                selectedPackage = null;
                selectedVehicleId = null; // Also reset vehicle selection
                vehicleAdapter.updateVehicles(new java.util.ArrayList<>()); // Clear list
                rvVehicles.setVisibility(View.GONE);
                tvNoVehicles.setVisibility(View.VISIBLE);
                tvNoVehicles.setText("Vui lòng chọn gói đăng ký trước");
            }
            checkFormValidity();
        });

        vehicleAdapter = new SubscriptionVehicleAdapter(vehicle -> {
            if (vehicle != null) {
                selectedVehicleId = vehicle.getId();
                selectedVehiclePlate = vehicle.getLicensePlate();
            } else {
                // Deselected
                selectedVehicleId = null;
                selectedVehiclePlate = null;
            }
            checkFormValidity();
        }, parkingLot.getId());

        // Set horizontal layout manager for packages
        LinearLayoutManager packagesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);
        rvPackages.setLayoutManager(packagesLayoutManager);
        rvPackages.setAdapter(packageAdapter);

        // Set vertical layout manager for vehicles (keep as vertical scroll)
        LinearLayoutManager vehiclesLayoutManager = new LinearLayoutManager(this);
        rvVehicles.setLayoutManager(vehiclesLayoutManager);
        rvVehicles.setAdapter(vehicleAdapter);

        // Add infinite scroll listener to NestedScrollView
        if (nestedScrollView != null) {
            nestedScrollView
                    .setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v,
                            scrollX, scrollY, oldScrollX, oldScrollY) -> {
                        // Check if scrolled to bottom
                        if (!isLoadingVehicles && !isLastVehiclePage) {
                            int childHeight = v.getChildAt(0).getMeasuredHeight();
                            int scrollViewHeight = v.getMeasuredHeight();
                            int scrollableHeight = childHeight - scrollViewHeight;

                            // Load more when within 200dp of bottom
                            if (scrollY >= scrollableHeight - 200) {
                                android.util.Log.d("SubscriptionSelection", "Near bottom, loading more vehicles...");
                                loadMoreVehicles();
                            }
                        }
                    });
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đăng ký vé tháng");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        // Date card click - use the card directly
        View cardDatePicker = findViewById(R.id.cardDatePicker);
        if (cardDatePicker != null) {
            cardDatePicker.setOnClickListener(v -> showDatePicker());
        }
        tvSelectedDate.setOnClickListener(v -> showDatePicker());
        etStartDate.setOnClickListener(v -> showDatePicker());
        btnContinue.setOnClickListener(v -> navigateToFloorSelection());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedStartDate = apiDateFormat.format(calendar.getTime());
                    String displayText = displayDateFormat.format(calendar.getTime());
                    etStartDate.setText(displayText);
                    tvSelectedDate.setText(displayText);
                    checkFormValidity();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        // Set maximum date to 30 days from today
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 30);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void loadPackages() {
        if (parkingLot.getSubscriptions() != null && !parkingLot.getSubscriptions().isEmpty()) {
            allPackages = parkingLot.getSubscriptions();

            // Filter only active packages
            List<SubscriptionPackage> activePackages = new ArrayList<>();
            for (SubscriptionPackage pkg : allPackages) {
                if (pkg.isActive()) {
                    activePackages.add(pkg);
                }
            }

            if (!activePackages.isEmpty()) {
                // Setup tabs based on available packages
                setupVehicleTypeTabs(activePackages);
            } else {
                showNoPackages();
            }
        } else {
            showNoPackages();
        }
    }

    private void loadVehicles() {
        if (isLoadingVehicles)
            return;

        isLoadingVehicles = true;
        showLoading(true);

        compositeDisposable.add(
                ApiClient.getApiService()
                        .getVehiclesWithFilter(currentVehiclePage, VEHICLE_PAGE_SIZE, "createdAt", "asc", true,
                                parkingLot.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    isLoadingVehicles = false;
                                    showLoading(false);

                                    if (response.isSuccess() && response.getData() != null) {
                                        VehicleResponse vehicleResponse = response.getData();
                                        List<Vehicle> vehicles = vehicleResponse.getContent();

                                        // Filter active vehicles
                                        List<Vehicle> activeVehicles = new ArrayList<>();
                                        for (Vehicle vehicle : vehicles) {
                                            if (vehicle.isActive()) {
                                                activeVehicles.add(vehicle);
                                            }
                                        }

                                        allVehicles.addAll(activeVehicles);
                                        isLastVehiclePage = vehicleResponse.isLast();

                                        android.util.Log.d("SubscriptionSelection",
                                                "Page " + currentVehiclePage + " loaded, total vehicles: "
                                                        + allVehicles.size() +
                                                        ", isLast: " + isLastVehiclePage);

                                        // Initially hide vehicle list until package is selected
                                        if (currentVehiclePage == 0) {
                                            rvVehicles.setVisibility(View.GONE);
                                            tvNoVehicles.setVisibility(View.VISIBLE);
                                            tvNoVehicles.setText("Vui lòng chọn gói đăng ký trước");
                                        }

                                        // Auto-load more if not enough vehicles
                                        if (allVehicles.size() < 5 && !isLastVehiclePage) {
                                            loadMoreVehicles();
                                        }
                                    } else {
                                        showNoVehiclesError();
                                    }
                                },
                                throwable -> {
                                    isLoadingVehicles = false;
                                    showLoading(false);
                                    showError("Lỗi tải danh sách xe: " + throwable.getMessage());
                                }));
    }

    private void loadMoreVehicles() {
        if (!isLoadingVehicles && !isLastVehiclePage) {
            currentVehiclePage++;
            loadVehicles();
        }
    }

    private void checkFormValidity() {
        boolean isValid = selectedPackageId != null
                && selectedVehicleId != null
                && selectedStartDate != null;
        btnContinue.setEnabled(isValid);
    }

    private void filterVehiclesByPackage(SubscriptionPackage selectedPackage) {
        // Reset selected vehicle when changing package
        selectedVehicleId = null;

        if (allVehicles.isEmpty()) {
            showNoVehicles();
            return;
        }

        // Filter vehicles by matching vehicle type
        List<Vehicle> filteredVehicles = new ArrayList<>();
        for (Vehicle vehicle : allVehicles) {
            if (vehicle.getVehicleType().equals(selectedPackage.getVehicleType())) {
                filteredVehicles.add(vehicle);
            }
        }

        if (!filteredVehicles.isEmpty()) {
            vehicleAdapter.updateVehicles(filteredVehicles);
            rvVehicles.setVisibility(View.VISIBLE);
            tvNoVehicles.setVisibility(View.GONE);
        } else {
            showNoVehicles();
        }
    }

    private void navigateToFloorSelection() {
        // Check vehicle type - nếu xe máy/xe đạp thì skip location selection
        if (selectedPackage != null &&
                (selectedPackage.getVehicleType().equals("MOTORBIKE") ||
                        selectedPackage.getVehicleType().equals("BIKE"))) {
            // Skip location selection, go directly to summary
            Intent intent = new Intent(this, SubscriptionSummaryActivity.class);
            intent.putExtra("PARKING_LOT_ID", parkingLot.getId());
            intent.putExtra("PARKING_LOT_NAME", parkingLot.getName());
            intent.putExtra("VEHICLE_ID", selectedVehicleId);
            intent.putExtra("VEHICLE_PLATE_NUMBER", selectedVehiclePlate);
            intent.putExtra("PACKAGE_ID", selectedPackageId);
            intent.putExtra("PACKAGE_NAME", selectedPackage.getName());
            intent.putExtra("PACKAGE_PRICE", selectedPackage.getPrice());
            intent.putExtra("START_DATE", selectedStartDate);
            // Không truyền SPOT_ID - để null
            startActivity(intent);
        } else {
            // Ô tô - vẫn phải chọn location
            Intent intent = new Intent(this, SubscriptionLocationSelectionActivity.class);
            intent.putExtra("PARKING_LOT_ID", parkingLot.getId());
            intent.putExtra("PARKING_LOT_NAME", parkingLot.getName());
            intent.putExtra("VEHICLE_ID", selectedVehicleId);
            intent.putExtra("VEHICLE_PLATE_NUMBER", selectedVehiclePlate);
            intent.putExtra("PACKAGE_ID", selectedPackageId);
            intent.putExtra("PACKAGE", selectedPackage); // Pass package object
            intent.putExtra("START_DATE", selectedStartDate);
            startActivity(intent);
        }
    }

    /**
     * Setup Vehicle Type Tabs based on available packages
     */
    private void setupVehicleTypeTabs(List<SubscriptionPackage> activePackages) {
        tabLayoutVehicleTypes.removeAllTabs();

        // Find unique vehicle types from packages
        List<String> availableTypes = new ArrayList<>();
        for (SubscriptionPackage pkg : activePackages) {
            if (!availableTypes.contains(pkg.getVehicleType())) {
                availableTypes.add(pkg.getVehicleType());
            }
        }

        // Add tabs in order: CAR -> MOTORBIKE -> BIKE
        String[] orderedTypes = { "CAR_UP_TO_9_SEATS", "MOTORBIKE", "BIKE" };
        for (String type : orderedTypes) {
            if (availableTypes.contains(type)) {
                TabLayout.Tab tab = tabLayoutVehicleTypes.newTab();
                tab.setText(getVehicleTypeTabName(type));
                tab.setTag(type);
                tabLayoutVehicleTypes.addTab(tab);
            }
        }

        // Select first tab by default
        if (tabLayoutVehicleTypes.getTabCount() > 0) {
            tabLayoutVehicleTypes.selectTab(tabLayoutVehicleTypes.getTabAt(0));
            String firstType = (String) tabLayoutVehicleTypes.getTabAt(0).getTag();
            currentVehicleType = firstType;
            filterPackagesByVehicleType(firstType);
        }

        // Listen for tab changes
        tabLayoutVehicleTypes.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String vehicleType = (String) tab.getTag();
                currentVehicleType = vehicleType;

                // Reset selections when changing vehicle type
                selectedPackageId = null;
                selectedPackage = null;
                selectedVehicleId = null;

                filterPackagesByVehicleType(vehicleType);

                // Hide vehicle list until new package is selected
                rvVehicles.setVisibility(View.GONE);
                tvNoVehicles.setVisibility(View.VISIBLE);
                tvNoVehicles.setText("Vui lòng chọn gói đăng ký trước");

                checkFormValidity();
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
     * Get display name for vehicle type tab
     */
    private String getVehicleTypeTabName(String vehicleType) {
        if (vehicleType == null)
            return "";
        switch (vehicleType) {
            case "CAR_UP_TO_9_SEATS":
                return "🚗 Ô tô";
            case "MOTORBIKE":
                return "🏍️ Xe máy";
            case "BIKE":
                return "🚲 Xe đạp";
            default:
                return vehicleType;
        }
    }

    /**
     * Filter packages by vehicle type
     */
    private void filterPackagesByVehicleType(String vehicleType) {
        if (allPackages == null || allPackages.isEmpty()) {
            showNoPackages();
            return;
        }

        // Filter active packages by vehicle type
        List<SubscriptionPackage> filteredPackages = new ArrayList<>();
        for (SubscriptionPackage pkg : allPackages) {
            if (pkg.isActive() && vehicleType.equals(pkg.getVehicleType())) {
                filteredPackages.add(pkg);
            }
        }

        if (!filteredPackages.isEmpty()) {
            packageAdapter.updatePackages(filteredPackages);
            rvPackages.setVisibility(View.VISIBLE);
            tvNoPackages.setVisibility(View.GONE);
        } else {
            showNoPackages();
        }
    }

    private void showNoPackages() {
        rvPackages.setVisibility(View.GONE);
        tvNoPackages.setVisibility(View.VISIBLE);
    }

    private void showNoVehicles() {
        rvVehicles.setVisibility(View.GONE);
        tvNoVehicles.setVisibility(View.VISIBLE);
        tvNoVehicles.setText("Không có xe nào phù hợp với gói đã chọn");
    }

    private void showNoVehiclesError() {
        rvVehicles.setVisibility(View.GONE);
        tvNoVehicles.setVisibility(View.VISIBLE);
        tvNoVehicles.setText("Không có xe nào phù hợp");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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
