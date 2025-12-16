package com.parkmate.android.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.parkmate.android.R;
import com.parkmate.android.utils.BLEBeaconTransmitter;

/**
 * BLEToggleActivity - Màn hình điều khiển BLE Beacon
 * Hiển thị nút toggle lớn ở giữa với hiệu ứng sóng năng lượng
 */
public class BLEToggleActivity extends BaseActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1002;

    private FrameLayout btnBLEToggle;
    private ImageView ivBLEIcon;
    private TextView tvBLEStatus;
    private TextView tvBLEDescription;

    // Wave animation rings
    private View bleWaveRing1;
    private View bleWaveRing2;
    private View bleWaveRing3;

    private BLEBeaconTransmitter bleTransmitter;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_ble_toggle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup toolbar with title (no back button vì có bottom nav)
        setupToolbarWithTitle("BLE Beacon", false);

        // Show bottom nav với BLE item selected
        setupBottomNavigation(true, R.id.nav_ble);

        // Load notification badge
        loadAndShowNotificationBadge();

        // Initialize BLE transmitter
        bleTransmitter = new BLEBeaconTransmitter(this);

        // Initialize views
        initViews();

        // Update UI based on current state
        updateUI();

        // Setup click listener
        setupClickListener();
    }

    private void initViews() {
        btnBLEToggle = findViewById(R.id.btnBLEToggle);
        ivBLEIcon = findViewById(R.id.ivBLEIcon);
        tvBLEStatus = findViewById(R.id.tvBLEStatus);
        tvBLEDescription = findViewById(R.id.tvBLEDescription);

        bleWaveRing1 = findViewById(R.id.bleWaveRing1);
        bleWaveRing2 = findViewById(R.id.bleWaveRing2);
        bleWaveRing3 = findViewById(R.id.bleWaveRing3);
    }

    private void setupClickListener() {
        if (btnBLEToggle != null) {
            btnBLEToggle.setOnClickListener(v -> toggleBLE());
        }
    }

    private void toggleBLE() {
        boolean isEnabled = bleTransmitter.isEnabled();

        if (isEnabled) {
            // Tắt BLE
            disableBLE();
        } else {
            // Bật BLE - cần check permissions trước
            enableBLE();
        }
    }

    private void enableBLE() {
        // Kiểm tra quyền trước tiên
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions();
            return;
        }

        // Kiểm tra Bluetooth có được bật không
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast(getString(R.string.ble_device_not_support));
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Yêu cầu bật Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            } catch (SecurityException e) {
                showToast(getString(R.string.ble_permission_required));
                requestBluetoothPermissions();
            }
            return;
        }

        // Kiểm tra thiết bị có hỗ trợ BLE advertising không
        if (!bleTransmitter.isAdvertisingSupported()) {
            showToast(getString(R.string.ble_advertising_not_support));
            return;
        }

        // Bật BLE
        boolean success = bleTransmitter.enable();

        // Cập nhật UI NGAY LẬP TỨC để hiệu ứng xuất hiện ngay
        updateUI();

        if (success) {
            showToast(getString(R.string.ble_enabled_success));
        } else {
            // Nếu thất bại, cập nhật lại UI sau 100ms
            btnBLEToggle.postDelayed(this::updateUI, 100);
            showToast("Không thể bật BLE. Vui lòng kiểm tra quyền Bluetooth");
        }
    }

    private void disableBLE() {
        bleTransmitter.disable();

        // Cập nhật UI NGAY LẬP TỨC để hiệu ứng tắt ngay
        updateUI();

        showToast(getString(R.string.ble_disabled_success));
    }

    private boolean hasBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE)
                    == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 11 trở xuống
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                    == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            // Android 11 trở xuống
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // Có đủ quyền rồi, thử bật BLE lại
                enableBLE();
            } else {
                showToast(getString(R.string.ble_permission_denied));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                // Bluetooth đã được bật, thử enable BLE
                enableBLE();
            } else {
                // User từ chối bật Bluetooth
                showToast(getString(R.string.ble_bluetooth_required));
            }
        }
    }

    private void updateUI() {
        boolean isEnabled = bleTransmitter.isEnabled();

        if (isEnabled) {
            // BLE đang bật
            tvBLEStatus.setText("BLE đang BẬT");
            tvBLEStatus.setTextColor(getColor(R.color.green));
            tvBLEDescription.setText("Ứng dụng đang phát broadcast dữ liệu để beacon IoT có thể nhận và gửi lên server.");

            // Đổi màu nút sang primary (xanh)
            btnBLEToggle.setBackgroundResource(R.drawable.ble_button_selector);

            // Show animation if not already showing
            if (bleWaveRing1.getVisibility() != View.VISIBLE) {
                startWaveAnimation();
            }
        } else {
            // BLE đang tắt
            tvBLEStatus.setText("BLE đang TẮT");
            tvBLEStatus.setTextColor(getColor(R.color.gray));
            tvBLEDescription.setText("Bật BLE để ứng dụng phát broadcast thông tin khi bạn ở gần bãi đỗ xe.");

            // Đổi màu nút sang xám
            btnBLEToggle.setBackgroundResource(R.drawable.bg_ble_button_off);

            // Hide animation
            stopWaveAnimation();
        }
    }

    private void startWaveAnimation() {
        if (bleWaveRing1 == null || bleWaveRing2 == null || bleWaveRing3 == null) return;

        // Load animation
        Animation waveAnim = AnimationUtils.loadAnimation(this, R.anim.ble_wave_animation);

        // Hiển thị các vòng sóng
        bleWaveRing1.setVisibility(View.VISIBLE);
        bleWaveRing2.setVisibility(View.VISIBLE);
        bleWaveRing3.setVisibility(View.VISIBLE);

        // Start animation với độ trễ khác nhau
        bleWaveRing1.startAnimation(waveAnim);

        bleWaveRing2.postDelayed(() -> {
            Animation waveAnim2 = AnimationUtils.loadAnimation(this, R.anim.ble_wave_animation);
            bleWaveRing2.startAnimation(waveAnim2);
        }, 500);

        bleWaveRing3.postDelayed(() -> {
            Animation waveAnim3 = AnimationUtils.loadAnimation(this, R.anim.ble_wave_animation);
            bleWaveRing3.startAnimation(waveAnim3);
        }, 1000);
    }

    private void stopWaveAnimation() {
        if (bleWaveRing1 == null || bleWaveRing2 == null || bleWaveRing3 == null) return;

        // Clear animation
        bleWaveRing1.clearAnimation();
        bleWaveRing2.clearAnimation();
        bleWaveRing3.clearAnimation();

        // Ẩn các vòng sóng
        bleWaveRing1.setVisibility(View.GONE);
        bleWaveRing2.setVisibility(View.GONE);
        bleWaveRing3.setVisibility(View.GONE);
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWaveAnimation();

        // QUAN TRỌNG: Không tự động tắt BLE khi đóng activity
        // Chỉ tắt khi user chủ động tắt bằng nút toggle
        // Nếu muốn tắt khi đóng app, uncomment dòng dưới:
        // if (bleTransmitter != null && bleTransmitter.isEnabled()) {
        //     bleTransmitter.disable();
        // }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update UI khi quay lại màn hình
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Cập nhật: Khi user rời khỏi màn hình, nếu BLE đang tắt thì đảm bảo cleanup
        if (!bleTransmitter.isEnabled()) {
            bleTransmitter.cleanup();
        }
    }
}

