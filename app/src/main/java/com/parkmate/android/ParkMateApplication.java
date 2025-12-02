package com.parkmate.android;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.parkmate.android.network.ApiClient;
import com.parkmate.android.utils.BLEBeaconTransmitter;
import com.parkmate.android.utils.ReservationHoldManager;
import com.parkmate.android.utils.SubscriptionHoldManager;
import com.parkmate.android.utils.TokenManager;
import com.parkmate.android.utils.UserManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ParkMateApplication extends Application {

    private static final String TAG = "ParkMateApplication";
    private static ParkMateApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Khởi tạo TokenManager và UserManager bất đồng bộ để tránh ANR
        // Sử dụng Handler để post lên main thread nhưng không block
        new Handler(Looper.getMainLooper()).post(() -> {
            TokenManager.init(this);
            UserManager.init(this);

            // Release any held spot/reservation when app starts (after crash, reopen, etc.)
            releaseHeldSpotOnStartup();
            releaseHeldReservationOnStartup();

            // Khởi động BLE nếu user đã bật trước đó
            initBeaconIfEnabled();
        });
    }

    /**
     * Release held spot when app starts
     * This handles cases like:
     * - App crash
     * - App removed from recent tasks
     * - App reopened after being killed by system
     */
    private void releaseHeldSpotOnStartup() {
        SubscriptionHoldManager holdManager = new SubscriptionHoldManager(this);

        if (holdManager.hasHeldSpot()) {
            long heldSpotId = holdManager.getHeldSpot();
            Log.d(TAG, "Found held spot on startup: " + heldSpotId + ", releasing...");

            // Release the spot via API
            ApiClient.getApiService()
                    .releaseHoldSpot(heldSpotId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                if (response.isSuccess()) {
                                    Log.d(TAG, "Successfully released held spot: " + heldSpotId);
                                } else {
                                    Log.w(TAG, "Failed to release held spot: " + heldSpotId);
                                }
                                // Clear from SharedPreferences regardless of API result
                                holdManager.clearHeldSpot();
                            },
                            throwable -> {
                                Log.e(TAG, "Error releasing held spot: " + heldSpotId, throwable);
                                // Clear from SharedPreferences even on error
                                holdManager.clearHeldSpot();
                            }
                    );
        }
    }

    /**
     * Release held reservation when app starts
     * This handles cases like:
     * - App crash during reservation process
     * - App removed from recent tasks
     * - App reopened after being killed by system
     */
    private void releaseHeldReservationOnStartup() {
        ReservationHoldManager holdManager = new ReservationHoldManager(this);

        if (holdManager.hasHold()) {
            String holdId = holdManager.getHoldId();
            Log.d(TAG, "Found held reservation on startup: " + holdId + ", releasing...");

            // Release the reservation hold via API
            ApiClient.getApiService()
                    .releaseHold(holdId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                if (response.isSuccess()) {
                                    Log.d(TAG, "Successfully released held reservation: " + holdId);
                                } else {
                                    Log.w(TAG, "Failed to release held reservation: " + holdId);
                                }
                                // Clear from SharedPreferences regardless of API result
                                holdManager.clearHoldId();
                            },
                            throwable -> {
                                Log.e(TAG, "Error releasing held reservation: " + holdId, throwable);
                                // Clear from SharedPreferences even on error
                                holdManager.clearHoldId();
                            }
                    );
        }
    }

    /**
     * Khởi động beacon transmitter nếu user đã bật chức năng này trước đó
     */
    private void initBeaconIfEnabled() {
        BLEBeaconTransmitter transmitter = new BLEBeaconTransmitter(this);
        if (transmitter.isEnabled()) {
            Log.d(TAG, "BLE transmitter was enabled, starting broadcast...");
            transmitter.enable();
        }
    }

    public static ParkMateApplication getInstance() {
        return instance;
    }
}

