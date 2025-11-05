package com.parkmate.android.activity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parkmate.android.R;
import com.parkmate.android.adapter.ParkingLotAdapter;
import com.parkmate.android.model.response.ParkingLotResponse;
import com.parkmate.android.repository.ParkingRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * SearchParkingActivity - Màn hình tìm kiếm bãi đỗ xe chung
 * Dùng cho cả HomeActivity và ParkingLotsActivity
 */
public class SearchParkingActivity extends AppCompatActivity {

    private static final String TAG = "SearchParkingActivity";

    // Extra keys
    public static final String EXTRA_SHOW_MAP_OPTION = "show_map_option";
    public static final String EXTRA_FROM_ACTIVITY = "from_activity";

    // Views
    private ImageView btnBack;
    private EditText etSearch;
    private ImageView btnFilter;
    private ChipGroup chipGroup;
    private Chip chipAll, chipActive, chip24Hour, chipNearby;
    private RecyclerView rvParkingLots;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;

    // Data
    private ParkingLotAdapter parkingLotAdapter;
    private ParkingRepository parkingRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<ParkingLotResponse.ParkingLot> allParkingLots = new ArrayList<>();
    private List<ParkingLotResponse.ParkingLot> filteredParkingLots = new ArrayList<>();

    // Filter state
    private String currentSearchQuery = "";
    private String currentStatus = null;
    private Boolean currentIs24Hour = null;
    private boolean showNearbyOnly = false;

