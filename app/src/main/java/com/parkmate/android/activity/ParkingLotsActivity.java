package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class ParkingLotsActivity extends BaseActivity {

    private RecyclerView rvParkingLots;
    private ParkingLotAdapter parkingLotAdapter;
    private EditText etSearch;
    private ImageButton btnFilter;
    private ProgressBar progressBar;
    private View llEmptyState;
    private Chip chipAll, chipActive, chip24Hour;

    private ParkingRepository parkingRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<ParkingLotResponse.ParkingLot> parkingLots = new ArrayList<>();

    // Filter variables
    private String currentStatus = null;
    private Boolean currentIs24Hour = null;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeViews();
        setupRecyclerView();
        setupSearch();
        setupFilters();

        // Setup bottom navigation
        setupBottomNavigation(true, R.id.nav_parking);

        parkingRepository = new ParkingRepository();
        loadParkingLots();
    }

    private void initializeViews() {
        rvParkingLots = findViewById(R.id.rvParkingLots);
        etSearch = findViewById(R.id.etSearch);
        btnFilter = findViewById(R.id.btnAdvancedFilter);
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

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                loadParkingLots();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnFilter.setOnClickListener(v -> {
            // TODO: Implement advanced filter dialog
            Toast.makeText(this, "Advanced filter coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFilters() {
        // Chips are handled individually, no ChipGroup needed

        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentStatus = null;
                currentIs24Hour = null;
                loadParkingLots();
            }
        });

        chipActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentStatus = "ACTIVE";
                currentIs24Hour = null;
                loadParkingLots();
            }
        });

        chip24Hour.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentStatus = null;
                currentIs24Hour = true;
                loadParkingLots();
            }
        });
    }

    private void setupToolbar() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadParkingLots() {
        showLoading(true);

        compositeDisposable.add(
            parkingRepository.getParkingLots(
                null, // ownedByMe
                currentSearchQuery.isEmpty() ? null : currentSearchQuery,
                null, // city
                currentIs24Hour,
                currentStatus,
                0, // page
                20, // size
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
                        showError("Failed to load parking lots");
                    }
                },
                throwable -> {
                    showLoading(false);
                    showError("Network error: " + throwable.getMessage());
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
