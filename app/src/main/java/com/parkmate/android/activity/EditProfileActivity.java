package com.parkmate.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.parkmate.android.R;
import com.parkmate.android.model.request.UpdateUserRequest;
import com.parkmate.android.model.response.UpdateUserResponse;
import com.parkmate.android.model.response.UploadImageResponse;
import com.parkmate.android.model.response.UserInfoResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.repository.AuthRepository;
import com.parkmate.android.utils.FileUtils;
import com.parkmate.android.utils.UserManager;

import java.io.File;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private ImageButton btnBack;
    private CardView cvProfileImage;
    private ImageView ivProfileImage;
    private LinearLayout llUploadPlaceholder;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnEditImage;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnRemoveImage;
    private TextInputEditText etFirstName;
    private TextInputEditText etLastName;
    private TextInputEditText etPhone;
    private Button btnSave;

    private AuthRepository authRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Uri selectedImageUri;
    private String currentProfileImageUrl;
    private boolean hasImageChanged = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    hasImageChanged = true;
                    displaySelectedImage();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        authRepository = new AuthRepository();

        initViews();
        setupListeners();
        loadUserInfo();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        cvProfileImage = findViewById(R.id.cvProfileImage);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        llUploadPlaceholder = findViewById(R.id.llUploadPlaceholder);
        btnEditImage = findViewById(R.id.btnEditImage);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        cvProfileImage.setOnClickListener(v -> openImagePicker());
        btnEditImage.setOnClickListener(v -> openImagePicker());

        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            currentProfileImageUrl = null;
            hasImageChanged = true;
            ivProfileImage.setImageResource(R.drawable.ic_person_24);
            btnRemoveImage.setVisibility(View.GONE);
            llUploadPlaceholder.setVisibility(View.VISIBLE);
        });

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .into(ivProfileImage);

            llUploadPlaceholder.setVisibility(View.GONE);
            btnRemoveImage.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserInfo() {
        String userId = UserManager.getInstance().getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Đang tải...");

        compositeDisposable.add(
                ApiClient.getApiService().getUserInfo(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::handleUserInfoLoaded,
                                this::handleLoadUserInfoError
                        )
        );
    }

    private void handleUserInfoLoaded(UserInfoResponse response) {
        btnSave.setEnabled(true);
        btnSave.setText("Lưu thay đổi");

        if (response != null && response.isSuccess() && response.getData() != null) {
            UserInfoResponse.UserData userData = response.getData();

            // Fill form with current data
            if (userData.getFirstName() != null) {
                etFirstName.setText(userData.getFirstName());
            }
            if (userData.getLastName() != null) {
                etLastName.setText(userData.getLastName());
            }
            if (userData.getPhone() != null) {
                etPhone.setText(userData.getPhone());
            }

            // Load profile image
            currentProfileImageUrl = userData.getProfilePicturePresignedUrl();
            if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(currentProfileImageUrl)
                        .placeholder(R.drawable.ic_person_24)
                        .error(R.drawable.ic_person_24)
                        .centerCrop()
                        .into(ivProfileImage);

                llUploadPlaceholder.setVisibility(View.GONE);
                btnRemoveImage.setVisibility(View.VISIBLE);
            } else {
                llUploadPlaceholder.setVisibility(View.VISIBLE);
                btnRemoveImage.setVisibility(View.GONE);
            }
        }
    }

    private void handleLoadUserInfoError(Throwable error) {
        btnSave.setEnabled(true);
        btnSave.setText("Lưu thay đổi");

        Log.e(TAG, "Error loading user info", error);
        Toast.makeText(this, "Lỗi tải thông tin người dùng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void saveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validation
        if (firstName.isEmpty()) {
            etFirstName.setError("Vui lòng nhập tên");
            etFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Vui lòng nhập họ");
            etLastName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        // Disable button during save
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        // Update user info first
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPhone(phone);

        compositeDisposable.add(
                authRepository.updateUser(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Log.d(TAG, "User info updated successfully");

                                    // Update UserManager with new info
                                    UserManager.getInstance().setUsername(firstName + " " + lastName);
                                    UserManager.getInstance().setFirstName(firstName);
                                    UserManager.getInstance().setLastName(lastName);
                                    UserManager.getInstance().setPhone(phone);

                                    // If image was changed, upload it
                                    if (hasImageChanged && selectedImageUri != null) {
                                        uploadProfileImage();
                                    } else {
                                        handleSaveSuccess();
                                    }
                                },
                                this::handleSaveError
                        )
        );
    }

    private void uploadProfileImage() {
        String userId = UserManager.getInstance().getUserId();
        if (userId == null) {
            handleSaveSuccess();
            return;
        }

        File imageFile = FileUtils.getFileFromUri(this, selectedImageUri);
        if (imageFile == null) {
            Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
            handleSaveSuccess();
            return;
        }

        btnSave.setText("Đang tải ảnh lên...");

        compositeDisposable.add(
                authRepository.uploadProfileImage(Long.parseLong(userId), imageFile)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Log.d(TAG, "Profile image uploaded successfully");
                                    handleSaveSuccess();
                                },
                                error -> {
                                    Log.e(TAG, "Error uploading profile image", error);
                                    Toast.makeText(this, "Lỗi tải ảnh lên, nhưng thông tin đã được lưu", Toast.LENGTH_SHORT).show();
                                    handleSaveSuccess();
                                }
                        )
        );
    }

    private void handleSaveSuccess() {
        btnSave.setEnabled(true);
        btnSave.setText("Lưu thay đổi");
        Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();

        // Return success result
        setResult(RESULT_OK);
        finish();
    }

    private void handleSaveError(Throwable error) {
        btnSave.setEnabled(true);
        btnSave.setText("Lưu thay đổi");

        Log.e(TAG, "Error saving profile", error);
        Toast.makeText(this, "Lỗi cập nhật hồ sơ: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

