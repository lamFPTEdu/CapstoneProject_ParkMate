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
    private TextView tvRegisterLink, tvForgotPassword;
    private ImageView ivGoogleLogin, ivFacebookLogin;

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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        ivGoogleLogin = findViewById(R.id.ivGoogleLogin);
        ivFacebookLogin = findViewById(R.id.ivFacebookLogin);

        loadingButton = new LoadingButton(btnLogin);
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

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        ivGoogleLogin.setOnClickListener(v -> Toast.makeText(LoginActivity.this, "Chức năng đăng nhập Google đang phát triển", Toast.LENGTH_SHORT).show());
        ivFacebookLogin.setOnClickListener(v -> Toast.makeText(LoginActivity.this, "Chức năng đăng nhập Facebook đang phát triển", Toast.LENGTH_SHORT).show());
    }

    private void setupColoredRegisterText() {
        String text = "Chưa có tài khoản? Đăng ký";
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf("Đăng ký");
        int end = start + "Đăng ký".length();
        ss.setSpan(new ClickableSpan() { @Override public void onClick(@NonNull View widget) { startActivity(new Intent(LoginActivity.this, RegisterActivity.class)); } }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvRegisterLink.setText(ss);
        tvRegisterLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;
        if (email.isEmpty()) { etEmail.setError("Vui lòng nhập email"); isValid = false; }
        if (password.isEmpty()) { etPassword.setError("Vui lòng nhập mật khẩu"); isValid = false; }
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
                                }
                        )
        );
    }

    private void cancelTimeoutWarning() {
        if (timeoutWarningHandler != null && timeoutWarningRunnable != null) {
            timeoutWarningHandler.removeCallbacks(timeoutWarningRunnable);
        }
    }

    private void handleLoginSuccess(LoginResponse resp, String email, boolean rememberMe) {
        boolean ok = resp != null && (resp.getSuccess() == null || Boolean.TRUE.equals(resp.getSuccess())); // nhiều BE không trả success
        if (!ok) {
            Toast.makeText(this, resp != null && resp.getMessage() != null ? resp.getMessage() : "Đăng nhập thất bại", Toast.LENGTH_LONG).show();
            return;
        }

        // Lưu token
        String rawToken = resp.getAnyToken();
        Log.d(TAG, "========== LOGIN SUCCESS ==========");
        Log.d(TAG, "Raw token from backend: " + (rawToken != null ? "YES (length=" + rawToken.length() + ")" : "NO - NULL!"));

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

        // Lưu thông tin user - ƯU TIÊN LẤY userId TỪ data.userResponse.id (id: 9) thay vì account.id (id: 13)
        try {
            String userId = null;
            String username = null;

            // Ưu tiên lấy từ data.userResponse.id (đây mới là userId thực sự để dùng cho entityId)
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

        applyRememberMe(email, rememberMe);
        Toast.makeText(this, resp.getMessage() != null ? resp.getMessage() : "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
        // Chuyển sang màn hình chính (HomeActivity) và clear back stack để tránh quay lại màn hình đăng nhập
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        // Xử lý timeout
        if (t instanceof java.util.concurrent.TimeoutException) {
            Toast.makeText(this, "Kết nối quá chậm, vui lòng kiểm tra mạng và thử lại", Toast.LENGTH_LONG).show();
            return;
        }

        if (t instanceof java.net.SocketTimeoutException) {
            Toast.makeText(this, "Không thể kết nối đến máy chủ, vui lòng thử lại", Toast.LENGTH_LONG).show();
            return;
        }

        if (t instanceof java.net.UnknownHostException || t instanceof java.net.ConnectException) {
            Toast.makeText(this, "Không có kết nối mạng, vui lòng kiểm tra và thử lại", Toast.LENGTH_LONG).show();
            return;
        }

        if (t instanceof HttpException) {
            HttpException http = (HttpException) t;
            try {
                String body = http.response() != null && http.response().errorBody() != null ? http.response().errorBody().string() : null;
                if (body != null && !body.isEmpty()) {
                    ErrorResponse er = new Gson().fromJson(body, ErrorResponse.class);
                    if (er != null && er.getError() != null) {
                        // Field errors
                        if (er.getError().getFieldErrors() != null) {
                            for (ErrorResponse.FieldError fe : er.getError().getFieldErrors()) {
                                applyFieldError(fe);
                            }
                        }
                        String code = er.getError().getCode();
                        String msg = er.getError().getMessage();
                        if (code != null) {
                            switch (code) {
                                case "INVALID_CREDENTIALS":
                                case "BAD_CREDENTIALS":
                                    if (etPassword != null) etPassword.setError(msg != null ? msg : "Sai email hoặc mật khẩu");
                                    Toast.makeText(this, msg != null ? msg : "Sai email hoặc mật khẩu", Toast.LENGTH_LONG).show();
                                    return;
                                case "ACCOUNT_NOT_ACTIVE":
                                    Toast.makeText(this, msg != null ? msg : "Tài khoản chưa kích hoạt", Toast.LENGTH_LONG).show();
                                    return;
                                default:
                                    break;
                            }
                        }
                        if (msg != null && !msg.isEmpty()) {
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }
            } catch (Exception ignored) {}
            Toast.makeText(this, "Đăng nhập thất bại (" + http.code() + ")", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, t.getMessage() != null ? t.getMessage() : "Lỗi đăng nhập", Toast.LENGTH_LONG).show();
        }
    }

    private void applyFieldError(ErrorResponse.FieldError fe) {
        if (fe == null) return;
        String field = fe.getField();
        String msg = fe.getMessage() != null ? fe.getMessage() : "Giá trị không hợp lệ";
        if (field == null) return;
        switch (field) {
            case "email": if (etEmail != null) etEmail.setError(msg); break;
            case "password": if (etPassword != null) etPassword.setError(msg); break;
        }
    }

    private String safeText(TextInputEditText et) { return et == null || et.getText() == null ? "" : et.getText().toString().trim(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimeoutWarning();
        compositeDisposable.clear();
    }
}
