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
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.parkmate.android.R;
import com.parkmate.android.adapter.AvailableSpotsAdapter;
import com.parkmate.android.adapter.ParkingImageAdapter;
import com.parkmate.android.adapter.PricingRulesAdapter;
import com.parkmate.android.adapter.SubscriptionPackageAdapter;
import com.parkmate.android.model.response.ParkingLotDetailResponse;
import com.parkmate.android.model.response.ParkingLotResponse;
import com.parkmate.android.repository.ParkingRepository;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ParkingLotDetailActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Views
    private Toolbar toolbar;
    private ViewPager2 viewPagerImages;
    private TextView tvParkingName;
    private TextView tvStatusBadge;
    private TextView tvAddress;
    private TextView tvTotalCapacity;
    private TextView tvTotalFloors;
    private TextView tv24Hour;
    private RecyclerView rvAvailableSpots;
    private TabLayout tabLayoutVehicleTypes;
    private RecyclerView rvSubscriptionPackages;
    private RecyclerView rvPricingRules;
    private TextView tvNoSubscription;
    private TextView tvNoPricing;
    private MaterialButton btnReserveSpot;
    private MaterialButton btnSubscribe;
    private MaterialButton btnGetDirections;
    private MaterialButton btnViewParkingMap;
    private View progressBar;

    // Rating views
    private View cardRatingsSection;
    private TextView tvAverageRating;
    private TextView tvTotalRatings;
    private RecyclerView rvRatings;
    private TextView tvNoRatings;
    private TextView btnViewAllRatings;

    // Adapters
    private ParkingImageAdapter imageAdapter;
    private AvailableSpotsAdapter availableSpotsAdapter;
    private SubscriptionPackageAdapter subscriptionPackageAdapter;
    private PricingRulesAdapter pricingRulesAdapter;
    private com.parkmate.android.adapter.ParkingLotRatingAdapter ratingAdapter;

    // Data
    private ParkingRepository parkingRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Long parkingLotId;
    private String parkingLotName;
    private Double parkingLotLatitude;
    private Double parkingLotLongitude;
    private ParkingLotResponse.ParkingLot parkingLot;

    // Full data from API for filtering by vehicle type
    private ParkingLotDetailResponse.ParkingLotDetail fullParkingLotDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parking_lot_detail_new);

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
        // Toolbar (hidden in new UI)
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Floating back button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Views
        viewPagerImages = findViewById(R.id.viewPagerImages);
        tvParkingName = findViewById(R.id.tvParkingName);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvAddress = findViewById(R.id.tvAddress);
        tvTotalCapacity = findViewById(R.id.tvTotalCapacity);
        tvTotalFloors = findViewById(R.id.tvTotalFloors);
        tv24Hour = findViewById(R.id.tv24Hour);
        rvAvailableSpots = findViewById(R.id.rvAvailableSpots);
        tabLayoutVehicleTypes = findViewById(R.id.tabLayoutVehicleTypes);
        rvSubscriptionPackages = findViewById(R.id.rvSubscriptionPackages);
        rvPricingRules = findViewById(R.id.rvPricingRules);
        tvNoSubscription = findViewById(R.id.tvNoSubscription);
        tvNoPricing = findViewById(R.id.tvNoPricing);
        btnReserveSpot = findViewById(R.id.btnReserveSpot);
        btnSubscribe = findViewById(R.id.btnSubscribe);
        btnGetDirections = findViewById(R.id.btnGetDirections);
        btnViewParkingMap = findViewById(R.id.btnViewParkingMapCard);
        progressBar = findViewById(R.id.progressBar);

        // Rating views
        cardRatingsSection = findViewById(R.id.cardRatingsSection);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalRatings = findViewById(R.id.tvTotalRatings);
        rvRatings = findViewById(R.id.rvRatings);
        tvNoRatings = findViewById(R.id.tvNoRatings);
        btnViewAllRatings = findViewById(R.id.btnViewAllRatings);

        // Setup Image ViewPager2
        imageAdapter = new ParkingImageAdapter();
        viewPagerImages.setAdapter(imageAdapter);

        // Setup Available Spots RecyclerView
        availableSpotsAdapter = new AvailableSpotsAdapter();
        rvAvailableSpots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvAvailableSpots.setAdapter(availableSpotsAdapter);

        // Setup Subscription Packages RecyclerView
        subscriptionPackageAdapter = new SubscriptionPackageAdapter();
        rvSubscriptionPackages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSubscriptionPackages.setAdapter(subscriptionPackageAdapter);

        // Setup Pricing Rules RecyclerView
        pricingRulesAdapter = new PricingRulesAdapter();
        rvPricingRules.setLayoutManager(new LinearLayoutManager(this));
        rvPricingRules.setAdapter(pricingRulesAdapter);

        // Setup Ratings RecyclerView (Horizontal)
        ratingAdapter = new com.parkmate.android.adapter.ParkingLotRatingAdapter();
        LinearLayoutManager ratingsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvRatings.setLayoutManager(ratingsLayoutManager);
        rvRatings.setAdapter(ratingAdapter);
    }

    private void setupRecyclerView() {
        // Removed floor selection
    }

    private void setupClickListeners() {
        btnReserveSpot.setOnClickListener(v -> openVehicleSelection());
        btnSubscribe.setOnClickListener(v -> openSubscriptionSelection());
        btnGetDirections.setOnClickListener(v -> openDirections());
        btnViewParkingMap.setOnClickListener(v -> openParkingMapView());
    }

    private void openVehicleSelection() {
        // GUEST MODE: Check login first
        if (!com.parkmate.android.utils.AuthHelper.checkLoginOrShowDialog(this, "đặt chỗ đỗ xe")) {
            return; // User is not logged in, dialog shown
        }

        // Kiểm tra xác thực căn cước trước khi cho phép đặt chỗ
        if (!com.parkmate.android.utils.IdVerificationHelper.checkIdVerification(this)) {
            return; // Dừng lại nếu chưa xác thực, dialog đã được hiển thị tự động
        }

        Intent intent = new Intent(this, VehicleSelectionActivity.class);
        intent.putExtra("PARKING_LOT_ID", parkingLotId);
        startActivity(intent);
    }

    private void openSubscriptionSelection() {
        // GUEST MODE: Check login first
        if (!com.parkmate.android.utils.AuthHelper.checkLoginOrShowDialog(this, "mua vé tháng")) {
            return; // User is not logged in, dialog shown
        }

        // Kiểm tra xác thực căn cước trước khi cho phép mua vé tháng
        if (!com.parkmate.android.utils.IdVerificationHelper.checkIdVerification(this)) {
            return; // Dừng lại nếu chưa xác thực, dialog đã được hiển thị tự động
        }

        // Check if parkingLot object is available
        if (parkingLot == null) {
            showError("Không có thông tin bãi xe");
            return;
        }

        Intent intent = new Intent(this, SubscriptionSelectionActivity.class);
        intent.putExtra("PARKING_LOT", parkingLot);
        startActivity(intent);
    }

    /**
     * Mở trang xem bản đồ bãi xe đầy đủ
     */
    private void openParkingMapView() {
        Intent intent = new Intent(this, ParkingMapViewActivity.class);
        intent.putExtra("PARKING_LOT_ID", parkingLotId);
        intent.putExtra("PARKING_LOT_NAME", parkingLotName);

        // Tạo địa chỉ đầy đủ
        String fullAddress = "";
        if (fullParkingLotDetail != null) {
            fullAddress = (fullParkingLotDetail.getStreetAddress() != null ? fullParkingLotDetail.getStreetAddress()
                    : "") + ", " +
                    (fullParkingLotDetail.getWard() != null ? fullParkingLotDetail.getWard() : "") + ", " +
                    (fullParkingLotDetail.getCity() != null ? fullParkingLotDetail.getCity() : "");
        }
        intent.putExtra("PARKING_LOT_ADDRESS", fullAddress);

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
                                        String errorMsg = response.getError() != null ? response.getError()
                                                : "Failed to load parking lot details";
                                        showError(errorMsg);
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    android.util.Log.e("ParkingLotDetail", "Error loading parking lot detail",
                                            throwable);

                                    // Hiển thị thông báo lỗi chi tiết hơn
                                    String errorMessage = "Network error";
                                    if (throwable instanceof com.google.gson.JsonSyntaxException) {
                                        errorMessage = "Data format error. Please contact support.";
                                        android.util.Log.e("ParkingLotDetail",
                                                "JSON parsing error: " + throwable.getMessage());
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
                                }));
    }

    private void displayParkingLotDetail(ParkingLotDetailResponse.ParkingLotDetail parkingLotDetail) {
        try {
            // Save full data for filtering
            this.fullParkingLotDetail = parkingLotDetail;

            // Basic Info
            if (parkingLotDetail.getName() != null) {
                tvParkingName.setText(parkingLotDetail.getName());
                // Update parking lot name variable
                parkingLotName = parkingLotDetail.getName();
            }

            String fullAddress = (parkingLotDetail.getStreetAddress() != null ? parkingLotDetail.getStreetAddress()
                    : "") + ", " +
                    (parkingLotDetail.getWard() != null ? parkingLotDetail.getWard() : "") + ", " +
                    (parkingLotDetail.getCity() != null ? parkingLotDetail.getCity() : "");
            tvAddress.setText(fullAddress);

            // Save location for directions
            parkingLotLatitude = parkingLotDetail.getLatitude();
            parkingLotLongitude = parkingLotDetail.getLongitude();

            // Convert to ParkingLot object for subscription
            parkingLot = convertToParkingLot(parkingLotDetail);

            // Set status badge
            String status = parkingLotDetail.getStatus() != null ? parkingLotDetail.getStatus() : "UNKNOWN";
            tvStatusBadge.setText(status);
            if ("ACTIVE".equals(status)) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active);
            } else {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_inactive);
            }

            // Set total capacity (sum of all vehicle types) - Thay stream() bằng for loop
            if (parkingLotDetail.getLotCapacity() != null && !parkingLotDetail.getLotCapacity().isEmpty()) {
                int totalCapacity = 0;
                for (ParkingLotDetailResponse.Capacity capacity : parkingLotDetail.getLotCapacity()) {
                    if (capacity.isActive()) {
                        totalCapacity += capacity.getCapacity();
                    }
                }
                tvTotalCapacity.setText(String.valueOf(totalCapacity));
            } else {
                tvTotalCapacity.setText("N/A");
            }

            // Set total floors
            if (parkingLotDetail.getTotalFloors() != null) {
                tvTotalFloors.setText(String.valueOf(parkingLotDetail.getTotalFloors()));
            } else {
                tvTotalFloors.setText("N/A");
            }

            // Set operating hours
            if (parkingLotDetail.getIs24Hour() != null && parkingLotDetail.getIs24Hour()) {
                tv24Hour.setText("24/7");
            } else {
                String openTime = parkingLotDetail.getOpenTime() != null ? parkingLotDetail.getOpenTime() : "N/A";
                String closeTime = parkingLotDetail.getCloseTime() != null ? parkingLotDetail.getCloseTime() : "N/A";
                tv24Hour.setText(openTime + " - " + closeTime);
            }

            // Load images into ViewPager2
            if (parkingLotDetail.getImages() != null && !parkingLotDetail.getImages().isEmpty()) {
                imageAdapter.submitList(parkingLotDetail.getImages());

                // Setup image counter (1/4)
                final int totalImages = parkingLotDetail.getImages().size();
                TextView tvImageCounter = findViewById(R.id.tvImageCounter);
                tvImageCounter.setText("1/" + totalImages);
                tvImageCounter.setVisibility(View.VISIBLE);

                viewPagerImages
                        .registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                            @Override
                            public void onPageSelected(int position) {
                                super.onPageSelected(position);
                                tvImageCounter.setText((position + 1) + "/" + totalImages);
                            }
                        });
            } else {
                // Show placeholder if no images
                imageAdapter.submitList(null);
                TextView tvImageCounter = findViewById(R.id.tvImageCounter);
                tvImageCounter.setVisibility(View.GONE);
            }

            // Display available spots
            if (parkingLotDetail.getAvailableSpots() != null && !parkingLotDetail.getAvailableSpots().isEmpty()) {
                availableSpotsAdapter.submitList(parkingLotDetail.getAvailableSpots());
            }

            // Setup Vehicle Type Tabs
            setupVehicleTypeTabs();

            // Display Ratings
            displayRatings(parkingLotDetail);

        } catch (Exception e) {
            android.util.Log.e("ParkingLotDetail", "Error displaying parking lot detail", e);
            showError("Error displaying parking lot information: " + e.getMessage());
        }
    }

    /**
     * Display ratings section (only latest 3 ratings)
     */
    private void displayRatings(ParkingLotDetailResponse.ParkingLotDetail parkingLotDetail) {
        if (parkingLotDetail.getRatings() != null && !parkingLotDetail.getRatings().isEmpty()) {
            cardRatingsSection.setVisibility(View.VISIBLE);
            tvNoRatings.setVisibility(View.GONE);
            rvRatings.setVisibility(View.VISIBLE);

            // Display average rating with 1 decimal place
            if (parkingLotDetail.getAverageRating() != null) {
                tvAverageRating.setText(
                        String.format(java.util.Locale.getDefault(), "%.1f", parkingLotDetail.getAverageRating()));
            } else {
                tvAverageRating.setText("0.0");
            }

            // Display total ratings
            if (parkingLotDetail.getTotalRatings() != null) {
                tvTotalRatings.setText("(" + parkingLotDetail.getTotalRatings() + ")");
            } else {
                tvTotalRatings.setText("(0)");
            }

            // Get only latest 3 ratings for preview
            java.util.List<com.parkmate.android.model.ParkingLotRating> latestRatings;
            if (parkingLotDetail.getRatings().size() > 3) {
                latestRatings = parkingLotDetail.getRatings().subList(0, 3);
            } else {
                latestRatings = parkingLotDetail.getRatings();
            }

            // Submit latest ratings to adapter
            ratingAdapter.submitList(latestRatings);

            // Setup "View More" button click listener
            btnViewAllRatings.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, AllRatingsActivity.class);
                intent.putExtra("parkingLotId", parkingLotId);
                intent.putExtra("parkingLotName", parkingLotDetail.getName());
                // Pass averageRating as double, but convert to string for intent
                double avgRating = parkingLotDetail.getAverageRating() != null ? parkingLotDetail.getAverageRating()
                        : 0.0;
                intent.putExtra("averageRating", avgRating);
                intent.putExtra("totalRatings",
                        parkingLotDetail.getTotalRatings() != null ? parkingLotDetail.getTotalRatings() : 0);
                startActivity(intent);
            });
        } else {
            // No ratings available
            cardRatingsSection.setVisibility(View.VISIBLE);
            tvNoRatings.setVisibility(View.VISIBLE);
            rvRatings.setVisibility(View.GONE);
            tvAverageRating.setText("0.0");
            tvTotalRatings.setText("(0)");
            btnViewAllRatings.setVisibility(View.GONE);
        }
    }

    private ParkingLotResponse.ParkingLot convertToParkingLot(ParkingLotDetailResponse.ParkingLotDetail detail) {
        ParkingLotResponse.ParkingLot lot = new ParkingLotResponse.ParkingLot();
        lot.setId(detail.getId());
        lot.setName(detail.getName());
        lot.setStreetAddress(detail.getStreetAddress());
        lot.setWard(detail.getWard());
        lot.setCity(detail.getCity());
        lot.setLatitude(detail.getLatitude());
        lot.setLongitude(detail.getLongitude());
        lot.setStatus(detail.getStatus());
        lot.setSubscriptions(detail.getSubscriptions());
        return lot;
    }

    /**
     * Setup Vehicle Type Tabs (Ô tô, Xe máy, Xe đạp)
     */
    private void setupVehicleTypeTabs() {
        if (fullParkingLotDetail == null)
            return;

        tabLayoutVehicleTypes.removeAllTabs();

        // Add tabs for each vehicle type that has data
        java.util.List<String> availableTypes = new ArrayList<>();

        // Check which vehicle types have subscription or pricing data
        if (fullParkingLotDetail.getSubscriptions() != null) {
            for (com.parkmate.android.model.SubscriptionPackage pkg : fullParkingLotDetail.getSubscriptions()) {
                if (pkg.isActive() && !availableTypes.contains(pkg.getVehicleType())) {
                    availableTypes.add(pkg.getVehicleType());
                }
            }
        }

        if (fullParkingLotDetail.getPricingRules() != null) {
            for (ParkingLotDetailResponse.PricingRule rule : fullParkingLotDetail.getPricingRules()) {
                if (rule.isActive() && !availableTypes.contains(rule.getVehicleType())) {
                    availableTypes.add(rule.getVehicleType());
                }
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
            filterDataByVehicleType(firstType);
        }

        // Listen for tab changes
        tabLayoutVehicleTypes.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String vehicleType = (String) tab.getTag();
                filterDataByVehicleType(vehicleType);
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
     * Filter subscription packages and pricing rules by vehicle type
     */
    private void filterDataByVehicleType(String vehicleType) {
        if (fullParkingLotDetail == null || vehicleType == null)
            return;

        // Filter subscription packages
        java.util.List<com.parkmate.android.model.SubscriptionPackage> filteredPackages = new ArrayList<>();
        if (fullParkingLotDetail.getSubscriptions() != null) {
            for (com.parkmate.android.model.SubscriptionPackage pkg : fullParkingLotDetail.getSubscriptions()) {
                if (pkg.isActive() && vehicleType.equals(pkg.getVehicleType())) {
                    filteredPackages.add(pkg);
                }
            }
        }

        // Update subscription adapter
        if (filteredPackages.isEmpty()) {
            rvSubscriptionPackages.setVisibility(View.GONE);
            tvNoSubscription.setVisibility(View.VISIBLE);
        } else {
            rvSubscriptionPackages.setVisibility(View.VISIBLE);
            tvNoSubscription.setVisibility(View.GONE);
            subscriptionPackageAdapter.submitList(filteredPackages);
        }

        // Filter pricing rules
        java.util.List<ParkingLotDetailResponse.PricingRule> filteredPricing = new ArrayList<>();
        if (fullParkingLotDetail.getPricingRules() != null) {
            for (ParkingLotDetailResponse.PricingRule rule : fullParkingLotDetail.getPricingRules()) {
                if (rule.isActive() && vehicleType.equals(rule.getVehicleType())) {
                    filteredPricing.add(rule);
                }
            }
        }

        // Update pricing adapter
        if (filteredPricing.isEmpty()) {
            rvPricingRules.setVisibility(View.GONE);
            tvNoPricing.setVisibility(View.VISIBLE);
        } else {
            rvPricingRules.setVisibility(View.VISIBLE);
            tvNoPricing.setVisibility(View.GONE);
            pricingRulesAdapter.submitList(filteredPricing);
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
            android.util.Log.e("ParkingLotDetail",
                    "ERROR: No coordinates - Lat: " + parkingLotLatitude + ", Lng: " + parkingLotLongitude);
            showError("Không có thông tin vị trí bãi xe");
            return;
        }

        android.util.Log.d("ParkingLotDetail",
                "Coordinates OK - Lat: " + parkingLotLatitude + ", Lng: " + parkingLotLongitude);

        // Kiểm tra permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.d("ParkingLotDetail", "Requesting location permission...");
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
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
