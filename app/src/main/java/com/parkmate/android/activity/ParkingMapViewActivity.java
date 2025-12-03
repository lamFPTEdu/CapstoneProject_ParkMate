package com.parkmate.android.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;
import com.parkmate.android.R;
import com.parkmate.android.model.ParkingArea;
import com.parkmate.android.model.ParkingFloor;
import com.parkmate.android.model.ParkingSpot;
import com.parkmate.android.model.response.AreaDetailResponse;
import com.parkmate.android.model.response.ParkingFloorDetailResponse;
import com.parkmate.android.model.response.ParkingLotDetailResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.view.EnhancedParkingMapView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Activity hiển thị bản đồ bãi xe (chỉ xem - không chọn)
 * Load Floor → Areas → Spots và vẽ map hoàn chỉnh
 */
public class ParkingMapViewActivity extends AppCompatActivity {
    private static final String TAG = "ParkingMapView";

    // Views
    private Toolbar toolbar;
    private TextView tvParkingLotName;
    private TextView tvParkingLotAddress;
    private EnhancedParkingMapView parkingMapView;
    private LinearLayout emptyStateLayout;
    private TabLayout tabLayoutFloors;
    private TabLayout tabLayoutVehicleType;
    private ProgressBar progressBar;
    private FrameLayout loadingOverlay;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    // Data
    private long parkingLotId;
    private List<ParkingLotDetailResponse.ParkingFloor> parkingFloors = new ArrayList<>();
    private String selectedVehicleType = "CAR_UP_TO_9_SEATS"; // Default
    private MapData currentMapData; // Cache map data để filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_map_view);

