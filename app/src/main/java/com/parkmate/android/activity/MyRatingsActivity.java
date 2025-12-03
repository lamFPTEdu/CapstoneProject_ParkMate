package com.parkmate.android.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.parkmate.android.R;
import com.parkmate.android.adapter.MyRatingsAdapter;
import com.parkmate.android.model.ParkingLotRating;
import com.parkmate.android.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Activity hiển thị tất cả đánh giá của user hiện tại
 * Sử dụng ownedByMe=true để lấy ratings của user
 */
public class MyRatingsActivity extends AppCompatActivity {
    private static final String TAG = "MyRatings";

    private MaterialToolbar toolbar;
    private RecyclerView rvMyRatings;
    private View tvNoRatings;  // Changed to View because it's a LinearLayout container in XML
    private View progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private MyRatingsAdapter ratingAdapter;
    private final List<ParkingLotRating> ratingList = new ArrayList<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    // Pagination
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int PAGE_SIZE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ratings);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        loadMyRatings();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMyRatings = findViewById(R.id.rvMyRatings);
        tvNoRatings = findViewById(R.id.tvNoRatings);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Đánh giá của tôi");
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        ratingAdapter = new MyRatingsAdapter(new MyRatingsAdapter.OnRatingActionListener() {
            @Override
            public void onEditRating(ParkingLotRating rating) {
                showEditRatingDialog(rating);
            }

            @Override
            public void onDeleteRating(ParkingLotRating rating) {
                showDeleteConfirmDialog(rating);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMyRatings.setLayoutManager(layoutManager);
        rvMyRatings.setAdapter(ratingAdapter);

        // Add item spacing
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.rating_item_spacing);
        rvMyRatings.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, android.view.View view,
                                       RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = spacingInPixels;
            }
        });

        // Add infinite scroll listener
        rvMyRatings.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
            loadMyRatings();
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }

    private void loadMyRatings() {
        if (isLoading) return;

        isLoading = true;

        // Only show full loading on first page
        if (currentPage == 0) {
            showLoading(true);
        }

        compositeDisposable.add(
                ApiClient.getApiService().getMyRatings(
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
                                    Log.e(TAG, "Error loading my ratings", throwable);
                                    showError("Không thể tải đánh giá. Vui lòng thử lại.");
                                }
                        )
        );
    }

    private void loadMoreRatings() {
        if (!isLoading && !isLastPage) {
            currentPage++;
            loadMyRatings();
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

            // Update adapter
            ratingAdapter.submitList(new ArrayList<>(ratingList));

            // Show/hide empty state
            if (ratingList.isEmpty()) {
                rvMyRatings.setVisibility(View.GONE);
                tvNoRatings.setVisibility(View.VISIBLE);
            } else {
                rvMyRatings.setVisibility(View.VISIBLE);
                tvNoRatings.setVisibility(View.GONE);
            }
        } else {
            // No ratings
            if (currentPage == 0) {
                rvMyRatings.setVisibility(View.GONE);
                tvNoRatings.setVisibility(View.VISIBLE);
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

    /**
     * Show dialog to edit rating
     */
    private void showEditRatingDialog(ParkingLotRating rating) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);

        // Find views
        TextView tvParkingLotName = dialogView.findViewById(R.id.tvParkingLotName);
        TextView tvRatingDescription = dialogView.findViewById(R.id.tvRatingDescription);
        com.google.android.material.textfield.TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        com.google.android.material.textfield.TextInputEditText etComment = dialogView.findViewById(R.id.etComment);
        ImageView star1 = dialogView.findViewById(R.id.star1);
        ImageView star2 = dialogView.findViewById(R.id.star2);
        ImageView star3 = dialogView.findViewById(R.id.star3);
        ImageView star4 = dialogView.findViewById(R.id.star4);
        ImageView star5 = dialogView.findViewById(R.id.star5);
        ImageView[] stars = {star1, star2, star3, star4, star5};

        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        com.google.android.material.button.MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        // Set parking lot name (if available from rating data)
        tvParkingLotName.setVisibility(View.GONE);

        // Track selected rating
        final int[] selectedRating = {0};

        // Set current values
        if (rating.getTitle() != null) {
            etTitle.setText(rating.getTitle());
        }
        if (rating.getComment() != null) {
            etComment.setText(rating.getComment());
        }
        if (rating.getOverallRating() != null) {
            selectedRating[0] = rating.getOverallRating();
            updateStars(stars, selectedRating[0], tvRatingDescription);
            btnSubmit.setEnabled(true);
        }

        // Star click listeners
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i;
            stars[i].setOnClickListener(v -> {
                selectedRating[0] = starIndex + 1;
                updateStars(stars, selectedRating[0], tvRatingDescription);
                btnSubmit.setEnabled(true);
            });
        }

        // Create dialog
        android.app.AlertDialog dialog = builder.setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Set dialog width to match parent with margins (90% of screen width)
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setText("Cập nhật");
        btnSubmit.setOnClickListener(v -> {
            String newTitle = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String newComment = etComment.getText() != null ? etComment.getText().toString().trim() : "";

            if (selectedRating[0] == 0) {
                showError("Vui lòng chọn số sao đánh giá");
                return;
            }

            dialog.dismiss();
            updateRating(rating.getId(), selectedRating[0], newTitle, newComment);
        });

        dialog.show();
    }

    /**
     * Update star display
     */
    private void updateStars(ImageView[] stars, int rating, TextView tvDescription) {
        String[] descriptions = {
                "Chọn số sao",
                "Rất tệ",
                "Tệ",
                "Trung bình",
                "Tốt",
                "Rất tốt"
        };

        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
                stars[i].setColorFilter(getResources().getColor(R.color.rating_star, null));
            } else {
                stars[i].setImageResource(R.drawable.ic_star_outline);
                stars[i].setColorFilter(getResources().getColor(R.color.text_tertiary, null));
            }
        }

        if (rating >= 0 && rating < descriptions.length) {
            tvDescription.setText(descriptions[rating]);
        }
    }

    /**
     * Update rating via API
     */
    private void updateRating(Long ratingId, int overallRating, String title, String comment) {
        showLoading(true);

        com.parkmate.android.model.request.UpdateRatingRequest request =
                new com.parkmate.android.model.request.UpdateRatingRequest(overallRating, title, comment);

        compositeDisposable.add(
                ApiClient.getApiService().updateRating(ratingId, request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    showLoading(false);
                                    if (response != null && response.isSuccess()) {
                                        showError("Cập nhật đánh giá thành công");
                                        // Refresh list
                                        currentPage = 0;
                                        isLastPage = false;
                                        ratingList.clear();
                                        loadMyRatings();
                                    } else {
                                        String errorMsg = response != null && response.getError() != null ?
                                                response.getError().toString() : "Không thể cập nhật đánh giá";
                                        showError(errorMsg);
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    Log.e(TAG, "Error updating rating", throwable);
                                    showError("Không thể cập nhật đánh giá. Vui lòng thử lại.");
                                }
                        )
        );
    }

    /**
     * Show confirmation dialog before delete
     */
    private void showDeleteConfirmDialog(ParkingLotRating rating) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xóa đánh giá")
                .setMessage("Bạn có chắc chắn muốn xóa đánh giá này?\n\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteRating(rating.getId());
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Delete rating via API
     */
    private void deleteRating(Long ratingId) {
        showLoading(true);

        compositeDisposable.add(
                ApiClient.getApiService().deleteRating(ratingId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    showLoading(false);
                                    if (response != null && response.isSuccess()) {
                                        showError("Đã xóa đánh giá");
                                        // Refresh list
                                        currentPage = 0;
                                        isLastPage = false;
                                        ratingList.clear();
                                        loadMyRatings();
                                    } else {
                                        String errorMsg = response != null && response.getError() != null ?
                                                response.getError().toString() : "Không thể xóa đánh giá";
                                        showError(errorMsg);
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    Log.e(TAG, "Error deleting rating", throwable);
                                    showError("Không thể xóa đánh giá. Vui lòng thử lại.");
                                }
                        )
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

