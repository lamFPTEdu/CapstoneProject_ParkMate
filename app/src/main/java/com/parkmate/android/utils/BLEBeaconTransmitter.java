package com.parkmate.android.utils;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.nio.charset.StandardCharsets;

/**
 * BLEBeaconTransmitter - Phát broadcast dữ liệu qua BLE để beacon IoT nhận
 * Kiến trúc:
 * [Smartphone] --BLE Broadcast--> [Beacon IoT] --WiFi/4G--> [Backend]
 */
public class BLEBeaconTransmitter {

    private static final String TAG = "BLETransmitter";
    private static final String PREFS_NAME = "ble_transmitter_prefs";
    private static final String PREFS_ENABLED = "transmitter_enabled";

    // UUID cho service của ParkMate - PHẢI GIỐNG VỚI IoT BEACON
    // Use 16-bit UUID: 0000FFF0-0000-1000-8000-00805F9B34FB
    private static final String SERVICE_UUID = "0000FFF0-0000-1000-8000-00805F9B34FB";

    private Context context;
    private BluetoothLeAdvertiser advertiser;
    private AdvertiseCallback advertiseCallback;
    private boolean isTransmitting = false;

    public BLEBeaconTransmitter(Context context) {
        this.context = context.getApplicationContext();
        initializeAdvertiser();
    }

    private void initializeAdvertiser() {
        // Check Bluetooth permissions for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BLUETOOTH_CONNECT permission not granted");
                return;
            }
        }

        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException when initializing advertiser", e);
        }
    }

    /**
     * Bật chế độ phát broadcast
     */
    public boolean enable() {
        if (isTransmitting) {
            Log.d(TAG, "Already transmitting");
            return true;
        }

        // Lấy thông tin user
        String userId = UserManager.getInstance().getUserId();

        if (userId == null) {
            Log.w(TAG, "User not logged in, cannot start transmitting");
            return false;
        }

        startAdvertising(userId);
        saveState(true);
        Log.d(TAG, "BLE transmitter enabled");
        return true;
    }

    /**
     * Tắt chế độ phát broadcast
     */
    public void disable() {
        Log.d(TAG, "disable() called, isTransmitting=" + isTransmitting);

        // Luôn gọi stopAdvertising bất kể isTransmitting state
        stopAdvertising();
        saveState(false);
        isTransmitting = false;

        Log.d(TAG, "BLE transmitter disabled");
    }

    /**
     * Kiểm tra trạng thái
     */
    public boolean isEnabled() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREFS_ENABLED, false);
    }

    /**
     * Toggle trạng thái
     */
    public void toggle() {
        if (isTransmitting) {
            disable();
        } else {
            enable();
        }
    }

    /**
     * Bắt đầu phát broadcast BLE
     */
    private void startAdvertising(String userId) {
        if (advertiser == null) {
            initializeAdvertiser();
            if (advertiser == null) {
                Log.e(TAG, "BLE Advertiser not available");
                return;
            }
        }

        // Tạo settings cho advertising
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .setTimeout(0) // Không timeout, phát liên tục
                .build();

        // Tạo data để phát
        AdvertiseData data = buildAdvertiseData(userId);

        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                isTransmitting = true;
                Log.d(TAG, "========================================");
                Log.d(TAG, "✓ BLE ADVERTISING STARTED SUCCESSFULLY");
                Log.d(TAG, "========================================");
                Log.d(TAG, "User ID: " + userId);
                Log.d(TAG, "Service UUID: " + SERVICE_UUID);

                try {
                    byte[] userIdBytes = userId.getBytes(StandardCharsets.UTF_8);
                    Log.d(TAG, "User ID Bytes: " + new String(userIdBytes, StandardCharsets.UTF_8));
                    Log.d(TAG, "Data length: " + userIdBytes.length + " bytes");
                } catch (Exception e) {
                    Log.e(TAG, "Error logging user ID bytes", e);
                }

                Log.d(TAG, "Mode: LOW_LATENCY | Power: HIGH | Connectable: false");
                Log.d(TAG, "========================================");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                isTransmitting = false;

                String errorMsg;
                switch (errorCode) {
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        errorMsg = "Data too large (try shorter user ID)";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        errorMsg = "Too many advertisers";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        errorMsg = "Already started";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                        errorMsg = "Internal error";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        errorMsg = "Feature not supported";
                        break;
                    default:
                        errorMsg = "Unknown error: " + errorCode;
                        break;
                }

                Log.e(TAG, "✗ Advertising failed: " + errorMsg);
            }
        };

        // Bắt đầu advertising
        // Check for Bluetooth ADVERTISE permission on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BLUETOOTH_ADVERTISE permission not granted");
                isTransmitting = false;
                return;
            }
        }

        try {
            advertiser.startAdvertising(settings, data, advertiseCallback);
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException: Missing Bluetooth permissions", se);
            isTransmitting = false;
        } catch (Exception e) {
            Log.e(TAG, "Error starting BLE advertising", e);
            isTransmitting = false;
        }
    }

    /**
     * Dừng phát broadcast BLE
     */
    private void stopAdvertising() {
        if (advertiser != null && advertiseCallback != null) {
            // Check for Bluetooth ADVERTISE permission on Android 12+ (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "BLUETOOTH_ADVERTISE permission not granted");
                    return;
                }
            }

            try {
                advertiser.stopAdvertising(advertiseCallback);
                isTransmitting = false;
                Log.d(TAG, "BLE advertising stopped");
            } catch (SecurityException se) {
                Log.e(TAG, "SecurityException: Missing Bluetooth permissions", se);
            } catch (Exception e) {
                Log.e(TAG, "Error stopping BLE advertising", e);
            }
        }
    }

    /**
     * Xây dựng dữ liệu để phát broadcast
     *
     * Format giống code Kotlin đã test thành công với IoT:
     * - Service UUID: 0000FFF0-0000-1000-8000-00805F9B34FB
     * - Service Data: userId as UTF-8 bytes
     */
    private AdvertiseData buildAdvertiseData(String userId) {
        try {
            // Encode userId as bytes (giống licensePlate.toByteArray(Charsets.UTF_8) trong Kotlin)
            byte[] userIdBytes = userId.getBytes(StandardCharsets.UTF_8);

            Log.d(TAG, "User ID bytes length: " + userIdBytes.length);

            // Build advertising data - MINIMAL (giống code Kotlin)
            return new AdvertiseData.Builder()
                    .setIncludeDeviceName(false)  // Don't include device name (saves space)
                    .setIncludeTxPowerLevel(false) // Don't include TX power (saves space)
                    .addServiceData(new ParcelUuid(java.util.UUID.fromString(SERVICE_UUID)), userIdBytes)
                    .build();
        } catch (Exception e) {
            Log.e(TAG, "Error building advertise data", e);
            return new AdvertiseData.Builder().build();
        }
    }

    /**
     * Lưu trạng thái
     */
    private void saveState(boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREFS_ENABLED, enabled).apply();
    }


    /**
     * Kiểm tra xem thiết bị có hỗ trợ BLE advertising không
     */
    public boolean isAdvertisingSupported() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null &&
               bluetoothAdapter.isEnabled() &&
               bluetoothAdapter.isMultipleAdvertisementSupported();
    }

    /**
     * Cleanup khi không dùng nữa
     */
    public void cleanup() {
        stopAdvertising();
        advertiser = null;
        advertiseCallback = null;
    }
}

