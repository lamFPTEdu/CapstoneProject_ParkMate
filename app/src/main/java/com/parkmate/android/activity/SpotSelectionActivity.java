package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.adapter.SpotAdapter;
import com.parkmate.android.model.response.ParkingFloorDetailResponse;
import com.parkmate.android.model.response.AreaDetailResponse;
import com.parkmate.android.repository.ParkingRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SpotSelectionActivity extends AppCompatActivity {

    private static final String TAG = "SpotSelectionActivity";

    private ImageView btnBack;
    private TextView tvAreaName;
    private TextView tvAvailableSpots;
    private TextView tvVehicleType;
    private TextView tvPricingInfo;
    private TextView tvSelectedSpot;
    private RecyclerView rvSpots;
    private MaterialButton btnBook;
    private ProgressBar progressBar;
    private android.widget.HorizontalScrollView hsvAreaSelector;

    private SpotAdapter spotAdapter;
    private ParkingRepository parkingRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Long floorId;
    private Long parkingLotId;
    private String floorName;
    private String parkingLotName;

    // Current selected area
    private Long currentAreaId;
    private String currentAreaName;
    private String currentVehicleType;

    // All areas in this floor
    private java.util.List<ParkingFloorDetailResponse.Area> allAreas;

    // Pricing information for current area
    private Integer initialCharge;
    private Integer stepRate;
    private Integer stepMinute;
    private Integer initialDurationMinute;
    private String pricingRuleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spot_selection);

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
            showError("ID tầng không hợp lệ");
            finish();
            return;
        }

        // Load floor detail with all areas
        loadFloorAreas();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.ivNavigation);
        tvAreaName = findViewById(R.id.tvAreaName);
        tvAvailableSpots = findViewById(R.id.tvAvailableSpots);
        tvVehicleType = findViewById(R.id.tvVehicleType);
        tvPricingInfo = findViewById(R.id.tvPricingInfo);
        tvSelectedSpot = findViewById(R.id.tvSelectedSpot);
        rvSpots = findViewById(R.id.rvSpots);
        btnBook = findViewById(R.id.btnBook);
        progressBar = findViewById(R.id.progressBar);
        hsvAreaSelector = findViewById(R.id.hsvAreaSelector);
    }

    private void setupRecyclerView() {
        spotAdapter = new SpotAdapter(new ArrayList<>(), this::onSpotClick);
        rvSpots.setLayoutManager(new GridLayoutManager(this, 4)); // 4 columns grid
        rvSpots.setAdapter(spotAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnBook.setOnClickListener(v -> {
            ParkingFloorDetailResponse.Spot selectedSpot = spotAdapter.getSelectedSpot();
            if (selectedSpot != null) {
                proceedToVehicleSelection(selectedSpot);
            }
        });
    }

    private void proceedToVehicleSelection(ParkingFloorDetailResponse.Spot selectedSpot) {
        Intent intent = new Intent(this, VehicleSelectionActivity.class);
        intent.putExtra("parking_lot_id", parkingLotId);
        intent.putExtra("floor_id", floorId);
        intent.putExtra("area_id", currentAreaId);
        intent.putExtra("spot_id", selectedSpot.getId());
        intent.putExtra("parking_lot_name", parkingLotName);
        intent.putExtra("floor_name", floorName);
        intent.putExtra("area_name", currentAreaName);
        intent.putExtra("spot_name", selectedSpot.getName());
        intent.putExtra("vehicle_type", currentVehicleType);

        // Pass pricing data to the next activity
        intent.putExtra("initial_charge", initialCharge);
        intent.putExtra("step_rate", stepRate);
        intent.putExtra("step_minute", stepMinute);
        intent.putExtra("initial_duration_minute", initialDurationMinute);
        intent.putExtra("pricing_rule_name", pricingRuleName);

        startActivity(intent);
    }

    private void loadAreaSpots() {
        showLoading(true);

        // Gọi API lấy chi tiết Area với danh sách spots
        compositeDisposable.add(
            parkingRepository.getAreaDetail(currentAreaId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        showLoading(false);
                        Log.d(TAG, "API Response success: " + response.isSuccess());

                        if (response.isSuccess() && response.getData() != null) {
                            AreaDetailResponse.AreaData areaData = response.getData();
                            Log.d(TAG, "Area name: " + areaData.getName());
                            Log.d(TAG, "Total spots: " + areaData.getTotalSpots());

                            // Chuyển đổi từ AreaDetailResponse.Spot sang ParkingFloorDetailResponse.Spot
                            List<ParkingFloorDetailResponse.Spot> spots = convertToSpotList(areaData.getSpots());

                            if (spots != null && !spots.isEmpty()) {
                                displayAreaSpots(spots, areaData.getTotalSpots());
                            } else {
                                showError("Không có chỗ đỗ nào");
                            }

                            // Extract pricing information from pricingRule
                            if (areaData.getPricingRule() != null) {
                                AreaDetailResponse.PricingRule pricingRule = areaData.getPricingRule();

                                // Check if there's an override pricing rule (e.g., weekend discount)
                                if (pricingRule.getOverridePricingRule() != null && pricingRule.getOverridePricingRule().getActive()) {
                                    AreaDetailResponse.OverridePricingRule overrideRule = pricingRule.getOverridePricingRule();
                                    initialCharge = overrideRule.getInitialCharge();
                                    stepRate = overrideRule.getStepRate();
                                    stepMinute = overrideRule.getStepMinute();
                                    initialDurationMinute = overrideRule.getInitialDurationMinute();
                                    pricingRuleName = overrideRule.getRuleName();
                                } else {
                                    // Use default pricing rule
                                    initialCharge = pricingRule.getInitialCharge();
                                    stepRate = pricingRule.getStepRate();
                                    stepMinute = pricingRule.getStepMinute();
                                    initialDurationMinute = pricingRule.getInitialDurationMinute();
                                    pricingRuleName = pricingRule.getRuleName();
                                }

                                // Display pricing information
                                displayPricingInfo();
                            }
                        } else {
                            showError("Không thể tải danh sách chỗ đỗ");
                        }
                    },
                    throwable -> {
                        showLoading(false);
                        Log.e(TAG, "Error loading area spots: " + throwable.getMessage(), throwable);
                        showError("Lỗi mạng: " + throwable.getMessage());
                    }
                )
        );
    }

    /**
     * Load tất cả areas của floor và tạo tabs
     */
    private void loadFloorAreas() {
        showLoading(true);

        compositeDisposable.add(
            parkingRepository.getParkingFloorDetail(floorId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        showLoading(false);
                        if (response.isSuccess() && response.getData() != null) {
                            ParkingFloorDetailResponse.FloorData floorData = response.getData();
                            allAreas = floorData.getAreas();

                            if (allAreas != null && !allAreas.isEmpty()) {
                                // Tạo tabs cho các areas
                                createAreaTabs();

                                // Chọn area đầu tiên mặc định
                                selectArea(allAreas.get(0));
                            } else {
                                showError("Không có khu vực nào khả dụng");
                                finish();
                            }
                        } else {
                            showError("Không thể tải thông tin tầng");
                            finish();
                        }
                    },
                    throwable -> {
                        showLoading(false);
                        Log.e(TAG, "Error loading floor: " + throwable.getMessage(), throwable);
                        showError("Lỗi mạng: " + throwable.getMessage());
                        finish();
                    }
                )
        );
    }

    /**
     * Tạo tabs động cho các areas
     */
    private void createAreaTabs() {
        // Get the LinearLayout container inside HorizontalScrollView
        android.widget.LinearLayout tabContainer = findViewById(R.id.llAreaTabsContainer);

        if (tabContainer == null) {
            Log.e(TAG, "Tab container not found!");
            return;
        }

        tabContainer.removeAllViews();

        for (int i = 0; i < allAreas.size(); i++) {
            ParkingFloorDetailResponse.Area area = allAreas.get(i);

            // Tạo tab button
            TextView tabButton = new TextView(this);
            tabButton.setText(area.getName());
            tabButton.setPadding(
                (int) (24 * getResources().getDisplayMetrics().density),
                (int) (12 * getResources().getDisplayMetrics().density),
                (int) (24 * getResources().getDisplayMetrics().density),
                (int) (12 * getResources().getDisplayMetrics().density)
            );
            tabButton.setTextSize(14);
            tabButton.setTypeface(null, android.graphics.Typeface.BOLD);

            // Set layout params with margin
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) {
                params.setMargins((int) (8 * getResources().getDisplayMetrics().density), 0, 0, 0);
            }
            tabButton.setLayoutParams(params);

            // Set tag để identify area
            tabButton.setTag(area.getId());

            // Click listener
            final int finalI = i;
            tabButton.setOnClickListener(v -> selectArea(allAreas.get(finalI)));

            tabContainer.addView(tabButton);
        }
    }

    /**
     * Chọn một area và load spots của nó
     */
    private void selectArea(ParkingFloorDetailResponse.Area area) {
        currentAreaId = area.getId();
        currentAreaName = area.getName();
        currentVehicleType = area.getVehicleType();

        // Update UI
        tvAreaName.setText(currentAreaName);
        tvVehicleType.setText(getVehicleTypeIcon(currentVehicleType) + " " + getVehicleTypeDisplayName(currentVehicleType));

        // Update tabs styling
        updateTabsSelection();

        // Clear current selection
        spotAdapter.setSelectedSpot(null);
        btnBook.setEnabled(false);
        btnBook.setText("Đặt chỗ");

        // Load spots for this area
        loadAreaSpots();
    }

    /**
     * Update visual state của tabs
     */
    private void updateTabsSelection() {
        android.widget.LinearLayout tabContainer = findViewById(R.id.llAreaTabsContainer);

        if (tabContainer == null) return;

        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            TextView tab = (TextView) tabContainer.getChildAt(i);
            Long areaId = (Long) tab.getTag();

            if (areaId != null && areaId.equals(currentAreaId)) {
                // Selected tab
                tab.setBackgroundResource(R.drawable.bg_area_tab_active);
                tab.setTextColor(0xFFFFFFFF);
            } else {
                // Unselected tab
                tab.setBackgroundResource(R.drawable.bg_area_tab_inactive);
                tab.setTextColor(0xFF2196F3);
            }
        }
    }

    /**
     * Chuyển đổi từ AreaDetailResponse.Spot sang ParkingFloorDetailResponse.Spot
     */
    private List<ParkingFloorDetailResponse.Spot> convertToSpotList(List<AreaDetailResponse.Spot> areaSpots) {
        if (areaSpots == null) return new ArrayList<>();

        List<ParkingFloorDetailResponse.Spot> spots = new ArrayList<>();
        for (AreaDetailResponse.Spot areaSpot : areaSpots) {
            ParkingFloorDetailResponse.Spot spot = new ParkingFloorDetailResponse.Spot();
            spot.setId(areaSpot.getId());
            spot.setName(areaSpot.getName());
            spot.setStatus(areaSpot.getStatus());
            spot.setBlockReason(areaSpot.getBlockReason());
            spot.setCreatedAt(areaSpot.getCreatedAt());
            spot.setUpdatedAt(areaSpot.getUpdatedAt());
            spots.add(spot);
        }
        return spots;
    }

    private void displayAreaSpots(List<ParkingFloorDetailResponse.Spot> spots, int totalSpots) {
        Log.d(TAG, "Displaying " + spots.size() + " spots out of " + totalSpots + " total");
        spotAdapter.updateData(spots);

        // Calculate available spots
        int availableCount = 0;
        for (ParkingFloorDetailResponse.Spot spot : spots) {
            if ("AVAILABLE".equals(spot.getStatus())) {
                availableCount++;
            }
        }
        tvAvailableSpots.setText(availableCount + " / " + totalSpots + " chỗ trống");

        Log.d(TAG, "Available spots: " + availableCount);
    }

    private void onSpotClick(ParkingFloorDetailResponse.Spot spot) {
        spotAdapter.setSelectedSpot(spot);
        tvSelectedSpot.setText("Đã chọn: " + spot.getName());
        btnBook.setText("Đặt chỗ (" + spot.getName() + ")");
        btnBook.setEnabled(true);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            rvSpots.setVisibility(View.GONE);
        } else {
            rvSpots.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getVehicleTypeDisplayName(String vehicleType) {
        switch (vehicleType) {
            case "CAR_UP_TO_9_SEATS": return "Ô tô (dưới 9 chỗ)";
            case "MOTORBIKE": return "Xe máy";
            case "BIKE": return "Xe đạp";
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

    private void displayPricingInfo() {
        StringBuilder pricingInfo = new StringBuilder("💰 ");

        // Format with thousand separator
        String formattedInitialCharge = String.format("%,d", initialCharge).replace(",", ".");
        pricingInfo.append(formattedInitialCharge).append("₫");

        if (initialDurationMinute != null && initialDurationMinute > 0) {
            if (initialDurationMinute >= 60) {
                pricingInfo.append(" giờ đầu");
            } else {
                pricingInfo.append(" ").append(initialDurationMinute).append(" phút đầu");
            }
        }

        if (stepRate != null && stepMinute != null && stepRate > 0) {
            String formattedStepRate = String.format("%,d", stepRate).replace(",", ".");
            pricingInfo.append(", sau đó ").append(formattedStepRate).append("₫");

            if (stepMinute >= 60) {
                pricingInfo.append("/giờ");
            } else {
                pricingInfo.append("/").append(stepMinute).append(" phút");
            }
        }

        tvPricingInfo.setText(pricingInfo.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
