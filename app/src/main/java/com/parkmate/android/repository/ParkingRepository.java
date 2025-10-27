package com.parkmate.android.repository;

import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;
import com.parkmate.android.model.response.ParkingLotResponse;
import com.parkmate.android.model.response.ParkingLotDetailResponse;
import com.parkmate.android.model.response.ParkingFloorDetailResponse;
import com.parkmate.android.model.response.AreaDetailResponse;

import io.reactivex.rxjava3.core.Single;

public class ParkingRepository {
    private final ApiService apiService;

    public ParkingRepository() {
        this.apiService = ApiClient.getApiService();
    }

    /**
     * Get parking lots with filters and pagination
     *
     * @param ownedByMe Filter by owned parking lots
     * @param name Search by parking lot name
     * @param city Search by city
     * @param is24Hour Filter by 24-hour operation
     * @param status Filter by status (PENDING, ACTIVE, etc.)
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sortBy Sort field (default: id)
     * @param sortOrder Sort direction ASC/DESC (default: ASC)
     */
    public Single<ParkingLotResponse> getParkingLots(
            Boolean ownedByMe,
            String name,
            String city,
            Boolean is24Hour,
            String status,
            Integer page,
            Integer size,
            String sortBy,
            String sortOrder) {
        return apiService.getParkingLots(
                ownedByMe,
                name,
                city,
                is24Hour,
                status,
                page,
                size,
                sortBy,
                sortOrder
        );
    }

    /**
     * Get all parking lots with default pagination
     */
    public Single<ParkingLotResponse> getAllParkingLots() {
        return getParkingLots(null, null, null, null, null, 0, 10, "id", "ASC");
    }

    /**
     * Get parking lots filtered by city
     */
    public Single<ParkingLotResponse> getParkingLotsByCity(String city, int page, int size) {
        return getParkingLots(null, null, city, null, null, page, size, "name", "ASC");
    }

    /**
     * Get active parking lots only
     */
    public Single<ParkingLotResponse> getActiveParkingLots(int page, int size) {
        return getParkingLots(null, null, null, null, "ACTIVE", page, size, "name", "ASC");
    }

    /**
     * Get parking lot detail by ID
     */
    public Single<ParkingLotDetailResponse> getParkingLotDetail(Long id) {
        return apiService.getParkingLotDetail(id);
    }

    /**
     * Get parking floor detail by ID
     * Includes areas and spots information
     */
    public Single<ParkingFloorDetailResponse> getParkingFloorDetail(Long floorId) {
        return apiService.getParkingFloorDetail(floorId);
    }

    /**
     * Get area detail by ID
     * Includes spots information for the specific area
     */
    public Single<AreaDetailResponse> getAreaDetail(Long areaId) {
        return apiService.getAreaDetail(areaId);
    }
}
