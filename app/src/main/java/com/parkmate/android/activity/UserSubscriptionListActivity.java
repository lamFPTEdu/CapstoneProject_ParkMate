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
import com.parkmate.android.model.UserSubscription;
import com.parkmate.android.model.response.UserSubscriptionResponse;
import com.parkmate.android.network.ApiClient;

import java.util.List;

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
        adapter = new UserSubscriptionAdapter(subscription -> {
            // Navigate to subscription detail/success screen to show QR code
            Intent intent = new Intent(this, SubscriptionSuccessActivity.class);
            intent.putExtra("SUBSCRIPTION", subscription);
            startActivity(intent);
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
        if (isLoading) return;

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
                                }
                        )
        );
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