        initializeViews();
        setupToolbar();
        getIntentData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvParkingLotName = findViewById(R.id.tvParkingLotName);
        tvParkingLotAddress = findViewById(R.id.tvParkingLotAddress);
        parkingMapView = findViewById(R.id.parkingMapView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tabLayoutFloors = findViewById(R.id.tabLayoutFloors);
        tabLayoutVehicleType = findViewById(R.id.tabLayoutVehicleType);
        progressBar = findViewById(R.id.progressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Disable click cho map (chỉ xem)
        parkingMapView.setOnSpotClickListener(null);

        // Setup vehicle type tabs
        setupVehicleTypeTabs();

        // Setup floor tab selection listener
        tabLayoutFloors.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position < parkingFloors.size()) {
                    loadFloorMap(parkingFloors.get(position).getId());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Setup vehicle type tab selection listener
        tabLayoutVehicleType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        selectedVehicleType = "CAR_UP_TO_9_SEATS";
                        break;
                    case 1:
                        selectedVehicleType = "MOTORBIKE";
                        break;
                    case 2:
                        selectedVehicleType = "BIKE";
                        break;
                }
                // Re-display map với filter mới
                if (currentMapData != null) {
                    displayMapWithFilter(currentMapData);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Setup tabs cho loại xe
     */
    private void setupVehicleTypeTabs() {
        tabLayoutVehicleType.removeAllTabs();
        tabLayoutVehicleType.addTab(tabLayoutVehicleType.newTab().setText("Xe hơi"));
        tabLayoutVehicleType.addTab(tabLayoutVehicleType.newTab().setText("Xe máy"));
        tabLayoutVehicleType.addTab(tabLayoutVehicleType.newTab().setText("Xe đạp"));
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
        parkingLotId = getIntent().getLongExtra("PARKING_LOT_ID", -1);
        String parkingLotName = getIntent().getStringExtra("PARKING_LOT_NAME");
        String parkingLotAddress = getIntent().getStringExtra("PARKING_LOT_ADDRESS");

        if (parkingLotId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin bãi xe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display parking lot info
        if (parkingLotName != null) {
            tvParkingLotName.setText(parkingLotName);
        }
        if (parkingLotAddress != null) {
            tvParkingLotAddress.setText(parkingLotAddress);
        }

        // Load parking lot details để lấy danh sách floors
        loadParkingLotDetails();
    }

    /**
     * Load thông tin chi tiết parking lot để lấy danh sách floors
     */
    private void loadParkingLotDetails() {
        showLoading(true);

        compositeDisposable.add(
            ApiClient.getApiService().getParkingLotDetail(parkingLotId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        showLoading(false);
                        if (response != null && response.isSuccess() && response.getData() != null) {
                            parkingFloors = response.getData().getParkingFloors();
                            if (parkingFloors != null && !parkingFloors.isEmpty()) {
                                setupFloorTabs();
                                // Auto load first floor
                                loadFloorMap(parkingFloors.get(0).getId());
                            } else {
                                showEmptyState("Bãi xe này chưa có thông tin tầng");
                            }
                        } else {
                            showEmptyState("Không thể tải thông tin bãi xe");
                        }
                    },
                    throwable -> {
                        showLoading(false);
                        Log.e(TAG, "Error loading parking lot", throwable);
                        showEmptyState("Lỗi: " + throwable.getMessage());
                    }
                )
        );
    }

    /**
     * Setup tabs cho các tầng
     */
    private void setupFloorTabs() {
        tabLayoutFloors.removeAllTabs();
        for (ParkingLotDetailResponse.ParkingFloor floor : parkingFloors) {
            TabLayout.Tab tab = tabLayoutFloors.newTab();
            tab.setText(floor.getFloorName());
            tabLayoutFloors.addTab(tab);
        }
        tabLayoutFloors.setVisibility(View.VISIBLE);
    }

    /**
     * Load floor map (floor + areas + spots)
     */
    private void loadFloorMap(long floorId) {
        showLoading(true);

        compositeDisposable.add(
            ApiClient.getApiService().getParkingFloorDetail(floorId)
                .subscribeOn(Schedulers.io())
                .flatMap(floorResponse -> {
                    if (!floorResponse.isSuccess() || floorResponse.getData() == null) {
                        return Single.error(new Exception("Không thể tải thông tin tầng"));
                    }

                    ParkingFloorDetailResponse.FloorData floorData = floorResponse.getData();

                    // Convert to ParkingFloor model
                    ParkingFloor floor = new ParkingFloor();
                    floor.setId(floorData.getId());
                    floor.setFloorNumber(floorData.getFloorNumber());
                    floor.setFloorName(floorData.getFloorName());
                    floor.setFloorTopLeftX(floorData.getFloorTopLeftX());
                    floor.setFloorTopLeftY(floorData.getFloorTopLeftY());
                    floor.setFloorWidth(floorData.getFloorWidth());
                    floor.setFloorHeight(floorData.getFloorHeight());

                    List<ParkingFloorDetailResponse.Area> areasData = floorData.getAreas();
                    if (areasData == null || areasData.isEmpty()) {
                        return Single.error(new Exception("Không có khu vực nào"));
                    }

                    // Load tất cả areas và spots
                    List<Single<AreaDetailResponse>> areaRequests = new ArrayList<>();
                    for (ParkingFloorDetailResponse.Area areaData : areasData) {
                        areaRequests.add(ApiClient.getApiService().getAreaDetail(areaData.getId()));
                    }

                    return Single.zip(areaRequests, objects -> {
                        List<ParkingArea> areas = new ArrayList<>();
                        List<ParkingSpot> allSpots = new ArrayList<>();

                        for (Object obj : objects) {
                            AreaDetailResponse areaResponse = (AreaDetailResponse) obj;
                            if (areaResponse.isSuccess() && areaResponse.getData() != null) {
                                AreaDetailResponse.AreaData areaDetail = areaResponse.getData();

                                // Convert to ParkingArea
                                ParkingArea area = new ParkingArea();
                                area.setId(areaDetail.getId());
                                area.setName(areaDetail.getName());
                                area.setAreaTopLeftX(areaDetail.getAreaTopLeftX());
                                area.setAreaTopLeftY(areaDetail.getAreaTopLeftY());
                                area.setAreaWidth(areaDetail.getAreaWidth());
                                area.setAreaHeight(areaDetail.getAreaHeight());
                                area.setVehicleType(areaDetail.getVehicleType());
                                area.setAreaType(areaDetail.getAreaType());
                                areas.add(area);

                                // Convert spots
                                if (areaDetail.getSpots() != null) {
                                    for (AreaDetailResponse.Spot spotData : areaDetail.getSpots()) {
                                        ParkingSpot spot = new ParkingSpot();
                                        spot.setId(spotData.getId());
                                        spot.setName(spotData.getName());
                                        spot.setSpotTopLeftX(spotData.getSpotTopLeftX());
                                        spot.setSpotTopLeftY(spotData.getSpotTopLeftY());
                                        spot.setSpotWidth(spotData.getSpotWidth());
                                        spot.setSpotHeight(spotData.getSpotHeight());
                                        spot.setStatus(spotData.getStatus());
                                        spot.setAvailableForSubscription(
                                            "AVAILABLE".equals(spotData.getStatus())
                                        );
                                        // LƯU AREA ID để biết spot thuộc area nào
                                        spot.setAreaId(areaDetail.getId());
                                        allSpots.add(spot);
                                    }
                                }
                            }
                        }

                        return new MapData(floor, areas, allSpots);
                    });
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    mapData -> {
                        showLoading(false);
                        currentMapData = mapData; // Cache data
                        displayMapWithFilter(mapData);
                        Log.d(TAG, "Map loaded: Floor=" + mapData.floor.getFloorName() +
                                   ", Areas=" + mapData.areas.size() +
                                   ", Spots=" + mapData.spots.size());
                    },
                    throwable -> {
                        showLoading(false);
                        Log.e(TAG, "Error loading map", throwable);
                        Toast.makeText(this, "Lỗi tải bản đồ: " + throwable.getMessage(),
                                     Toast.LENGTH_SHORT).show();
                    }
                )
        );
    }

    /**
     * Hiển thị map với filter theo loại xe
     */
    private void displayMapWithFilter(MapData mapData) {
        // Filter areas và spots theo vehicle type
        List<ParkingArea> filteredAreas = new ArrayList<>();
        List<ParkingSpot> filteredSpots = new ArrayList<>();

        // Bước 1: Filter areas theo vehicle type
        for (ParkingArea area : mapData.areas) {
            if (selectedVehicleType.equals(area.getVehicleType())) {
                filteredAreas.add(area);
            }
        }

        // Bước 2: Filter spots theo areaId của filtered areas
        for (ParkingSpot spot : mapData.spots) {
            for (ParkingArea area : filteredAreas) {
                if (spot.getAreaId() != null && spot.getAreaId().equals(area.getId())) {
                    filteredSpots.add(spot);
                    break;
                }
            }
        }

        Log.d(TAG, "Filtered for " + selectedVehicleType +
                   ": Areas=" + filteredAreas.size() +
                   ", Spots=" + filteredSpots.size() +
                   ", Total areas=" + mapData.areas.size() +
                   ", Total spots=" + mapData.spots.size());

        if (filteredAreas.isEmpty()) {
            // Không có area nào cho loại xe này - KHÔNG ẨN MAP
            Toast.makeText(this, "Không có khu vực dành cho loại xe này", Toast.LENGTH_SHORT).show();
            // Vẫn hiển thị floor nhưng không có area/spot
            displayMap(mapData.floor, filteredAreas, filteredSpots);
        } else {
            displayMap(mapData.floor, filteredAreas, filteredSpots);
        }
    }

    /**
     * Hiển thị map lên view
     */
    private void displayMap(ParkingFloor floor, List<ParkingArea> areas, List<ParkingSpot> spots) {
        emptyStateLayout.setVisibility(View.GONE);
        parkingMapView.setVisibility(View.VISIBLE);
        parkingMapView.setMapData(floor, areas, spots);
    }

    /**
     * Hiển thị trạng thái empty
     */
    private void showEmptyState(String message) {
        emptyStateLayout.setVisibility(View.VISIBLE);
        parkingMapView.setVisibility(View.GONE);
        tabLayoutFloors.setVisibility(View.GONE);

        TextView emptyText = emptyStateLayout.findViewById(android.R.id.text1);
        if (emptyText != null) {
            emptyText.setText(message);
        }
    }

    /**
     * Show/hide loading overlay
     */
    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Helper class để chứa data của map
     */
    private static class MapData {
        final ParkingFloor floor;
        final List<ParkingArea> areas;
        final List<ParkingSpot> spots;

        MapData(ParkingFloor floor, List<ParkingArea> areas, List<ParkingSpot> spots) {
            this.floor = floor;
            this.areas = areas;
            this.spots = spots;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}

