package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
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
 * ParkingLotsActivity - Hiển thị danh sách tất cả bãi đỗ xe
 * Search được xử lý trong SearchParkingActivity
 * Filter chips ở local để lọc nhanh
 */
public class ParkingLotsActivity extends BaseActivity {

    private RecyclerView rvParkingLots;
    private ParkingLotAdapter parkingLotAdapter;
    private ProgressBar progressBar;
    private View llEmptyState;
    private Chip chipAll, chipActive, chip24Hour;

    private ParkingRepository parkingRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<ParkingLotResponse.ParkingLot> parkingLots = new ArrayList<>();

    // Filter state - Mặc định chỉ hiển thị bãi xe ACTIVE
    private String currentStatus = "ACTIVE";
    private Boolean currentIs24Hour = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup toolbar with search that opens SearchParkingActivity
        setupToolbarWithSearch(
                true, // Main screen (show menu icon, not back button)
                "Tìm kiếm bãi đỗ xe...",
                v -> openSearchActivity(),
                null, // No filter button
                false // Hide navigation icon completely for clean look
        );

        initializeViews();
        setupRecyclerView();
        setupFilters();

        // Setup bottom navigation
        setupBottomNavigation(true, R.id.nav_parking);

        // Load notification badge count
        loadNotificationBadgeCount();

        parkingRepository = new ParkingRepository();
        loadParkingLots();
    }

    private void initializeViews() {
        rvParkingLots = findViewById(R.id.rvParkingLots);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);
        chipAll = findViewById(R.id.chipAll);
        chipActive = findViewById(R.id.chipActive);
        chip24Hour = findViewById(R.id.chip24Hour);
    }

    private void setupRecyclerView() {
        parkingLotAdapter = new ParkingLotAdapter(parkingLots, this::onParkingLotClick);
        rvParkingLots.setLayoutManager(new LinearLayoutManager(this));
        rvParkingLots.setAdapter(parkingLotAdapter);
    }

    private void setupFilters() {
        // Thiết lập chip Active được chọn sẵn
        chipActive.setChecked(true);
        chipAll.setChecked(false);

        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck other chips when "All" is selected
                chipActive.setChecked(false);
                chip24Hour.setChecked(false);
                currentStatus = null;
                currentIs24Hour = null;
                loadParkingLots();
            }
        });

        chipActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck "All" when selecting specific filter
                chipAll.setChecked(false);
                currentStatus = "ACTIVE";
            } else {
                // If unchecking and no other filter selected, show all
                if (!chip24Hour.isChecked()) {
                    chipAll.setChecked(true);
                    currentStatus = null;
                } else {
                    currentStatus = null;
                }
            }
            loadParkingLots();
        });

        chip24Hour.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck "All" when selecting specific filter
                chipAll.setChecked(false);
                currentIs24Hour = true;
            } else {
                // If unchecking and no other filter selected, show all
                if (!chipActive.isChecked()) {
                    chipAll.setChecked(true);
                    currentIs24Hour = null;
                } else {
                    currentIs24Hour = null;
                }
            }
            loadParkingLots();
        });
    }

    private void openSearchActivity() {
        Intent intent = new Intent(this, SearchParkingActivity.class);
        intent.putExtra(SearchParkingActivity.EXTRA_FROM_ACTIVITY, "ParkingLotsActivity");
        startActivity(intent);
    }

    private void loadParkingLots() {
        showLoading(true);

        compositeDisposable.add(
            parkingRepository.getParkingLots(
                null, // ownedByMe
                null, // name - load all (search in SearchParkingActivity)
                null, // city
                currentIs24Hour, // filter by 24hour chip
                currentStatus, // filter by status chip
                0, // page
                50, // size - load more for better UX
                "name", // sortBy
                "ASC" // sortOrder
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    showLoading(false);
                    if (response.isSuccess() && response.getData() != null) {
                        parkingLots.clear();
                        parkingLots.addAll(response.getData().getContent());
                        parkingLotAdapter.notifyDataSetChanged();

                        updateEmptyState();
                    } else {
                        showError("Không thể tải danh sách bãi xe");
                    }
                },
                throwable -> {
                    showLoading(false);
                    showError("Lỗi mạng: " + throwable.getMessage());
                }
            )
        );
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
    }

    private void updateEmptyState() {
        boolean isEmpty = parkingLots.isEmpty();
        llEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvParkingLots.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_parking_lots;
    }

    /**
     * Load số lượng notifications chưa đọc từ SharedPreferences
     */
    private void loadNotificationBadgeCount() {
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("parkmate_notifications", MODE_PRIVATE);
            int unreadCount = prefs.getInt("unread_count", 0);
            setupNotificationBadge(unreadCount);
        } catch (Exception e) {
            setupNotificationBadge(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notification badge khi user quay lại màn hình
        loadNotificationBadgeCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    protected void customizeToolbar() {
        // Hide navigation button for main screen
        if (ivNavigation != null) {
            ivNavigation.setVisibility(View.GONE);
        }
    }
}
