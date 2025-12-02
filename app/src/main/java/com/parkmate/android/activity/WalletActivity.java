package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.parkmate.android.R;
import com.parkmate.android.adapter.TransactionAdapter;
import com.parkmate.android.model.Transaction;
import com.parkmate.android.model.Wallet;
import com.parkmate.android.repository.WalletRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class WalletActivity extends BaseActivity {

    private static final String TAG = "WalletActivity";
    private static final int REQUEST_TOP_UP = 1001;

    // Views
    private TextView tvBalance, tvViewAll;
    private CardView cvTopUp;
    private RecyclerView rvTransactions;
    private LinearLayout llEmptyState;
    private android.widget.ImageView ivToggleBalance;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Adapters
    private TransactionAdapter transactionAdapter;

    // Repository
    private WalletRepository walletRepository;
    private CompositeDisposable compositeDisposable;

    // Data
    private Wallet currentWallet;
    private List<Transaction> transactionList;
    private boolean isBalanceVisible = false;  // Mặc định ẩn số dư
    private String actualBalance = "0đ";

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_wallet;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup toolbar with title (no back button vì có bottom nav)
        setupToolbarWithTitle(getString(R.string.wallet_title), false);

        setupBottomNavigation(true, R.id.nav_wallet);

        // Load notification badge count
        loadNotificationBadgeCount();

        walletRepository = new WalletRepository();
        compositeDisposable = new CompositeDisposable();
        transactionList = new ArrayList<>();

        initViews();
        setupClickListeners();
        loadWalletData();
        loadTransactions();
    }

    private void initViews() {
        tvBalance = findViewById(R.id.tvBalance);
        tvViewAll = findViewById(R.id.tvViewAll);
        cvTopUp = findViewById(R.id.cvTopUp);
        rvTransactions = findViewById(R.id.rvTransactions);
        llEmptyState = findViewById(R.id.llEmptyState);
        ivToggleBalance = findViewById(R.id.ivToggleBalance);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Setup RecyclerView
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(transactionList);
        rvTransactions.setAdapter(transactionAdapter);
        showEmptyState();

        // Setup SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.colorPrimary,
                    R.color.primary
            );
            swipeRefreshLayout.setOnRefreshListener(this::refreshWalletData);
        }
    }

    private void setupClickListeners() {

        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> openTransactionHistory());
        }

        if (cvTopUp != null) {
            cvTopUp.setOnClickListener(v -> {
                Intent intent = new Intent(WalletActivity.this, TopUpActivity.class);
                startActivityForResult(intent, REQUEST_TOP_UP);
            });
        }

        if (ivToggleBalance != null) {
            ivToggleBalance.setOnClickListener(v -> toggleBalanceVisibility());
        }
    }

    private void openTransactionHistory() {
        Intent intent = new Intent(WalletActivity.this, TransactionHistoryActivity.class);
        startActivity(intent);
    }

    /**
     * Refresh wallet data khi người dùng pull-to-refresh
     */
    private void refreshWalletData() {
        Log.d(TAG, "Refreshing wallet data...");
        loadWalletData();
        loadTransactions();

        // Dừng animation refresh sau khi load xong
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.postDelayed(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1000);
        }
    }

    private void loadWalletData() {
        Disposable disposable = walletRepository.getMyWallet()
                .subscribe(
                        response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                currentWallet = response.getData();
                                displayWalletInfo(currentWallet);
                            } else {
                                String errorMsg = response.getError() != null
                                    ? response.getError()
                                    : "Không thể tải thông tin ví";
                                showError(errorMsg);
                            }
                        },
                        error -> {
                            Log.e(TAG, "Lỗi khi tải thông tin ví: " + error.getMessage(), error);
                            showError("Không thể kết nối đến server. Vui lòng thử lại.");
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void loadTransactions() {
        Disposable disposable = walletRepository.getTransactions(0, 3, "createdAt", "desc")
                .subscribe(
                        response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                List<Transaction> transactions = response.getData().getContent();
                                if (transactions != null && !transactions.isEmpty()) {
                                    transactionList.clear();
                                    transactionList.addAll(transactions);
                                    transactionAdapter.notifyDataSetChanged();
                                    hideEmptyState();
                                    Log.d(TAG, "Load được " + transactions.size() + " giao dịch gần nhất");
                                } else {
                                    showEmptyState();
                                }
                            } else {
                                String errorMsg = response.getError() != null
                                        ? response.getError()
                                        : "Không thể tải lịch sử giao dịch";
                                Log.e(TAG, errorMsg);
                                showEmptyState();
                            }
                        },
                        error -> {
                            Log.e(TAG, "Lỗi khi tải lịch sử giao dịch: " + error.getMessage(), error);
                            showEmptyState();
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void displayWalletInfo(Wallet wallet) {
        if (wallet == null) return;

        actualBalance = wallet.getFormattedBalance();
        updateBalanceDisplay();

        Log.d(TAG, "Hiển thị thông tin ví: Balance=" + wallet.getBalance() +
                   ", Currency=" + wallet.getCurrency() +
                   ", IsActive=" + wallet.isActive());
    }

    private void showEmptyState() {
        if (rvTransactions != null) {
            rvTransactions.setVisibility(View.GONE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyState() {
        if (rvTransactions != null) {
            rvTransactions.setVisibility(View.VISIBLE);
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        if (tvBalance != null) {
            tvBalance.setText("0đ");
        }
    }

    /**
     * Toggle giữa hiện và ẩn số dư
     */
    private void toggleBalanceVisibility() {
        isBalanceVisible = !isBalanceVisible;
        updateBalanceDisplay();
    }

    /**
     * Cập nhật hiển thị số dư dựa trên trạng thái isBalanceVisible
     */
    private void updateBalanceDisplay() {
        if (tvBalance == null || ivToggleBalance == null) return;

        if (isBalanceVisible) {
            // Hiện số dư thật - icon con mắt gạch (để nhấn vào ẩn đi)
            tvBalance.setText(actualBalance);
            ivToggleBalance.setImageResource(R.drawable.ic_visibility_off_24);
        } else {
            // Ẩn số dư bằng dấu sao - icon con mắt mở (để nhấn vào xem)
            String maskedBalance = "********";
            tvBalance.setText(maskedBalance);
            ivToggleBalance.setImageResource(R.drawable.ic_visibility_24);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TOP_UP && resultCode == RESULT_OK) {
            loadWalletData();
            loadTransactions();
            Toast.makeText(this, "Đã cập nhật số dư ví", Toast.LENGTH_SHORT).show();
        }
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
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
    }
}

