package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.parkmate.android.R;

/**
 * VÍ DỤ: Activity sử dụng toolbar với chế độ search
 *
 * Cách sử dụng:
 * 1. Kế thừa từ BaseActivity
 * 2. Gọi setupToolbarWithSearch() để hiển thị search bar
 * 3. Xử lý sự kiện click vào search bar và filter button
 */
public class SearchExampleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiết lập toolbar với chế độ search (toolbar trong suốt + search bar)
        setupToolbarWithSearch(
            false,  // false = hiển thị nút back (true = hiển thị menu)
            "Tìm kiếm địa điểm đỗ xe...",  // placeholder text
            v -> onSearchBarClicked(),  // xử lý khi click vào search bar
            v -> onFilterClicked()  // xử lý khi click vào filter button (null nếu không dùng)
        );

        // Thiết lập bottom navigation
        setupBottomNavigation(true, R.id.nav_home);
    }

    /**
     * Xử lý khi click vào search bar
     * Thường sẽ mở màn hình search chi tiết
     */
    private void onSearchBarClicked() {
        Toast.makeText(this, "Mở màn hình tìm kiếm", Toast.LENGTH_SHORT).show();
        // TODO: Mở màn hình search detail
        // Intent intent = new Intent(this, SearchDetailActivity.class);
        // startActivity(intent);
    }

    /**
     * Xử lý khi click vào filter button
     * Thường sẽ mở bottom sheet hoặc dialog filter
     */
    private void onFilterClicked() {
        Toast.makeText(this, "Mở bộ lọc", Toast.LENGTH_SHORT).show();
        // TODO: Mở filter dialog/bottom sheet
        // FilterBottomSheet.show(getSupportFragmentManager());
    }

    @Override
    protected int getLayoutResourceId() {
        // Return layout resource của activity này
        return R.layout.activity_home; // Thay bằng layout thực tế của bạn
    }
}


/**
 * VÍ DỤ 2: Activity chỉ dùng toolbar thường (không có search)
 * Toolbar trong suốt, chỉ có nút back/menu
 */
class NormalToolbarExampleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiết lập toolbar thường (không có search, chỉ nút back/menu)
        setupToolbar(false);  // false = nút back, true = nút menu

        // Thiết lập bottom navigation
        setupBottomNavigation(true, R.id.nav_home);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }
}
