package com.parkmate.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.parkmate.android.R;
import com.parkmate.android.model.request.LoginRequest;
import com.parkmate.android.model.response.ErrorResponse;
import com.parkmate.android.model.response.LoginResponse;
import com.parkmate.android.repository.AuthRepository;
import com.parkmate.android.utils.LoadingButton;
import com.parkmate.android.utils.TokenManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private MaterialButton btnLogin;
    private View fabBiometricLogin;
    private TextView tvRegisterLink, tvForgotPassword;

    private AuthRepository authRepository;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private LoadingButton loadingButton;
    private static final String PREF_LOGIN = "login_prefs";
    private static final String KEY_REMEMBER_EMAIL = "remember_email";

    private Handler timeoutWarningHandler;
    private Runnable timeoutWarningRunnable;
    private boolean isLoggingIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra xem user đã đăng nhập chưa (có token hợp lệ)
        if (checkIfAlreadyLoggedIn()) {
            navigateToHome();
            return;
        }

        setContentView(R.layout.layout_login);

        // Setup edge-to-edge display
        com.parkmate.android.utils.EdgeToEdgeHelper.setupEdgeToEdge(this);

        authRepository = new AuthRepository();
        initViews();
        restoreRememberedEmail();
        setupClickListeners();
        setupColoredRegisterText();

        // Kiểm tra xem có bị redirect do token hết hạn không
        checkSessionExpired();

        // KHÔNG auto-show biometric nữa, chỉ show khi user bấm nút
        // checkBiometricLogin();
    }

    /**
     * Kiểm tra và hiển thị biometric login nếu user đã bật
     */
    private void checkBiometricLogin() {
        com.parkmate.android.utils.BiometricManager biometricManager = com.parkmate.android.utils.BiometricManager
                .getInstance(this);

        if (biometricManager.isBiometricEnabled()) {
            // Delay một chút để UI render xong
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                showBiometricLogin();
            }, 500);
        }
    }

    /**
     * Hiển thị prompt biometric login
     */
    private void showBiometricLogin() {
        com.parkmate.android.utils.BiometricManager.getInstance(this).authenticate(this,
                new com.parkmate.android.utils.BiometricManager.BiometricCallback() {
                    @Override
                    public void onSuccess(String email, String password) {
                        // Auto login với credentials đã lưu
                        android.util.Log.d(TAG, "Biometric success - Email: " + email);

                        if (etEmail != null) {
                            etEmail.setText(email); // Truyền email vào field email
                        }
                        if (etPassword != null) {
                            etPassword.setText(password);
                        }

                        if (email == null || email.isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Không thể đăng nhập. Vui lòng thử lại.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        performLogin(email, password, false); // Đăng nhập với email và password
                    }

                    @Override
                    public void onError(String error) {
                        // User có thể chọn đăng nhập thủ công
                        android.util.Log.d(TAG, "Biometric login error: " + error);
                        // Không hiển thị toast error để tránh spam user
                    }
                });
    }

    /**
     * Kiểm tra và hiển thị thông báo nếu phiên đăng nhập hết hạn
     */
    private void checkSessionExpired() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("SESSION_EXPIRED", false)) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Kiểm tra xem user đã đăng nhập chưa bằng cách check token và userId
     * 
     * @return true nếu đã có token và userId hợp lệ
     */
    private boolean checkIfAlreadyLoggedIn() {
        try {
            String token = TokenManager.getInstance().getToken();
            String userId = com.parkmate.android.utils.UserManager.getInstance().getUserId();

            // Nếu có token và userId thì coi như đã đăng nhập
            boolean isLoggedIn = token != null && !token.isEmpty() &&
                    userId != null && !userId.isEmpty();

            if (isLoggedIn) {
                Log.d(TAG, "User already logged in - Token exists, navigating to Home");
            }

            return isLoggedIn;
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status", e);
            return false;
        }
    }

    /**
     * Chuyển về màn hình Home và xóa back stack để không quay lại LoginActivity
     */
    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        fabBiometricLogin = findViewById(R.id.fabBiometricLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        loadingButton = new LoadingButton(btnLogin);

        // Setup biometric button visibility
        setupBiometricButton();
    }

    /**
     * Setup biometric button - chỉ hiện khi có biometric enabled
     */
    private void setupBiometricButton() {
        try {
            com.parkmate.android.utils.BiometricManager biometricManager = com.parkmate.android.utils.BiometricManager
                    .getInstance(this);

            if (fabBiometricLogin != null) {
                // Chỉ hiện nút nếu:
                // 1. Device có biometric
                // 2. User đã bật biometric trong Profile
                boolean shouldShow = biometricManager.isBiometricAvailable() &&
                        biometricManager.isBiometricEnabled();

                fabBiometricLogin.setVisibility(shouldShow ? android.view.View.VISIBLE : android.view.View.GONE);

                android.util.Log.d(TAG, "Biometric button visibility: " + (shouldShow ? "VISIBLE" : "GONE"));
                android.util.Log.d(TAG, "Biometric available: " + biometricManager.isBiometricAvailable());
                android.util.Log.d(TAG, "Biometric enabled: " + biometricManager.isBiometricEnabled());
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error setting up biometric button", e);
            if (fabBiometricLogin != null) {
                fabBiometricLogin.setVisibility(android.view.View.GONE);
            }
        }
    }

    private void restoreRememberedEmail() {
        SharedPreferences sp = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        String remembered = sp.getString(KEY_REMEMBER_EMAIL, null);
        if (remembered != null && etEmail != null) {
            etEmail.setText(remembered);
            cbRememberMe.setChecked(true);
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = safeText(etEmail);
            String password = safeText(etPassword);
            boolean rememberMe = cbRememberMe.isChecked();
            if (validateInput(email, password)) {
                performLogin(email, password, rememberMe);
            }
        });

        // Biometric login button
        if (fabBiometricLogin != null) {
            fabBiometricLogin.setOnClickListener(v -> {
                android.util.Log.d(TAG, "Biometric button clicked");
                showBiometricLogin();
            });
        }

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void setupColoredRegisterText() {
        String text = "Chưa có tài khoản? Đăng ký";
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf("Đăng ký");
        int end = start + "Đăng ký".length();
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvRegisterLink.setText(ss);
        tvRegisterLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            isValid = false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            isValid = false;
        }
        return isValid;
    }

    private void performLogin(String email, String password, boolean rememberMe) {
        if (isLoggingIn) {
            Toast.makeText(this, "Đang xử lý đăng nhập, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }

        isLoggingIn = true;
        loadingButton.showLoading("Đang đăng nhập...");

        // Cảnh báo nếu đăng nhập quá 10 giây
        timeoutWarningHandler = new Handler(Looper.getMainLooper());
        timeoutWarningRunnable = () -> {
            if (isLoggingIn) {
                Toast.makeText(LoginActivity.this, "Kết nối chậm, vui lòng đợi thêm...", Toast.LENGTH_LONG).show();
            }
        };
        timeoutWarningHandler.postDelayed(timeoutWarningRunnable, 10000); // 10 giây

        LoginRequest req = new LoginRequest(email, password);
        compositeDisposable.add(
                authRepository.login(req)
                        .timeout(60, TimeUnit.SECONDS) // Timeout 60 giây
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> {
                                    cancelTimeoutWarning();
                                    isLoggingIn = false;
                                    loadingButton.hideLoading();
                                    handleLoginSuccess(resp, email, rememberMe);
                                },
                                throwable -> {
                                    cancelTimeoutWarning();
                                    isLoggingIn = false;
                                    loadingButton.hideLoading();
                                    handleLoginError(throwable);
                                }));
    }

    private void cancelTimeoutWarning() {
        if (timeoutWarningHandler != null && timeoutWarningRunnable != null) {
            timeoutWarningHandler.removeCallbacks(timeoutWarningRunnable);
        }
    }

    private void handleLoginSuccess(LoginResponse resp, String email, boolean rememberMe) {
        boolean ok = resp != null && (resp.getSuccess() == null || Boolean.TRUE.equals(resp.getSuccess())); // nhiều BE
                                                                                                            // không trả
                                                                                                            // success
        if (!ok) {
            Toast.makeText(this, resp != null && resp.getMessage() != null ? resp.getMessage() : "Đăng nhập thất bại",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Lưu token
        String rawToken = resp.getAnyToken();
        Log.d(TAG, "========== LOGIN SUCCESS ==========");
        Log.d(TAG, "Raw token from backend: "
                + (rawToken != null ? "YES (length=" + rawToken.length() + ")" : "NO - NULL!"));

        if (rawToken != null && !rawToken.isEmpty()) {
            String cleaned = rawToken.startsWith("Bearer ") ? rawToken.substring(7) : rawToken;
            Log.d(TAG, "Token after cleaning: " + cleaned.substring(0, Math.min(50, cleaned.length())) + "...");

            try {
                TokenManager.getInstance().saveToken(cleaned);

                // KIỂM TRA LẠI token đã được lưu chưa
                String verifyToken = TokenManager.getInstance().getToken();
                if (verifyToken != null && verifyToken.equals(cleaned)) {
                    Log.d(TAG, "✅ Token saved and verified successfully!");
                } else {
                    Log.e(TAG, "❌ Token save FAILED - verification mismatch!");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ EXCEPTION when saving token: " + e.getMessage(), e);
            }
        } else {
            Log.e(TAG, "❌ WARNING: Backend returned NULL or EMPTY token!");
        }

        // Lưu refresh token
        String refreshToken = null;
        if (resp.getData() != null && resp.getData().getAuthResponse() != null) {
            refreshToken = resp.getData().getAuthResponse().getRefreshToken();
        }

        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                TokenManager.getInstance().saveRefreshToken(refreshToken);
                Log.d(TAG, "✅ Refresh token saved successfully!");
            } catch (Exception e) {
                Log.e(TAG, "❌ EXCEPTION when saving refresh token: " + e.getMessage(), e);
            }
        } else {
            Log.w(TAG, "⚠️ No refresh token returned from backend");
        }

        // Lưu thông tin user - ƯU TIÊN LẤY userId TỪ data.userResponse.id (id: 9) thay
        // vì account.id (id: 13)
        try {
            String userId = null;
            String username = null;

            // Ưu tiên lấy từ data.userResponse.id (đây mới là userId thực sự để dùng cho
            // entityId)
            if (resp.getData() != null && resp.getData().getUserResponse() != null) {
                Long userResponseId = resp.getData().getUserResponse().getId();
                if (userResponseId != null) {
                    userId = String.valueOf(userResponseId);
                    Log.d(TAG, "✅ Got userId from userResponse.id: " + userId);
                }

                // Lấy email từ account làm username
                if (resp.getData().getUserResponse().getAccount() != null) {
                    username = resp.getData().getUserResponse().getAccount().getEmail();
                }
            }

            // Fallback: nếu không có userResponse.id thì lấy từ các field khác
            if (userId == null) {
                userId = resp.getUserId();
                if (userId == null && resp.getData() != null) {
                    userId = resp.getData().getUserId();
                }
                Log.d(TAG, "⚠️ Fallback userId: " + userId);
            }

            if (username == null) {
                username = resp.getUsername();
                if (username == null && resp.getData() != null) {
                    username = resp.getData().getUsername();
                }
            }

            // Fallback: lấy tên từ email nếu backend không trả username
            String userName = email.split("@")[0];

            com.parkmate.android.utils.UserManager.getInstance().saveUserInfo(userId, email, userName);

            // Lưu username riêng
            if (username != null && !username.isEmpty()) {
                com.parkmate.android.utils.UserManager.getInstance().setUsername(username);
            } else {
                // Fallback: dùng phần trước @ của email làm username
                com.parkmate.android.utils.UserManager.getInstance().setUsername(userName);
            }

            Log.d(TAG, "User info saved - ID: " + userId + ", Email: " + email + ", Username: " + username);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving user info", e);
        }

        // Lưu trạng thái xác thực căn cước (isIdVerified)
        try {
            boolean isIdVerified = false;
            if (resp.getData() != null &&
                    resp.getData().getUserResponse() != null &&
                    resp.getData().getUserResponse().getAccount() != null) {

                Boolean verified = resp.getData().getUserResponse().getAccount().getIsIdVerified();
                isIdVerified = verified != null && verified;

                // Lưu vào UserManager
                com.parkmate.android.utils.UserManager.getInstance().setIdVerified(isIdVerified);

                Log.d(TAG, "ID Verification status saved: " + isIdVerified);

                // Hiển thị cảnh báo nếu chưa xác thực
                if (!isIdVerified) {
                    // Delay một chút để cho màn hình Home load xong
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        showIdVerificationWarning();
                    }, 1000);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving ID verification status", e);
        }

        applyRememberMe(email, rememberMe);
        Toast.makeText(this, resp.getMessage() != null ? resp.getMessage() : "Đăng nhập thành công", Toast.LENGTH_SHORT)
                .show();
        // Chuyển sang màn hình chính (HomeActivity) và clear back stack để tránh quay
        // lại màn hình đăng nhập
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void applyRememberMe(String email, boolean remember) {
        SharedPreferences sp = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        if (remember) {
            sp.edit().putString(KEY_REMEMBER_EMAIL, email).apply();
        } else {
            sp.edit().remove(KEY_REMEMBER_EMAIL).apply();
        }
    }

    private void handleLoginError(Throwable t) {
        // Use centralized ApiErrorHandler
        String errorCode = com.parkmate.android.utils.ApiErrorHandler.getErrorCode(t);
        String errorMessage = com.parkmate.android.utils.ApiErrorHandler.parseError(t);

        // Parse response to get BE message if available
        ErrorResponse er = com.parkmate.android.utils.ApiErrorHandler.parseErrorResponse(t);
        if (er != null && er.getMessage() != null && !er.getMessage().isEmpty()) {
            // Use BE message directly (contains details like lock duration)
            errorMessage = er.getMessage();
        }

        // Handle account locked - show dialog instead of toast
        if ("ACCOUNT_TEMPORARILY_LOCKED".equals(errorCode)) {
            showAccountLockedDialog(errorMessage);
            return;
        }

        // Special handling for login: UNCATEGORIZED_EXCEPTION usually means wrong
        // credentials
        if ("UNCATEGORIZED_EXCEPTION".equals(errorCode) ||
                "PASSWORD_MISMATCH".equals(errorCode) ||
                "PASSWORD_MISMATCH_WITH_ATTEMPTS".equals(errorCode)) {
            errorMessage = "Sai email hoặc mật khẩu";
            if (etPassword != null) {
                etPassword.setError(errorMessage);
            }
        }

        // Apply field errors if any
        if (er != null && er.getError() != null && er.getError().getFieldErrors() != null) {
            for (ErrorResponse.FieldError fe : er.getError().getFieldErrors()) {
                applyFieldError(fe);
            }
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Show dialog when account is temporarily locked
     */
    private void showAccountLockedDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Tài khoản bị khóa")
                .setMessage(message)
                .setPositiveButton("Đã hiểu", null)
                .setCancelable(true)
                .show();
    }

    private void applyFieldError(ErrorResponse.FieldError fe) {
        if (fe == null)
            return;
        String field = fe.getField();
        String msg = fe.getMessage() != null ? fe.getMessage() : "Giá trị không hợp lệ";
        if (field == null)
            return;
        switch (field) {
            case "email":
                if (etEmail != null)
                    etEmail.setError(msg);
                break;
            case "password":
                if (etPassword != null)
                    etPassword.setError(msg);
                break;
        }
    }

    private String safeText(TextInputEditText et) {
        return et == null || et.getText() == null ? "" : et.getText().toString().trim();
    }

    /**
     * Hiển thị dialog yêu cầu xác thực căn cước công dân
     */
    private void showIdVerificationWarning() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cần xác thực danh tính")
                .setMessage("Tài khoản của bạn chưa được xác thực.\n\n" +
                        "Vui lòng cập nhật thông tin căn cước công dân để sử dụng đầy đủ tính năng (tạo đặt chỗ, đăng ký gói).")
                .setPositiveButton("Xác thực ngay", (dialog, which) -> {
                    // TODO: Navigate to ID verification screen khi có màn hình
                    Toast.makeText(this, "Chức năng xác thực căn cước đang được phát triển", Toast.LENGTH_SHORT).show();
                    // Intent intent = new Intent(this, IdVerificationActivity.class);
                    // startActivity(intent);
                })
                .setNegativeButton("Để sau", null)
                .setCancelable(true)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimeoutWarning();
        compositeDisposable.clear();
    }
}
