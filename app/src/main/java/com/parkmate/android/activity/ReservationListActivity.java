package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.parkmate.android.R;
import com.parkmate.android.adapter.ReservationAdapter;
import com.parkmate.android.model.Reservation;
import com.parkmate.android.repository.ReservationRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Activity hiển thị danh sách reservation của user
 */
public class ReservationListActivity extends AppCompatActivity {
    private static final String TAG = "ReservationList";

    // Views
    private MaterialToolbar toolbar;
    private RecyclerView rvReservations;
    private ProgressBar progressBar;
    private LinearLayout tvEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Adapter
    private ReservationAdapter adapter;
    private List<Reservation> reservationList = new ArrayList<>();

    // Repository
    private ReservationRepository reservationRepository;
    private CompositeDisposable compositeDisposable;

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int PAGE_SIZE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        reservationRepository = new ReservationRepository();
        compositeDisposable = new CompositeDisposable();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        loadReservations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvReservations = findViewById(R.id.rvReservations);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ReservationAdapter(this, reservationList, reservation -> {
            // Click vào item -> xem chi tiết
            Intent intent = new Intent(this, ReservationDetailActivity.class);
            intent.putExtra("reservation", reservation);
            startActivity(intent);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvReservations.setLayoutManager(layoutManager);
        rvReservations.setAdapter(adapter);

        // Add infinite scroll listener
        rvReservations.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        loadMoreReservations();
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            reservationList.clear();
            loadReservations();
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }

    private void loadReservations() {
        if (isLoading) return;

        isLoading = true;
        showLoading(true);

        compositeDisposable.add(
            reservationRepository.getMyReservations(currentPage, PAGE_SIZE)
                .subscribe(
                    response -> {
                        isLoading = false;
                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);

                        if (response.isSuccess() && response.getData() != null) {
                            // Lấy content từ PageResponse
                            if (response.getData().getContent() != null) {
                                List<Reservation> newReservations = response.getData().getContent();

                                if (currentPage == 0) {
                                    reservationList.clear();
                                }

                                reservationList.addAll(newReservations);

                                // Check if this is the last page
                                isLastPage = response.getData().isLast();


                                adapter.notifyDataSetChanged();

                                // Hiển thị empty state nếu không có data
                                if (reservationList.isEmpty()) {
                                    tvEmptyState.setVisibility(View.VISIBLE);
                                    rvReservations.setVisibility(View.GONE);
                                } else {
                                    tvEmptyState.setVisibility(View.GONE);
                                    rvReservations.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    },
                    error -> {
                        isLoading = false;
                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);
                        Log.e(TAG, "Lỗi tải danh sách: " + error.getMessage(), error);
                        Toast.makeText(this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                )
        );
    }

    private void loadMoreReservations() {
        if (!isLoading && !isLastPage) {
            currentPage++;
            loadReservations();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload khi quay lại màn hình
        currentPage = 0;
        isLastPage = false;
        reservationList.clear();
        loadReservations();
    }
}
