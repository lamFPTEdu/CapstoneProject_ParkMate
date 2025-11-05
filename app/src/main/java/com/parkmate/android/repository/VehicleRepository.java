package com.parkmate.android.repository;

import android.util.Log;

import com.parkmate.android.model.Vehicle;
import com.parkmate.android.model.request.AddVehicleRequest;
import com.parkmate.android.model.response.ApiResponse;
import com.parkmate.android.model.response.VehicleResponse;
import com.parkmate.android.model.response.UploadImageResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;

import java.io.File;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    /**
     * Upload ảnh xe
     * @param entityId ID của xe (vehicle)
     * @param imageFile File ảnh cần upload
     */
    public Single<UploadImageResponse> uploadVehicleImage(Long entityId, File imageFile) {
        // Detect MIME type từ file extension
        String mimeType = "image/jpeg"; // Default
        String fileName = imageFile.getName().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            mimeType = "image/png";
        } else if (fileName.endsWith(".webp")) {
            mimeType = "image/webp";
        }

        Log.d(TAG, "Uploading vehicle image: entityId=" + entityId);
        Log.d(TAG, "File: " + imageFile.getName() + ", size=" + imageFile.length() + " bytes");
        Log.d(TAG, "MIME type: " + mimeType);

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        return apiService.uploadIdImage(entityId, "VEHICLE_IMAGE", body)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Upload vehicle image HTTP " + he.code() + " msg=" + he.message());
                        try {
                            if (he.response() != null && he.response().errorBody() != null) {
                                String errorBody = he.response().errorBody().string();
                                Log.e(TAG, "Upload error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot parse error body", e);
                        }
                    } else {
                        Log.e(TAG, "Upload vehicle image error: " + err.getMessage(), err);
                    }
                });
    }
}

