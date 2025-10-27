package com.parkmate.android.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.parkmate.android.R;
import com.parkmate.android.adapter.TransactionAdapter;
import com.parkmate.android.model.Transaction;
import com.parkmate.android.repository.WalletRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class TransactionHistoryActivity extends AppCompatActivity {

    private static final String TAG = "TransactionHistory";
    private static final int PAGE_SIZE = 10;

    // Views
    private ImageButton btnBack;
    private Chip chipAll, chipTopUp, chipPayment;
    private RecyclerView rvTransactions;
    private ProgressBar progressBar;
    private ProgressBar progressBarLoadMore;
    private LinearLayout llEmptyState;

    // Adapter
    private TransactionAdapter transactionAdapter;
    private LinearLayoutManager layoutManager;

    // Repository
    private WalletRepository walletRepository;
    private CompositeDisposable compositeDisposable;

    // Data & Pagination
    private List<Transaction> transactionList;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String currentFilter = "ALL"; // ALL, TOP_UP, PAYMENT

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        walletRepository = new WalletRepository();
        compositeDisposable = new CompositeDisposable();
        transactionList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadTransactions(true);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        chipAll = findViewById(R.id.chipAll);
        chipTopUp = findViewById(R.id.chipTopUp);
        chipPayment = findViewById(R.id.chipPayment);
        rvTransactions = findViewById(R.id.rvTransactions);
        progressBar = findViewById(R.id.progressBar);
        progressBarLoadMore = findViewById(R.id.progressBarLoadMore);
        llEmptyState = findViewById(R.id.llEmptyState);
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        rvTransactions.setLayoutManager(layoutManager);
        transactionAdapter = new TransactionAdapter(transactionList);
        rvTransactions.setAdapter(transactionAdapter);

        // Infinite scroll
        rvTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        loadMoreTransactions();
                    }
                }
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        chipAll.setOnClickListener(v -> {
            if (!chipAll.isChecked()) {
                chipAll.setChecked(true);
                return;
            }
            uncheckOtherChips(chipAll);
            currentFilter = "ALL";
            resetPagination();
            loadTransactions(true);
        });

        chipTopUp.setOnClickListener(v -> {
            if (!chipTopUp.isChecked()) {
                chipTopUp.setChecked(true);
                return;
            }
            uncheckOtherChips(chipTopUp);
            currentFilter = "TOP_UP";
            resetPagination();
            loadTransactions(true);
        });

        chipPayment.setOnClickListener(v -> {
            if (!chipPayment.isChecked()) {
                chipPayment.setChecked(true);
                return;
            }
            uncheckOtherChips(chipPayment);
            currentFilter = "PAYMENT";
            resetPagination();
            loadTransactions(true);
        });
    }

    private void uncheckOtherChips(Chip selectedChip) {
        if (selectedChip != chipAll) chipAll.setChecked(false);
        if (selectedChip != chipTopUp) chipTopUp.setChecked(false);
        if (selectedChip != chipPayment) chipPayment.setChecked(false);
    }

    private void loadTransactions(boolean isFirstPage) {
        if (isLoading) return;

        isLoading = true;
        if (isFirstPage) {
            progressBar.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.GONE);
        } else {
            progressBarLoadMore.setVisibility(View.VISIBLE);
        }

        Disposable disposable = walletRepository.getTransactions(currentPage, PAGE_SIZE, "createdAt", "desc")
                .subscribe(
                        response -> {
                            isLoading = false;
                            progressBar.setVisibility(View.GONE);
                            progressBarLoadMore.setVisibility(View.GONE);

                            if (response.isSuccess() && response.getData() != null) {
                                List<Transaction> transactions = response.getData().getContent();

                                // Filter local nếu cần
                                List<Transaction> filteredTransactions = filterTransactions(transactions);

                                if (isFirstPage) {
                                    transactionList.clear();
                                    transactionList.addAll(filteredTransactions);
                                    transactionAdapter.notifyDataSetChanged();
                                } else {
                                    transactionAdapter.addData(filteredTransactions);
                                }

                                // Check if last page
                                isLastPage = response.getData().isLast();

                                if (transactionList.isEmpty()) {
                                    showEmptyState();
                                } else {
                                    hideEmptyState();
                                }

                                Log.d(TAG, "Loaded page " + currentPage + ", items: " + filteredTransactions.size());
                            } else {
                                if (isFirstPage) {
                                    showEmptyState();
                                }
                                String errorMsg = response.getError() != null
                                        ? response.getError()
                                        : "Không thể tải lịch sử giao dịch";
                                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            isLoading = false;
                            progressBar.setVisibility(View.GONE);
                            progressBarLoadMore.setVisibility(View.GONE);

                            if (isFirstPage) {
                                showEmptyState();
                            }
                            Log.e(TAG, "Error loading transactions: " + error.getMessage(), error);
                            Toast.makeText(this, "Lỗi kết nối. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void loadMoreTransactions() {
        currentPage++;
        loadTransactions(false);
    }

    private List<Transaction> filterTransactions(List<Transaction> transactions) {
        if ("ALL".equals(currentFilter)) {
            return transactions;
        }

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (currentFilter.equals(transaction.getTransactionType())) {
                filtered.add(transaction);
            }
        }
        return filtered;
    }

    private void resetPagination() {
        currentPage = 0;
        isLastPage = false;
        transactionList.clear();
    }

    private void showEmptyState() {
        rvTransactions.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        rvTransactions.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
    }
}

