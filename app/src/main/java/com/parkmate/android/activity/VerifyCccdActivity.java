package com.parkmate.android.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.parkmate.android.R;
import com.parkmate.android.model.request.UpdateUserRequest;
import com.parkmate.android.model.response.UpdateUserResponse;
import com.parkmate.android.model.response.UploadImageResponse;
import com.parkmate.android.model.response.UserInfoResponse;
import com.parkmate.android.repository.AuthRepository;
import com.parkmate.android.utils.FileUtils;
import com.parkmate.android.utils.UserManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class VerifyCccdActivity extends AppCompatActivity {

    private static final String TAG = "VerifyCccdActivity";

    private ImageButton btnBack;
    private TextInputEditText etCccdNumber, etFullName, etDateOfBirth, etIssueDate, etIssuePlace, etExpiryDate, etPermanentAddress;
    private AutoCompleteTextView actvGender, actvNationality;
    private TextInputLayout tilDateOfBirth, tilIssueDate, tilExpiryDate;
    private CheckBox cbCommitment;
    private FrameLayout flFrontImage, flBackImage;
    private ImageView ivFrontImage, ivBackImage;
    private LinearLayout llFrontImagePlaceholder, llBackImagePlaceholder;
    private ProgressBar pbFrontUpload, pbBackUpload;
    private ImageView ivFrontUploadSuccess, ivBackUploadSuccess;
    private MaterialButton btnSubmit;

    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private Uri frontImageUri, backImageUri;
    private Uri tempCameraImageUri;
    private boolean frontImageUploaded = false;
    private boolean backImageUploaded = false;
    private String frontImagePath = "";
    private String backImagePath = "";

    private ActivityResultLauncher<Intent> frontCameraLauncher;
    private ActivityResultLauncher<Intent> backCameraLauncher;
    private ActivityResultLauncher<Intent> frontGalleryLauncher;
    private ActivityResultLauncher<Intent> backGalleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private boolean pendingFrontCamera = false;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_cccd);

        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        authRepository = new AuthRepository();

        setupImageCaptureLaunchers();
        setupPermissionLauncher();

        initViews();
        setupDropdowns();
        setupListeners();
        setupDatePickers();

        // Load thông tin user đã có (nếu đã xác thực CCCD trước đó)
        loadUserInfo();
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
        btnBack = findViewById(R.id.btnBack);
        etCccdNumber = findViewById(R.id.etCccdNumber);
        etFullName = findViewById(R.id.etFullName);
        actvGender = findViewById(R.id.actvGender);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        actvNationality = findViewById(R.id.actvNationality);
        etPermanentAddress = findViewById(R.id.etPermanentAddress);
        etIssueDate = findViewById(R.id.etIssueDate);
        etIssuePlace = findViewById(R.id.etIssuePlace);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        tilDateOfBirth = findViewById(R.id.tilDateOfBirth);
        tilIssueDate = findViewById(R.id.tilIssueDate);
        tilExpiryDate = findViewById(R.id.tilExpiryDate);

        flFrontImage = findViewById(R.id.flFrontImage);
        flBackImage = findViewById(R.id.flBackImage);
        ivFrontImage = findViewById(R.id.ivFrontImage);
        ivBackImage = findViewById(R.id.ivBackImage);
        llFrontImagePlaceholder = findViewById(R.id.llFrontImagePlaceholder);
        llBackImagePlaceholder = findViewById(R.id.llBackImagePlaceholder);
        pbFrontUpload = findViewById(R.id.pbFrontUpload);
        pbBackUpload = findViewById(R.id.pbBackUpload);
        ivFrontUploadSuccess = findViewById(R.id.ivFrontUploadSuccess);
        ivBackUploadSuccess = findViewById(R.id.ivBackUploadSuccess);

        cbCommitment = findViewById(R.id.cbCommitment);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupDropdowns() {
        // Setup Gender Dropdown
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genderOptions
        );
        actvGender.setAdapter(genderAdapter);

        // Setup Nationality Dropdown
        String[] nationalityOptions = getResources().getStringArray(R.array.nationality_options);
        ArrayAdapter<String> nationalityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nationalityOptions
        );
        actvNationality.setAdapter(nationalityAdapter);

        // Set default nationality to Vietnam
        actvNationality.setText("Việt Nam", false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        flFrontImage.setOnClickListener(v -> showImageSourceDialog(true));
        flBackImage.setOnClickListener(v -> showImageSourceDialog(false));

        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void setupDatePickers() {
        etDateOfBirth.setOnClickListener(v -> showDatePicker(etDateOfBirth));
        etIssueDate.setOnClickListener(v -> showDatePicker(etIssueDate));
        etExpiryDate.setOnClickListener(v -> showDatePicker(etExpiryDate));
    }

    private void showDatePicker(TextInputEditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    editText.setText(dateFormatter.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showImageSourceDialog(boolean isFront) {
        String title = isFront ? "Chọn nguồn hình ảnh cho mặt trước CCCD" : "Chọn nguồn hình ảnh cho m��t sau CCCD";
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
                    } else {
                        openGallery(isFront);
                    }
                })
                .show();
    }

    private void openCamera(boolean isFront) {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = createImageFile();
            if (photoFile != null) {
                tempCameraImageUri = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraImageUri);
                if (isFront) {
                    frontCameraLauncher.launch(takePictureIntent);
                } else {
                    backCameraLauncher.launch(takePictureIntent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening camera", e);
            Toast.makeText(this, "Không thể mở camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery(boolean isFront) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (isFront) {
            frontGalleryLauncher.launch(intent);
        } else {
            backGalleryLauncher.launch(intent);
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
            String imageFileName = "CCCD_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (Exception e) {
            Log.e(TAG, "Error creating image file", e);
            return null;
        }
    }

    private void handleSubmit() {
        if (!validateInput()) {
            return;
        }

        // Kiểm tra checkbox cam kết
        if (!cbCommitment.isChecked()) {
            Toast.makeText(this, "Vui lòng xác nhận cam kết thông tin chính xác", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra phải có ảnh (hoặc URI mới hoặc path đã có từ backend)
        boolean hasFrontImage = (frontImageUri != null) || (frontImageUploaded && !frontImagePath.isEmpty());
        boolean hasBackImage = (backImageUri != null) || (backImageUploaded && !backImagePath.isEmpty());

        if (!hasFrontImage || !hasBackImage) {
            Toast.makeText(this, "Vui lòng tải lên ảnh mặt trước và mặt sau CCCD", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang xử lý...");

        // Lấy userId từ UserManager - đây là userResponse.id (id: 9) để dùng cho entityId khi upload ảnh
        // API updateUser sẽ dùng token từ header Authorization để xác định user
        String userId = UserManager.getInstance().getUserId();
        Log.d(TAG, "========== VERIFY CCCD SUBMIT ==========");
        Log.d(TAG, "UserId from UserManager: " + userId);

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "❌ UserId is NULL or EMPTY - User not logged in!");
            Toast.makeText(this, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            resetSubmitButton();
            return;
        }

        Long entityId;
        try {
            entityId = Long.parseLong(userId);
            Log.d(TAG, "✅ EntityId for upload: " + entityId + " (from userResponse.id)");
        } catch (NumberFormatException e) {
            Log.e(TAG, "❌ Invalid userId format: " + userId, e);
            Toast.makeText(this, "Lỗi xác thực phiên đăng nhập. Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            resetSubmitButton();
            return;
        }

        // Nếu có ảnh mới được chọn, upload chúng. Nếu không, dùng path đã có
        if (frontImageUri != null || backImageUri != null) {
            // Có ít nhất một ảnh mới cần upload
            uploadImages(entityId);
        } else {
            // Không có ảnh mới, chỉ cập nhật thông tin text với path ảnh đã có
            updateUserInfo();
        }
    }

    private boolean validateInput() {
        String cccdNumber = etCccdNumber.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String gender = actvGender.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String nationality = actvNationality.getText().toString().trim();
        String permanentAddress = etPermanentAddress.getText().toString().trim();
        String issueDate = etIssueDate.getText().toString().trim();
        String issuePlace = etIssuePlace.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        if (cccdNumber.isEmpty()) {
            etCccdNumber.setError("Vui lòng nhập số CCCD");
            etCccdNumber.requestFocus();
            return false;
        }

        if (cccdNumber.length() != 9 && cccdNumber.length() != 12) {
            etCccdNumber.setError("Số CCCD phải có 9 hoặc 12 số");
            etCccdNumber.requestFocus();
            return false;
        }

        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return false;
        }

        if (gender.isEmpty()) {
            actvGender.setError("Vui lòng chọn giới tính");
            actvGender.requestFocus();
            return false;
        }

        if (dateOfBirth.isEmpty()) {
            etDateOfBirth.setError("Vui lòng chọn ngày sinh");
            etDateOfBirth.requestFocus();
            return false;
        }

        if (nationality.isEmpty()) {
            actvNationality.setError("Vui lòng chọn quốc tịch");
            actvNationality.requestFocus();
            return false;
        }

        if (permanentAddress.isEmpty()) {
            etPermanentAddress.setError("Vui lòng nhập địa chỉ thường trú");
            etPermanentAddress.requestFocus();
            return false;
        }

        if (issueDate.isEmpty()) {
            etIssueDate.setError("Vui lòng chọn ngày cấp");
            etIssueDate.requestFocus();
            return false;
        }

        if (issuePlace.isEmpty()) {
            etIssuePlace.setError("Vui lòng nhập nơi cấp");
            etIssuePlace.requestFocus();
            return false;
        }

        if (expiryDate.isEmpty()) {
            etExpiryDate.setError("Vui lòng chọn ngày hết hạn");
            etExpiryDate.requestFocus();
            return false;
        }

        return true;
    }

    private void uploadImages(Long entityId) {
        // Kiểm tra xem ảnh nào cần upload
        boolean needUploadFront = (frontImageUri != null);
        boolean needUploadBack = (backImageUri != null);

        if (needUploadFront) {
            // Upload front image
            pbFrontUpload.setVisibility(View.VISIBLE);

            File frontFile = FileUtils.getFileFromUri(this, frontImageUri);
            if (frontFile == null) {
                Toast.makeText(this, "Không thể đọc ảnh mặt trước", Toast.LENGTH_SHORT).show();
                resetSubmitButton();
                return;
            }

            compositeDisposable.add(
                    authRepository.uploadIdImage(entityId, "FRONT_ID_CARD", frontFile)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    response -> {
                                        pbFrontUpload.setVisibility(View.GONE);
                                        ivFrontUploadSuccess.setVisibility(View.VISIBLE);
                                        frontImageUploaded = true;
                                        frontImagePath = response.getImagePath() != null ? response.getImagePath() : "";
                                        Log.d(TAG, "Front image uploaded successfully: " + frontImagePath);

                                        // Kiểm tra xem có cần upload back image không
                                        if (needUploadBack) {
                                            uploadBackImage(entityId);
                                        } else {
                                            // Không cần upload back image, cập nhật thông tin luôn
                                            updateUserInfo();
                                        }
                                    },
                                    error -> {
                                        pbFrontUpload.setVisibility(View.GONE);
                                        Log.e(TAG, "Error uploading front image", error);
                                        Toast.makeText(this, "Lỗi tải lên ảnh mặt trước: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        resetSubmitButton();
                                    }
                            )
            );
        } else if (needUploadBack) {
            // Chỉ cần upload back image
            uploadBackImage(entityId);
        }
    }

    private void uploadBackImage(Long entityId) {
        pbBackUpload.setVisibility(View.VISIBLE);

        File backFile = FileUtils.getFileFromUri(this, backImageUri);
        if (backFile == null) {
            Toast.makeText(this, "Không thể đọc ảnh mặt sau", Toast.LENGTH_SHORT).show();
            resetSubmitButton();
            return;
        }

        compositeDisposable.add(
                authRepository.uploadIdImage(entityId, "BACK_ID_CARD", backFile)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    pbBackUpload.setVisibility(View.GONE);
                                    ivBackUploadSuccess.setVisibility(View.VISIBLE);
                                    backImageUploaded = true;
                                    backImagePath = response.getImagePath() != null ? response.getImagePath() : "";
                                    Log.d(TAG, "Back image uploaded successfully: " + backImagePath);

                                    // Cả 2 ảnh đã xử lý xong, gọi API cập nhật user
                                    updateUserInfo();
                                },
                                error -> {
                                    pbBackUpload.setVisibility(View.GONE);
                                    Log.e(TAG, "Error uploading back image", error);
                                    Toast.makeText(this, "Lỗi tải lên ảnh mặt sau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    resetSubmitButton();
                                }
                        )
        );
    }

    private void updateUserInfo() {
        UpdateUserRequest request = new UpdateUserRequest();

        // Set tất cả các trường thông tin CCCD
        request.setIdNumber(etCccdNumber.getText().toString().trim());
        request.setFullName(etFullName.getText().toString().trim());
        request.setGender(actvGender.getText().toString().trim());
        request.setDateOfBirth(convertToIso8601(etDateOfBirth.getText().toString().trim()));
        request.setNationality(actvNationality.getText().toString().trim());

        // Gửi địa chỉ thường trú vào field "address" (không phải "permanentAddress")
        // Vì backend lưu vào userResponse.address theo swagger
        request.setAddress(etPermanentAddress.getText().toString().trim());

        request.setIssueDate(convertToIso8601(etIssueDate.getText().toString().trim()));
        request.setIssuePlace(etIssuePlace.getText().toString().trim());
        request.setExpiryDate(convertToIso8601(etExpiryDate.getText().toString().trim()));
        request.setFrontIdPath(frontImagePath);
        request.setBackIdImgPath(backImagePath);

        Log.d(TAG, "Updating user info with address: " + etPermanentAddress.getText().toString().trim());

        compositeDisposable.add(
                authRepository.updateUser(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Log.d(TAG, "User info updated successfully");
                                    Toast.makeText(this, "Xác thực CCCD thành công! Ví của bạn đã được kích hoạt.", Toast.LENGTH_LONG).show();

                                    // Quay về ProfileActivity
                                    finish();
                                },
                                error -> {
                                    Log.e(TAG, "Error updating user info", error);
                                    Toast.makeText(this, "Lỗi cập nhật thông tin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    resetSubmitButton();
                                }
                        )
        );
    }

    /**
     * Load thông tin user từ backend và pre-fill vào các trường
     */
    private void loadUserInfo() {
        String userId = UserManager.getInstance().getUserId();

        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "UserId not found, skipping user info loading");
            return;
        }

        Log.d(TAG, "Loading user info for userId: " + userId);

        compositeDisposable.add(
                authRepository.getUserInfo(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.isSuccess() && response.getData() != null) {
                                        Log.d(TAG, "User info loaded successfully");
                                        prefillUserData(response.getData());
                                    } else {
                                        Log.w(TAG, "User info response not successful or data is null");
                                    }
                                },
                                error -> {
                                    Log.e(TAG, "Error loading user info", error);
                                    // Không hiển thị lỗi cho user vì đây chỉ là pre-fill data
                                    // User vẫn có thể nhập thủ công
                                }
                        )
        );
    }

    /**
     * Pre-fill dữ liệu user vào các trường
     */
    private void prefillUserData(UserInfoResponse.UserData userData) {
        Log.d(TAG, "Pre-filling user data");

        // Số CCCD
        if (userData.getIdNumber() != null && !userData.getIdNumber().isEmpty()) {
            etCccdNumber.setText(userData.getIdNumber());
        }

        // Họ tên
        if (userData.getFullName() != null && !userData.getFullName().isEmpty()) {
            etFullName.setText(userData.getFullName());
        }

        // Giới tính
        if (userData.getGender() != null && !userData.getGender().isEmpty()) {
            actvGender.setText(userData.getGender(), false);
        }

        // Ngày sinh - convert từ ISO8601 sang dd/MM/yyyy
        if (userData.getDateOfBirth() != null && !userData.getDateOfBirth().isEmpty()) {
            String formattedDate = convertFromIso8601(userData.getDateOfBirth());
            etDateOfBirth.setText(formattedDate);
        }

        // Quốc tịch
        if (userData.getNationality() != null && !userData.getNationality().isEmpty()) {
            actvNationality.setText(userData.getNationality(), false);
        }

        // Địa chỉ thường trú
        if (userData.getAddress() != null && !userData.getAddress().isEmpty()) {
            etPermanentAddress.setText(userData.getAddress());
        }

        // Ngày cấp
        if (userData.getIssueDate() != null && !userData.getIssueDate().isEmpty()) {
            String formattedDate = convertFromIso8601(userData.getIssueDate());
            etIssueDate.setText(formattedDate);
        }

        // Nơi cấp
        if (userData.getIssuePlace() != null && !userData.getIssuePlace().isEmpty()) {
            etIssuePlace.setText(userData.getIssuePlace());
        }

        // Ngày hết hạn
        if (userData.getExpiryDate() != null && !userData.getExpiryDate().isEmpty()) {
            String formattedDate = convertFromIso8601(userData.getExpiryDate());
            etExpiryDate.setText(formattedDate);
        }

        // Đường dẫn ảnh mặt trước
        if (userData.getFrontIdPath() != null && !userData.getFrontIdPath().isEmpty()) {
            frontImagePath = userData.getFrontIdPath();
            frontImageUploaded = true;
            ivFrontUploadSuccess.setVisibility(View.VISIBLE);
            Log.d(TAG, "Front ID image already uploaded: " + frontImagePath);
        }

        // Load ảnh mặt trước từ presigned URL nếu có
        if (userData.getFrontPhotoPresignedUrl() != null && !userData.getFrontPhotoPresignedUrl().isEmpty()) {
            Log.d(TAG, "Loading front image from URL: " + userData.getFrontPhotoPresignedUrl());

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.bg_image_placeholder) // placeholder khi đang load
                    .error(R.drawable.bg_image_placeholder) // ảnh hiển thị khi lỗi
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // không cache vì URL có thời hạn
                    .skipMemoryCache(true);

            Glide.with(this)
                    .load(userData.getFrontPhotoPresignedUrl())
                    .apply(options)
                    .into(ivFrontImage);

            ivFrontImage.setVisibility(View.VISIBLE);
            llFrontImagePlaceholder.setVisibility(View.GONE);
        }

        // Đường dẫn ảnh mặt sau
        if (userData.getBackIdImgPath() != null && !userData.getBackIdImgPath().isEmpty()) {
            backImagePath = userData.getBackIdImgPath();
            backImageUploaded = true;
            ivBackUploadSuccess.setVisibility(View.VISIBLE);
            Log.d(TAG, "Back ID image already uploaded: " + backImagePath);
        }

        // Load ảnh mặt sau từ presigned URL nếu có
        if (userData.getBackPhotoPresignedUrl() != null && !userData.getBackPhotoPresignedUrl().isEmpty()) {
            Log.d(TAG, "Loading back image from URL: " + userData.getBackPhotoPresignedUrl());

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.bg_image_placeholder) // placeholder khi đang load
                    .error(R.drawable.bg_image_placeholder) // ảnh hiển thị khi lỗi
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // không cache vì URL có thời hạn
                    .skipMemoryCache(true);

            Glide.with(this)
                    .load(userData.getBackPhotoPresignedUrl())
                    .apply(options)
                    .into(ivBackImage);

            ivBackImage.setVisibility(View.VISIBLE);
            llBackImagePlaceholder.setVisibility(View.GONE);
        }

        Log.d(TAG, "User data pre-filled successfully");
    }

    private String convertToIso8601(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateString));
        } catch (Exception e) {
            Log.e(TAG, "Error converting date", e);
            return dateString;
        }
    }

    /**
     * Chuyển đổi từ ISO8601 (yyyy-MM-dd'T'HH:mm:ss) sang dd/MM/yyyy
     */
    private String convertFromIso8601(String iso8601Date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(iso8601Date));
        } catch (Exception e) {
            // Thử parse với format khác nếu format trên không match
            try {
                SimpleDateFormat altInputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return outputFormat.format(altInputFormat.parse(iso8601Date));
            } catch (Exception e2) {
                Log.e(TAG, "Error converting date from ISO8601", e2);
                return iso8601Date;
            }
        }
    }

    private void resetSubmitButton() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Xác thực");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