    // User location (if available)
    private Location userLocation;
    private static final double NEARBY_RADIUS_KM = 5.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_parking);

        initializeViews();
        setupSearch();
        setupFilters();
        setupRecyclerView();

        parkingRepository = new ParkingRepository();

        // Auto focus vào search
        etSearch.requestFocus();

        // Load tất cả bãi xe
        loadAllParkingLots();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        btnFilter = findViewById(R.id.btnFilter);
        chipGroup = findViewById(R.id.chipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipActive = findViewById(R.id.chipActive);
        chip24Hour = findViewById(R.id.chip24Hour);
        chipNearby = findViewById(R.id.chipNearby);
        rvParkingLots = findViewById(R.id.rvParkingLots);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);

        btnBack.setOnClickListener(v -> finish());

        btnFilter.setOnClickListener(v -> {
            // TODO: Show advanced filter dialog
            Toast.makeText(this, "Bộ lọc nâng cao đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                filterParkingLots();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard
                v.clearFocus();
                return true;
            }
            return false;
        });
    }

    private void setupFilters() {
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck all other chips when "All" is selected
                chipActive.setChecked(false);
                chip24Hour.setChecked(false);
                chipNearby.setChecked(false);
                currentStatus = null;
                currentIs24Hour = null;
                showNearbyOnly = false;
                filterParkingLots();
            }
        });

        chipActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck "All" when selecting specific filter
                chipAll.setChecked(false);
                currentStatus = "ACTIVE";
            } else {
                // If unchecking and no other filter selected, show all
                if (!chip24Hour.isChecked() && !chipNearby.isChecked()) {
                    chipAll.setChecked(true);
                }
                currentStatus = null;
            }
            filterParkingLots();
        });

        chip24Hour.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck "All" when selecting specific filter
                chipAll.setChecked(false);
                currentIs24Hour = true;
            } else {
                // If unchecking and no other filter selected, show all
                if (!chipActive.isChecked() && !chipNearby.isChecked()) {
                    chipAll.setChecked(true);
                }
                currentIs24Hour = null;
            }
            filterParkingLots();
        });

        chipNearby.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck "All" when selecting specific filter
                chipAll.setChecked(false);
                showNearbyOnly = true;
            } else {
                // If unchecking and no other filter selected, show all
                if (!chipActive.isChecked() && !chip24Hour.isChecked()) {
                    chipAll.setChecked(true);
                }
                showNearbyOnly = false;
            }
            filterParkingLots();
        });
    }

    private void setupRecyclerView() {
        parkingLotAdapter = new ParkingLotAdapter(filteredParkingLots, this::onParkingLotClick);
        rvParkingLots.setLayoutManager(new LinearLayoutManager(this));
        rvParkingLots.setAdapter(parkingLotAdapter);
    }

    private void loadAllParkingLots() {
        showLoading(true);

        compositeDisposable.add(
            parkingRepository.getParkingLots(
                null, // ownedByMe
                null, // name (load all)
                null, // city
                null, // is24Hour
                "ACTIVE", // status (chỉ lấy active)
                0, // page
                100, // size (lấy nhiều để search local)
                "name", // sortBy
                "ASC" // sortOrder
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    showLoading(false);
                    if (response.isSuccess() && response.getData() != null) {
                        allParkingLots.clear();
                        allParkingLots.addAll(response.getData().getContent());
                        Log.d(TAG, "Loaded " + allParkingLots.size() + " parking lots");

                        // Apply current filters
                        filterParkingLots();
                    } else {
                        showError("Không thể tải danh sách bãi xe");
                    }
                },
                throwable -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading parking lots", throwable);
                    showError("Lỗi mạng: " + throwable.getMessage());
                }
            )
        );
    }

    private void filterParkingLots() {
        filteredParkingLots.clear();

        for (ParkingLotResponse.ParkingLot lot : allParkingLots) {
            boolean matches = true;

            // Filter by search query
            if (!currentSearchQuery.isEmpty()) {
                String query = currentSearchQuery.toLowerCase();
                String name = lot.getName() != null ? lot.getName().toLowerCase() : "";
                String address = lot.getFullAddress() != null ? lot.getFullAddress().toLowerCase() : "";

                if (!name.contains(query) && !address.contains(query)) {
                    matches = false;
                }
            }

            // Filter by status
            if (matches && currentStatus != null) {
                if (!currentStatus.equals(lot.getStatus())) {
                    matches = false;
                }
            }

            // Filter by 24h
            if (matches && currentIs24Hour != null) {
                if (lot.getIs24Hour() == null || !lot.getIs24Hour().equals(currentIs24Hour)) {
                    matches = false;
                }
            }

            // Filter by nearby
            if (matches && showNearbyOnly) {
                if (userLocation != null && lot.getLatitude() != null && lot.getLongitude() != null) {
                    double distance = calculateDistance(
                        userLocation.getLatitude(),
                        userLocation.getLongitude(),
                        lot.getLatitude(),
                        lot.getLongitude()
                    );
                    if (distance > NEARBY_RADIUS_KM) {
                        matches = false;
                    }
                } else {
                    // Không có location, bỏ qua filter này
                    matches = true;
                }
            }

            if (matches) {
                filteredParkingLots.add(lot);
            }
        }

        // Sort by distance if showing nearby
        if (showNearbyOnly && userLocation != null) {
            filteredParkingLots.sort((lot1, lot2) -> {
                double dist1 = calculateDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    lot1.getLatitude(), lot1.getLongitude()
                );
                double dist2 = calculateDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    lot2.getLatitude(), lot2.getLongitude()
                );
                return Double.compare(dist1, dist2);
            });
        }

        parkingLotAdapter.notifyDataSetChanged();
        updateEmptyState();

        Log.d(TAG, "Filtered: " + filteredParkingLots.size() + " / " + allParkingLots.size() + " lots");
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private void onParkingLotClick(ParkingLotResponse.ParkingLot parkingLot) {
        Intent intent = new Intent(this, ParkingLotDetailActivity.class);
        intent.putExtra("parking_lot_id", parkingLot.getId());
        intent.putExtra("parking_lot_name", parkingLot.getName());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvParkingLots.setVisibility(show ? View.GONE : View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredParkingLots.isEmpty();
        llEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvParkingLots.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Set user location để filter "Gần tôi"
     */
    public void setUserLocation(Location location) {
        this.userLocation = location;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}

