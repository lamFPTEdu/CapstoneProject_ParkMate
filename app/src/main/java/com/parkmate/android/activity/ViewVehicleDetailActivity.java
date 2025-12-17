package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.parkmate.android.R;
import com.parkmate.android.model.Vehicle;
import com.parkmate.android.model.response.ApiResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ViewVehicleDetailActivity extends AppCompatActivity {

    public static final String EXTRA_VEHICLE_ID = "extra_vehicle_id";

    private MaterialToolbar toolbar;
    private ImageView ivVehicleImage;
    private LinearLayout llUploadPlaceholder;

    // New TextViews for displaying data
    private TextView tvVehicleType;
    private TextView tvLicensePlate;
    private TextView tvBrand;
    private TextView tvModel;
    private TextView tvColor;
    private TextView tvElectric;

    private ApiService apiService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Long vehicleId;
    private Vehicle vehicle;
    private final Map<String, String> reverseVehicleTypeMap = new HashMap<>();

    private final ActivityResultLauncher<Intent> editVehicleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Reload lại thông tin xe sau khi sửa
                    loadVehicleDetail();
                    setResult(RESULT_OK); // Trả kết quả về VehicleActivity
                }
            });

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
        toolbar = findViewById(R.id.toolbar);
        ivVehicleImage = findViewById(R.id.ivVehicleImage);
        llUploadPlaceholder = findViewById(R.id.llUploadPlaceholder);

        // New TextViews
        tvVehicleType = findViewById(R.id.tvVehicleType);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        tvBrand = findViewById(R.id.tvBrand);
        tvModel = findViewById(R.id.tvModel);
        tvColor = findViewById(R.id.tvColor);
        tvElectric = findViewById(R.id.tvElectric);
    }

    private void setupVehicleTypes() {
        reverseVehicleTypeMap.put("MOTORBIKE", getString(R.string.vehicle_type_motorbike));
        reverseVehicleTypeMap.put("BIKE", getString(R.string.vehicle_type_bike));
        reverseVehicleTypeMap.put("CAR_UP_TO_9_SEATS", getString(R.string.vehicle_type_car_up_to_9_seats));
        reverseVehicleTypeMap.put("OTHER", getString(R.string.vehicle_type_other));
    }

    private void loadVehicleDetail() {
        compositeDisposable.add(
                apiService.getVehicleById(vehicleId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleLoadSuccess, this::handleLoadError));
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
        if (vehicle == null)
            return;

        // Hiển thị ảnh xe
        if (vehicle.getVehiclePhotoUrl() != null && !vehicle.getVehiclePhotoUrl().isEmpty()) {
            String imageUrl = vehicle.getVehiclePhotoUrl();
            // Nếu là đường dẫn tương đối, thêm base URL
            if (!imageUrl.startsWith("http")) {
                imageUrl = ApiClient.getBaseUrl() + imageUrl;
            }

            ivVehicleImage.setVisibility(View.VISIBLE);
            llUploadPlaceholder.setVisibility(View.GONE);

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_24)
                    .error(R.drawable.ic_image_24)
                    .centerCrop()
                    .into(ivVehicleImage);
        } else {
            // Không có ảnh, hiển thị placeholder
            ivVehicleImage.setVisibility(View.GONE);
            llUploadPlaceholder.setVisibility(View.VISIBLE);
        }

        // Populate TextViews
        if (tvLicensePlate != null) {
            tvLicensePlate.setText(vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "N/A");
        }
        if (tvBrand != null) {
            tvBrand.setText(vehicle.getBrand() != null ? vehicle.getBrand() : "N/A");
        }
        if (tvModel != null) {
            tvModel.setText(vehicle.getModel() != null ? vehicle.getModel() : "N/A");
        }
        if (tvColor != null) {
            tvColor.setText(vehicle.getColor() != null ? vehicle.getColor() : "N/A");
        }
        if (tvElectric != null) {
            tvElectric.setText(vehicle.isElectric() ? "Có" : "Không");
        }

        if (tvVehicleType != null && vehicle.getVehicleType() != null) {
            String displayName = reverseVehicleTypeMap.get(vehicle.getVehicleType());
            tvVehicleType.setText(displayName != null ? displayName : vehicle.getVehicleType());
        }
    }

    private void setupListeners() {
        // Setup toolbar navigation
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());

            // Setup menu click for edit
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    // Mở màn hình chỉnh sửa
                    Intent intent = new Intent(ViewVehicleDetailActivity.this, EditVehicleActivity.class);
                    intent.putExtra(EditVehicleActivity.EXTRA_VEHICLE_ID, vehicleId);
                    editVehicleLauncher.launch(intent);
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
