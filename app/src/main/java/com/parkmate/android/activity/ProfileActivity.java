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
    private ConstraintLayout menuMyRatings; // NEW
    private androidx.appcompat.widget.SwitchCompat switchBiometric;
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
        menuMyRatings = findViewById(R.id.menuMyRatings); // NEW
        switchBiometric = findViewById(R.id.switchBiometric);
        menuLogout = findViewById(R.id.menuLogout);

        // Hide the btnBack since we're using toolbar navigation
        if (btnBack != null) {
            btnBack.setVisibility(android.view.View.GONE);
        }

        // Setup biometric switch
        setupBiometricSwitch();
    }

    /**
     * Setup biometric switch state
     */
    private void setupBiometricSwitch() {
        try {
            android.util.Log.d("BiometricDebug", "Setting up biometric switch");

            if (switchBiometric == null) {
                android.util.Log.e("BiometricDebug", "Switch is null! ID not found in layout");
                Toast.makeText(this, "Lỗi: Switch biometric không tìm thấy trong layout", Toast.LENGTH_LONG).show();
                return;
            }

            android.util.Log.d("BiometricDebug", "Switch found, visibility: " + switchBiometric.getVisibility() + ", enabled: " + switchBiometric.isEnabled());

            com.parkmate.android.utils.BiometricManager biometricManager = null;
            try {
                biometricManager = com.parkmate.android.utils.BiometricManager.getInstance(this);
                android.util.Log.d("BiometricDebug", "BiometricManager created successfully");
            } catch (Exception e) {
                android.util.Log.e("BiometricDebug", "Error creating BiometricManager - dependencies not loaded", e);
                // Disable switch if BiometricManager not available
                switchBiometric.setEnabled(false);
                switchBiometric.setChecked(false);
                switchBiometric.setVisibility(android.view.View.VISIBLE);
                Toast.makeText(this, "Biometric chưa khả dụng. Vui lòng sync Gradle!", Toast.LENGTH_LONG).show();
                return;
            }

            if (biometricManager.isBiometricAvailable()) {
                android.util.Log.d("BiometricDebug", "Biometric is available on device");
                String currentUserId = com.parkmate.android.utils.UserManager.getInstance().getUserId();
                boolean isEnabled = biometricManager.isBiometricEnabledForUser(currentUserId);
                android.util.Log.d("BiometricDebug", "Biometric enabled for user: " + isEnabled);
                switchBiometric.setChecked(isEnabled);
                switchBiometric.setEnabled(true);
                switchBiometric.setVisibility(android.view.View.VISIBLE);
                android.util.Log.d("BiometricDebug", "Switch configured: visible, enabled, checked=" + isEnabled);
            } else {
                android.util.Log.d("BiometricDebug", "Biometric not available on device");
                android.util.Log.w("BiometricDebug", "Device không có biometric hoặc chưa setup vân tay!");

                // Vẫn hiển thị switch nhưng disable
                switchBiometric.setVisibility(android.view.View.VISIBLE);
                switchBiometric.setEnabled(false);
                switchBiometric.setChecked(false);

                android.util.Log.d("BiometricDebug", "Switch configured: visible, disabled (grey)");

                // Hiển thị hướng dẫn
                Toast.makeText(this,
                    "Thiết bị không hỗ trợ sinh trắc học hoặc chưa setup vân tay.\n" +
                    "Vào Settings > Security > Fingerprint để thêm vân tay.",
                    Toast.LENGTH_LONG).show();
            }

            // Log final state
            android.util.Log.d("BiometricDebug", "Final switch state - Visibility: " + switchBiometric.getVisibility() +
                ", Enabled: " + switchBiometric.isEnabled() +
                ", Checked: " + switchBiometric.isChecked());

        } catch (NoClassDefFoundError e) {
            android.util.Log.e("BiometricDebug", "BiometricManager class not found - Gradle not synced!", e);
            if (switchBiometric != null) {
                switchBiometric.setVisibility(android.view.View.VISIBLE);
                switchBiometric.setEnabled(false);
                switchBiometric.setChecked(false);
            }
            Toast.makeText(this, "Cần sync Gradle để sử dụng Biometric!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            android.util.Log.e("BiometricDebug", "Error setting up biometric switch", e);
            if (switchBiometric != null) {
                switchBiometric.setVisibility(android.view.View.VISIBLE);
                switchBiometric.setEnabled(false);
                switchBiometric.setChecked(false);
            }
            Toast.makeText(this, "Lỗi khởi tạo Biometric: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

        if (menuMyRatings != null) {
            menuMyRatings.setOnClickListener(v -> {
                // Navigate to my ratings screen
                android.content.Intent intent = new android.content.Intent(this, MyRatingsActivity.class);
                startActivity(intent);
            });
        }

        if (switchBiometric != null) {
            switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
                try {
                    android.util.Log.d("BiometricDebug", "Switch changed: " + isChecked + ", isPressed: " + buttonView.isPressed());

                    if (!buttonView.isPressed()) {
                        // Skip if change is from code, not user interaction
                        android.util.Log.d("BiometricDebug", "Skipping - not user interaction");
                        return;
                    }

                    // Check if BiometricManager is available
                    com.parkmate.android.utils.BiometricManager biometricManager = null;
                    try {
                        biometricManager = com.parkmate.android.utils.BiometricManager.getInstance(this);
                        android.util.Log.d("BiometricDebug", "BiometricManager instance created");
                    } catch (Exception e) {
                        android.util.Log.e("BiometricDebug", "Error creating BiometricManager", e);
                        Toast.makeText(this, "Lỗi: Biometric Manager không khả dụng. Vui lòng sync Gradle!", Toast.LENGTH_LONG).show();
                        switchBiometric.setChecked(false);
                        return;
                    }

                    if (isChecked) {
                        android.util.Log.d("BiometricDebug", "Showing enable dialog");
                        // Show dialog to enter password
                        showEnableBiometricDialog();
                    } else {
                        android.util.Log.d("BiometricDebug", "Disabling biometric");
                        biometricManager.disableBiometric();
                        Toast.makeText(this, "Đã tắt xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("BiometricDebug", "Error in switch listener", e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    switchBiometric.setChecked(false);
                }
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                showLogoutConfirmDialog();
            });
        }
    }

    /**
     * Show dialog to enable biometric authentication
     */
    private void showEnableBiometricDialog() {
        try {
            android.util.Log.d("BiometricDebug", "Creating enable biometric dialog");

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_enable_biometric, null);
            com.google.android.material.textfield.TextInputEditText etPassword =
                dialogView.findViewById(R.id.etPassword);

            android.util.Log.d("BiometricDebug", "Dialog view inflated successfully");

            builder.setView(dialogView)
                .setTitle("Bật xác thực sinh trắc học")
                .setMessage("Nhập mật khẩu hiện tại để xác nhận")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    try {
                        String password = etPassword.getText().toString().trim();

                        if (password.isEmpty()) {
                            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                            switchBiometric.setChecked(false);
                            return;
                        }

                        String email = com.parkmate.android.utils.UserManager.getInstance().getUserEmail();
                        String userId = com.parkmate.android.utils.UserManager.getInstance().getUserId();

                        android.util.Log.d("BiometricDebug", "Email from UserManager: " + email);
                        android.util.Log.d("BiometricDebug", "UserId from UserManager: " + userId);

                        // Show debug info
                        Toast.makeText(this, "Email: " + (email != null ? email : "NULL") + "\nUserID: " + (userId != null ? "OK" : "NULL"), Toast.LENGTH_LONG).show();

                        // Validate email
                        if (email == null || email.isEmpty()) {
                            android.util.Log.e("BiometricDebug", "Email is NULL! Cannot enable biometric without email");
                            switchBiometric.setChecked(false);
                            Toast.makeText(this, "❌ Email NULL! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (userId == null || userId.isEmpty()) {
                            android.util.Log.e("BiometricDebug", "UserId is NULL!");
                            switchBiometric.setChecked(false);
                            Toast.makeText(this, "❌ UserID NULL! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        com.parkmate.android.utils.BiometricManager biometricManager =
                            com.parkmate.android.utils.BiometricManager.getInstance(this);
                        boolean success = biometricManager.enableBiometric(userId, email, password);

                        android.util.Log.d("BiometricDebug", "Enable biometric result: " + success);

                        if (success) {
                            Toast.makeText(this, "✅ Đã lưu:\nEmail: " + email + "\nPassword: ***", Toast.LENGTH_LONG).show();
                        } else {
                            switchBiometric.setChecked(false);
                            Toast.makeText(this, "❌ Lỗi khi lưu biometric!", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("BiometricDebug", "Error enabling biometric", e);
                        switchBiometric.setChecked(false);
                        Toast.makeText(this, "❌ Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    switchBiometric.setChecked(false);
                    dialog.dismiss();
                })
                .setOnCancelListener(dialog -> {
                    switchBiometric.setChecked(false);
                })
                .show();

            android.util.Log.d("BiometricDebug", "Dialog shown successfully");
        } catch (Exception e) {
            android.util.Log.e("BiometricDebug", "Error showing enable biometric dialog", e);
            Toast.makeText(this, "Lỗi hiển thị dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
            switchBiometric.setChecked(false);
        }
    }

    /**
     * Show logout confirmation dialog with option to keep biometric
     */
    private void showLogoutConfirmDialog() {
        com.parkmate.android.utils.BiometricManager biometricManager =
            com.parkmate.android.utils.BiometricManager.getInstance(this);

        // Kiểm tra xem có biometric được bật không
        if (biometricManager.isBiometricEnabled()) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có muốn giữ xác thực sinh trắc học cho lần đăng nhập sau?")
                .setPositiveButton("Giữ lại", (dialog, which) -> {
                    handleLogout(false); // Không xóa biometric
                })
                .setNegativeButton("Xóa hết", (dialog, which) -> {
                    handleLogout(true); // Xóa cả biometric
                })
                .setNeutralButton("Hủy", null)
                .show();
        } else {
            // Nếu không có biometric, logout bình thường
            new android.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    handleLogout(false);
                })
                .setNegativeButton("Hủy", null)
                .show();
        }
    }

    private void handleLogout() {
        handleLogout(false);
    }

    /**
     * Handle logout with option to clear biometric
     *
     * GUEST MODE: Sau khi logout, user được chuyển về HomeActivity để browse như guest
     *
     * @param clearBiometric true = xóa biometric data, false = giữ lại
     */
    private void handleLogout(boolean clearBiometric) {
        try {
            // Chỉ xóa biometric khi user chủ động chọn
            if (clearBiometric) {
                com.parkmate.android.utils.BiometricManager.getInstance(this).disableBiometric();
            }

            com.parkmate.android.utils.UserManager.getInstance().clearUserInfo();
            com.parkmate.android.utils.TokenManager.getInstance().clearToken();

            // Clear guest welcome message flag để hiển thị lại khi logout
            android.content.SharedPreferences prefs = getSharedPreferences("guest_mode", MODE_PRIVATE);
            prefs.edit().putBoolean("welcome_shown_this_session", false).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Đã đăng xuất. Bạn có thể tiếp tục browse như khách.", Toast.LENGTH_LONG).show();

        // GUEST MODE: Chuyển về HomeActivity thay vì LoginActivity
        // User có thể tiếp tục browse app như guest
        android.content.Intent intent = new android.content.Intent(this, HomeActivity.class);
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


