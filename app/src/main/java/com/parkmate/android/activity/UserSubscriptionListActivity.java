package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.parkmate.android.R;
import com.parkmate.android.adapter.UserSubscriptionAdapter;
import com.parkmate.android.dialog.RatingDialog;
import com.parkmate.android.dialog.RenewalConfirmDialog;
import com.parkmate.android.model.UserSubscription;
import com.parkmate.android.model.request.CreateRatingRequest;
import com.parkmate.android.model.response.UserSubscriptionResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.utils.UserManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserSubscriptionListActivity extends AppCompatActivity {

    private static final String TAG = "UserSubscriptionList";
    private static final int PAGE_SIZE = 10;

    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvSubscriptions;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;

    private UserSubscriptionAdapter adapter;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_subscription_list);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadSubscriptions(currentPage);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        rvSubscriptions = findViewById(R.id.rvSubscriptions);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            adapter.clearSubscriptions();
            loadSubscriptions(currentPage);
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vé tháng của tôi");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new UserSubscriptionAdapter(new UserSubscriptionAdapter.OnSubscriptionClickListener() {
            @Override
            public void onSubscriptionClick(UserSubscription subscription) {
                // Navigate to subscription detail/success screen to show QR code
                Intent intent = new Intent(UserSubscriptionListActivity.this, SubscriptionSuccessActivity.class);
                intent.putExtra("SUBSCRIPTION", subscription);
                startActivity(intent);
            }

            @Override
            public void onRateClick(UserSubscription subscription) {
                // Click vào button rating -> show rating dialog
                showRatingDialog(subscription);
            }

            @Override
            public void onRenewClick(UserSubscription subscription) {
                // Click vào button gia hạn -> show renewal confirmation dialog
                showRenewalConfirmDialog(subscription);
            }

            @Override
            public void onCancelClick(UserSubscription subscription) {
                // Click vào button hủy đăng ký -> show cancel confirmation dialog
                showCancelConfirmDialog(subscription);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvSubscriptions.setLayoutManager(layoutManager);
        rvSubscriptions.setAdapter(adapter);

        // Infinite scroll
        rvSubscriptions.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadSubscriptions(currentPage + 1);
                    }
                }
            }
        });
    }

    private void loadSubscriptions(int page) {
        if (isLoading)
            return;

        isLoading = true;
        showLoading(page == 0);

        Log.d(TAG, "Loading subscriptions - page: " + page);

        compositeDisposable.add(
                ApiClient.getApiService()
                        .getUserSubscriptions(page, PAGE_SIZE, "id", "DESC", true)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    isLoading = false;
                                    showLoading(false);

                                    if (response.isSuccess() && response.getData() != null) {
                                        handleSubscriptionsResponse(response.getData(), page);
                                    } else {
                                        showError("Không thể tải danh sách vé tháng");
                                        if (page == 0) {
                                            showEmptyState();
                                        }
                                    }
                                },
                                throwable -> {
                                    isLoading = false;
                                    showLoading(false);
                                    Log.e(TAG, "Error loading subscriptions", throwable);
                                    showError("Lỗi: " + throwable.getMessage());
                                    if (page == 0) {
                                        showEmptyState();
                                    }
                                }));
    }

    private void handleSubscriptionsResponse(UserSubscriptionResponse response, int page) {
        List<UserSubscription> subscriptions = response.getContent();

        Log.d(TAG, String.format("Page %d - Total elements: %d, Total pages: %d, Is last: %s, Content size: %d",
                page, response.getTotalElements(), response.getTotalPages(),
                response.isLast(), subscriptions.size()));

        if (page == 0) {
            adapter.clearSubscriptions();
            if (subscriptions.isEmpty()) {
                showEmptyState();
                return;
            }
        }

        if (!subscriptions.isEmpty()) {
            adapter.addSubscriptions(subscriptions);
            currentPage = page;
            rvSubscriptions.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }

        isLastPage = response.isLast();
        Log.d(TAG, "Total subscriptions loaded: " + adapter.getItemCount());
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showEmptyState() {
        rvSubscriptions.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showRatingDialog(UserSubscription subscription) {
        String parkingLotName = subscription.getParkingLotName() != null
                ? subscription.getParkingLotName()
                : "Bãi đỗ xe";

        RatingDialog dialog = new RatingDialog(this, parkingLotName, (rating, title, comment) -> {
            // Submit rating to API
            submitRating(subscription, rating, title, comment);
        });
        dialog.show();
    }

    private void showRenewalConfirmDialog(UserSubscription subscription) {
        RenewalConfirmDialog dialog = new RenewalConfirmDialog(this, subscription,
                new RenewalConfirmDialog.OnRenewalConfirmListener() {
                    @Override
                    public void onConfirm() {
                        // User confirmed renewal
                        renewSubscription(subscription);
                    }

                    @Override
                    public void onCancel() {
                        // User cancelled - do nothing
                        Log.d(TAG, "User cancelled renewal");
                    }
                });
        dialog.show();
    }

    private void renewSubscription(UserSubscription subscription) {
        progressBar.setVisibility(View.VISIBLE);

        // Create request body with autoRenew=true
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("autoRenew", true);

        Log.d(TAG, "Renewing subscription ID: " + subscription.getId());

        compositeDisposable.add(
                ApiClient.getApiService()
                        .renewUserSubscription(subscription.getId(), requestBody)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (response.isSuccess()) {
                                        Toast.makeText(this, "✅ Gia hạn thành công!", Toast.LENGTH_SHORT).show();
                                        // Reload list to get updated data
                                        currentPage = 0;
                                        isLastPage = false;
                                        adapter.clearSubscriptions();
                                        loadSubscriptions(currentPage);
                                    } else {
                                        String errorMsg = response.getMessage() != null
                                                ? response.getMessage()
                                                : "Không thể gia hạn gói đăng ký";
                                        Toast.makeText(this, "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                },
                                error -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e(TAG, "Error renewing subscription", error);

                                    String errorMsg = "Không thể gia hạn";
                                    if (error.getMessage() != null) {
                                        if (error.getMessage().contains("401")) {
                                            errorMsg = "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại";
                                        } else if (error.getMessage().contains("404")) {
                                            errorMsg = "Không tìm thấy gói đăng ký";
                                        } else if (error.getMessage().contains("403")) {
                                            errorMsg = "Bạn không có quyền gia hạn gói này";
                                        }
                                    }
                                    Toast.makeText(this, "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                                }));
    }

    private void submitRating(UserSubscription subscription, int rating, String title, String comment) {
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
        long parkingLotId = subscription.getParkingLotId();

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

    private void showCancelConfirmDialog(UserSubscription subscription) {
        com.parkmate.android.dialog.CancelSubscriptionDialog dialog = new com.parkmate.android.dialog.CancelSubscriptionDialog(
                this,
                subscription,
                new com.parkmate.android.dialog.CancelSubscriptionDialog.OnCancelSubscriptionListener() {
                    @Override
                    public void onConfirmCancel(String reason) {
                        cancelSubscription(subscription, reason);
                    }

                    @Override
                    public void onBack() {
                        // User cancelled - do nothing
                        Log.d(TAG, "User cancelled subscription cancellation");
                    }
                });
        dialog.show();
    }

    private void cancelSubscription(UserSubscription subscription, String reason) {
        progressBar.setVisibility(View.VISIBLE);

        com.parkmate.android.model.request.CancelSubscriptionRequest request = new com.parkmate.android.model.request.CancelSubscriptionRequest(
                reason);

        Log.d(TAG, "Cancelling subscription ID: " + subscription.getId());

        compositeDisposable.add(
                ApiClient.getApiService()
                        .cancelUserSubscription(subscription.getId(), request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (response.isSuccess()) {
                                        // Reload list to get updated data (no toast - push notification will be
                                        // received)
                                        currentPage = 0;
                                        isLastPage = false;
                                        adapter.clearSubscriptions();
                                        loadSubscriptions(currentPage);
                                    } else {
                                        String errorMsg = response.getMessage() != null
                                                ? response.getMessage()
                                                : "Không thể hủy đăng ký";
                                        Toast.makeText(this, "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                },
                                error -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e(TAG, "Error cancelling subscription", error);

                                    String errorMsg = "Không thể hủy đăng ký";
                                    if (error.getMessage() != null) {
                                        if (error.getMessage().contains("401")) {
                                            errorMsg = "Phiên đăng nhập hết hạn";
                                        } else if (error.getMessage().contains("404")) {
                                            errorMsg = "Không tìm thấy đăng ký";
                                        } else if (error.getMessage().contains("403")) {
                                            errorMsg = "Bạn không có quyền hủy đăng ký này";
                                        } else if (error.getMessage().contains("400")) {
                                            errorMsg = "Đăng ký đã hết hạn hoặc đã bị hủy";
                                        }
                                    }
                                    Toast.makeText(this, "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
