package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.parkmate.android.R;
import com.parkmate.android.model.request.RegisterRequest;
import com.parkmate.android.model.response.ErrorResponse;
import com.parkmate.android.model.response.OtpVerifyResponse;
import com.parkmate.android.model.response.RegisterResponse;
import com.parkmate.android.repository.AuthRepository;
import com.google.gson.Gson;
import retrofit2.HttpException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import android.os.CountDownTimer;
import com.parkmate.android.utils.LoadingButton;

/**
 * RegisterActivity - Chỉ xử lý đăng ký cơ bản
 * Flow: Basic Info → Check Email → OTP → Success
 * CCCD sẽ được xác thực riêng trong ProfileActivity sau khi đăng nhập
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // Chỉ còn 4 screens
    private static final int SCREEN_BASIC_INFO = 0;
    private static final int SCREEN_CHECK_EMAIL = 1;
    private static final int SCREEN_OTP = 2;
    private static final int SCREEN_SUCCESS = 3;

    // Views cho Basic Info
    private ViewFlipper viewFlipper;
    private TextInputEditText etEmail, etPassword, etConfirmPassword, etPhoneNumber;
    private TextInputEditText etFirstName, etLastName;
    private MaterialButton btnNext;
    private ImageView ivBack, ivGoogleSignUp, ivFacebookSignUp;
    private TextView tvLoginLink;

    // Views cho Check Email & OTP
    private TextView tvUserEmail, tvResendCode;
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerify;

    // Data
    private String userEmail;

    // Utils
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AuthRepository authRepository;
    private CountDownTimer resendTimer;
    private static final long RESEND_OTP_INTERVAL_MS = 60000L;
    private LoadingButton loadingButtonVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Setup edge-to-edge display
        com.parkmate.android.utils.EdgeToEdgeHelper.setupEdgeToEdge(this);

        authRepository = new AuthRepository();
        initViews();
        setupListeners();
        setupLoginLink();
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        ivBack = findViewById(R.id.ivBack);

        // Basic Info views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        btnNext = findViewById(R.id.btnNext);
        ivGoogleSignUp = findViewById(R.id.ivGoogleSignUp);
        ivFacebookSignUp = findViewById(R.id.ivFacebookSignUp);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> {
            if (viewFlipper.getDisplayedChild() > 0) {
                viewFlipper.showPrevious();
            } else {
                finish();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (validateBasicInfo()) {
                userEmail = safeText(etEmail);
                performRegister();
            }
        });
    }

    private void performRegister() {
        RegisterRequest request = buildRegisterRequest();

        btnNext.setEnabled(false);
        btnNext.setText("Đang đăng ký...");

        compositeDisposable.add(
                authRepository.register(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> {
                                    btnNext.setEnabled(true);
                                    btnNext.setText("Tiếp tục");

                                    storeRegisterToken(resp);
                                    Toast.makeText(this, "Đăng ký thành công! OTP đã được gửi đến email của bạn.", Toast.LENGTH_SHORT).show();

                                    viewFlipper.setDisplayedChild(SCREEN_CHECK_EMAIL);
                                    setupCheckEmailScreen();
                                },
                                err -> {
                                    btnNext.setEnabled(true);
                                    btnNext.setText("Tiếp tục");

                                    if (tryHandleAccountExists(err)) return;

                                    boolean handled = parseAndApplyBackendErrors(err);
                                    if (!handled) {
                                        String msg = err.getMessage();
                                        if (msg == null) msg = "Đăng ký thất bại";
                                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                                    }
                                }
                        )
        );
    }

    private void storeRegisterToken(RegisterResponse resp) {
        if (resp == null) return;
        String rawToken = resp.getAnyToken();
        if (rawToken == null || rawToken.isEmpty()) return;

        String cleaned = rawToken.startsWith("Bearer ") ? rawToken.substring(7) : rawToken;
        try {
            com.parkmate.android.utils.TokenManager.getInstance().saveToken(cleaned);
        } catch (IllegalStateException e) {
            Log.e(TAG, "TokenManager chưa init.", e);
        }
    }

    private boolean tryHandleAccountExists(Throwable t) {
        if (!(t instanceof HttpException)) return false;

        try {
            HttpException httpEx = (HttpException) t;
            String body = httpEx.response() != null && httpEx.response().errorBody() != null
                    ? httpEx.response().errorBody().string() : null;
            if (body == null || body.isEmpty()) return false;

            ErrorResponse er = new Gson().fromJson(body, ErrorResponse.class);
            if (er == null || er.getError() == null) return false;

            String code = er.getError().getCode();
            if ("ACCOUNT_ALREADY_EXISTS".equalsIgnoreCase(code)) {
                showEmailExistsDialog(userEmail, er);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse ACCOUNT_ALREADY_EXISTS failed", e);
        }
        return false;
    }

    private void showEmailExistsDialog(String email, ErrorResponse er) {
        if (etEmail != null) etEmail.setError(getString(R.string.error_email_exists));

        String detail = getString(R.string.error_email_exists_detail, email);
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_email_exists)
                .setMessage(detail)
                .setCancelable(false)
                .setPositiveButton(R.string.action_login_now, (d, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton(R.string.action_use_other_email, (d, w) -> {
                    viewFlipper.setDisplayedChild(SCREEN_BASIC_INFO);
                    if (etEmail != null) {
                        etEmail.requestFocus();
                        etEmail.selectAll();
                    }
                })
                .setNeutralButton(R.string.action_forgot_password, (d, w) -> {
                    Intent i = new Intent(this, ForgotPasswordActivity.class);
                    i.putExtra("email_prefill", email);
                    startActivity(i);
                })
                .show();
    }

    private void setupCheckEmailScreen() {
        View checkEmailView = viewFlipper.getChildAt(SCREEN_CHECK_EMAIL);
        if (checkEmailView == null) return;

        TextView tvTitle = checkEmailView.findViewById(R.id.tvTitle);
        TextView tvDescription = checkEmailView.findViewById(R.id.tvDescription);
        tvUserEmail = checkEmailView.findViewById(R.id.tvUserEmail);
        Button btnEmailContinue = checkEmailView.findViewById(R.id.btnContinue);

        if (tvTitle != null) tvTitle.setText("Kiểm tra Email");
        if (tvDescription != null) tvDescription.setText("Chúng tôi đã gửi mã OTP đến email của bạn. Vui lòng kiểm tra hộp thư.");
        if (tvUserEmail != null) tvUserEmail.setText(userEmail);

        if (btnEmailContinue != null) {
            btnEmailContinue.setText("Tiếp tục");
            btnEmailContinue.setOnClickListener(v -> {
                viewFlipper.setDisplayedChild(SCREEN_OTP);
                setupOtpScreen();
            });
        }
    }

    private void setupOtpScreen() {
        View otpView = viewFlipper.getChildAt(SCREEN_OTP);
        if (otpView == null) return;

        TextView tvTitle = otpView.findViewById(R.id.tvTitle);
        TextView tvDescription = otpView.findViewById(R.id.tvDescription);
        etOtp1 = otpView.findViewById(R.id.etOtp1);
        etOtp2 = otpView.findViewById(R.id.etOtp2);
        etOtp3 = otpView.findViewById(R.id.etOtp3);
        etOtp4 = otpView.findViewById(R.id.etOtp4);
        etOtp5 = otpView.findViewById(R.id.etOtp5);
        etOtp6 = otpView.findViewById(R.id.etOtp6);
        tvResendCode = otpView.findViewById(R.id.tvResendCode);
        btnVerify = otpView.findViewById(R.id.btnVerify);

        if (tvTitle != null) tvTitle.setText("Nhập mã OTP");
        if (tvDescription != null) tvDescription.setText("Vui lòng nhập mã OTP đã được gửi đến email của bạn");

        setupOtpTextWatchers();
        startResendCountdown();

        if (btnVerify != null) {
            loadingButtonVerify = new LoadingButton(btnVerify);
            btnVerify.setOnClickListener(v -> {
                if (validateOTP()) {
                    String otp = collectOtp();
                    verifyOtpOnly(otp);
                }
            });
        }

        if (tvResendCode != null) {
            tvResendCode.setOnClickListener(v -> {
                if (tvResendCode.isEnabled()) {
                    Toast.makeText(this, "Yêu cầu gửi lại OTP (cần implement API)", Toast.LENGTH_SHORT).show();
                    startResendCountdown();
                }
            });
        }
    }

    private void verifyOtpOnly(String otp) {
        loadingButtonVerify.showLoading("Đang xác thực...");

        compositeDisposable.add(
                authRepository.verifyOtp(userEmail, otp)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::onOtpVerifiedSuccess,
                                this::onOtpVerifyFailed
                        )
        );
    }

    private void onOtpVerifiedSuccess(OtpVerifyResponse resp) {
        if (loadingButtonVerify != null) loadingButtonVerify.hideLoading();

        boolean ok = resp != null && Boolean.TRUE.equals(resp.getSuccess());
        if (!ok) {
            String msg = resp != null && resp.getMessage() != null
                    ? resp.getMessage() : "Xác thực OTP không thành công";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            if (etOtp1 != null) etOtp1.setError(msg);
            return;
        }

        proceedToSuccess();
    }

    private void onOtpVerifyFailed(Throwable t) {
        if (loadingButtonVerify != null) loadingButtonVerify.hideLoading();

        boolean handled = parseAndApplyBackendErrors(t);
        if (!handled) {
            String msg = t.getMessage();
            if (msg == null) msg = "Xác thực OTP thất bại";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    private void proceedToSuccess() {
        viewFlipper.setDisplayedChild(SCREEN_SUCCESS);
        setupSuccessScreen();
        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
    }

    private void setupSuccessScreen() {
        View successView = viewFlipper.getChildAt(SCREEN_SUCCESS);
        if (successView == null) return;

        // Lấy các TextView để set text cho chức năng ĐĂNG KÝ
        TextView tvTitle = successView.findViewById(R.id.tvTitle);
        TextView tvDescription = successView.findViewById(R.id.tvDescription);
        Button btnGoToLogin = successView.findViewById(R.id.btnLogin);

        // SET TEXT CHO CHỨC NĂNG ĐĂNG KÝ
        if (tvTitle != null) {
            tvTitle.setText(R.string.registration_success);  // "Đăng ký thành công!"
        }
        if (tvDescription != null) {
            tvDescription.setText(R.string.registration_success_description);  // "Tài khoản của bạn đã được tạo thành công."
        }
        if (btnGoToLogin != null) {
            btnGoToLogin.setText(R.string.login);  // "Đăng nhập"
            btnGoToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private boolean validateBasicInfo() {
        String email = safeText(etEmail);
        String password = safeText(etPassword);
        String confirmPassword = safeText(etConfirmPassword);
        String phone = safeText(etPhoneNumber);
        String firstName = safeText(etFirstName);
        String lastName = safeText(etLastName);

        clearBasicErrors();

        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return false;
        }

        if (phone.isEmpty()) {
            etPhoneNumber.setError("Số điện thoại không được để trống");
            return false;
        }
        if (phone.length() < 10) {
            etPhoneNumber.setError("Số điện thoại phải có ít nhất 10 số");
            return false;
        }

        if (firstName.isEmpty()) {
            etFirstName.setError("Họ không được để trống");
            return false;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Tên không được để trống");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Mật khẩu không được để trống");
            return false;
        }
        if (password.length() < 8) {
            etPassword.setError("Mật khẩu phải có ít nhất 8 ký tự");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return false;
        }

        return true;
    }

    private void clearBasicErrors() {
        if (etEmail != null) etEmail.setError(null);
        if (etPassword != null) etPassword.setError(null);
        if (etConfirmPassword != null) etConfirmPassword.setError(null);
        if (etPhoneNumber != null) etPhoneNumber.setError(null);
        if (etFirstName != null) etFirstName.setError(null);
        if (etLastName != null) etLastName.setError(null);
    }

    private void setupOtpTextWatchers() {
        if (etOtp1 == null) return;

        etOtp1.addTextChangedListener(new OTPTextWatcher(etOtp1, etOtp2));
        etOtp2.addTextChangedListener(new OTPTextWatcher(etOtp2, etOtp3));
        etOtp3.addTextChangedListener(new OTPTextWatcher(etOtp3, etOtp4));
        etOtp4.addTextChangedListener(new OTPTextWatcher(etOtp4, etOtp5));
        etOtp5.addTextChangedListener(new OTPTextWatcher(etOtp5, etOtp6));
        etOtp6.addTextChangedListener(new OTPTextWatcher(etOtp6, null));
    }

    private void startResendCountdown() {
        if (tvResendCode == null) return;

        tvResendCode.setEnabled(false);
        if (resendTimer != null) resendTimer.cancel();

        resendTimer = new CountDownTimer(RESEND_OTP_INTERVAL_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                tvResendCode.setText("Gửi lại mã (" + sec + "s)");
            }

            @Override
            public void onFinish() {
                tvResendCode.setEnabled(true);
                tvResendCode.setText(getString(R.string.resend_code));
            }
        }.start();
    }

    private boolean validateOTP() {
        if (etOtp1 == null || etOtp2 == null || etOtp3 == null ||
            etOtp4 == null || etOtp5 == null || etOtp6 == null) {
            return false;
        }

        if (safeText(etOtp1).isEmpty() || safeText(etOtp2).isEmpty() ||
            safeText(etOtp3).isEmpty() || safeText(etOtp4).isEmpty() ||
            safeText(etOtp5).isEmpty() || safeText(etOtp6).isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String collectOtp() {
        return safeText(etOtp1) + safeText(etOtp2) + safeText(etOtp3) +
               safeText(etOtp4) + safeText(etOtp5) + safeText(etOtp6);
    }

    private RegisterRequest buildRegisterRequest() {
        String email = safeText(etEmail);
        String password = safeText(etPassword);
        String phone = safeText(etPhoneNumber);
        String firstName = safeText(etFirstName);
        String lastName = safeText(etLastName);

        // Chỉ gửi thông tin cơ bản
        // CCCD sẽ được xác thực sau trong ProfileActivity
        return new RegisterRequest(
                email,
                password,
                phone,
                firstName,
                lastName,
                null,  // fullName
                null,  // idNumber
                null,  // dateOfBirth
                null,  // issuePlace
                null,  // issueDate
                null,  // expiryDate
                null,  // address
                null,  // frontIdPath
                null   // backIdImgPath
        );
    }

    private String safeText(EditText et) {
        return et == null || et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void applyFieldError(ErrorResponse.FieldError fe) {
        if (fe == null) return;

        String field = fe.getField();
        String msg = fe.getMessage() != null ? fe.getMessage() : "Giá trị không hợp lệ";

        if (field == null) {
            if (etOtp1 != null) etOtp1.setError(msg);
            return;
        }

        switch (field) {
            case "email":
                if (etEmail != null) etEmail.setError(msg);
                break;
            case "password":
                if (etPassword != null) etPassword.setError(msg);
                break;
            case "confirmPassword":
                if (etConfirmPassword != null) etConfirmPassword.setError(msg);
                break;
            case "phone":
                if (etPhoneNumber != null) etPhoneNumber.setError(msg);
                break;
            case "firstName":
                if (etFirstName != null) etFirstName.setError(msg);
                break;
            case "lastName":
                if (etLastName != null) etLastName.setError(msg);
                break;
            case "otp":
            default:
                if (etOtp1 != null) etOtp1.setError(msg);
                break;
        }
    }

    private boolean parseAndApplyBackendErrors(Throwable t) {
        if (!(t instanceof HttpException)) return false;

        HttpException httpEx = (HttpException) t;
        try {
            String body = httpEx.response() != null && httpEx.response().errorBody() != null
                    ? httpEx.response().errorBody().string() : null;
            if (body == null || body.isEmpty()) return false;

            Gson gson = new Gson();
            ErrorResponse er = gson.fromJson(body, ErrorResponse.class);
            if (er == null || er.getError() == null) return false;

            if (er.getError().getFieldErrors() != null && !er.getError().getFieldErrors().isEmpty()) {
                for (ErrorResponse.FieldError fe : er.getError().getFieldErrors()) {
                    applyFieldError(fe);
                }
                if (er.getMessage() != null) {
                    Toast.makeText(this, er.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            String code = er.getError().getCode();
            if (code != null) {
                switch (code) {
                    case "UNCATEGORIZED_EXCEPTION":
                        Toast.makeText(this, "Hệ thống đang bận hoặc gặp sự cố. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                        return true;
                    case "ACCOUNT_ALREADY_EXISTS":
                        showEmailExistsDialog(userEmail, er);
                        return true;
                    case "INVALID_OTP":
                    case "OTP_INVALID":
                        showOtpInlineError(er.getMessage() != null ? er.getMessage() : "Mã OTP không hợp lệ");
                        return true;
                    case "OTP_EXPIRED":
                        showOtpInlineError(er.getMessage() != null ? er.getMessage() : "Mã OTP đã hết hạn, vui lòng yêu cầu mã mới");
                        return true;
                    default:
                        if (er.getMessage() != null) {
                            Toast.makeText(this, er.getMessage(), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Parse backend error fail", e);
            return false;
        }
    }

    private void showOtpInlineError(String msg) {
        if (etOtp1 != null) etOtp1.setError(msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (resendTimer != null) resendTimer.cancel();
        compositeDisposable.clear();
        super.onDestroy();
    }

    private class OTPTextWatcher implements TextWatcher {
        private final EditText currentEditText;
        private final EditText nextEditText;

        public OTPTextWatcher(EditText currentEditText, EditText nextEditText) {
            this.currentEditText = currentEditText;
            this.nextEditText = nextEditText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextEditText != null) {
                nextEditText.requestFocus();
            }
        }
    }

    private void setupLoginLink() {
        if (tvLoginLink == null) return;

        String text = "Đã có tài khoản? Đăng nhập";
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf("Đăng nhập");
        int end = start + "Đăng nhập".length();

        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvLoginLink.setText(ss);
        tvLoginLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

