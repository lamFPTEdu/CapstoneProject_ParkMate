package com.parkmate.android.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.adapter.CccdStepAdapter;
import com.parkmate.android.fragment.CccdStep1Fragment;
import com.parkmate.android.fragment.CccdStep2Fragment;
import com.parkmate.android.fragment.CccdStep3Fragment;
import com.parkmate.android.model.request.UpdateUserRequest;
import com.parkmate.android.repository.AuthRepository;
import com.parkmate.android.utils.FileUtils;
import com.parkmate.android.utils.UserManager;
import com.parkmate.android.viewmodel.CccdViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Activity xác thực CCCD với Stepped Wizard (3 bước)
 * Bước 1: Thông tin cơ bản
 * Bước 2: Chi tiết CCCD
 * Bước 3: Upload ảnh
 */
public class VerifyCccdActivity extends AppCompatActivity {

    private static final String TAG = "VerifyCccdActivity";
    private static final int TOTAL_STEPS = 3;

    // Views
    private ImageButton btnBack;
    private ViewPager2 viewPagerSteps;
    private MaterialButton btnPrevious, btnNext;
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private TextView tvStep1Number, tvStep2Number, tvStep3Number;
    private TextView tvStep1Label, tvStep2Label, tvStep3Label;
    private View viewConnector1, viewConnector2;

    // Data
    private CccdViewModel viewModel;
    private CccdStepAdapter adapter;
    private int currentStep = 0;

