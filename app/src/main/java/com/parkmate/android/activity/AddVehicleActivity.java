package com.parkmate.android.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.parkmate.android.R;
import com.parkmate.android.model.Vehicle;
import com.parkmate.android.model.request.AddVehicleRequest;
import com.parkmate.android.model.response.ApiResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddVehicleActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private CardView cvVehicleImage;
    private ImageView ivVehicleImage;
    private LinearLayout llUploadPlaceholder;
    private ImageButton btnRemoveImage;
    private AutoCompleteTextView actvVehicleType;
    private TextInputEditText etLicensePlate;
    private TextInputEditText etBrand;
    private TextInputEditText etModel;
    private TextInputEditText etColor;
    private SwitchCompat switchElectric;
    private Button btnAddVehicle;

    private ApiService apiService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Uri selectedImageUri;
    private String base64Image;

    // Vehicle type mapping
    private final Map<String, String> vehicleTypeMap = new HashMap<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    displaySelectedImage();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        apiService = ApiClient.getApiService();

        initViews();
        setupVehicleTypes();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        cvVehicleImage = findViewById(R.id.cvVehicleImage);
        ivVehicleImage = findViewById(R.id.ivVehicleImage);
        llUploadPlaceholder = findViewById(R.id.llUploadPlaceholder);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        actvVehicleType = findViewById(R.id.actvVehicleType);
        etLicensePlate = findViewById(R.id.etLicensePlate);
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etColor = findViewById(R.id.etColor);
        switchElectric = findViewById(R.id.switchElectric);
        btnAddVehicle = findViewById(R.id.btnAddVehicle);
    }

    private void setupVehicleTypes() {
        // Map display name to API value
        vehicleTypeMap.put(getString(R.string.vehicle_type_motorbike), "MOTORBIKE");
        vehicleTypeMap.put(getString(R.string.vehicle_type_bike), "BIKE");
        vehicleTypeMap.put(getString(R.string.vehicle_type_car_up_to_9_seats), "CAR_UP_TO_9_SEATS");
        vehicleTypeMap.put(getString(R.string.vehicle_type_other), "OTHER");

        String[] vehicleTypes = {
                getString(R.string.vehicle_type_motorbike),
                getString(R.string.vehicle_type_bike),
                getString(R.string.vehicle_type_car_up_to_9_seats),
                getString(R.string.vehicle_type_other)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                vehicleTypes
        );
        actvVehicleType.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        cvVehicleImage.setOnClickListener(v -> openImagePicker());

        btnRemoveImage.setOnClickListener(v -> removeSelectedImage());

        btnAddVehicle.setOnClickListener(v -> validateAndAddVehicle());
    }

    private void openImagePicker() {
        // Kiểm tra quyền đọc storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, getString(R.string.image_picker_title)));
    }

    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

                // Resize bitmap nếu quá lớn
                bitmap = resizeBitmap(bitmap, 1024);

                // Convert to base64
                base64Image = bitmapToBase64(bitmap);

                // Hiển thị ảnh
                ivVehicleImage.setImageBitmap(bitmap);
                ivVehicleImage.setVisibility(View.VISIBLE);
                llUploadPlaceholder.setVisibility(View.GONE);
                btnRemoveImage.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void removeSelectedImage() {
        selectedImageUri = null;
        base64Image = null;
        ivVehicleImage.setImageDrawable(null);
        ivVehicleImage.setVisibility(View.GONE);
        llUploadPlaceholder.setVisibility(View.VISIBLE);
        btnRemoveImage.setVisibility(View.GONE);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void validateAndAddVehicle() {
        // Lấy giá trị từ các field
        String vehicleTypeDisplay = actvVehicleType.getText().toString().trim();
        String licensePlate = etLicensePlate.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String color = etColor.getText().toString().trim();
        boolean isElectric = switchElectric.isChecked();

        // Validate
        if (vehicleTypeDisplay.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn loại xe", Toast.LENGTH_SHORT).show();
            return;
        }

        if (licensePlate.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập biển số xe", Toast.LENGTH_SHORT).show();
            etLicensePlate.requestFocus();
            return;
        }

        if (brand.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập hãng xe", Toast.LENGTH_SHORT).show();
            etBrand.requestFocus();
            return;
        }

        if (model.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập model xe", Toast.LENGTH_SHORT).show();
            etModel.requestFocus();
            return;
        }

        if (color.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập màu xe", Toast.LENGTH_SHORT).show();
            etColor.requestFocus();
            return;
        }

        // Convert vehicle type từ display name sang API value
        String vehicleType = vehicleTypeMap.get(vehicleTypeDisplay);
        if (vehicleType == null) {
            vehicleType = "MOTORBIKE"; // Default
        }

        // Tạo request - TẠM THỜI KHÔNG GỬI ẢNH vì backend chưa hỗ trợ
        AddVehicleRequest request = new AddVehicleRequest(
                licensePlate,
                brand,
                model,
                color,
                vehicleType,
                null, // Tạm thời gửi null thay vì base64 image
                isElectric
        );

        // Log request để debug
        android.util.Log.d("AddVehicle", "Request: " +
                "licensePlate=" + licensePlate +
                ", brand=" + brand +
                ", model=" + model +
                ", color=" + color +
                ", vehicleType=" + vehicleType +
                ", electric=" + isElectric);

        // Gọi API
        addVehicle(request);
    }

    private void addVehicle(AddVehicleRequest request) {
        // Disable button để tránh click nhiều lần
        btnAddVehicle.setEnabled(false);
        btnAddVehicle.setText("Đang thêm...");

        compositeDisposable.add(
                apiService.addVehicle(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::handleAddVehicleSuccess,
                                this::handleAddVehicleError
                        )
        );
    }

    private void handleAddVehicleSuccess(ApiResponse<Vehicle> response) {
        btnAddVehicle.setEnabled(true);
        btnAddVehicle.setText(R.string.add_vehicle_button);

        if (response != null && response.isSuccess()) {
            Toast.makeText(this, R.string.add_vehicle_success, Toast.LENGTH_SHORT).show();

            // Trả kết quả về màn hình trước và đóng activity
            setResult(RESULT_OK);
            finish();
        } else {
            String errorMsg = response != null && response.getMessage() != null
                    ? response.getMessage()
                    : getString(R.string.add_vehicle_error);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAddVehicleError(Throwable throwable) {
        btnAddVehicle.setEnabled(true);
        btnAddVehicle.setText(R.string.add_vehicle_button);

        String errorMessage = getString(R.string.add_vehicle_error) + ": " + throwable.getMessage();
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        throwable.printStackTrace();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
