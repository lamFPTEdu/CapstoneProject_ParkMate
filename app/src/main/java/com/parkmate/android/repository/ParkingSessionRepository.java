package com.parkmate.android.repository;

import com.parkmate.android.model.response.ApiResponse;
import com.parkmate.android.model.response.ParkingSessionResponse;
import com.parkmate.android.network.ApiClient;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Repository cho Parking Session API
 */
public class ParkingSessionRepository {

    /**
     * Lấy danh sách parking sessions của user hiện tại
     */
    public Single<ApiResponse<ParkingSessionResponse>> getMySessions(
            int page,
            int size,
            String sortBy,
            String sortOrder
    ) {
        return ApiClient.getApiService()
                .getParkingSessions(page, size, sortBy, sortOrder, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Lấy danh sách parking sessions với filters
     */
    public Single<ApiResponse<ParkingSessionResponse>> getSessionsWithFilters(
            int page,
            int size,
            String sortBy,
            String sortOrder,
            String sessionStatus,
            String referenceType,
            String startTime,
            String endTime,
            Long totalLessThan,
            Long totalMoreThan,
            Integer durationMinuteGreaterThan,
            Integer durationMinuteLessThan
    ) {
        return ApiClient.getApiService()
                .getParkingSessionsWithFilters(
                        page, size, sortBy, sortOrder, true,
                        sessionStatus, referenceType, startTime, endTime,
                        totalLessThan, totalMoreThan,
                        durationMinuteGreaterThan, durationMinuteLessThan
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}