    // Network
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_cccd);

        viewModel = new ViewModelProvider(this).get(CccdViewModel.class);
        authRepository = new AuthRepository();

        initViews();
        setupViewPager();
        setupListeners();
        loadUserInfo();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        viewPagerSteps = findViewById(R.id.viewPagerSteps);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        tvStep1Number = findViewById(R.id.tvStep1Number);
        tvStep2Number = findViewById(R.id.tvStep2Number);
        tvStep3Number = findViewById(R.id.tvStep3Number);

        tvStep1Label = findViewById(R.id.tvStep1Label);
        tvStep2Label = findViewById(R.id.tvStep2Label);
        tvStep3Label = findViewById(R.id.tvStep3Label);

        viewConnector1 = findViewById(R.id.viewConnector1);
        viewConnector2 = findViewById(R.id.viewConnector2);
    }

    private void setupViewPager() {
        adapter = new CccdStepAdapter(this);
        viewPagerSteps.setAdapter(adapter);
        viewPagerSteps.setUserInputEnabled(false); // Disable swipe, use buttons only

        viewPagerSteps.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentStep = position;
                updateStepIndicator(position);
                updateButtons(position);
                viewModel.setCurrentStep(position);
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                navigateToStep(currentStep - 1);
            } else {
                finish();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentStep > 0) {
                saveCurrentStepData();
                navigateToStep(currentStep - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                saveCurrentStepData();
                if (currentStep < TOTAL_STEPS - 1) {
                    navigateToStep(currentStep + 1);
                } else {
                    handleSubmit();
                }
            }
        });

        // Step indicators - allow clicking to navigate back to completed steps
        layoutStep1.setOnClickListener(v -> {
            if (currentStep > 0) {
                saveCurrentStepData();
                navigateToStep(0);
            }
        });

        layoutStep2.setOnClickListener(v -> {
            if (currentStep > 1) {
                saveCurrentStepData();
                navigateToStep(1);
            } else if (currentStep == 0 && validateCurrentStep()) {
                saveCurrentStepData();
                navigateToStep(1);
            }
        });

        layoutStep3.setOnClickListener(v -> {
            // Only allow clicking if step 2 is completed
            if (currentStep == 2)
                return; // Already here
            // Need to validate previous steps first
        });
    }

    private void navigateToStep(int step) {
        viewPagerSteps.setCurrentItem(step, true);
    }

    private boolean validateCurrentStep() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + currentStep);
        if (fragment == null) {
            // Try alternative approach
            fragment = adapter.createFragment(currentStep);
        }

        // Get fragment from ViewPager2
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPagerSteps.getCurrentItem());

        switch (currentStep) {
            case 0:
                if (currentFragment instanceof CccdStep1Fragment) {
                    return ((CccdStep1Fragment) currentFragment).validate();
                }
                break;
            case 1:
                if (currentFragment instanceof CccdStep2Fragment) {
                    return ((CccdStep2Fragment) currentFragment).validate();
                }
                break;
            case 2:
                if (currentFragment instanceof CccdStep3Fragment) {
                    return ((CccdStep3Fragment) currentFragment).validate();
                }
                break;
        }
        return true;
    }

    private void saveCurrentStepData() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPagerSteps.getCurrentItem());

        switch (currentStep) {
            case 0:
                if (currentFragment instanceof CccdStep1Fragment) {
                    ((CccdStep1Fragment) currentFragment).saveData();
                }
                break;
            case 1:
                if (currentFragment instanceof CccdStep2Fragment) {
                    ((CccdStep2Fragment) currentFragment).saveData();
                }
                break;
        }
    }

    private void updateStepIndicator(int position) {
        // Reset all steps to inactive
        setStepState(tvStep1Number, tvStep1Label, position >= 0, position > 0);
        setStepState(tvStep2Number, tvStep2Label, position >= 1, position > 1);
        setStepState(tvStep3Number, tvStep3Label, position >= 2, false);

        // Update connectors
        viewConnector1.setBackgroundColor(ContextCompat.getColor(this,
                position > 0 ? R.color.primary : R.color.divider));
        viewConnector2.setBackgroundColor(ContextCompat.getColor(this,
                position > 1 ? R.color.primary : R.color.divider));
    }

    private void setStepState(TextView numberView, TextView labelView, boolean isActive, boolean isCompleted) {
        if (isCompleted) {
            numberView.setBackgroundResource(R.drawable.bg_step_completed);
            numberView.setText("✓");
            numberView.setTextColor(ContextCompat.getColor(this, R.color.white));
            labelView.setTextColor(ContextCompat.getColor(this, R.color.success));
        } else if (isActive) {
            numberView.setBackgroundResource(R.drawable.bg_step_active);
            numberView.setTextColor(ContextCompat.getColor(this, R.color.white));
            labelView.setTextColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            numberView.setBackgroundResource(R.drawable.bg_step_inactive);
            numberView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            labelView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void updateButtons(int position) {
        // Previous button
        btnPrevious.setVisibility(position > 0 ? View.VISIBLE : View.INVISIBLE);

        // Next button text
        if (position == TOTAL_STEPS - 1) {
            btnNext.setText("Xác thực");
        } else {
            btnNext.setText("Tiếp tục");
        }
    }

    private void handleSubmit() {
        btnNext.setEnabled(false);
        btnNext.setText("Đang xử lý...");

        String userId = UserManager.getInstance().getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            resetSubmitButton();
            return;
        }

        Long entityId;
        try {
            entityId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Lỗi xác thực phiên đăng nhập", Toast.LENGTH_SHORT).show();
            resetSubmitButton();
            return;
        }

        // Upload images if needed
        if (viewModel.getFrontImageUriValue() != null || viewModel.getBackImageUriValue() != null) {
            uploadImages(entityId);
        } else {
            updateUserInfo();
        }
    }

    private void uploadImages(Long entityId) {
        if (viewModel.getFrontImageUriValue() != null) {
            File frontFile = FileUtils.getFileFromUri(this, viewModel.getFrontImageUriValue());
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
                                        viewModel.setFrontImagePath(
                                                response.getImagePath() != null ? response.getImagePath() : "");
                                        viewModel.setFrontImageUploaded(true);

                                        if (viewModel.getBackImageUriValue() != null) {
                                            uploadBackImage(entityId);
                                        } else {
                                            updateUserInfo();
                                        }
                                    },
                                    error -> {
                                        Toast.makeText(this, "Lỗi tải ảnh mặt trước", Toast.LENGTH_SHORT).show();
                                        resetSubmitButton();
                                    }));
        } else if (viewModel.getBackImageUriValue() != null) {
            uploadBackImage(entityId);
        }
    }

    private void uploadBackImage(Long entityId) {
        File backFile = FileUtils.getFileFromUri(this, viewModel.getBackImageUriValue());
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
                                    viewModel.setBackImagePath(
                                            response.getImagePath() != null ? response.getImagePath() : "");
                                    viewModel.setBackImageUploaded(true);
                                    updateUserInfo();
                                },
                                error -> {
                                    Toast.makeText(this, "Lỗi tải ảnh mặt sau", Toast.LENGTH_SHORT).show();
                                    resetSubmitButton();
                                }));
    }

    private void updateUserInfo() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setIdNumber(viewModel.getCccdNumberValue());
        request.setFullName(viewModel.getFullNameValue());
        request.setGender(viewModel.getGenderValue());
        request.setDateOfBirth(convertToIso8601(viewModel.getDateOfBirthValue()));
        request.setNationality(viewModel.getNationalityValue());
        request.setAddress(viewModel.getPermanentAddressValue());
        request.setIssueDate(convertToIso8601(viewModel.getIssueDateValue()));
        request.setIssuePlace(viewModel.getIssuePlaceValue());
        request.setExpiryDate(convertToIso8601(viewModel.getExpiryDateValue()));
        request.setFrontIdPath(viewModel.getFrontImagePathValue());
        request.setBackIdImgPath(viewModel.getBackImagePathValue());

        compositeDisposable.add(
                authRepository.updateUser(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Toast.makeText(this, "Xác thực CCCD thành công!", Toast.LENGTH_LONG).show();
                                    finish();
                                },
                                error -> {
                                    Toast.makeText(this, "Lỗi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                                    resetSubmitButton();
                                }));
    }

    private void loadUserInfo() {
        String userId = UserManager.getInstance().getUserId();
        if (userId == null || userId.isEmpty())
            return;

        compositeDisposable.add(
                authRepository.getUserInfo(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.isSuccess() && response.getData() != null) {
                                        prefillUserData(response.getData());
                                    }
                                },
                                error -> Log.e(TAG, "Error loading user info", error)));
    }

    private void prefillUserData(com.parkmate.android.model.response.UserInfoResponse.UserData userData) {
        if (userData.getIdNumber() != null)
            viewModel.setCccdNumber(userData.getIdNumber());
        if (userData.getFullName() != null)
            viewModel.setFullName(userData.getFullName());
        if (userData.getGender() != null)
            viewModel.setGender(userData.getGender());
        if (userData.getDateOfBirth() != null)
            viewModel.setDateOfBirth(convertFromIso8601(userData.getDateOfBirth()));
        if (userData.getNationality() != null)
            viewModel.setNationality(userData.getNationality());
        if (userData.getAddress() != null)
            viewModel.setPermanentAddress(userData.getAddress());
        if (userData.getIssueDate() != null)
            viewModel.setIssueDate(convertFromIso8601(userData.getIssueDate()));
        if (userData.getIssuePlace() != null)
            viewModel.setIssuePlace(userData.getIssuePlace());
        if (userData.getExpiryDate() != null)
            viewModel.setExpiryDate(convertFromIso8601(userData.getExpiryDate()));

        // Set image paths (for re-upload)
        if (userData.getFrontIdPath() != null && !userData.getFrontIdPath().isEmpty()) {
            viewModel.setFrontImagePath(userData.getFrontIdPath());
            viewModel.setFrontImageUploaded(true);
        }
        if (userData.getBackIdImgPath() != null && !userData.getBackIdImgPath().isEmpty()) {
            viewModel.setBackImagePath(userData.getBackIdImgPath());
            viewModel.setBackImageUploaded(true);
        }

        // Set presigned URLs for image display (Glide will load these)
        if (userData.getFrontPhotoPresignedUrl() != null && !userData.getFrontPhotoPresignedUrl().isEmpty()) {
            viewModel.setFrontPhotoPresignedUrl(userData.getFrontPhotoPresignedUrl());
        }
        if (userData.getBackPhotoPresignedUrl() != null && !userData.getBackPhotoPresignedUrl().isEmpty()) {
            viewModel.setBackPhotoPresignedUrl(userData.getBackPhotoPresignedUrl());
        }
    }

    private String convertToIso8601(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }

    private String convertFromIso8601(String iso8601Date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(iso8601Date));
        } catch (Exception e) {
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return outputFormat.format(altFormat.parse(iso8601Date));
            } catch (Exception e2) {
                return iso8601Date;
            }
        }
    }

    private void resetSubmitButton() {
        btnNext.setEnabled(true);
        btnNext.setText("Xác thực");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
