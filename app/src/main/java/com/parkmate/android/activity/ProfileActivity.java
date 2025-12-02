package com.parkmate.android.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.parkmate.android.R;
import com.parkmate.android.model.response.UserInfoResponse;
import com.parkmate.android.network.ApiClient;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfileActivity extends BaseActivity {

    private ImageButton btnBack;
    private ImageButton btnEditProfile;
    private ImageView profileAvatar;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private ConstraintLayout menuMyBookings;
    private ConstraintLayout menuMyVehicles;
    private ConstraintLayout menuPaymentMethods;
    private ConstraintLayout menuVerifyCccd; // NEW
    private ConstraintLayout menuParkingHistory;
    private ConstraintLayout menuMySubscriptions;
    private ConstraintLayout menuLogout;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup toolbar with title, no back button (vì đã có bottom nav)
        setupToolbarWithTitle(getString(R.string.profile_title), false);

        // Setup bottom navigation with Account tab selected
        setupBottomNavigation(true, R.id.nav_account);

        // Load notification badge count
        loadNotificationBadgeCount();

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
        menuVerifyCccd = findViewById(R.id.menuVerifyCccd); // NEW
        menuParkingHistory = findViewById(R.id.menuParkingHistory);
        menuMySubscriptions = findViewById(R.id.menuMySubscriptions);
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
            String userId = userManager.getUserId();

            // Hiển thị username thay vì name
            String username = userManager.getUsername();
            if (tvUserName != null && username != null) {
                tvUserName.setText(username);
            } else if (tvUserName != null) {
                // Fallback nếu không có username
                tvUserName.setText(R.string.profile_user_name);
            }

            // Hiển thị email user (giữ nguyên)
            String userEmail = userManager.getUserEmail();
            if (tvUserEmail != null && userEmail != null) {
                tvUserEmail.setText(userEmail);
            }

            // Load profile image từ API
            if (userId != null && !userId.isEmpty()) {
                loadProfileImage(userId);
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

    /**
     * Load profile image từ API
     */
    private void loadProfileImage(String userId) {
        android.util.Log.d("ProfileActivity", "Loading profile image for userId: " + userId);

        compositeDisposable.add(
                ApiClient.getApiService().getUserInfo(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    android.util.Log.d("ProfileActivity", "getUserInfo response received");
                                    if (response != null && response.isSuccess() && response.getData() != null) {
                                        UserInfoResponse.UserData userData = response.getData();
                                        String profileImageUrl = userData.getProfilePicturePresignedUrl();

                                        android.util.Log.d("ProfileActivity", "Profile image URL: " + profileImageUrl);

                                        if (profileImageUrl != null && !profileImageUrl.isEmpty() && profileAvatar != null) {
                                            android.util.Log.d("ProfileActivity", "Loading image with Glide");
                                            Glide.with(ProfileActivity.this)
                                                    .load(profileImageUrl)
                                                    .placeholder(R.drawable.ic_person_24)
                                                    .error(R.drawable.ic_person_24)
                                                    .circleCrop()
                                                    .into(profileAvatar);
                                        } else {
                                            android.util.Log.d("ProfileActivity", "No profile image URL or avatar view is null");
                                        }

                                        // Update username if available from API
                                        String firstName = userData.getFirstName();
                                        String lastName = userData.getLastName();
                                        if (firstName != null && lastName != null && !firstName.isEmpty() && !lastName.isEmpty()) {
                                            String fullName = firstName + " " + lastName;
                                            android.util.Log.d("ProfileActivity", "Updating username to: " + fullName);
                                            if (tvUserName != null) {
                                                tvUserName.setText(fullName);
                                            }
                                            // Update UserManager
                                            com.parkmate.android.utils.UserManager.getInstance().setUsername(fullName);
                                        }
                                    } else {
                                        android.util.Log.e("ProfileActivity", "Invalid response or no data");
                                    }
                                },
                                error -> {
                                    // Silently fail - just use default avatar
                                    android.util.Log.e("ProfileActivity", "Error loading profile image", error);
                                }
                        )
        );
    }

    private void setupClickListeners() {
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                // Navigate to edit profile screen
                android.content.Intent intent = new android.content.Intent(this, EditProfileActivity.class);
                startActivityForResult(intent, 100);
            });
        }

        if (profileAvatar != null) {
            profileAvatar.setOnClickListener(v -> {
                // Navigate to edit profile screen (same as edit button)
                android.content.Intent intent = new android.content.Intent(this, EditProfileActivity.class);
                startActivityForResult(intent, 100);
            });
        }

        if (menuMyBookings != null) {
            menuMyBookings.setOnClickListener(v -> {
                // Navigate to reservation list screen
                android.content.Intent intent = new android.content.Intent(this, ReservationListActivity.class);
                startActivity(intent);
            });
        }

        if (menuMyVehicles != null) {
            menuMyVehicles.setOnClickListener(v -> {
                // Navigate to my vehicles screen
                android.content.Intent intent = new android.content.Intent(this, VehicleActivity.class);
                startActivity(intent);
            });
        }

        if (menuPaymentMethods != null) {
            menuPaymentMethods.setOnClickListener(v -> {
                // Navigate to Account QR Code screen
                android.content.Intent intent = new android.content.Intent(this, AccountQrActivity.class);
                startActivity(intent);
            });
        }

        if (menuVerifyCccd != null) {
            menuVerifyCccd.setOnClickListener(v -> {
                // Navigate to verify CCCD screen
                android.content.Intent intent = new android.content.Intent(this, VerifyCccdActivity.class);
                startActivity(intent);
            });
        }

        if (menuParkingHistory != null) {
            menuParkingHistory.setOnClickListener(v -> {
                // Navigate to parking history screen
                android.content.Intent intent = new android.content.Intent(this, ParkingHistoryActivity.class);
                startActivity(intent);
            });
        }


        if (menuMySubscriptions != null) {
            menuMySubscriptions.setOnClickListener(v -> {
                // Navigate to user subscriptions list screen
                android.content.Intent intent = new android.content.Intent(this, UserSubscriptionListActivity.class);
                startActivity(intent);
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

    @Override
    protected void customizeToolbar() {
        // Hide navigation button for main screen
        if (ivNavigation != null) {
            ivNavigation.setVisibility(View.GONE);
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
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh profile after edit
            loadUserInfo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}