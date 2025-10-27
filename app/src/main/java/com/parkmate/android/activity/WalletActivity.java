package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private ImageView ivHistory;
    private TextView tvBalance, tvWalletId, tvViewAll;
    private CardView cvTopUp;
    private RecyclerView rvTransactions;
    private LinearLayout llEmptyState;

    // Adapters
    private TransactionAdapter transactionAdapter;

    // Repository
    private WalletRepository walletRepository;
    private CompositeDisposable compositeDisposable;

    // Data
    private Wallet currentWallet;
    private List<Transaction> transactionList;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_wallet;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation(true, R.id.nav_wallet);

        walletRepository = new WalletRepository();
        compositeDisposable = new CompositeDisposable();
        transactionList = new ArrayList<>();

        initViews();
        setupClickListeners();
        loadWalletData();
        loadTransactions();
    }

    @Override
    protected void customizeToolbar() {
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
        }
        if (ivNavigation != null) {
            ivNavigation.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        androidx.appcompat.widget.Toolbar walletToolbar = findViewById(R.id.toolbar);
        ivHistory = findViewById(R.id.ivHistory);
        tvBalance = findViewById(R.id.tvBalance);
        tvWalletId = findViewById(R.id.tvWalletId);
        tvViewAll = findViewById(R.id.tvViewAll);
        cvTopUp = findViewById(R.id.cvTopUp);
        rvTransactions = findViewById(R.id.rvTransactions);
        llEmptyState = findViewById(R.id.llEmptyState);

        if (walletToolbar != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(walletToolbar, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(transactionList);
        rvTransactions.setAdapter(transactionAdapter);
        showEmptyState();
    }

    private void setupClickListeners() {
        if (ivHistory != null) {
            ivHistory.setOnClickListener(v -> openTransactionHistory());
        }

        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> openTransactionHistory());
        }

        if (cvTopUp != null) {
            cvTopUp.setOnClickListener(v -> {
                Intent intent = new Intent(WalletActivity.this, TopUpActivity.class);
                startActivityForResult(intent, REQUEST_TOP_UP);
            });
        }
    }

    private void openTransactionHistory() {
        Intent intent = new Intent(WalletActivity.this, TransactionHistoryActivity.class);
        startActivity(intent);
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

        tvBalance.setText(wallet.getFormattedBalance());
        String walletId = "ID: PM-" + wallet.getUserId();
        tvWalletId.setText(walletId);

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
        if (tvWalletId != null) {
            tvWalletId.setText("ID: --");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
    }
}

