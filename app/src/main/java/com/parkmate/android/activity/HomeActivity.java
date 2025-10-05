package com.parkmate.android.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.parkmate.android.R;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        // Thiết lập toolbar với search bar (toolbar trong suốt + search)
        setupToolbarWithSearch(
            true,  // true = hiển thị menu (màn hình chính)
            "Tìm kiếm địa điểm đỗ xe...",  // placeholder
            v -> onSearchBarClicked(),  // click vào search bar
            v -> onFilterClicked()  // click vào filter
        );

        setupBottomNavigation(true, R.id.nav_home);

        // Xử lý insets cho layout gốc của màn hình home (id: main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.getPaddingBottom());
            return insets;
        });
    }

    /**
     * Xử lý khi click vào search bar
     */
    private void onSearchBarClicked() {
        // TODO: Mở màn hình tìm kiếm chi tiết (SearchActivity) sau này
        Toast.makeText(this, "Mở màn hình tìm kiếm", Toast.LENGTH_SHORT).show();
    }

    /**
     * Xử lý khi click vào filter button
     */
    private void onFilterClicked() {
        // TODO: Hiển thị bottom sheet bộ lọc sau này
        Toast.makeText(this, "Mở bộ lọc tìm kiếm", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }
}