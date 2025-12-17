package com.parkmate.android.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.parkmate.android.R;
import com.parkmate.android.adapter.ParkingSessionAdapter;
import com.parkmate.android.model.ParkingSession;
import com.parkmate.android.repository.ParkingSessionRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Activity hiển thị lịch sử đỗ xe với filter và infinite scroll
 */
public class ParkingHistoryActivity extends AppCompatActivity {
    private static final String TAG = "ParkingHistory";

    // Views
    private MaterialToolbar toolbar;
    private RecyclerView rvParkingSessions;
    private ProgressBar progressBar;
    private LinearLayout tvEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton btnFilter;

    // Filter Bottom Sheet
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheetFilter;
    private ImageButton btnCloseFilter;
    private Button btnResetFilter;
    private Button btnApplyFilter;
    private ChipGroup chipGroupStatus;
    private ChipGroup chipGroupReferenceType;
    private ChipGroup chipGroupDuration;
    private ChipGroup chipGroupAmount;

    // Adapter
    private ParkingSessionAdapter adapter;
    private List<ParkingSession> sessionList = new ArrayList<>();

    // Repository
    private ParkingSessionRepository repository;
    private CompositeDisposable compositeDisposable;

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int PAGE_SIZE = 10;

    // Filter params
    private String filterStatus = null;
    private String filterReferenceType = null;
    private Integer filterDurationMin = null;
    private Integer filterDurationMax = null;
    private Long filterAmountMin = null;
    private Long filterAmountMax = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new ParkingSessionRepository();
        compositeDisposable = new CompositeDisposable();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        setupBottomSheet();
        setupFilterListeners();
        loadSessions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvParkingSessions = findViewById(R.id.rvParkingSessions);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        btnFilter = findViewById(R.id.btnFilter);

        // Bottom sheet views
        bottomSheetFilter = findViewById(R.id.bottomSheetFilter);
        btnCloseFilter = findViewById(R.id.btnCloseFilter);
        btnResetFilter = findViewById(R.id.btnResetFilter);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        chipGroupReferenceType = findViewById(R.id.chipGroupReferenceType);
        chipGroupDuration = findViewById(R.id.chipGroupDuration);
        chipGroupAmount = findViewById(R.id.chipGroupAmount);
    }

    private void setupToolbar() {
        // Don't use setSupportActionBar - let MaterialToolbar manage its own menu from
        // XML
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup menu item click for filter
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_filter) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new ParkingSessionAdapter(this, sessionList, session -> {
            // TODO: Navigate to session detail if needed
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvParkingSessions.setLayoutManager(layoutManager);
        rvParkingSessions.setAdapter(adapter);

        // Add infinite scroll listener
        rvParkingSessions.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadMoreSessions();
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            adapter.clearSessions();
            loadSessions();
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetFilter);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        btnCloseFilter.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
    }

    private void setupFilterListeners() {
        btnResetFilter.setOnClickListener(v -> {
            resetFilters();
        });

        btnApplyFilter.setOnClickListener(v -> {
            applyFilters();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
    }

    private void resetFilters() {
        // Reset chip selections
        chipGroupStatus.check(R.id.chipAllStatus);
        chipGroupReferenceType.check(R.id.chipAllTypes);
        chipGroupDuration.check(R.id.chipAllDuration);
        chipGroupAmount.check(R.id.chipAllAmount);

        // Clear filter params
        filterStatus = null;
        filterReferenceType = null;
        filterDurationMin = null;
        filterDurationMax = null;
        filterAmountMin = null;
        filterAmountMax = null;

        // Reload data
        currentPage = 0;
        isLastPage = false;
        adapter.clearSessions();
        loadSessions();
    }

    private void applyFilters() {
        // Get selected status
        int selectedStatusId = chipGroupStatus.getCheckedChipId();
        if (selectedStatusId == R.id.chipActive) {
            filterStatus = "ACTIVE";
        } else if (selectedStatusId == R.id.chipCompleted) {
            filterStatus = "COMPLETED";
        } else {
            filterStatus = null;
        }

        // Get selected reference type
        int selectedTypeId = chipGroupReferenceType.getCheckedChipId();
        if (selectedTypeId == R.id.chipWalkIn) {
            filterReferenceType = "WALK_IN";
        } else if (selectedTypeId == R.id.chipReservation) {
            filterReferenceType = "RESERVATION";
        } else if (selectedTypeId == R.id.chipSubscription) {
            filterReferenceType = "SUBSCRIPTION";
        } else {
            filterReferenceType = null;
        }

        // Get selected duration
        int selectedDurationId = chipGroupDuration.getCheckedChipId();
        if (selectedDurationId == R.id.chipUnder1Hour) {
            filterDurationMin = null;
            filterDurationMax = 60;
        } else if (selectedDurationId == R.id.chip1To2Hours) {
            filterDurationMin = 60;
            filterDurationMax = 120;
        } else if (selectedDurationId == R.id.chipOver2Hours) {
            filterDurationMin = 120;
            filterDurationMax = null;
        } else {
            filterDurationMin = null;
            filterDurationMax = null;
        }

        // Get selected amount
        int selectedAmountId = chipGroupAmount.getCheckedChipId();
        if (selectedAmountId == R.id.chipUnder50K) {
            filterAmountMin = null;
            filterAmountMax = 50000L;
        } else if (selectedAmountId == R.id.chip50KTo100K) {
            filterAmountMin = 50000L;
            filterAmountMax = 100000L;
        } else if (selectedAmountId == R.id.chipOver100K) {
            filterAmountMin = 100000L;
            filterAmountMax = null;
        } else {
            filterAmountMin = null;
            filterAmountMax = null;
        }

        // Reload data with filters
        currentPage = 0;
        isLastPage = false;
        adapter.clearSessions();
        loadSessions();
    }

    private void loadSessions() {
        if (isLoading)
            return;

        isLoading = true;
        showLoading(true);

        compositeDisposable.add(
                repository.getSessionsWithFilters(
                        currentPage,
                        PAGE_SIZE,
                        "entryTime",
                        "DESC",
                        filterStatus,
                        filterReferenceType,
                        null, // startTime
                        null, // endTime
                        filterAmountMax,
                        filterAmountMin,
                        filterDurationMin,
                        filterDurationMax).subscribe(
                                response -> {
                                    isLoading = false;
                                    showLoading(false);
                                    swipeRefreshLayout.setRefreshing(false);

                                    if (response.isSuccess() && response.getData() != null) {
                                        List<ParkingSession> newSessions = response.getData().getContent();

                                        if (newSessions != null && !newSessions.isEmpty()) {
                                            adapter.addSessions(newSessions);
                                            currentPage++;

                                            // Check if last page
                                            isLastPage = response.getData().isLast();
                                        }

                                        // Show empty state if no data
                                        if (adapter.getItemCount() == 0) {
                                            tvEmptyState.setVisibility(View.VISIBLE);
                                        } else {
                                            tvEmptyState.setVisibility(View.GONE);
                                        }
                                    }
                                },
                                error -> {
                                    isLoading = false;
                                    showLoading(false);
                                    swipeRefreshLayout.setRefreshing(false);
                                    Log.e(TAG, "Error loading sessions", error);
                                    Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT)
                                            .show();

                                    if (adapter.getItemCount() == 0) {
                                        tvEmptyState.setVisibility(View.VISIBLE);
                                    }
                                }));
    }

    private void loadMoreSessions() {
        loadSessions();
    }

    private void showLoading(boolean show) {
        if (currentPage == 0) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
