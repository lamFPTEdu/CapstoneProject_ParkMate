package com.parkmate.android.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.parkmate.android.R;
import com.parkmate.android.model.response.ErrorResponse;
import com.parkmate.android.repository.AuthRepository;
import com.parkmate.android.utils.LoadingButton;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    private ViewFlipper viewFlipper;
    private androidx.appcompat.widget.Toolbar toolbar;
    private String userEmail;
    private AuthRepository authRepository;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private LoadingButton loadingButtonSend;
    private LoadingButton loadingButtonVerify;
    private LoadingButton loadingButtonReset;

    // Email Screen Views
    private TextInputEditText etEmail;
    private Button btnSend;

    // Check Email Screen Views
    private TextView tvUserEmail;
    private Button btnContinue;

    // OTP Screen Views
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private TextView tvResendCode;
    private Button btnVerify;

    // New Password Screen Views
    private TextInputEditText etResetEmail, etNewPassword, etConfirmPassword;
    private Button btnResetPassword;

    // Success Screen View
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Setup edge-to-edge display
        com.parkmate.android.utils.EdgeToEdgeHelper.setupEdgeToEdge(this);

        authRepository = new AuthRepository();
        initViews();
        setupListeners();

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize Email Screen Views
        View emailView = viewFlipper.getChildAt(0);
        etEmail = emailView.findViewById(R.id.etEmail);
        btnSend = emailView.findViewById(R.id.btnSend);
        loadingButtonSend = new LoadingButton(btnSend);

        // Initialize Check Email Screen Views
        View checkEmailView = viewFlipper.getChildAt(1);
        tvUserEmail = checkEmailView.findViewById(R.id.tvUserEmail);
        btnContinue = checkEmailView.findViewById(R.id.btnContinue);

        // Initialize OTP Screen Views
        View otpView = viewFlipper.getChildAt(2);
        etOtp1 = otpView.findViewById(R.id.etOtp1);
        etOtp2 = otpView.findViewById(R.id.etOtp2);
        etOtp3 = otpView.findViewById(R.id.etOtp3);
        etOtp4 = otpView.findViewById(R.id.etOtp4);
        etOtp5 = otpView.findViewById(R.id.etOtp5);
        etOtp6 = otpView.findViewById(R.id.etOtp6);
        tvResendCode = otpView.findViewById(R.id.tvResendCode);
        btnVerify = otpView.findViewById(R.id.btnVerify);
        loadingButtonVerify = new LoadingButton(btnVerify);

        // Initialize New Password Screen Views
        View newPasswordView = viewFlipper.getChildAt(3);
        etResetEmail = newPasswordView.findViewById(R.id.etEmail);
        etNewPassword = newPasswordView.findViewById(R.id.etNewPassword);
        etConfirmPassword = newPasswordView.findViewById(R.id.etConfirmPassword);
        btnResetPassword = newPasswordView.findViewById(R.id.btnResetPassword);
        loadingButtonReset = new LoadingButton(btnResetPassword);

        // Initialize Success Screen Views
        View successView = viewFlipper.getChildAt(4);
        TextView tvTitle = successView.findViewById(R.id.tvTitle);
        TextView tvDescription = successView.findViewById(R.id.tvDescription);
        btnLogin = successView.findViewById(R.id.btnLogin);

        // SET TEXT CHO CHỨC NĂNG ĐẶT LẠI MẬT KHẨU
        if (tvTitle != null) {
            tvTitle.setText(R.string.success);  // "Đặt lại mật khẩu thành công!"
        }
        if (tvDescription != null) {
            tvDescription.setText(R.string.success_description);  // "Mật khẩu của bạn đã được đặt lại thành công."
        }
        if (btnLogin != null) {
            btnLogin.setText(R.string.login);  // "Đăng nhập"
        }
    }

    private void setupListeners() {
        // Toolbar navigation listener
        toolbar.setNavigationOnClickListener(v -> handleBackPress());

        // Email Screen Listeners
        btnSend.setOnClickListener(v -> {
            if (validateEmail()) {
                userEmail = etEmail.getText().toString().trim();
                // Here would be API call to send OTP to email
                sendOtp(userEmail);
            }
        });

        // Check Email Screen Listeners
        btnContinue.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(2); // Move to OTP screen
        });

        // OTP Screen Listeners
        setupOtpInputs();

        // Ẩn nút "Gửi lại mã" vì resend OTP chỉ dành cho Register, không dành cho Forgot Password
        if (tvResendCode != null) {
            tvResendCode.setVisibility(View.GONE);
        }
        btnVerify.setOnClickListener(v -> {
            if (validateOtp()) {
                // Here would be API call to verify OTP
                verifyOtp();
            }
        });

        // New Password Screen Listeners
        btnResetPassword.setOnClickListener(v -> {
            if (validatePasswords()) {
                // Here would be API call to reset password
                resetPassword();
            }
        });

        // Success Screen Listeners
        btnLogin.setOnClickListener(v -> {
            // Navigate to login screen
            navigateToLogin();
        });
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email cannot be empty");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            return false;
        }
        return true;
    }

    private void sendOtp(String email) {
        loadingButtonSend.showLoading("Đang gửi...");

        compositeDisposable.add(
                authRepository.forgotPassword(email)
                        .timeout(30, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    loadingButtonSend.hideLoading();
                                    if (response != null && (response.getSuccess() == null || Boolean.TRUE.equals(response.getSuccess()))) {
                                        String message = response.getMessage() != null ? response.getMessage() : "Mã xác thực đã được gửi đến email của bạn";
                                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                        tvUserEmail.setText(email);
                                        viewFlipper.setDisplayedChild(1); // Move to Check Email screen
                                    } else {
                                        Toast.makeText(this, response.getMessage() != null ? response.getMessage() : "Gửi mã thất bại", Toast.LENGTH_LONG).show();
                                    }
                                },
                                throwable -> {
                                    loadingButtonSend.hideLoading();
                                    handleError(throwable, "Không thể gửi mã xác thực");
                                }
                        )
        );
    }

    private void setupOtpInputs() {
        EditText[] otpFields = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};

        for (int i = 0; i < otpFields.length; i++) {
            final int currentIndex = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpFields.length - 1) {
                        otpFields[currentIndex + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private boolean validateOtp() {
        // Get OTP from all fields
        String otp = etOtp1.getText().toString() +
                etOtp2.getText().toString() +
                etOtp3.getText().toString() +
                etOtp4.getText().toString() +
                etOtp5.getText().toString() +
                etOtp6.getText().toString();

        if (otp.length() != 6) {
            Toast.makeText(this, "Please enter a valid 6-digit otp", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void verifyOtp() {
        // Lưu OTP code vào biến để dùng sau
        // Hiển thị email trong màn hình reset password (disabled)
        if (etResetEmail != null && userEmail != null) {
            etResetEmail.setText(userEmail);
        }
        viewFlipper.setDisplayedChild(3); // Move to New Password screen
    }

    private boolean validatePasswords() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Password cannot be empty");
            return false;
        } else if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            return false;
        } else if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm password cannot be empty");
            return false;
        } else if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void resetPassword() {
        loadingButtonReset.showLoading("Đang đặt lại...");

        // Lấy mã OTP từ các field
        String resetCode = etOtp1.getText().toString() +
                etOtp2.getText().toString() +
                etOtp3.getText().toString() +
                etOtp4.getText().toString() +
                etOtp5.getText().toString() +
                etOtp6.getText().toString();

        String newPassword = etNewPassword.getText().toString().trim();

        compositeDisposable.add(
                authRepository.resetPassword(userEmail, resetCode, newPassword)
                        .timeout(30, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    loadingButtonReset.hideLoading();
                                    if (response != null && (response.getSuccess() == null || Boolean.TRUE.equals(response.getSuccess()))) {
                                        String message = response.getMessage() != null ? response.getMessage() : "Đặt lại mật khẩu thành công";
                                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                        viewFlipper.setDisplayedChild(4); // Move to Success screen
                                        hideBackButton(); // Ẩn nút back trên toolbar
                                    } else {
                                        Toast.makeText(this, response.getMessage() != null ? response.getMessage() : "Đặt lại mật khẩu thất bại", Toast.LENGTH_LONG).show();
                                    }
                                },
                                throwable -> {
                                    loadingButtonReset.hideLoading();
                                    handleError(throwable, "Không thể đặt lại mật khẩu");
                                }
                        )
        );
    }

    /**
     * Ẩn nút back trên toolbar (dùng khi đến màn hình Success)
     */
    private void hideBackButton() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void handleBackPress() {
        int currentScreen = viewFlipper.getDisplayedChild();

        // Nếu đang ở màn hình Success (screen 4), không cho back
        // Buộc user phải bấm nút "Đăng nhập"
        if (currentScreen == 4) {
            // Về màn hình đăng nhập thay vì back
            navigateToLogin();
            return;
        }

        if (currentScreen > 0) {
            viewFlipper.setDisplayedChild(currentScreen - 1);
        } else {
            finish();
        }
    }

    /**
     * Navigate to login screen
     */
    private void navigateToLogin() {
        android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        handleBackPress();
        return true;
    }

    /**
     * Xử lý lỗi API
     */
    private void handleError(Throwable t, String defaultMessage) {
        Log.e(TAG, "Error: " + t.getMessage(), t);

        // Xử lý timeout
        if (t instanceof java.util.concurrent.TimeoutException) {
            Toast.makeText(this, "Kết nối quá chậm, vui lòng thử lại", Toast.LENGTH_LONG).show();
            return;
        }

        if (t instanceof java.net.SocketTimeoutException) {
            Toast.makeText(this, "Không thể kết nối đến máy chủ", Toast.LENGTH_LONG).show();
            return;
        }

        if (t instanceof java.net.UnknownHostException || t instanceof java.net.ConnectException) {
            Toast.makeText(this, "Không có kết nối mạng", Toast.LENGTH_LONG).show();
            return;
        }

        if (t instanceof HttpException) {
            HttpException http = (HttpException) t;
            try {
                String body = http.response() != null && http.response().errorBody() != null
                        ? http.response().errorBody().string()
                        : null;
                if (body != null && !body.isEmpty()) {
                    ErrorResponse er = new Gson().fromJson(body, ErrorResponse.class);
                    if (er != null && er.getError() != null) {
                        String msg = er.getError().getMessage();
                        if (msg != null && !msg.isEmpty()) {
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            Toast.makeText(this, defaultMessage + " (" + http.code() + ")", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, t.getMessage() != null ? t.getMessage() : defaultMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

