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

        rvReservations.setLayoutManager(new LinearLayoutManager(this));
        rvReservations.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadReservations);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }

    private void loadReservations() {
        showLoading(true);

        compositeDisposable.add(
            reservationRepository.getMyReservations()
                .subscribe(
                    response -> {
                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);

                        if (response.isSuccess() && response.getData() != null) {
                            reservationList.clear();

                            // Lấy content từ PageResponse
                            if (response.getData().getContent() != null) {
                                reservationList.addAll(response.getData().getContent());
                            }

                            adapter.notifyDataSetChanged();

                            // Hiển thị empty state nếu không có data
                            if (reservationList.isEmpty()) {
                                tvEmptyState.setVisibility(View.VISIBLE);
                                rvReservations.setVisibility(View.GONE);
                            } else {
                                tvEmptyState.setVisibility(View.GONE);
                                rvReservations.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(this, "Không thể tải danh sách đặt chỗ", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);
                        Log.e(TAG, "Lỗi tải danh sách: " + error.getMessage(), error);
                        Toast.makeText(this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                )
        );
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
        loadReservations();
    }
}
