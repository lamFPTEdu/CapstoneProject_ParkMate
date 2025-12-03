package com.parkmate.android.repository;

import android.util.Log;

import com.parkmate.android.model.request.ReservationRequest;
import com.parkmate.android.model.response.ReservationResponse;
import com.parkmate.android.model.response.ReservationListResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * Repository để xử lý API liên quan đến Reservation (Đặt chỗ)
 */
public class ReservationRepository {
    private static final String TAG = "ReservationRepository";
    private final ApiService apiService;

    public ReservationRepository() {
        this.apiService = ApiClient.getApiService();
    }

    /**
     * Tạo reservation mới
     * @param request ReservationRequest chứa thông tin đặt chỗ
     * @return Single<ReservationResponse>
     */
    public Single<ReservationResponse> createReservation(ReservationRequest request) {
        return apiService.createReservation(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(response -> {
                    Log.d(TAG, "Create reservation success: " + response.getMessage());
                })
                .doOnError(error -> {
                    if (error instanceof HttpException) {
                        HttpException he = (HttpException) error;
                        Log.e(TAG, "Create reservation HTTP " + he.code() + " msg=" + he.message());
                    } else {
                        Log.e(TAG, "Create reservation error: " + error.getMessage(), error);
                    }
                });
    }

    /**
     * Lấy danh sách reservation của user hiện tại
     * @return Single<ReservationListResponse>
     */
    public Single<ReservationListResponse> getMyReservations() {
        return apiService.getMyReservations(true) // ownedByMe = true
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(response -> {
                    Log.d(TAG, "Get my reservations success");
                })
                .doOnError(error -> {
                    if (error instanceof HttpException) {
                        HttpException he = (HttpException) error;
                        Log.e(TAG, "Get reservations HTTP " + he.code() + " msg=" + he.message());
                    } else {
                        Log.e(TAG, "Get reservations error: " + error.getMessage(), error);
                    }
                });
    }

    /**
     * Lấy danh sách reservation của user hiện tại với pagination
     * @param page Page number
     * @param size Page size
     * @return Single<ReservationListResponse>
     */
    public Single<ReservationListResponse> getMyReservations(int page, int size) {
        // Sắp xếp theo createdAt desc để đơn mới nhất lên đầu
        return apiService.getMyReservations(true, page, size, "createdAt", "desc")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(response -> {
                    Log.d(TAG, "Get my reservations page " + page + " success");
                })
                .doOnError(error -> {
                    if (error instanceof HttpException) {
                        HttpException he = (HttpException) error;
                        Log.e(TAG, "Get reservations HTTP " + he.code() + " msg=" + he.message());
                    } else {
                        Log.e(TAG, "Get reservations error: " + error.getMessage(), error);
                    }
                });
    }

    /**
     * Lấy chi tiết reservation theo ID
     * @param id ID của reservation
     * @return Single<ReservationResponse>
     */
    public Single<ReservationResponse> getReservationById(Long id) {
        return apiService.getReservationById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(response -> {
                    Log.d(TAG, "Get reservation detail success");
                })
                .doOnError(error -> {
                    if (error instanceof HttpException) {
                        HttpException he = (HttpException) error;
                        Log.e(TAG, "Get reservation detail HTTP " + he.code());
                    } else {
                        Log.e(TAG, "Get reservation detail error: " + error.getMessage(), error);
                    }
                });
    }

    /**
     * Hủy reservation
     * @param id ID của reservation cần hủy
     * @return Single<ApiResponse<Void>>
     */
    public Single<com.parkmate.android.model.response.ApiResponse<Void>> cancelReservation(Long id) {
        return apiService.cancelReservation(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(response -> {
                    Log.d(TAG, "Cancel reservation success");
                })
                .doOnError(error -> {
                    if (error instanceof HttpException) {
                        HttpException he = (HttpException) error;
                        Log.e(TAG, "Cancel reservation HTTP " + he.code());
                    } else {
                        Log.e(TAG, "Cancel reservation error: " + error.getMessage(), error);
                    }
                });
    }
}
