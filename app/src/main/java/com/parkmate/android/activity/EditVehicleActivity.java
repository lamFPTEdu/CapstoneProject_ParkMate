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

public class EditVehicleActivity extends AppCompatActivity {

    public static final String EXTRA_VEHICLE_ID = "extra_vehicle_id";

    private ImageButton btnBack;
    private ImageButton btnEdit; // Sẽ dùng làm nút hủy (X)
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
    private Button btnUpdateVehicle;

    private ApiService apiService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Uri selectedImageUri;
    private String base64Image;
    private Long vehicleId;
    private Vehicle vehicle;

    private final Map<String, String> vehicleTypeMap = new HashMap<>();
    private final Map<String, String> reverseVehicleTypeMap = new HashMap<>();

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
        setContentView(R.layout.activity_view_vehicle_detail);

        apiService = ApiClient.getApiService();
        vehicleId = getIntent().getLongExtra(EXTRA_VEHICLE_ID, -1L);

        if (vehicleId == -1L) {
            Toast.makeText(this, "Không tìm thấy thông tin xe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupVehicleTypes();
        setupListeners();
        loadVehicleDetail();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
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
        btnUpdateVehicle = findViewById(R.id.btnUpdateVehicle);

        // Setup chế độ chỉnh sửa
        btnEdit.setImageResource(R.drawable.ic_close_24);
        btnUpdateVehicle.setVisibility(View.VISIBLE);
        cvVehicleImage.setClickable(true);

        // Enable tất cả các trường
        actvVehicleType.setEnabled(true);
        etLicensePlate.setEnabled(true);
        etBrand.setEnabled(true);
        etModel.setEnabled(true);
        etColor.setEnabled(true);
        switchElectric.setEnabled(true);
    }

    private void setupVehicleTypes() {
        vehicleTypeMap.put(getString(R.string.vehicle_type_motorbike), "MOTORBIKE");
        vehicleTypeMap.put(getString(R.string.vehicle_type_bike), "BIKE");
        vehicleTypeMap.put(getString(R.string.vehicle_type_car_up_to_9_seats), "CAR_UP_TO_9_SEATS");
        vehicleTypeMap.put(getString(R.string.vehicle_type_other), "OTHER");

        reverseVehicleTypeMap.put("MOTORBIKE", getString(R.string.vehicle_type_motorbike));
        reverseVehicleTypeMap.put("BIKE", getString(R.string.vehicle_type_bike));
        reverseVehicleTypeMap.put("CAR_UP_TO_9_SEATS", getString(R.string.vehicle_type_car_up_to_9_seats));
        reverseVehicleTypeMap.put("OTHER", getString(R.string.vehicle_type_other));

        String[] vehicleTypes = {
                getString(R.string.vehicle_type_motorbike),
                getString(R.string.vehicle_type_bike),
                getString(R.string.vehicle_type_car_up_to_9_seats),
                getString(R.string.vehicle_type_other)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vehicleTypes);
        actvVehicleType.setAdapter(adapter);
    }

    private void loadVehicleDetail() {
        compositeDisposable.add(
                apiService.getVehicleById(vehicleId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleLoadSuccess, this::handleLoadError)
        );
    }

    private void handleLoadSuccess(ApiResponse<Vehicle> response) {
        if (response.isSuccess() && response.getData() != null) {
            vehicle = response.getData();
            populateVehicleData();
        } else {
            Toast.makeText(this, "Không thể tải thông tin xe", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void handleLoadError(Throwable throwable) {
        Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void populateVehicleData() {
        if (vehicle == null) return;

        etLicensePlate.setText(vehicle.getLicensePlate());
        etBrand.setText(vehicle.getBrand());
        etModel.setText(vehicle.getModel());
        etColor.setText(vehicle.getColor());
        switchElectric.setChecked(vehicle.isElectric());

        if (vehicle.getVehicleType() != null) {
            String displayName = reverseVehicleTypeMap.get(vehicle.getVehicleType());
            if (displayName != null) {
                actvVehicleType.setText(displayName, false);
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> finish()); // Nút X để hủy
        cvVehicleImage.setOnClickListener(v -> openImagePicker());
        btnRemoveImage.setOnClickListener(v -> removeSelectedImage());
        btnUpdateVehicle.setOnClickListener(v -> validateAndUpdateVehicle());
    }

    private void openImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
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
                bitmap = resizeBitmap(bitmap, 1024);
                base64Image = bitmapToBase64(bitmap);

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
        if (width <= maxSize && height <= maxSize) return bitmap;

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

    private void validateAndUpdateVehicle() {
        String vehicleTypeDisplay = actvVehicleType.getText().toString().trim();
        String licensePlate = etLicensePlate.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String color = etColor.getText().toString().trim();
        boolean isElectric = switchElectric.isChecked();

        if (vehicleTypeDisplay.isEmpty() || licensePlate.isEmpty() || brand.isEmpty() ||
            model.isEmpty() || color.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        String vehicleType = vehicleTypeMap.get(vehicleTypeDisplay);
        if (vehicleType == null) {
            Toast.makeText(this, "Loại xe không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        AddVehicleRequest request = new AddVehicleRequest(licensePlate, brand, model, color, vehicleType, base64Image, isElectric);
        updateVehicle(request);
    }

    private void updateVehicle(AddVehicleRequest request) {
        compositeDisposable.add(
                apiService.updateVehicle(vehicleId, request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleUpdateSuccess, this::handleUpdateError)
        );
    }

    private void handleUpdateSuccess(ApiResponse<Vehicle> response) {
        if (response.isSuccess()) {
            Toast.makeText(this, "Cập nhật xe thành công", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleUpdateError(Throwable throwable) {
        Toast.makeText(this, "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

