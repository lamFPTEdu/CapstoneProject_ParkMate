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
import com.parkmate.android.dialog.RatingDialog;
import com.parkmate.android.model.Reservation;
import com.parkmate.android.model.request.CreateRatingRequest;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.repository.ReservationRepository;
import com.parkmate.android.utils.UserManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
        adapter = new ReservationAdapter(this, reservationList, new ReservationAdapter.OnReservationClickListener() {
            @Override
            public void onReservationClick(Reservation reservation) {
                // Click vào item -> xem chi tiết
                Intent intent = new Intent(ReservationListActivity.this, ReservationDetailActivity.class);
                intent.putExtra("reservation", reservation);
                startActivity(intent);
            }

            @Override
            public void onRateClick(Reservation reservation) {
                // Click vào button rating -> show rating dialog
                showRatingDialog(reservation);
            }

            @Override
            public void onCancelClick(Reservation reservation) {
                // Click vào button cancel -> show confirmation dialog
                showCancelConfirmDialog(reservation);
            }
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
        if (isLoading)
            return;

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
                                }));
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

    private void showCancelConfirmDialog(Reservation reservation) {
        com.parkmate.android.model.RefundPolicy policy = reservation.getRefundPolicy();
        int refundMinutes = policy != null ? policy.getRefundWindowMinutes() : 30;

        String parkingLotName = reservation.getParkingLotName() != null
                ? reservation.getParkingLotName()
                : "Bãi đỗ xe";

        String timeInfo = reservation.getReservedFrom() != null
                ? reservation.getReservedFrom()
                : "";

        int fee = reservation.getInitialFee() != null ? reservation.getInitialFee() : 0;

        // Create custom dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cancel_reservation, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Bind data to views
        android.widget.TextView tvParkingLotName = dialogView.findViewById(R.id.tvParkingLotName);
        android.widget.TextView tvTimeInfo = dialogView.findViewById(R.id.tvTimeInfo);
        android.widget.TextView tvDepositFee = dialogView.findViewById(R.id.tvDepositFee);
        android.widget.TextView tvRefundNote = dialogView.findViewById(R.id.tvRefundNote);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        com.google.android.material.button.MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        tvParkingLotName.setText(parkingLotName);
        tvTimeInfo.setText(timeInfo);
        tvDepositFee.setText(String.format("%,dđ", fee));
        tvRefundNote.setText(String.format("Hủy trước %d phút để được hoàn tiền cọc", refundMinutes));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            cancelReservation(reservation);
        });

        dialog.show();
    }

    private void cancelReservation(Reservation reservation) {
        progressBar.setVisibility(View.VISIBLE);

        compositeDisposable.add(
                reservationRepository.cancelReservation(reservation.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (response.isSuccess()) {
                                        Toast.makeText(this, "✅ Đã hủy đặt chỗ thành công!", Toast.LENGTH_SHORT).show();
                                        // Reload danh sách
                                        currentPage = 0;
                                        isLastPage = false;
                                        reservationList.clear();
                                        loadReservations();
                                    } else {
                                        String errorMsg = response.getMessage() != null
                                                ? response.getMessage()
                                                : "Không thể hủy đặt chỗ";
                                        Toast.makeText(this, "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                },
                                error -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e(TAG, "Error cancelling reservation", error);

                                    String errorMsg = "Không thể hủy đặt chỗ";
                                    if (error.getMessage() != null) {
                                        if (error.getMessage().contains("404")) {
                                            errorMsg = "Không tìm thấy đặt chỗ này";
                                        } else if (error.getMessage().contains("403")) {
                                            errorMsg = "Bạn không có quyền hủy đặt chỗ này";
                                        } else if (error.getMessage().contains("400")) {
                                            errorMsg = "Không thể hủy đặt chỗ đã hoàn thành hoặc đã hủy";
                                        } else if (error.getMessage().contains("409")) {
                                            errorMsg = "Đặt chỗ đã quá hạn để hủy hoặc đang được xử lý";
                                        }
                                    }
                                    Toast.makeText(this, "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                                }));
    }

    private void showRatingDialog(Reservation reservation) {
        String parkingLotName = reservation.getParkingLotName() != null
                ? reservation.getParkingLotName()
                : "Bãi đỗ xe";

        RatingDialog dialog = new RatingDialog(this, parkingLotName, (rating, title, comment) -> {
            // Submit rating to API
            submitRating(reservation, rating, title, comment);
        });
        dialog.show();
    }

    private void submitRating(Reservation reservation, int rating, String title, String comment) {
        // Get user ID from UserManager
        String userIdStr = UserManager.getInstance().getUserId();
        if (userIdStr == null || userIdStr.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "ID người dùng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get parking lot ID
        String parkingLotIdStr = reservation.getParkingLotId();
        if (parkingLotIdStr == null || parkingLotIdStr.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin bãi đỗ xe", Toast.LENGTH_SHORT).show();
            return;
        }

        long parkingLotId;
        try {
            parkingLotId = Long.parseLong(parkingLotIdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "ID bãi đỗ xe không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create rating request
        CreateRatingRequest request = new CreateRatingRequest(userId, rating, title, comment);

        // Show loading
        progressBar.setVisibility(View.VISIBLE);

        // Call API
        compositeDisposable.add(
                ApiClient.getApiService().createRating(parkingLotId, request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (response.isSuccess()) {
                                        Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String errorMsg = response.getMessage() != null
                                                ? response.getMessage()
                                                : "Không thể gửi đánh giá";
                                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                },
                                error -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e(TAG, "Error submitting rating: " + error.getMessage());

                                    String errorMsg = "Không thể gửi đánh giá";
                                    if (error.getMessage() != null) {
                                        if (error.getMessage().contains("409")) {
                                            errorMsg = "Bạn đã đánh giá bãi đỗ xe này rồi";
                                        } else if (error.getMessage().contains("404")) {
                                            errorMsg = "Không tìm thấy bãi đỗ xe";
                                        }
                                    }
                                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                                }));
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
