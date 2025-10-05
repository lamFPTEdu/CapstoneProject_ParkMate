package com.parkmate.android.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.parkmate.android.R;

public class ProfileActivity extends BaseActivity {

    private ImageButton btnBack;
    private ImageButton btnEditProfile;
    private ImageView profileAvatar;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private ConstraintLayout menuMyBookings;
    private ConstraintLayout menuMyVehicles;
    private ConstraintLayout menuPaymentMethods;
    private ConstraintLayout menuParkingHistory;
    private ConstraintLayout menuSettings;
    private ConstraintLayout menuHelpSupport;
    private ConstraintLayout menuLogout;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup toolbar without search
        setupToolbar(false); // false = show back button

        // Setup bottom navigation with Account tab selected
        setupBottomNavigation(true, R.id.nav_account);

        initProfileViews();
        loadUserInfo(); // Load thông tin user đã đăng nhập
        setupClickListeners();
    }

    private void initProfileViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        profileAvatar = findViewById(R.id.profileAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        menuMyBookings = findViewById(R.id.menuMyBookings);
        menuMyVehicles = findViewById(R.id.menuMyVehicles);
        menuPaymentMethods = findViewById(R.id.menuPaymentMethods);
        menuParkingHistory = findViewById(R.id.menuParkingHistory);
        menuSettings = findViewById(R.id.menuSettings);
        menuHelpSupport = findViewById(R.id.menuHelpSupport);
        menuLogout = findViewById(R.id.menuLogout);

        // Hide the btnBack since we're using toolbar navigation
        if (btnBack != null) {
            btnBack.setVisibility(android.view.View.GONE);
        }
    }

    /**
     * Load thông tin user từ UserManager và hiển thị lên UI
     */
    private void loadUserInfo() {
        try {
            com.parkmate.android.utils.UserManager userManager = com.parkmate.android.utils.UserManager.getInstance();

            // Hiển thị username thay vì name
            String username = userManager.getUsername();
            if (tvUserName != null && username != null) {
                tvUserName.setText(username);
            } else if (tvUserName != null) {
                // Fallback nếu không có username
                tvUserName.setText("User");
            }

            // Hiển thị email user (giữ nguyên)
            String userEmail = userManager.getUserEmail();
            if (tvUserEmail != null && userEmail != null) {
                tvUserEmail.setText(userEmail);
            }

        } catch (Exception e) {
            // Nếu chưa đăng nhập, hiển thị thông tin mặc định
            if (tvUserName != null) {
                tvUserName.setText(R.string.profile_user_name);
            }
            if (tvUserEmail != null) {
                tvUserEmail.setText(R.string.profile_user_email);
            }
        }
    }

    private void setupClickListeners() {
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                // TODO: Navigate to edit profile screen
                Toast.makeText(this, "Chỉnh sửa hồ sơ", Toast.LENGTH_SHORT).show();
            });
        }

        if (profileAvatar != null) {
            profileAvatar.setOnClickListener(v -> {
                // TODO: Show avatar options (change photo, view photo)
                Toast.makeText(this, "Thay đổi ảnh đại diện", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuMyBookings != null) {
            menuMyBookings.setOnClickListener(v -> {
                // TODO: Navigate to my bookings screen
                Toast.makeText(this, "Đặt chỗ của tôi", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuMyVehicles != null) {
            menuMyVehicles.setOnClickListener(v -> {
                // TODO: Navigate to my vehicles screen
                Toast.makeText(this, "Quản lý xe của tôi", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuPaymentMethods != null) {
            menuPaymentMethods.setOnClickListener(v -> {
                // TODO: Navigate to payment methods screen
                Toast.makeText(this, "Phương thức thanh toán", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuParkingHistory != null) {
            menuParkingHistory.setOnClickListener(v -> {
                // TODO: Navigate to parking history screen
                Toast.makeText(this, "Lịch sử đỗ xe", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuSettings != null) {
            menuSettings.setOnClickListener(v -> {
                // TODO: Navigate to settings screen
                Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuHelpSupport != null) {
            menuHelpSupport.setOnClickListener(v -> {
                // TODO: Navigate to help & support screen
                Toast.makeText(this, "Trợ giúp & Hỗ trợ", Toast.LENGTH_SHORT).show();
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                // TODO: Show logout confirmation dialog
                handleLogout();
            });
        }
    }

    private void handleLogout() {
        // Xóa thông tin user và token
        try {
            com.parkmate.android.utils.UserManager.getInstance().clearUserInfo();
            com.parkmate.android.utils.TokenManager.getInstance().clearToken();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình đăng nhập và clear back stack
        android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP |
                       android.content.Intent.FLAG_ACTIVITY_NEW_TASK |
                       android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}