package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parkmate.android.R;

/**
 * BaseActivity - Lớp cơ sở cho tất cả các Activity trong ứng dụng
 * Cung cấp các chức năng chung như Toolbar và BottomNavigationView
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected ImageView ivNavigation;
    protected BottomNavigationView bottomNavigationView;
    protected FrameLayout contentFrame;

    // Search components trong toolbar
    protected ConstraintLayout searchContainer;
    protected ImageView ivSearchIcon;
    protected TextView tvSearchPlaceholder;
    protected FrameLayout btnFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Khởi tạo views
        initViews();
        applySystemBarInsets();

        // Nạp layout của activity con vào contentFrame
        getLayoutInflater().inflate(getLayoutResourceId(), contentFrame);

        // Tùy chỉnh toolbar (nếu cần)
        customizeToolbar();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivNavigation = findViewById(R.id.ivNavigation);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        contentFrame = findViewById(R.id.content_frame);

        // Khởi tạo search components từ layout_search_destination
        // Root của search destination component
        searchContainer = findViewById(R.id.searchDestinationRoot);
        tvSearchPlaceholder = findViewById(R.id.tvSearchPlaceholder);
        btnFilter = findViewById(R.id.btnFilter);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            // Thêm padding top cho toolbar để tránh bị dính status bar + thêm margin top
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                // Thêm padding top = status bar height + 12dp margin
                int extraMargin = (int) (12 * getResources().getDisplayMetrics().density);
                v.setPadding(v.getPaddingLeft(), systemBars.top + extraMargin, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
    }

    private void applySystemBarInsets() {
        if (bottomNavigationView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Sử dụng margin bottom thay vì padding để tránh đẩy nội dung (icon + text) lên
                // Margin sẽ đẩy toàn bộ Bottom Nav lên, giữ nguyên layout bên trong
                if (v.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                    android.view.ViewGroup.MarginLayoutParams params =
                        (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    params.bottomMargin = systemBars.bottom;
                    v.setLayoutParams(params);
                }

                return WindowInsetsCompat.CONSUMED;
            });
        }
        if (contentFrame != null) {
            ViewCompat.setOnApplyWindowInsetsListener(contentFrame, (v, insets) -> {
                // Content frame không cần xử lý insets vì bottom nav đã xử lý
                return insets;
            });
        }
    }

    /**
     * Thiết lập toolbar với nút điều hướng (không có title)
     *
     * @param isMainScreen true nếu đây là màn hình chính (hiển thị icon menu), false nếu là màn hình con (hiển thị nút back)
     */
    protected void setupToolbar(boolean isMainScreen) {
        // Ẩn search container mặc định
        if (searchContainer != null) {
            searchContainer.setVisibility(View.GONE);
        }

        if (ivNavigation != null) {
            if (isMainScreen) {
                // Hiển thị icon menu cho màn hình chính
                ivNavigation.setImageResource(R.drawable.ic_menu_24);
                ivNavigation.setOnClickListener(v -> openMenu());
            } else {
                // Hiển thị mũi tên quay lại cho các màn hình khác
                ivNavigation.setImageResource(R.drawable.ic_arrow_back_24);
                ivNavigation.setOnClickListener(v -> onBackPressed());
            }
        }
    }

    /**
     * Thiết lập toolbar với chế độ search
     *
     * @param isMainScreen true nếu đây là màn hình chính (hiển thị icon menu), false nếu là màn hình con (hiển thị nút back)
     * @param searchPlaceholder Text placeholder cho ô search
     * @param onSearchClick Callback khi click vào search bar
     * @param onFilterClick Callback khi click vào nút filter (có thể null nếu không dùng)
     */
    protected void setupToolbarWithSearch(boolean isMainScreen, String searchPlaceholder,
                                         View.OnClickListener onSearchClick,
                                         View.OnClickListener onFilterClick) {
        setupToolbarWithSearch(isMainScreen, searchPlaceholder, onSearchClick, onFilterClick, true);
    }

    /**
     * Thiết lập toolbar với chế độ search (có tùy chọn ẩn navigation icon)
     *
     * @param isMainScreen true nếu đây là màn hình chính (hiển thị icon menu), false nếu là màn hình con (hiển thị nút back)
     * @param searchPlaceholder Text placeholder cho ô search
     * @param onSearchClick Callback khi click vào search bar
     * @param onFilterClick Callback khi click vào nút filter (có thể null nếu không dùng)
     * @param showNavigationIcon true để hiển thị icon navigation, false để ẩn hoàn toàn
     */
    protected void setupToolbarWithSearch(boolean isMainScreen, String searchPlaceholder,
                                         View.OnClickListener onSearchClick,
                                         View.OnClickListener onFilterClick,
                                         boolean showNavigationIcon) {
        // Hiển thị search container
        if (searchContainer != null) {
            searchContainer.setVisibility(View.VISIBLE);
            searchContainer.setOnClickListener(onSearchClick);
        }

        if (tvSearchPlaceholder != null && searchPlaceholder != null) {
            tvSearchPlaceholder.setText(searchPlaceholder);
        }

        if (btnFilter != null) {
            if (onFilterClick != null) {
                btnFilter.setVisibility(View.VISIBLE);
                btnFilter.setOnClickListener(onFilterClick);
            } else {
                btnFilter.setVisibility(View.GONE);
            }
        }

        if (ivNavigation != null) {
            if (!showNavigationIcon) {
                // Ẩn icon navigation hoàn toàn
                ivNavigation.setVisibility(View.GONE);
            } else {
                ivNavigation.setVisibility(View.VISIBLE);
                if (isMainScreen) {
                    ivNavigation.setImageResource(R.drawable.ic_menu_24);
                    ivNavigation.setOnClickListener(v -> openMenu());
                } else {
                    ivNavigation.setImageResource(R.drawable.ic_arrow_back_24);
                    ivNavigation.setOnClickListener(v -> onBackPressed());
                }
            }
        }
    }

    /**
     * Mở menu (được ghi đè trong các activity con nếu cần)
     */
    protected void openMenu() {
        // TODO: Implement menu (e.g. Navigation Drawer) if needed later
    }

    /**
     * Thiết lập BottomNavigationView
     *
     * @param showBottomNav true nếu muốn hiển thị bottom navigation, false nếu muốn ẩn
     * @param selectedItemId ID của menu item được chọn (nếu có)
     */
    protected void setupBottomNavigation(boolean showBottomNav, int selectedItemId) {
        if (bottomNavigationView == null) return;
        if (!showBottomNav) {
            bottomNavigationView.setVisibility(android.view.View.GONE);
            return;
        }
        bottomNavigationView.setVisibility(android.view.View.VISIBLE);

        // Disable ripple effect completely
        try { bottomNavigationView.setItemRippleColor(null); } catch (Exception ignored) {}

        if (selectedItemId > 0) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }
        final int currentSelected = selectedItemId;
        // Xử lý sự kiện khi chọn item trên bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentSelected) return true; // already here
            handleBottomNavSelection(item);
            return true;
        });
    }

    private void handleBottomNavSelection(MenuItem item) {
        int id = item.getItemId();
        String currentClassName = this.getClass().getSimpleName();

        if (id == R.id.nav_home) {
            if (!currentClassName.equals("HomeActivity")) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        } else if (id == R.id.nav_parking) {
            if (!currentClassName.equals("ParkingLotsActivity")) {
                Intent intent = new Intent(this, ParkingLotsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        } else if (id == R.id.nav_wallet) {
            if (!currentClassName.equals("WalletActivity")) {
                Intent intent = new Intent(this, WalletActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        } else if (id == R.id.nav_account) {
            if (!currentClassName.equals("ProfileActivity")) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        }
    }

    /**
     * @return ID của layout resource cho activity con
     */
    protected abstract int getLayoutResourceId();

    /**
     * Phương thức cho phép các activity con tùy chỉnh toolbar
     * Gọi sau khi toolbar đã được khởi tạo
     */
    protected void customizeToolbar() {
        // Mặc định không làm gì, có thể ghi đè trong các activity con
    }
}
