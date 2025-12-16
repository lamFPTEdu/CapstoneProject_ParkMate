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

    // Toolbar title and action button
    protected TextView tvToolbarTitle;
    protected ImageView ivToolbarAction;

    // Search components trong toolbar
    protected ConstraintLayout searchContainer;
    protected TextView tvSearchPlaceholder;
    protected FrameLayout btnFilter;

    // Notification components trong toolbar
    protected FrameLayout btnToolbarNotification;
    protected TextView tvToolbarNotificationBadge;

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

        // Load notification badge
        loadAndShowNotificationBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật notification badge mỗi khi activity được hiển thị
        loadAndShowNotificationBadge();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivNavigation = findViewById(R.id.ivNavigation);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        contentFrame = findViewById(R.id.content_frame);

        // Khởi tạo toolbar title và action button
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        ivToolbarAction = findViewById(R.id.ivToolbarAction);

        // Khởi tạo search components từ toolbar
        searchContainer = findViewById(R.id.searchDestinationRoot);
        tvSearchPlaceholder = findViewById(R.id.tvSearchPlaceholder);
        btnFilter = findViewById(R.id.btnFilter);

        // Notification components từ toolbar
        btnToolbarNotification = findViewById(R.id.btnToolbarNotification);
        tvToolbarNotificationBadge = findViewById(R.id.tvToolbarNotificationBadge);

        // Thiết lập click listener cho notification button
        if (btnToolbarNotification != null) {
            btnToolbarNotification.setOnClickListener(v -> openNotifications());
        }

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
                v.setPadding(v.getPaddingLeft(), systemBars.top + extraMargin, v.getPaddingRight(),
                        v.getPaddingBottom());
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
                    android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) v
                            .getLayoutParams();
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
     * @param isMainScreen true nếu đây là màn hình chính (hiển thị icon menu),
     *                     false nếu là màn hình con (hiển thị nút back)
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
                ivNavigation.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
            }
        }
    }

    /**
     * Thiết lập toolbar với title (không có search, chỉ có title text)
     *
     * @param title          Text hiển thị trên toolbar
     * @param showBackButton true để hiển thị nút back, false để ẩn nút navigation
     */
    protected void setupToolbarWithTitle(String title, boolean showBackButton) {
        // Ẩn search container
        if (searchContainer != null) {
            searchContainer.setVisibility(View.GONE);
        }

        // Ẩn action button mặc định
        if (ivToolbarAction != null) {
            ivToolbarAction.setVisibility(View.GONE);
        }

        // Hiển thị title trên toolbar
        if (tvToolbarTitle != null) {
            tvToolbarTitle.setVisibility(View.VISIBLE);
            tvToolbarTitle.setText(title);
        }

        // Thiết lập nút navigation
        if (ivNavigation != null) {
            if (showBackButton) {
                ivNavigation.setVisibility(View.VISIBLE);
                ivNavigation.setImageResource(R.drawable.ic_arrow_back_24);
                ivNavigation.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
            } else {
                // Ẩn nút navigation hoàn toàn
                ivNavigation.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Thiết lập toolbar với title và action button icon (vd: icon "Đọc tất cả")
     *
     * @param title               Text hiển thị trên toolbar
     * @param showBackButton      true để hiển thị nút back, false để ẩn nút
     *                            navigation
     * @param actionIconRes       Resource ID của icon action button
     * @param actionClickListener Listener khi click action button
     */
    protected void setupToolbarWithTitleAndAction(String title, boolean showBackButton,
            int actionIconRes, View.OnClickListener actionClickListener) {
        // Ẩn search container
        if (searchContainer != null) {
            searchContainer.setVisibility(View.GONE);
        }

        // Hiển thị title
        if (tvToolbarTitle != null) {
            tvToolbarTitle.setVisibility(View.VISIBLE);
            tvToolbarTitle.setText(title);
        }

        // Hiển thị action button với icon
        if (ivToolbarAction != null && actionIconRes != 0 && actionClickListener != null) {
            ivToolbarAction.setVisibility(View.VISIBLE);
            ivToolbarAction.setImageResource(actionIconRes);
            ivToolbarAction.setOnClickListener(actionClickListener);
        }

        // Thiết lập nút navigation
        if (ivNavigation != null) {
            if (showBackButton) {
                ivNavigation.setVisibility(View.VISIBLE);
                ivNavigation.setImageResource(R.drawable.ic_arrow_back_24);
                ivNavigation.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
            } else {
                // Ẩn nút navigation hoàn toàn
                ivNavigation.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Thiết lập toolbar với chế độ search
     *
     * @param isMainScreen      true nếu đây là màn hình chính (hiển thị icon menu),
     *                          false nếu là màn hình con (hiển thị nút back)
     * @param searchPlaceholder Text placeholder cho ô search
     * @param onSearchClick     Callback khi click vào search bar
     * @param onFilterClick     Callback khi click vào nút filter (có thể null nếu
     *                          không dùng)
     */
    protected void setupToolbarWithSearch(boolean isMainScreen, String searchPlaceholder,
            View.OnClickListener onSearchClick,
            View.OnClickListener onFilterClick) {
        setupToolbarWithSearch(isMainScreen, searchPlaceholder, onSearchClick, onFilterClick, true);
    }

    /**
     * Thiết lập toolbar với chế độ search (có tùy chọn ẩn navigation icon)
     *
     * @param isMainScreen       true nếu đây là màn hình chính (hiển thị icon
     *                           menu), false nếu là màn hình con (hiển thị nút
     *                           back)
     * @param searchPlaceholder  Text placeholder cho ô search
     * @param onSearchClick      Callback khi click vào search bar
     * @param onFilterClick      Callback khi click vào nút filter (có thể null nếu
     *                           không dùng)
     * @param showNavigationIcon true để hiển thị icon navigation, false để ẩn hoàn
     *                           toàn
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
                    ivNavigation.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
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
     * Thiết lập notification badge trên toolbar
     *
     * @param unreadCount số lượng notification chưa đọc (0 = ẩn badge)
     */
    protected void setupNotificationBadge(int unreadCount) {
        if (tvToolbarNotificationBadge == null) {
            android.util.Log.e("BaseActivity", "Toolbar notification badge is null!");
            return;
        }

        android.util.Log.d("BaseActivity", "Setting up notification badge with count: " + unreadCount);

        if (unreadCount > 0) {
            tvToolbarNotificationBadge.setVisibility(View.VISIBLE);
            tvToolbarNotificationBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
        } else {
            tvToolbarNotificationBadge.setVisibility(View.GONE);
            android.util.Log.d("BaseActivity", "Badge hidden (count = 0)");
        }
    }

    /**
     * Load và hiển thị badge số lượng notifications chưa đọc
     * Helper method để các activity dễ dàng cập nhật badge
     *
     * GUEST MODE: Ẩn notification button nếu user chưa đăng nhập
     */
    protected void loadAndShowNotificationBadge() {
        // Check if user is logged in
        if (!com.parkmate.android.utils.AuthHelper.isUserLoggedIn()) {
            // Guest user - hide notification button
            setNotificationButtonVisible(false);
            return;
        }

        // Logged in user - show notification button with badge count
        setNotificationButtonVisible(true);
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("parkmate_notifications", MODE_PRIVATE);
            int unreadCount = prefs.getInt("unread_count", 0);
            setupNotificationBadge(unreadCount);
        } catch (Exception e) {
            setupNotificationBadge(0);
        }
    }

    /**
     * Mở màn hình notifications
     */
    protected void openNotifications() {
        Intent intent = new Intent(this, NotificationActivity.class);
        startActivity(intent);
    }

    /**
     * Thiết lập hiển thị/ẩn notification button trên toolbar
     *
     * @param visible true để hiển thị, false để ẩn
     */
    protected void setNotificationButtonVisible(boolean visible) {
        if (btnToolbarNotification != null) {
            btnToolbarNotification.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Thiết lập BottomNavigationView
     *
     * @param showBottomNav  true nếu muốn hiển thị bottom navigation, false nếu
     *                       muốn ẩn
     * @param selectedItemId ID của menu item được chọn (nếu có)
     */
    protected void setupBottomNavigation(boolean showBottomNav, int selectedItemId) {
        if (bottomNavigationView == null)
            return;
        if (!showBottomNav) {
            bottomNavigationView.setVisibility(android.view.View.GONE);
            return;
        }
        bottomNavigationView.setVisibility(android.view.View.VISIBLE);

        // Disable ripple effect completely
        try {
            bottomNavigationView.setItemRippleColor(null);
        } catch (Exception ignored) {
        }

        if (selectedItemId > 0) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }
        final int currentSelected = selectedItemId;
        // Xử lý sự kiện khi chọn item trên bottom navigation
        // GUEST MODE FIX: Return false nếu navigation bị block để không highlight tab
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentSelected)
                return true; // already here
            boolean navigationSuccess = handleBottomNavSelection(item);
            return navigationSuccess; // Only highlight if navigation succeeds
        });
    }

    /**
     * Thiết lập BottomNavigationView nhưng KHÔNG highlight item nào
     * Dùng cho các trang không nằm trong bottom nav menu (VD:
     * ParkingLotDetailActivity)
     *
     * @param showBottomNav true nếu muốn hiển thị bottom navigation, false nếu muốn
     *                      ẩn
     */
    protected void setupBottomNavigationWithoutSelection(boolean showBottomNav) {
        if (bottomNavigationView == null)
            return;
        if (!showBottomNav) {
            bottomNavigationView.setVisibility(android.view.View.GONE);
            return;
        }
        bottomNavigationView.setVisibility(android.view.View.VISIBLE);

        // Disable ripple effect completely
        try {
            bottomNavigationView.setItemRippleColor(null);
        } catch (Exception ignored) {
        }

        // CLEAR selected item - không highlight item nào
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

        // Xử lý sự kiện khi chọn item trên bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            handleBottomNavSelection(item);
            return true;
        });
    }

    /**
     * Handle bottom navigation selection
     *
     * GUEST MODE: Return false nếu navigation bị block (guest không có quyền)
     * để bottom nav KHÔNG highlight tab đó, giữ highlight ở tab hiện tại
     *
     * @param item MenuItem được click
     * @return true nếu navigation thành công, false nếu bị block
     */
    private boolean handleBottomNavSelection(MenuItem item) {
        int id = item.getItemId();
        String currentClassName = this.getClass().getSimpleName();

        if (id == R.id.nav_home) {
            if (!currentClassName.equals("HomeActivity")) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true; // Navigation success

        } else if (id == R.id.nav_parking) {
            if (!currentClassName.equals("ParkingLotsActivity")) {
                Intent intent = new Intent(this, ParkingLotsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true; // Navigation success

        } else if (id == R.id.nav_ble) {
            // BLE toggle requires login
            if (!com.parkmate.android.utils.AuthHelper.isUserLoggedIn()) {
                com.parkmate.android.utils.AuthHelper.showLoginRequiredDialog(this, "sử dụng BLE toggle");
                return false; // Navigation blocked - don't highlight tab
            }
            // Navigate to BLE Toggle Activity
            if (!currentClassName.equals("BLEToggleActivity")) {
                Intent intent = new Intent(this, BLEToggleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true; // Navigation success

        } else if (id == R.id.nav_wallet) {
            // Wallet requires login
            if (!com.parkmate.android.utils.AuthHelper.isUserLoggedIn()) {
                com.parkmate.android.utils.AuthHelper.showLoginRequiredDialog(this, "xem ví tiền");
                return false; // Navigation blocked - don't highlight tab
            }
            if (!currentClassName.equals("WalletActivity")) {
                Intent intent = new Intent(this, WalletActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true; // Navigation success

        } else if (id == R.id.nav_account) {
            // Account requires login
            if (!com.parkmate.android.utils.AuthHelper.isUserLoggedIn()) {
                com.parkmate.android.utils.AuthHelper.showLoginRequiredDialog(this, "xem tài khoản");
                return false; // Navigation blocked - don't highlight tab
            }
            if (!currentClassName.equals("ProfileActivity")) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true; // Navigation success
        }

        return false; // Unknown item
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
