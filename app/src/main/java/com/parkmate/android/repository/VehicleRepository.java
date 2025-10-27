package com.parkmate.android.repository;

import android.util.Log;

import com.parkmate.android.model.Vehicle;
import com.parkmate.android.model.request.AddVehicleRequest;
import com.parkmate.android.model.response.ApiResponse;
import com.parkmate.android.model.response.VehicleResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;

import io.reactivex.rxjava3.core.Single;
import retrofit2.HttpException;

public class VehicleRepository {
    private static final String TAG = "VehicleRepository";
    private final ApiService apiService;

    public VehicleRepository() {
        this.apiService = ApiClient.getApiService();
    }

    /**
     * Lấy danh sách xe của người dùng
     */
    public Single<ApiResponse<VehicleResponse>> getVehicles(int page, int size, String sortBy, String sortOrder, boolean ownedByMe) {
        return apiService.getVehicles(page, size, sortBy, sortOrder, ownedByMe)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Get vehicles HTTP " + he.code() + " msg=" + he.message());
                    } else {
                        Log.e(TAG, "Get vehicles error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Thêm xe mới
     */
    public Single<ApiResponse<Vehicle>> addVehicle(AddVehicleRequest request) {
        return apiService.addVehicle(request)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Add vehicle HTTP " + he.code() + " msg=" + he.message());
                    } else {
                        Log.e(TAG, "Add vehicle error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Cập nhật thông tin xe
     */
    public Single<ApiResponse<Vehicle>> updateVehicle(Long vehicleId, AddVehicleRequest request) {
        return apiService.updateVehicle(vehicleId, request)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Update vehicle HTTP " + he.code() + " msg=" + he.message());
                    } else {
                        Log.e(TAG, "Update vehicle error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Xóa xe
     */
    public Single<ApiResponse<Void>> deleteVehicle(Long vehicleId) {
        return apiService.deleteVehicle(vehicleId)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Delete vehicle HTTP " + he.code() + " msg=" + he.message());
                    } else {
                        Log.e(TAG, "Delete vehicle error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Lấy thông tin chi tiết xe
     */
    public Single<ApiResponse<Vehicle>> getVehicleById(Long vehicleId) {
        return apiService.getVehicleById(vehicleId)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Get vehicle by id HTTP " + he.code() + " msg=" + he.message());
                    } else {
                        Log.e(TAG, "Get vehicle by id error: " + err.getMessage(), err);
                    }
                });
    }
}

