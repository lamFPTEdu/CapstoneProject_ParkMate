package com.parkmate.android.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.parkmate.android.R;
import com.parkmate.android.viewmodel.CccdViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Step 3: Upload ảnh CCCD
 * - Ảnh mặt trước
 * - Ảnh mặt sau
 * - Checkbox cam kết
 */
public class CccdStep3Fragment extends Fragment {

    private CccdViewModel viewModel;
    private MaterialCardView cardFrontImage, cardBackImage;
    private ImageView ivFrontImage, ivBackImage;
    private LinearLayout llFrontImagePlaceholder, llBackImagePlaceholder;
    private ProgressBar pbFrontUpload, pbBackUpload;
    private ImageView ivFrontUploadSuccess, ivBackUploadSuccess;
    private CheckBox cbCommitment;

    private Uri tempCameraImageUri;
    private boolean isCapturingFront = true;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CccdViewModel.class);
        setupLaunchers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cccd_step3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
        restoreData();
        observeData(); // Observe LiveData for async updates
    }

    /**
     * Observe LiveData to update UI when data is loaded from API
     */
    private void observeData() {
        // Observe front image presigned URL
        viewModel.getFrontPhotoPresignedUrl().observe(getViewLifecycleOwner(), url -> {
            if (url != null && !url.isEmpty() && viewModel.getFrontImageUriValue() == null) {
                loadFrontImageFromUrl(url);
            }
        });

        // Observe back image presigned URL
        viewModel.getBackPhotoPresignedUrl().observe(getViewLifecycleOwner(), url -> {
            if (url != null && !url.isEmpty() && viewModel.getBackImageUriValue() == null) {
                loadBackImageFromUrl(url);
            }
        });
    }

    /**
     * Load front image from presigned URL using Glide
     */
    private void loadFrontImageFromUrl(String url) {
        if (getContext() == null)
            return;
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .into(ivFrontImage);
        ivFrontImage.setVisibility(View.VISIBLE);
        llFrontImagePlaceholder.setVisibility(View.GONE);
        ivFrontUploadSuccess.setVisibility(View.VISIBLE);
    }

    /**
     * Load back image from presigned URL using Glide
     */
    private void loadBackImageFromUrl(String url) {
        if (getContext() == null)
            return;
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .into(ivBackImage);
        ivBackImage.setVisibility(View.VISIBLE);
        llBackImagePlaceholder.setVisibility(View.GONE);
        ivBackUploadSuccess.setVisibility(View.VISIBLE);
    }

    private void setupLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(requireContext(), "Cần cấp quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && tempCameraImageUri != null) {
                        if (isCapturingFront) {
                            viewModel.setFrontImageUri(tempCameraImageUri);
                            showFrontImage(tempCameraImageUri);
                        } else {
                            viewModel.setBackImageUri(tempCameraImageUri);
                            showBackImage(tempCameraImageUri);
                        }
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            if (isCapturingFront) {
                                viewModel.setFrontImageUri(imageUri);
                                showFrontImage(imageUri);
                            } else {
                                viewModel.setBackImageUri(imageUri);
                                showBackImage(imageUri);
                            }
                        }
                    }
                });
    }

    private void initViews(View view) {
        cardFrontImage = view.findViewById(R.id.cardFrontImage);
        cardBackImage = view.findViewById(R.id.cardBackImage);
        ivFrontImage = view.findViewById(R.id.ivFrontImage);
        ivBackImage = view.findViewById(R.id.ivBackImage);
        llFrontImagePlaceholder = view.findViewById(R.id.llFrontImagePlaceholder);
        llBackImagePlaceholder = view.findViewById(R.id.llBackImagePlaceholder);
        pbFrontUpload = view.findViewById(R.id.pbFrontUpload);
        pbBackUpload = view.findViewById(R.id.pbBackUpload);
        ivFrontUploadSuccess = view.findViewById(R.id.ivFrontUploadSuccess);
        ivBackUploadSuccess = view.findViewById(R.id.ivBackUploadSuccess);
        cbCommitment = view.findViewById(R.id.cbCommitment);
    }

    private void setupListeners() {
        cardFrontImage.setOnClickListener(v -> {
            isCapturingFront = true;
            showImageSourceDialog();
        });

        cardBackImage.setOnClickListener(v -> {
            isCapturingFront = false;
            showImageSourceDialog();
        });

        cbCommitment.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setCommitmentChecked(isChecked);
        });
    }

    private void showImageSourceDialog() {
        String title = isCapturingFront ? "Chọn nguồn ảnh mặt trước" : "Chọn nguồn ảnh mặt sau";
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(new CharSequence[] { "Chụp ảnh từ camera", "Chọn từ thư viện" }, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermission();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = createImageFile();
            if (photoFile != null) {
                tempCameraImageUri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraImageUri);
                cameraLauncher.launch(takePictureIntent);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không thể mở camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(System.currentTimeMillis());
            String imageFileName = "CCCD_" + timeStamp + "_";
            File storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (Exception e) {
            return null;
        }
    }

    private void showFrontImage(Uri uri) {
        Glide.with(this).load(uri).into(ivFrontImage);
        ivFrontImage.setVisibility(View.VISIBLE);
        llFrontImagePlaceholder.setVisibility(View.GONE);
    }

    private void showBackImage(Uri uri) {
        Glide.with(this).load(uri).into(ivBackImage);
        ivBackImage.setVisibility(View.VISIBLE);
        llBackImagePlaceholder.setVisibility(View.GONE);
    }

    private void restoreData() {
        // Restore front image
        Uri frontUri = viewModel.getFrontImageUriValue();
        if (frontUri != null) {
            showFrontImage(frontUri);
        } else if (viewModel.isFrontImageUploaded()) {
            ivFrontUploadSuccess.setVisibility(View.VISIBLE);
        }

        // Restore back image
        Uri backUri = viewModel.getBackImageUriValue();
        if (backUri != null) {
            showBackImage(backUri);
        } else if (viewModel.isBackImageUploaded()) {
            ivBackUploadSuccess.setVisibility(View.VISIBLE);
        }

        // Restore checkbox
        cbCommitment.setChecked(viewModel.isCommitmentChecked());
    }

    /**
     * Validate step 3 data
     * 
     * @return true if valid
     */
    public boolean validate() {
        if (!viewModel.hasFrontImage()) {
            Toast.makeText(requireContext(), "Vui lòng tải lên ảnh mặt trước CCCD", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!viewModel.hasBackImage()) {
            Toast.makeText(requireContext(), "Vui lòng tải lên ảnh mặt sau CCCD", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!cbCommitment.isChecked()) {
            Toast.makeText(requireContext(), "Vui lòng xác nhận cam kết thông tin chính xác", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }

        return true;
    }

    // Expose views for activity to update upload status
    public ProgressBar getPbFrontUpload() {
        return pbFrontUpload;
    }

    public ProgressBar getPbBackUpload() {
        return pbBackUpload;
    }

    public ImageView getIvFrontUploadSuccess() {
        return ivFrontUploadSuccess;
    }

    public ImageView getIvBackUploadSuccess() {
        return ivBackUploadSuccess;
    }
}
