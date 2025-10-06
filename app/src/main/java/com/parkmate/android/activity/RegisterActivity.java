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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import android.os.CountDownTimer;
import com.parkmate.android.utils.LoadingButton;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int SCREEN_BASIC_INFO = 0;
    private static final int SCREEN_CCCD_INFO = 1;
    private static final int SCREEN_UPLOAD_IMAGES = 2;
    private static final int SCREEN_CHECK_EMAIL = 3;
    private static final int SCREEN_OTP = 4;
    private static final int SCREEN_SUCCESS = 5;

    private ViewFlipper viewFlipper;
    private TextInputEditText etEmail, etPassword, etConfirmPassword, etPhoneNumber;
    private TextInputEditText etFirstName, etLastName;
    private TextInputEditText etCccdNumber, etFullName, etDateOfBirth, etIssueDate, etIssuePlace, etExpiryDate;
    private TextInputEditText etNationality, etPermanentAddress;
    private AutoCompleteTextView actvGender;
    private TextInputLayout tilDateOfBirth, tilIssueDate, tilExpiryDate, tilGender;
    private MaterialButton btnNext;
    private Button btnContinue, btnVerify, btnLogin;
    private ImageView ivBack, ivGoogleSignUp, ivFacebookSignUp;
    private TextView tvLoginLink, tvUserEmail, tvResendCode;

    private FrameLayout flFrontImage, flBackImage;
    private ImageView ivFrontImage, ivBackImage;
    private LinearLayout llFrontImagePlaceholder, llBackImagePlaceholder;
    private ProgressBar pbFrontUpload, pbBackUpload;
    private ImageView ivFrontUploadSuccess, ivBackUploadSuccess;
    private Button btnContinueUpload, btnSkipUpload;

    private CheckBox cbCommitment;

    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;

    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private Uri frontImageUri, backImageUri;
    private String userEmail;
    private Long entityId;
    private boolean frontImageUploaded = false;
    private boolean backImageUploaded = false;

    private Uri tempCameraImageUri;

    private ActivityResultLauncher<Intent> frontCameraLauncher;
    private ActivityResultLauncher<Intent> backCameraLauncher;
    private ActivityResultLauncher<Intent> frontGalleryLauncher;
    private ActivityResultLauncher<Intent> backGalleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private boolean pendingFrontCamera = false;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AuthRepository authRepository;
    private CountDownTimer resendTimer;
    private static final long RESEND_OTP_INTERVAL_MS = 60000L;
    private LoadingButton loadingButtonContinue;
    private LoadingButton loadingButtonVerify;
    private LoadingButton loadingButtonUpload;

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

        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        setupImageCaptureLaunchers();
        setupPermissionLauncher();

        authRepository = new AuthRepository();
        initViews();
        setupGenderDropdown();
        setupListeners();
        setupLoginLink();
        setupDatePickers();
        setupImageCapture();
    }

    private void setupPermissionLauncher() {
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        if (pendingFrontCamera) {
                            openCamera(true);
                        } else {
                            openCamera(false);
                        }
                        pendingFrontCamera = false;
                    } else {
                        Toast.makeText(this, "Cần cấp quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupImageCaptureLaunchers() {
        frontCameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (tempCameraImageUri != null) {
                            frontImageUri = tempCameraImageUri;
                            ivFrontImage.setImageURI(frontImageUri);
                            ivFrontImage.setVisibility(View.VISIBLE);
                            llFrontImagePlaceholder.setVisibility(View.GONE);
                        }
                    }
                }
        );

        backCameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (tempCameraImageUri != null) {
                            backImageUri = tempCameraImageUri;
                            ivBackImage.setImageURI(backImageUri);
                            ivBackImage.setVisibility(View.VISIBLE);
                            llBackImagePlaceholder.setVisibility(View.GONE);
                        }
                    }
                }
        );

        frontGalleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            frontImageUri = imageUri;
                            ivFrontImage.setImageURI(frontImageUri);
                            ivFrontImage.setVisibility(View.VISIBLE);
                            llFrontImagePlaceholder.setVisibility(View.GONE);
                        }
                    }
                }
        );

        backGalleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            backImageUri = imageUri;
                            ivBackImage.setImageURI(backImageUri);
                            ivBackImage.setVisibility(View.VISIBLE);
                            llBackImagePlaceholder.setVisibility(View.GONE);
                        }
                    }
                }
        );
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);

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

        etCccdNumber = findViewById(R.id.etCccdNumber);
        etFullName = findViewById(R.id.etFullName);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etIssueDate = findViewById(R.id.etIssueDate);
        etIssuePlace = findViewById(R.id.etIssuePlace);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        tilDateOfBirth = findViewById(R.id.tilDateOfBirth);
        tilIssueDate = findViewById(R.id.tilIssueDate);
        tilExpiryDate = findViewById(R.id.tilExpiryDate);

        actvGender = findViewById(R.id.actvGender);
        tilGender = findViewById(R.id.tilGender);
        etNationality = findViewById(R.id.etNationality);
        etPermanentAddress = findViewById(R.id.etPermanentAddress);

        btnContinue = findViewById(R.id.btnContinue);

        ivBack = findViewById(R.id.ivBack);

        flFrontImage = findViewById(R.id.flFrontImage);
        flBackImage = findViewById(R.id.flBackImage);
        ivFrontImage = findViewById(R.id.ivFrontImage);
        ivBackImage = findViewById(R.id.ivBackImage);
        llFrontImagePlaceholder = findViewById(R.id.llFrontImagePlaceholder);
        llBackImagePlaceholder = findViewById(R.id.llBackImagePlaceholder);

        cbCommitment = findViewById(R.id.cbCommitment);
    }

    private void setupGenderDropdown() {
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
        if (flFrontImage != null) {
            flFrontImage.setOnClickListener(v -> showImageSourceDialog(true));
        }

        if (flBackImage != null) {
            flBackImage.setOnClickListener(v -> showImageSourceDialog(false));
        }
    }

    private void showImageSourceDialog(boolean isFront) {
        String title = isFront ? "Chọn nguồn hình ảnh cho mặt trước CCCD" : "Chọn nguồn hình ảnh cho mặt sau CCCD";
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(new CharSequence[]{"Chụp ảnh từ camera", "Chọn từ thư viện"}, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            pendingFrontCamera = isFront;
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
                        } else {
                            openCamera(isFront);
                        }
                    } else if (which == 1) {
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        if (isFront) {
                            frontGalleryLauncher.launch(pickPhotoIntent);
                        } else {
                            backGalleryLauncher.launch(pickPhotoIntent);
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void openCamera(boolean isFront) {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                tempCameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        photoFile
                );

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraImageUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (isFront) {
                    frontCameraLauncher.launch(takePictureIntent);
                } else {
                    backCameraLauncher.launch(takePictureIntent);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi mở camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new java.util.Date());
            String imageFileName = "CCCD_" + timeStamp + "_";

            File storageDir = getCacheDir();
            File imageFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );

            return imageFile;
        } catch (Exception e) {
            return null;
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
            loadingButtonContinue = new LoadingButton(btnContinue);

            btnContinue.setOnClickListener(v -> {
                if (validateCccdInfo()) {
                    userEmail = safeText(etEmail);
                    loadingButtonContinue.showLoading("Đang đăng ký...");
                    performRegisterForOtp();
                }
            });
        }
    }

    private void performRegisterForOtp() {
        RegisterRequest request = buildRegisterRequest();
        compositeDisposable.add(
                authRepository.register(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> {
                                    if (loadingButtonContinue != null) loadingButtonContinue.hideLoading();

                                    storeEntityId(resp);
                                    storeRegisterToken(resp);
                                    Toast.makeText(this, "Đăng ký thành công! OTP đã được gửi đến email của bạn.", Toast.LENGTH_SHORT).show();
                                    viewFlipper.setDisplayedChild(SCREEN_UPLOAD_IMAGES);
                                    setupImageUploadScreen();
                                },
                                err -> {
                                    if (loadingButtonContinue != null) loadingButtonContinue.hideLoading();
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

    private void storeEntityId(RegisterResponse resp) {
        if (resp == null) {
            return;
        }

        if (resp.getData() != null && resp.getData().getUserResponse() != null && resp.getData().getUserResponse().getId() != null) {
            entityId = resp.getData().getUserResponse().getId();
            return;
        }

        if (resp.getUserResponse() != null && resp.getUserResponse().getId() != null) {
            entityId = resp.getUserResponse().getId();
            return;
        }

        if (resp.getEntityId() != null) {
            entityId = resp.getEntityId();
            return;
        }

        if (resp.getData() != null && resp.getData().getEntityId() != null) {
            entityId = resp.getData().getEntityId();
        }
    }

    private void storeRegisterToken(RegisterResponse resp) {
        if (resp == null) return;
        String rawToken = resp.getAnyToken();
        if (rawToken == null || rawToken.isEmpty()) {
            return;
        }
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

    private void setupImageUploadScreen() {
        View uploadView = viewFlipper.getChildAt(SCREEN_UPLOAD_IMAGES);
        if (uploadView == null) {
            return;
        }

        flFrontImage = uploadView.findViewById(R.id.flFrontImage);
        flBackImage = uploadView.findViewById(R.id.flBackImage);
        ivFrontImage = uploadView.findViewById(R.id.ivFrontImage);
        ivBackImage = uploadView.findViewById(R.id.ivBackImage);
        llFrontImagePlaceholder = uploadView.findViewById(R.id.llFrontImagePlaceholder);
        llBackImagePlaceholder = uploadView.findViewById(R.id.llBackImagePlaceholder);
        pbFrontUpload = uploadView.findViewById(R.id.pbFrontUpload);
        pbBackUpload = uploadView.findViewById(R.id.pbBackUpload);
        ivFrontUploadSuccess = uploadView.findViewById(R.id.ivFrontUploadSuccess);
        ivBackUploadSuccess = uploadView.findViewById(R.id.ivBackUploadSuccess);
        btnContinueUpload = uploadView.findViewById(R.id.btnContinue);
        btnSkipUpload = uploadView.findViewById(R.id.btnSkip);

        frontImageUploaded = false;
        backImageUploaded = false;

        if (flFrontImage != null) {
            flFrontImage.setOnClickListener(v -> showImageSourceDialog(true));
        }

        if (flBackImage != null) {
            flBackImage.setOnClickListener(v -> showImageSourceDialog(false));
        }

        if (btnContinueUpload != null) {
            btnContinueUpload.setOnClickListener(v -> {
                if (frontImageUri != null || backImageUri != null) {
                    uploadImagesAndProceed();
                } else {
                    viewFlipper.setDisplayedChild(SCREEN_CHECK_EMAIL);
                    setupCheckEmailScreen();
                }
            });
        }

        if (btnSkipUpload != null) {
            btnSkipUpload.setOnClickListener(v -> {
                viewFlipper.setDisplayedChild(SCREEN_CHECK_EMAIL);
                setupCheckEmailScreen();
            });
        }
    }

    private void uploadImagesAndProceed() {
        if (entityId == null) {
            Toast.makeText(this, "Lỗi: Không có entityId để upload ảnh", Toast.LENGTH_SHORT).show();
            viewFlipper.setDisplayedChild(SCREEN_CHECK_EMAIL);
            setupCheckEmailScreen();
            return;
        }

        if (pbFrontUpload != null && frontImageUri != null) pbFrontUpload.setVisibility(View.VISIBLE);
        if (pbBackUpload != null && backImageUri != null) pbBackUpload.setVisibility(View.VISIBLE);

        if (frontImageUri != null) {
            uploadImageWithProgress(frontImageUri, "FRONT_ID_CARD", true, () -> {
                if (backImageUri != null) {
                    uploadImageWithProgress(backImageUri, "BACK_ID_CARD", false, this::navigateToCheckEmail);
                } else {
                    navigateToCheckEmail();
                }
            });
        } else if (backImageUri != null) {
            uploadImageWithProgress(backImageUri, "BACK_ID_CARD", false, this::navigateToCheckEmail);
        } else {
            navigateToCheckEmail();
        }
    }

    private void uploadImageWithProgress(Uri imageUri, String imageType, boolean isFront, Runnable onComplete) {
        try {
            File imageFile = getFileFromUri(imageUri);
            if (imageFile == null || !imageFile.exists()) {
                hideProgressBar(isFront);
                onComplete.run();
                return;
            }

            compositeDisposable.add(
                    authRepository.uploadIdImage(entityId, imageType, imageFile)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    uploadResp -> {
                                        hideProgressBar(isFront);
                                        showSuccessIcon(isFront);
                                        if (isFront) {
                                            frontImageUploaded = true;
                                        } else {
                                            backImageUploaded = true;
                                        }
                                        Toast.makeText(this, "Upload ảnh " + (isFront ? "mặt trước" : "mặt sau") + " thành công", Toast.LENGTH_SHORT).show();
                                        onComplete.run();
                                    },
                                    error -> {
                                        hideProgressBar(isFront);
                                        Toast.makeText(this, "Không thể tải lên ảnh " + (isFront ? "mặt trước" : "mặt sau"), Toast.LENGTH_SHORT).show();
                                        onComplete.run();
                                    }
                            )
            );
        } catch (Exception e) {
            hideProgressBar(isFront);
            onComplete.run();
        }
    }

    private void hideProgressBar(boolean isFront) {
        if (isFront && pbFrontUpload != null) {
            pbFrontUpload.setVisibility(View.GONE);
        } else if (!isFront && pbBackUpload != null) {
            pbBackUpload.setVisibility(View.GONE);
        }
    }

    private void showSuccessIcon(boolean isFront) {
        if (isFront && ivFrontUploadSuccess != null) {
            ivFrontUploadSuccess.setVisibility(View.VISIBLE);
        } else if (!isFront && ivBackUploadSuccess != null) {
            ivBackUploadSuccess.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToCheckEmail() {
        viewFlipper.setDisplayedChild(SCREEN_CHECK_EMAIL);
        setupCheckEmailScreen();
    }

    private void setupCheckEmailScreen() {
        View checkEmailView = viewFlipper.getChildAt(SCREEN_CHECK_EMAIL);
        if (checkEmailView == null) {
            return;
        }
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
        if (otpView == null) {
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
            String msg = resp != null && resp.getMessage() != null ? resp.getMessage() : "Xác thực OTP không thành công";
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
        if (successView == null) {
            return;
        }
        Button btnGoToLogin = successView.findViewById(R.id.btnLogin);
        if (btnGoToLogin != null) {
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
    }

    private boolean validateCccdInfo() {
        String cccdNumber = safeText(etCccdNumber);
        String fullName = safeText(etFullName);
        String dateOfBirth = safeText(etDateOfBirth);
        String issueDate = safeText(etIssueDate);
        String issuePlace = safeText(etIssuePlace);
        String expiryDate = safeText(etExpiryDate);
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

        if (fullName.isEmpty()) {
            etFullName.setError("Họ và tên (trên CCCD) không được để trống");
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

        if (expiryDate.isEmpty()) {
            etExpiryDate.setError("Ngày hết hạn không được để trống");
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
        if (etFullName != null) etFullName.setError(null);
        if (etDateOfBirth != null) etDateOfBirth.setError(null);
        if (etIssueDate != null) etIssueDate.setError(null);
        if (etIssuePlace != null) etIssuePlace.setError(null);
        if (etExpiryDate != null) etExpiryDate.setError(null);
        if (actvGender != null) actvGender.setError(null);
        if (etNationality != null) etNationality.setError(null);
        if (etPermanentAddress != null) etPermanentAddress.setError(null);
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
        String email = safeText(etEmail);
        String password = safeText(etPassword);
        String phone = safeText(etPhoneNumber);

        String firstName = safeText(etFirstName);
        String lastName = safeText(etLastName);

        String fullName = safeText(etFullName);

        String cccdNumber = safeText(etCccdNumber);
        String permanentAddress = safeText(etPermanentAddress);
        String issuePlace = safeText(etIssuePlace);

        String dateOfBirthUi = safeText(etDateOfBirth);
        String dateOfBirthIso = formatDateToIso(dateOfBirthUi);

        String issueDateUi = safeText(etIssueDate);
        String issueDateIso = formatDateToIso(issueDateUi);

        String expiryDateUi = safeText(etExpiryDate);
        String expiryDateIso = formatDateToIso(expiryDateUi);

        String frontIdPath = null;
        String backIdImgPath = null;

        return new RegisterRequest(
                email,
                password,
                phone,
                firstName,
                lastName,
                fullName,
                cccdNumber,
                dateOfBirthIso,
                issuePlace,
                issueDateIso,
                expiryDateIso,
                permanentAddress,
                frontIdPath,
                backIdImgPath
        );
    }

    private String safeText(EditText et) {
        return et == null || et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String formatDateToIso(String dateUi) {
        if (dateUi == null || dateUi.isEmpty()) return "1990-01-01T00:00:00";
        try {
            SimpleDateFormat uiFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00", Locale.getDefault());
            return isoFmt.format(uiFmt.parse(dateUi));
        } catch (Exception e) {
            return "1990-01-01T00:00:00";
        }
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
            case "idNumber":
            case "cccdNumber":
                if (etCccdNumber != null) etCccdNumber.setError(msg);
                break;
            case "firstName":
                if (etFirstName != null) etFirstName.setError(msg);
                break;
            case "lastName":
                if (etLastName != null) etLastName.setError(msg);
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
            case "expiryDate":
                if (etExpiryDate != null) etExpiryDate.setError(msg);
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
        if (etExpiryDate != null) {
            etExpiryDate.setOnClickListener(v -> showDatePickerDialog(etExpiryDate));
            if (tilExpiryDate != null) {
                tilExpiryDate.setEndIconOnClickListener(v -> showDatePickerDialog(etExpiryDate));
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

        if (editText == etDateOfBirth) {
            Calendar minAge = Calendar.getInstance();
            Calendar maxAge = Calendar.getInstance();
            minAge.add(Calendar.YEAR, -100);
            maxAge.add(Calendar.YEAR, -18);
            datePickerDialog.getDatePicker().setMaxDate(maxAge.getTimeInMillis());
            datePickerDialog.getDatePicker().setMinDate(minAge.getTimeInMillis());
        }
        if (editText == etIssueDate) {
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }
        if (editText == etExpiryDate) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        }
        datePickerDialog.show();
    }

    private File getFileFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }

        try {
            return copyUriToCache(uri);
        } catch (Exception e) {
            Log.e(TAG, "Error getting file from URI: " + e.getMessage());
            return null;
        }
    }

    private File copyUriToCache(Uri uri) {
        try {
            String fileName = "cccd_upload_" + System.currentTimeMillis() + ".jpg";
            File cacheFile = new File(getCacheDir(), fileName);

            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                return null;
            }

            int maxSize = 1920;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            if (width > maxSize || height > maxSize) {
                float scale = Math.min((float) maxSize / width, (float) maxSize / height);
                int newWidth = Math.round(width * scale);
                int newHeight = Math.round(height * scale);
                bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }

            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(cacheFile);
            boolean compressed = bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream);

            outputStream.flush();
            outputStream.close();
            bitmap.recycle();

            if (!compressed) {
                return null;
            }

            return cacheFile;
        } catch (Exception e) {
            Log.e(TAG, "Error copying URI to cache: " + e.getMessage());
            return null;
        }
    }
}
