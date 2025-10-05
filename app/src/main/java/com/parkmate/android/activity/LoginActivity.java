package com.parkmate.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
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

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private MaterialButton btnLogin;
    private TextView tvRegisterLink, tvForgotPassword;
    private ImageView ivGoogleLogin, ivFacebookLogin;

    private AuthRepository authRepository;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private LoadingButton loadingButton; // Thay thế LoadingDialogFragment
    private static final String PREF_LOGIN = "login_prefs";
    private static final String KEY_REMEMBER_EMAIL = "remember_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        authRepository = new AuthRepository();
        initViews();
        restoreRememberedEmail();
        setupClickListeners();
        setupColoredRegisterText();
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

        // Khởi tạo LoadingButton
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
        loadingButton.showLoading("Đang đăng nhập..."); // Hiển thị loading trong button
        LoginRequest req = new LoginRequest(email, password);
        compositeDisposable.add(
                authRepository.login(req)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> {
                                    loadingButton.hideLoading(); // Ẩn loading
                                    handleLoginSuccess(resp, email, rememberMe);
                                },
                                throwable -> {
                                    loadingButton.hideLoading(); // Ẩn loading
                                    handleLoginError(throwable);
                                }
                        )
        );
    }

    private void handleLoginSuccess(LoginResponse resp, String email, boolean rememberMe) {
        boolean ok = resp != null && (resp.getSuccess() == null || Boolean.TRUE.equals(resp.getSuccess())); // nhiều BE không trả success
        if (!ok) {
            Toast.makeText(this, resp != null && resp.getMessage() != null ? resp.getMessage() : "Đăng nhập thất bại", Toast.LENGTH_LONG).show();
            return;
        }
        // Lưu token
        String rawToken = resp.getAnyToken();
        if (rawToken != null && !rawToken.isEmpty()) {
            String cleaned = rawToken.startsWith("Bearer ") ? rawToken.substring(7) : rawToken;
            try { TokenManager.getInstance().saveToken(cleaned); } catch (Exception ignored) {}
        }

        // Lưu thông tin user
        try {
            String userId = resp.getUserId();
            if (userId == null && resp.getData() != null) {
                userId = resp.getData().getUserId();
            }

            // Lấy username từ response
            String username = resp.getUsername();
            if (username == null && resp.getData() != null) {
                username = resp.getData().getUsername();
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
        } catch (Exception ignored) {}

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
        compositeDisposable.clear();
    }
}
