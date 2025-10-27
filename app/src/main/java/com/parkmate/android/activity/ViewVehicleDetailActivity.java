package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputEditText;
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

    private ImageButton btnBack;
    private ImageButton btnEdit;
    private AutoCompleteTextView actvVehicleType;
    private TextInputEditText etLicensePlate;
    private TextInputEditText etBrand;
    private TextInputEditText etModel;
    private TextInputEditText etColor;
    private SwitchCompat switchElectric;
    private Button btnUpdateVehicle;

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
        actvVehicleType = findViewById(R.id.actvVehicleType);
        etLicensePlate = findViewById(R.id.etLicensePlate);
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etColor = findViewById(R.id.etColor);
        switchElectric = findViewById(R.id.switchElectric);
        btnUpdateVehicle = findViewById(R.id.btnUpdateVehicle);

        // Chế độ xem - disable tất cả trường
        actvVehicleType.setEnabled(false);
        etLicensePlate.setEnabled(false);
        etBrand.setEnabled(false);
        etModel.setEnabled(false);
        etColor.setEnabled(false);
        switchElectric.setEnabled(false);
        btnUpdateVehicle.setVisibility(View.GONE);
        btnEdit.setImageResource(R.drawable.ic_edit_24);
    }

    private void setupVehicleTypes() {
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

        btnEdit.setOnClickListener(v -> {
            // Mở màn hình chỉnh sửa
            Intent intent = new Intent(ViewVehicleDetailActivity.this, EditVehicleActivity.class);
            intent.putExtra(EditVehicleActivity.EXTRA_VEHICLE_ID, vehicleId);
            editVehicleLauncher.launch(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
