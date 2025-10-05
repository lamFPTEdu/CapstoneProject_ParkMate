package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

        // Khởi tạo search components
        searchContainer = findViewById(R.id.searchContainer);
        ivSearchIcon = findViewById(R.id.ivSearchIcon);
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
                Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                // Chỉ áp dụng padding trái/phải, không cộng thêm padding dưới để thanh dính sát cạnh dưới
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), 0);
                return insets;
            });
        }
        if (contentFrame != null) {
            ViewCompat.setOnApplyWindowInsetsListener(contentFrame, (v, insets) -> {
                // Giữ nguyên padding mặc định, nội dung có thể kéo dài đến phía trên bottom nav
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
            if (isMainScreen) {
                ivNavigation.setImageResource(R.drawable.ic_menu_24);
                ivNavigation.setOnClickListener(v -> openMenu());
            } else {
                ivNavigation.setImageResource(R.drawable.ic_arrow_back_24);
                ivNavigation.setOnClickListener(v -> onBackPressed());
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
        if (id == R.id.nav_home) {
            if (!(this instanceof HomeActivity)) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        } else if (id == R.id.nav_account) {
            if (!(this instanceof ProfileActivity)) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        } else {
            // Placeholder for future screens (Parking, Wallet)
            Toast.makeText(this, "Tính năng sẽ sớm ra mắt", Toast.LENGTH_SHORT).show();
            // TODO: When activities are created, start them here similar to HomeActivity
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
