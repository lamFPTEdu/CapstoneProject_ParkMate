package com.parkmate.android.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.parkmate.android.R;
import com.parkmate.android.model.request.RegisterRequest;
import com.parkmate.android.model.response.ErrorResponse;
import com.parkmate.android.model.response.OtpVerifyResponse;
import com.parkmate.android.model.response.RegisterResponse;
import com.parkmate.android.repository.AuthRepository;
import com.google.gson.Gson;
import retrofit2.HttpException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import android.os.CountDownTimer;
import com.parkmate.android.utils.LoadingButton;
import com.parkmate.android.utils.validation.RegisterValidator;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    // Added screen index constants to avoid magic numbers
    private static final int SCREEN_BASIC_INFO = 0;
    private static final int SCREEN_CCCD_INFO = 1;
    private static final int SCREEN_CHECK_EMAIL = 2;
    private static final int SCREEN_OTP = 3;
    private static final int SCREEN_SUCCESS = 4;

    private ViewFlipper viewFlipper;
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword, etPhoneNumber;
    private TextInputEditText etCccdNumber, etFirstName, etLastName, etDateOfBirth, etIssueDate, etIssuePlace;
    private TextInputEditText etNationality, etPermanentAddress;
    private AutoCompleteTextView actvGender;
    private TextInputLayout tilDateOfBirth, tilIssueDate, tilGender;
    private MaterialButton btnNext;
    private Button btnContinue, btnVerify, btnLogin;
    private ImageView ivBack, ivGoogleSignUp, ivFacebookSignUp;
    private TextView tvLoginLink, tvUserEmail, tvResendCode;
    private FrameLayout flFrontImage, flBackImage;
    private ImageView ivFrontImage, ivBackImage;
    private LinearLayout llFrontImagePlaceholder, llBackImagePlaceholder;
    private CheckBox cbCommitment;

    // OTP fields
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;

    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private Uri frontImageUri, backImageUri;
    private String userEmail; // Store user email for OTP verification

    // Activity result launchers for camera and gallery
    private ActivityResultLauncher<Intent> frontCameraLauncher;
    private ActivityResultLauncher<Intent> backCameraLauncher;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private static final Pattern USERNAME_ALLOWED = Pattern.compile("^[A-Za-z0-9_-]+$");
    private AuthRepository authRepository;
    private CountDownTimer resendTimer;
    private static final long RESEND_OTP_INTERVAL_MS = 60000L;
    private LoadingButton loadingButtonContinue;
    private LoadingButton loadingButtonVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo Calendar và định dạng ngày tháng
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Khởi tạo ActivityResultLauncher cho chụp ảnh
        setupImageCaptureLaunchers();

        authRepository = new AuthRepository();
        initViews();
        setupGenderDropdown();
        setupListeners();
        setupLoginLink();
        setupDatePickers();
        setupImageCapture();
    }

    private void setupImageCaptureLaunchers() {
        // Launcher cho hình ảnh mặt trước CCCD
        frontCameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Xử lý hình ảnh được chụp
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Uri imageUri = (Uri) extras.get("data");
                            if (imageUri != null) {
                                frontImageUri = imageUri;
                                ivFrontImage.setImageURI(frontImageUri);
                                ivFrontImage.setVisibility(View.VISIBLE);
                                llFrontImagePlaceholder.setVisibility(View.GONE);
                            }
                        }
                    }
                }
        );

        // Launcher cho hình ảnh mặt sau CCCD
        backCameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Xử lý hình ảnh được chụp
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Uri imageUri = (Uri) extras.get("data");
                            if (imageUri != null) {
                                backImageUri = imageUri;
                                ivBackImage.setImageURI(backImageUri);
                                ivBackImage.setVisibility(View.VISIBLE);
                                llBackImagePlaceholder.setVisibility(View.GONE);
                            }
                        }
                    }
                }
        );
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);

        // Ánh xạ các view từ layout đầu tiên (Thông tin cơ bản)
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnNext = findViewById(R.id.btnNext);
        ivGoogleSignUp = findViewById(R.id.ivGoogleSignUp);
        ivFacebookSignUp = findViewById(R.id.ivFacebookSignUp);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Ánh xạ các view từ layout thứ hai (Thông tin CCCD)
        etCccdNumber = findViewById(R.id.etCccdNumber);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etIssueDate = findViewById(R.id.etIssueDate);
        etIssuePlace = findViewById(R.id.etIssuePlace);
        tilDateOfBirth = findViewById(R.id.tilDateOfBirth);
        tilIssueDate = findViewById(R.id.tilIssueDate);

        // Ánh xạ các trường mới được thêm vào
        actvGender = findViewById(R.id.actvGender);
        tilGender = findViewById(R.id.tilGender);
        etNationality = findViewById(R.id.etNationality);
        etPermanentAddress = findViewById(R.id.etPermanentAddress);

        // Ánh xạ nút "Tiếp tục" trong layout CCCD info
        btnContinue = findViewById(R.id.btnContinue);

        // Ánh xạ nút quay lại dưới dạng mũi tên
        ivBack = findViewById(R.id.ivBack);

        // Ánh xạ các view cho hình ảnh
        flFrontImage = findViewById(R.id.flFrontImage);
        flBackImage = findViewById(R.id.flBackImage);
        ivFrontImage = findViewById(R.id.ivFrontImage);
        ivBackImage = findViewById(R.id.ivBackImage);
        llFrontImagePlaceholder = findViewById(R.id.llFrontImagePlaceholder);
        llBackImagePlaceholder = findViewById(R.id.llBackImagePlaceholder);

        // Ánh xạ checkbox cam kết
        cbCommitment = findViewById(R.id.cbCommitment);
    }

    private void setupGenderDropdown() {
        // Thiết lập dropdown cho lựa chọn giới tính
        String[] genderOptions = {"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genderOptions
        );
        if (actvGender != null) {
            actvGender.setAdapter(adapter);
        }
    }

    private void setupImageCapture() {
        // Thiết lập sự kiện khi nhấn vào khung chụp ảnh
        if (flFrontImage != null) {
            flFrontImage.setOnClickListener(v -> {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    frontCameraLauncher.launch(takePictureIntent);
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Không tìm thấy ứng dụng máy ảnh",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (flBackImage != null) {
            flBackImage.setOnClickListener(v -> {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    backCameraLauncher.launch(takePictureIntent);
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Không tìm thấy ứng dụng máy ảnh",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                viewFlipper.setDisplayedChild(SCREEN_CCCD_INFO);
            }
        });

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                if (validateCccdInfo()) {
                    userEmail = etEmail.getText().toString().trim();
                    // CHỈ chuyển sang màn Check Email, CHƯA gửi OTP ở bước này
                    viewFlipper.setDisplayedChild(SCREEN_CHECK_EMAIL);
                    setupCheckEmailScreen();
                }
            });
        }
    }

    private void setupCheckEmailScreen() {
        View checkEmailView = viewFlipper.getChildAt(SCREEN_CHECK_EMAIL);
        if (checkEmailView == null) {
            Log.e(TAG, "Check Email view is null at index " + SCREEN_CHECK_EMAIL);
            return;
        }
        TextView tvTitle = checkEmailView.findViewById(R.id.tvTitle);
        TextView tvDescription = checkEmailView.findViewById(R.id.tvDescription);
        tvUserEmail = checkEmailView.findViewById(R.id.tvUserEmail);
        Button btnEmailContinue = checkEmailView.findViewById(R.id.btnContinue);

        if (tvTitle != null) tvTitle.setText(R.string.register_check_email);
        if (tvDescription != null) tvDescription.setText(R.string.register_check_email_description);
        if (tvUserEmail != null) tvUserEmail.setText(userEmail);

        if (btnEmailContinue != null) {
            // Khởi tạo LoadingButton cho nút Continue
            loadingButtonContinue = new LoadingButton(btnEmailContinue);

            btnEmailContinue.setOnClickListener(v -> {
                // Gọi register tại đây để BE gửi OTP (không tự gửi OTP thủ công nữa)
                loadingButtonContinue.showLoading("Đang đăng ký...");
                performRegisterForOtp();
            });
        }
    }

    // Đăng ký để backend gửi OTP – sau thành công chuyển sang màn hình OTP
    private void performRegisterForOtp() {
        RegisterRequest request = buildRegisterRequest();
        compositeDisposable.add(
                authRepository.register(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> {
                                    if (loadingButtonContinue != null) loadingButtonContinue.hideLoading();
                                    // Lưu token (nếu có) trước khi chuyển sang OTP
                                    storeRegisterToken(resp);
                                    Toast.makeText(this, "OTP đã được gửi tới email", Toast.LENGTH_SHORT).show();
                                    viewFlipper.setDisplayedChild(SCREEN_OTP);
                                    setupOtpScreen();
                                },
                                err -> {
                                    if (loadingButtonContinue != null) loadingButtonContinue.hideLoading();
                                    if (tryHandleAccountExists(err)) return; // nếu đã xử lý dialog thì dừng
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
        if (rawToken == null || rawToken.isEmpty()) {
            Log.w(TAG, "Register response không có token để lưu (có thể backend không yêu cầu Authorization cho verify hoặc đang thiếu spec)");
            return;
        }
        String cleaned = rawToken.startsWith("Bearer ") ? rawToken.substring(7) : rawToken;
        try {
            com.parkmate.android.utils.TokenManager.getInstance().saveToken(cleaned);
            Log.d(TAG, "Đã lưu token đăng ký (length=" + cleaned.length() + ")");
        } catch (IllegalStateException e) {
            Log.e(TAG, "TokenManager chưa init. Cần gọi TokenManager.init(context) trong Application hoặc SplashActivity trước.", e);
        }
    }

    // Thử parse lỗi account exists
    private boolean tryHandleAccountExists(Throwable t) {
        if (!(t instanceof HttpException)) return false;
        try {
            HttpException httpEx = (HttpException) t;
            String body = httpEx.response() != null && httpEx.response().errorBody() != null ? httpEx.response().errorBody().string() : null;
            if (body == null || body.isEmpty()) return false;
            ErrorResponse er = new Gson().fromJson(body, ErrorResponse.class);
            if ( er == null || er.getError() == null ) return false;
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

    private void setupOtpScreen() {
        View otpView = viewFlipper.getChildAt(SCREEN_OTP);
        if (otpView == null) {
            Log.e(TAG, "OTP view is null at index " + SCREEN_OTP);
            return;
        }
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

        if (tvTitle != null) tvTitle.setText(R.string.register_enter_otp);
        if (tvDescription != null) tvDescription.setText(R.string.register_otp_description);

        setupOtpTextWatchers();
        startResendCountdown();

        if (btnVerify != null) {
            // Khởi tạo LoadingButton cho nút Verify
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
                    // Trường hợp BE có endpoint resend OTP riêng -> TODO sau
                    Toast.makeText(this, "Yêu cầu gửi lại OTP (cần implement API)", Toast.LENGTH_SHORT).show();
                    startResendCountdown();
                }
            });
        }
    }

    // Chỉ verify OTP (không gọi register nữa)
    private void verifyOtpOnly(String otp) {
        loadingButtonVerify.showLoading("Đang xác thực...");
        compositeDisposable.add(
                authRepository.verifyOtp(userEmail, otp)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::onOtpVerifiedSuccess,
                                this::onOtpVerifyFailedFinal
                        )
        );
    }

    private void onOtpVerifiedSuccess(OtpVerifyResponse resp) {
        if (loadingButtonVerify != null) loadingButtonVerify.hideLoading();
        boolean ok = resp != null && Boolean.TRUE.equals(resp.getSuccess());
        Log.d(TAG, "OTP verify response successFlag=" + ok + ", message=" + (resp == null ? "null" : resp.getMessage()));
        if (!ok) {
            String msg = resp != null && resp.getMessage() != null ? resp.getMessage() : "Xác thực OTP không thành công";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            // Highlight first OTP box as error hint
            if (etOtp1 != null) etOtp1.setError(msg);
            return;
        }
        // OTP ok -> sang success
        viewFlipper.setDisplayedChild(SCREEN_SUCCESS);
        setupSuccessScreen();
        Toast.makeText(this, resp.getMessage() != null ? resp.getMessage() : "Xác thực thành công", Toast.LENGTH_SHORT).show();
    }

    private void onOtpVerifyFailedFinal(Throwable throwable) {
        if (loadingButtonVerify != null) loadingButtonVerify.hideLoading();
        Log.e(TAG, "OTP verify failed raw: " + throwable.getMessage(), throwable);
        boolean handled = parseAndApplyBackendErrors(throwable);
        if (!handled) {
            Toast.makeText(this, "Xác thực OTP thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSuccessScreen() {
        View successView = viewFlipper.getChildAt(SCREEN_SUCCESS);
        if (successView == null) {
            Log.e(TAG, "Success view is null at index " + SCREEN_SUCCESS);
            return;
        }
        TextView tvSuccessTitle = successView.findViewById(R.id.tvTitle);
        TextView tvSuccessDescription = successView.findViewById(R.id.tvDescription);
        btnLogin = successView.findViewById(R.id.btnLogin);

        if (tvSuccessTitle != null) tvSuccessTitle.setText(R.string.registration_success);
        if (tvSuccessDescription != null) tvSuccessDescription.setText(R.string.registration_success_description);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
    }

    // ==== Refactor validateBasicInfo dùng RegisterValidator ====
    private boolean validateBasicInfo() {
        String username = safeText(etUsername);
        String email = safeText(etEmail);
        String password = safeText(etPassword);
        String confirmPassword = safeText(etConfirmPassword);
        String phone = safeText(etPhoneNumber);

        // Validate username
        clearBasicErrors();

        if (username.isEmpty()) {
            etUsername.setError("Username không được để trống");
            return false;
        }
        if (username.length() < 3) {
            etUsername.setError("Username phải có ít nhất 3 ký tự");
            return false;
        }
        if (!USERNAME_ALLOWED.matcher(username).matches()) {
            etUsername.setError("Username chỉ được chứa chữ cái, số, dấu gạch dưới và gạch ngang");
            return false;
        }

        // Validate email
        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return false;
        }

        // Validate phone
        if (phone.isEmpty()) {
            etPhoneNumber.setError("Số điện thoại không được để trống");
            return false;
        }
        if (phone.length() < 10) {
            etPhoneNumber.setError("Số điện thoại phải có ít nhất 10 số");
            return false;
        }

        // Validate password
        if (password.isEmpty()) {
            etPassword.setError("Mật khẩu không được để trống");
            return false;
        }
        if (password.length() < 8) {
            etPassword.setError("Mật khẩu phải có ít nhất 8 ký tự");
            return false;
        }

        // Validate confirm password
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return false;
        }

        return true;
    }

    private void clearBasicErrors() {
        if (etUsername != null) etUsername.setError(null);
        if (etEmail != null) etEmail.setError(null);
        if (etPassword != null) etPassword.setError(null);
        if (etConfirmPassword != null) etConfirmPassword.setError(null);
        if (etPhoneNumber != null) etPhoneNumber.setError(null);
    }

    private boolean validateCccdInfo() {
        String cccdNumber = safeText(etCccdNumber);
        String firstName = safeText(etFirstName);
        String lastName = safeText(etLastName);
        String dateOfBirth = safeText(etDateOfBirth);
        String issueDate = safeText(etIssueDate);
        String issuePlace = safeText(etIssuePlace);
        String gender = actvGender != null ? actvGender.getText().toString().trim() : "";
        String nationality = safeText(etNationality);
        String permanentAddress = safeText(etPermanentAddress);
        boolean commitment = cbCommitment != null && cbCommitment.isChecked();

        clearCccdErrors();

        if (cccdNumber.isEmpty()) {
            etCccdNumber.setError("Số CCCD không được để trống");
            return false;
        }
        if (cccdNumber.length() != 9 && cccdNumber.length() != 12) {
            etCccdNumber.setError("Số CCCD phải có 9 hoặc 12 số");
            return false;
        }

        if (firstName.isEmpty()) {
            etFirstName.setError("Họ và tên đệm không được để trống");
            return false;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Tên không được để trống");
            return false;
        }

        if (dateOfBirth.isEmpty()) {
            etDateOfBirth.setError("Ngày sinh không được để trống");
            return false;
        }

        if (gender.isEmpty()) {
            actvGender.setError("Vui lòng chọn giới tính");
            return false;
        }

        if (nationality.isEmpty()) {
            etNationality.setError("Quốc tịch không được để trống");
            return false;
        }

        if (permanentAddress.isEmpty()) {
            etPermanentAddress.setError("Địa chỉ thường trú không được để trống");
            return false;
        }

        if (issueDate.isEmpty()) {
            etIssueDate.setError("Ngày cấp không được để trống");
            return false;
        }

        if (issuePlace.isEmpty()) {
            etIssuePlace.setError("Nơi cấp không được để trống");
            return false;
        }

        if (!commitment) {
            Toast.makeText(this, "Bạn cần cam kết thông tin là chính xác", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void clearCccdErrors() {
        if (etCccdNumber != null) etCccdNumber.setError(null);
        if (etFirstName != null) etFirstName.setError(null);
        if (etLastName != null) etLastName.setError(null);
        if (etDateOfBirth != null) etDateOfBirth.setError(null);
        if (etIssueDate != null) etIssueDate.setError(null);
        if (etIssuePlace != null) etIssuePlace.setError(null);
        if (actvGender != null) actvGender.setError(null);
        if (etNationality != null) etNationality.setError(null);
        if (etPermanentAddress != null) etPermanentAddress.setError(null);
    }

    // =================== HELPER METHODS KHÔI PHỤC (BỊ THIẾU) ===================
    private void sendOTPToEmail(String email) {
        // TODO: Gọi API gửi OTP thực tế – hiện chỉ hiển thị thông báo
        Toast.makeText(this, "Đã gửi mã OTP đến: " + email, Toast.LENGTH_SHORT).show();
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
            @Override public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                tvResendCode.setText("Gửi lại mã (" + sec + "s)");
            }
            @Override public void onFinish() {
                tvResendCode.setEnabled(true);
                tvResendCode.setText(getString(R.string.resend_code));
            }
        }.start();
    }

    private boolean validateOTP() {
        if (etOtp1 == null || etOtp2 == null || etOtp3 == null || etOtp4 == null || etOtp5 == null || etOtp6 == null)
            return false;
        if (safeText(etOtp1).isEmpty() || safeText(etOtp2).isEmpty() || safeText(etOtp3).isEmpty() ||
                safeText(etOtp4).isEmpty() || safeText(etOtp5).isEmpty() || safeText(etOtp6).isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String collectOtp() {
        return safeText(etOtp1) + safeText(etOtp2) + safeText(etOtp3) + safeText(etOtp4) + safeText(etOtp5) + safeText(etOtp6);
    }

    private RegisterRequest buildRegisterRequest() {
        // Lấy username trực tiếp từ form Basic Info (không auto-generate nữa)
        String username = safeText(etUsername);
        String email = safeText(etEmail);
        String password = safeText(etPassword);
        String phone = safeText(etPhoneNumber);

        // Lấy firstName và lastName từ form CCCD
        String firstName = safeText(etFirstName);
        String lastName = safeText(etLastName);

        // Các thông tin CCCD khác
        String cccdNumber = safeText(etCccdNumber);
        String permanentAddress = safeText(etPermanentAddress);
        String dateOfBirthUi = safeText(etDateOfBirth); // dd/MM/yyyy
        String dateOfBirthIso = formatDobToIso(dateOfBirthUi);

        // Phần chụp ảnh tạm thời null (đã bị ẩn)
        String frontPhotoPath = null;
        String backPhotoPath = null;

        return new RegisterRequest(
                email,
                username,
                password,
                phone,
                firstName,
                lastName,
                cccdNumber,
                dateOfBirthIso,
                permanentAddress,
                frontPhotoPath,
                backPhotoPath
        );
    }

    private String safeText(EditText et) {
        return et == null || et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isEmpty()) return new String[]{"", ""};
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return new String[]{parts[0], parts[0]};
        StringBuilder first = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) first.append(" ");
            first.append(parts[i]);
        }
        String last = parts[parts.length - 1];
        return new String[]{first.toString(), last};
    }

    private String formatDobToIso(String dobUi) {
        if (dobUi == null || dobUi.isEmpty()) return "1990-01-01T00:00:00"; // fallback
        try {
            SimpleDateFormat uiFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00", Locale.getDefault());
            return isoFmt.format(uiFmt.parse(dobUi));
        } catch (Exception e) {
            return "1990-01-01T00:00:00";
        }
    }

    private String generateUsername(String email, String fullName) {
        String base = null;
        if (email != null && email.contains("@")) base = email.substring(0, email.indexOf('@'));
        if (base == null || base.isEmpty()) {
            base = fullName == null ? "" : fullName.trim().replaceAll("\\s+", "-").toLowerCase(Locale.getDefault());
        }
        base = base.replaceAll("[^A-Za-z0-9_-]", "_")
                   .replaceAll("_+", "_");
        if (base.isEmpty()) base = "user" + System.currentTimeMillis();
        if (base.length() > 30) base = base.substring(0, 30);
        return base;
    }

    // Map field error from backend to the appropriate UI element
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
            case "username":
                // Hiện chưa có ô riêng username -> reuse email cho cảnh báo
                if (etEmail != null) etEmail.setError(msg);
                break;
            case "password":
                if (etPassword != null) etPassword.setError(msg);
                break;
            case "confirmPassword":
                if (etConfirmPassword != null) etConfirmPassword.setError(msg);
                break;
            case "idNumber":
            case "cccdNumber":
                if (etCccdNumber != null) etCccdNumber.setError(msg);
                break;
            case "firstName":
            case "lastName":
                if (etFirstName != null) etFirstName.setError(msg);
                break;
            case "dateOfBirth":
                if (etDateOfBirth != null) etDateOfBirth.setError(msg);
                break;
            case "issueDate":
                if (etIssueDate != null) etIssueDate.setError(msg);
                break;
            case "issuePlace":
                if (etIssuePlace != null) etIssuePlace.setError(msg);
                break;
            case "gender":
                if (actvGender != null) actvGender.setError(msg);
                break;
            case "nationality":
                if (etNationality != null) etNationality.setError(msg);
                break;
            case "address":
            case "permanentAddress":
                if (etPermanentAddress != null) etPermanentAddress.setError(msg);
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
            Log.e(TAG, "HTTP " + httpEx.code() + " OTP error body: " + body);
            if (body == null || body.isEmpty()) return false;
            Gson gson = new Gson();
            ErrorResponse er = gson.fromJson(body, ErrorResponse.class);
            if (er == null || er.getError() == null) return false;

            // Field errors first
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
                        Log.e(TAG, "Server uncategorized exception: " + er.getMessage());
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
    // ================= END HELPER METHODS ======================

    @Override
    protected void onDestroy() {
        if (resendTimer != null) resendTimer.cancel();
        compositeDisposable.clear();
        super.onDestroy();
    }

    // Inner class for OTP text change handling
    private class OTPTextWatcher implements TextWatcher {
        private EditText currentEditText;
        private EditText nextEditText;

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

    // === Khôi phục hàm setupLoginLink & setupDatePickers (bị thiếu) ===
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
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLoginLink.setText(ss);
        tvLoginLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupDatePickers() {
        if (etDateOfBirth != null) {
            etDateOfBirth.setOnClickListener(v -> showDatePickerDialog(etDateOfBirth));
            if (tilDateOfBirth != null) {
                tilDateOfBirth.setEndIconOnClickListener(v -> showDatePickerDialog(etDateOfBirth));
            }
        }
        if (etIssueDate != null) {
            etIssueDate.setOnClickListener(v -> showDatePickerDialog(etIssueDate));
            if (tilIssueDate != null) {
                tilIssueDate.setEndIconOnClickListener(v -> showDatePickerDialog(etIssueDate));
            }
        }
    }

    private void showDatePickerDialog(final TextInputEditText editText) {
        Calendar currentCalendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    String formatted = dateFormatter.format(selected.getTime());
                    editText.setText(formatted);
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
        );

        if (editText == etDateOfBirth) { // giới hạn tuổi 18 - 100
            Calendar minAge = Calendar.getInstance();
            Calendar maxAge = Calendar.getInstance();
            minAge.add(Calendar.YEAR, -100);
            maxAge.add(Calendar.YEAR, -18);
            datePickerDialog.getDatePicker().setMaxDate(maxAge.getTimeInMillis());
            datePickerDialog.getDatePicker().setMinDate(minAge.getTimeInMillis());
        }
        if (editText == etIssueDate) { // không cho chọn quá ngày hiện tại
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }
        datePickerDialog.show();
    }
}
