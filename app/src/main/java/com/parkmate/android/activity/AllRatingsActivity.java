package com.parkmate.android.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.parkmate.android.R;
import com.parkmate.android.adapter.ParkingLotRatingAdapter;
import com.parkmate.android.model.ParkingLotRating;
import com.parkmate.android.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AllRatingsActivity extends AppCompatActivity {
    private static final String TAG = "AllRatings";

    private MaterialToolbar toolbar;
    private TextView tvParkingLotName;
    private TextView tvAverageRating;
    private TextView tvTotalRatings;
    private RecyclerView rvAllRatings;
    private TextView tvNoRatings;
    private View progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ParkingLotRatingAdapter ratingAdapter;
    private List<ParkingLotRating> ratingList = new ArrayList<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Long parkingLotId;
    private String parkingLotName;
    private Double averageRating;
    private Integer totalRatingsCount;

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int PAGE_SIZE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_ratings);

        // Get data from intent
        parkingLotId = getIntent().getLongExtra("parkingLotId", -1);
        parkingLotName = getIntent().getStringExtra("parkingLotName");
        averageRating = getIntent().getDoubleExtra("averageRating", 0.0);
        totalRatingsCount = getIntent().getIntExtra("totalRatings", 0);

        if (parkingLotId == -1) {
            showError("Invalid parking lot ID");
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        loadAllRatings();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvParkingLotName = findViewById(R.id.tvParkingLotName);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalRatings = findViewById(R.id.tvTotalRatings);
        rvAllRatings = findViewById(R.id.rvAllRatings);
        tvNoRatings = findViewById(R.id.tvNoRatings);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Set parking lot name
        if (parkingLotName != null) {
            tvParkingLotName.setText(parkingLotName);
        }

        // Set initial rating data from intent with 1 decimal place
        tvAverageRating.setText(String.format(java.util.Locale.getDefault(), "%.1f", averageRating));
        if (totalRatingsCount != null && totalRatingsCount > 0) {
            tvTotalRatings.setText(String.format(java.util.Locale.getDefault(), "Dựa trên %d đánh giá", totalRatingsCount));
        } else {
            tvTotalRatings.setText("Chưa có đánh giá");
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Tất cả đánh giá");
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        ratingAdapter = new ParkingLotRatingAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvAllRatings.setLayoutManager(layoutManager);
        rvAllRatings.setAdapter(ratingAdapter);

        // Add item spacing
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.rating_item_spacing);
        rvAllRatings.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, android.view.View view,
                                       RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = spacingInPixels;
            }
        });

        // Add infinite scroll listener
        rvAllRatings.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        loadMoreRatings();
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            ratingList.clear();
            loadAllRatings();
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }

    private void loadAllRatings() {
        if (isLoading) return;

        isLoading = true;

        // Only show full loading on first page
        if (currentPage == 0) {
            showLoading(true);
        }

        compositeDisposable.add(
                ApiClient.getApiService().getParkingLotRatings(
                        parkingLotId,
                        currentPage,
                        PAGE_SIZE,
                        "createdAt",
                        "DESC"
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    isLoading = false;
                                    showLoading(false);
                                    swipeRefreshLayout.setRefreshing(false);

                                    if (response != null && response.isSuccess() && response.getData() != null) {
                                        handleRatingsResponse(response.getData());
                                    } else {
                                        String errorMsg = response != null && response.getError() != null ? response.getError() : "Failed to load ratings";
                                        showError(errorMsg);
                                    }
                                },
                                throwable -> {
                                    isLoading = false;
                                    showLoading(false);
                                    swipeRefreshLayout.setRefreshing(false);
                                    Log.e(TAG, "Error loading ratings", throwable);
                                    showError("Không thể tải đánh giá. Vui lòng thử lại.");
                                }
                        )
        );
    }

    private void loadMoreRatings() {
        if (!isLoading && !isLastPage) {
            currentPage++;
            loadAllRatings();
        }
    }

    private void handleRatingsResponse(com.parkmate.android.model.response.ParkingLotRatingsResponse.RatingsData data) {
        if (data != null && data.getContent() != null) {
            // If first page, clear the list
            if (currentPage == 0) {
                ratingList.clear();
            }

            // Add new ratings
            ratingList.addAll(data.getContent());

            // Check if this is the last page
            isLastPage = data.getLast() != null && data.getLast();

            // Update total ratings from API response
            if (data.getTotalElements() != null) {
                tvTotalRatings.setText(String.format(java.util.Locale.getDefault(), "Dựa trên %d đánh giá", data.getTotalElements()));
            }

            // Update adapter
            ratingAdapter.submitList(new ArrayList<>(ratingList));

            // Show/hide empty state
            if (ratingList.isEmpty()) {
                rvAllRatings.setVisibility(View.GONE);
                tvNoRatings.setVisibility(View.VISIBLE);
                tvAverageRating.setText("0");
                tvTotalRatings.setText("Chưa có đánh giá");
            } else {
                rvAllRatings.setVisibility(View.VISIBLE);
                tvNoRatings.setVisibility(View.GONE);
            }
        } else {
            // No ratings
            if (currentPage == 0) {
                rvAllRatings.setVisibility(View.GONE);
                tvNoRatings.setVisibility(View.VISIBLE);
                tvAverageRating.setText("0");
                tvTotalRatings.setText("Chưa có đánh giá");
            }
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

