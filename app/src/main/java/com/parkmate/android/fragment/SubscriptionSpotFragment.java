package com.parkmate.android.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.parkmate.android.R;
import com.parkmate.android.activity.SubscriptionLocationSelectionActivity;
import com.parkmate.android.model.ParkingArea;
import com.parkmate.android.model.ParkingFloor;
import com.parkmate.android.model.ParkingSpot;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.view.EnhancedParkingMapView;
import com.parkmate.android.viewmodel.SubscriptionLocationViewModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Fragment để chọn chỗ đỗ (Spot) với bản đồ trực quan HOÀN CHỈNH
 * Hiển thị Floor (nền) + Areas (khu vực) + Spots (chỗ đỗ)
 * Sử dụng ViewModel để share data với Activity và các Fragments khác
 */
public class SubscriptionSpotFragment extends Fragment {
    private static final String TAG = "SubscriptionSpotFragment";

    private EnhancedParkingMapView parkingMapView;
    private FrameLayout loadingOverlay;
    private MaterialCardView cardSelectedSpot;
    private TextView tvSelectedSpotName;
    private MaterialButton btnConfirmSpot;

    private SubscriptionLocationViewModel viewModel;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ParkingSpot selectedSpot;
    private Long heldSpotId = null;

    // Cache data
    private ParkingFloor currentFloor;
    private List<ParkingArea> currentAreas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscription_spot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel from Activity scope
        viewModel = new ViewModelProvider(requireActivity()).get(SubscriptionLocationViewModel.class);

        initializeViews(view);
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Clear previous selection UI when returning from Summary
        selectedSpot = null;
        if (cardSelectedSpot != null) {
            cardSelectedSpot.setVisibility(View.GONE);
        }
        loadCompleteMap();
    }

    private void initializeViews(View view) {
        parkingMapView = view.findViewById(R.id.parkingMapView);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        cardSelectedSpot = view.findViewById(R.id.cardSelectedSpot);
        tvSelectedSpotName = view.findViewById(R.id.tvSelectedSpotName);
        btnConfirmSpot = view.findViewById(R.id.btnConfirmSpot);

        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (heldSpotId != null) {
                    releaseHoldSpot(heldSpotId);
                    heldSpotId = null;
                }
                selectedSpot = null;
                cardSelectedSpot.setVisibility(View.GONE);
            });
        }
    }

    private void setupListeners() {
        parkingMapView.setOnSpotClickListener(spot -> {
            // Chỉ lưu selectedSpot, KHÔNG hold ở đây
            selectedSpot = spot;
            tvSelectedSpotName.setText("Chỗ đỗ: " + spot.getName());
            cardSelectedSpot.setVisibility(View.VISIBLE);

            Log.d(TAG, "Spot selected: " + spot.getName() + " (will hold at Summary)");
        });

        btnConfirmSpot.setOnClickListener(v -> {
            if (selectedSpot != null) {
                SubscriptionLocationSelectionActivity activity = (SubscriptionLocationSelectionActivity) getActivity();
                if (activity != null) {
                    activity.onSpotSelected(selectedSpot.getId(), selectedSpot.getName());
                }
            }
        });
    }

    /**
     * Load toàn bộ map: Floor + Areas + Spots
     */
    private void loadCompleteMap() {
        Long floorId = viewModel.getSelectedFloorIdValue();
        Long areaId = viewModel.getSelectedAreaIdValue();

        if (floorId == null || areaId == null) {
            return;
        }

        showLoading(true);

        String formattedStartDate = viewModel.getFormattedStartDate();

        // Load cả 3 API song song: Floor + Areas + Spots
        Single<ParkingFloor> floorSingle = loadFloorData();
        Single<List<ParkingArea>> areasSingle = loadAreasData(formattedStartDate);
        Single<List<ParkingSpot>> spotsSingle = loadSpotsData(formattedStartDate);

        // Combine tất cả kết quả
        compositeDisposable.add(
                Single.zip(floorSingle, areasSingle, spotsSingle,
                        (floor, areas, spots) -> new MapData(floor, areas, spots))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                mapData -> {
                                    showLoading(false);

                                    // Cache data
                                    currentFloor = mapData.floor;
                                    currentAreas = mapData.areas;

                                    // DEBUG: Log chi tiết để kiểm tra coordinates
                                    Log.d(TAG, "=== SUBSCRIPTION MAP DATA DEBUG ===");
                                    Log.d(TAG, "Floor: " + mapData.floor.getFloorName() +
                                            " at (" + mapData.floor.getFloorTopLeftX() + ", " +
                                            mapData.floor.getFloorTopLeftY() + ")" +
                                            " size: " + mapData.floor.getFloorWidth() + "x" +
                                            mapData.floor.getFloorHeight());

                                    for (int i = 0; i < mapData.areas.size(); i++) {
                                        ParkingArea area = mapData.areas.get(i);
                                        Log.d(TAG, "Area " + i + ": " + area.getName() +
                                                " ID=" + area.getId() +
                                                " at (" + area.getAreaTopLeftX() + ", " +
                                                area.getAreaTopLeftY() + ")" +
                                                " size: " + area.getAreaWidth() + "x" +
                                                area.getAreaHeight());
                                    }

                                    if (!mapData.spots.isEmpty()) {
                                        ParkingSpot firstSpot = mapData.spots.get(0);
                                        ParkingSpot lastSpot = mapData.spots.get(mapData.spots.size() - 1);
                                        Log.d(TAG, "First Spot: " + firstSpot.getName() +
                                                " areaId=" + firstSpot.getAreaId() +
                                                " at (" + firstSpot.getSpotTopLeftX() + ", " +
                                                firstSpot.getSpotTopLeftY() + ")");
                                        Log.d(TAG, "Last Spot: " + lastSpot.getName() +
                                                " areaId=" + lastSpot.getAreaId() +
                                                " at (" + lastSpot.getSpotTopLeftX() + ", " +
                                                lastSpot.getSpotTopLeftY() + ")");
                                    }
                                    Log.d(TAG, "=== END DEBUG ===");

                                    // Vẽ map hoàn chỉnh
                                    parkingMapView.setMapData(mapData.floor, mapData.areas, mapData.spots);
                                    parkingMapView.setVisibility(View.VISIBLE);

                                    Log.d(TAG, "Map loaded: Floor=" + mapData.floor.getFloorName() +
                                            ", Areas=" + mapData.areas.size() +
                                            ", Spots=" + mapData.spots.size());
                                },
                                throwable -> {
                                    showLoading(false);
                                    showError("Lỗi tải bản đồ: " + throwable.getMessage());
                                    Log.e(TAG, "Error loading map", throwable);
                                }));
    }

    private Single<ParkingFloor> loadFloorData() {
        Long parkingLotId = viewModel.getParkingLotIdValue();
        Long vehicleId = viewModel.getVehicleIdValue();
        Long packageId = viewModel.getPackageIdValue();
        String startDate = viewModel.getFormattedStartDate();
        Long selectedFloorId = viewModel.getSelectedFloorIdValue();

        return ApiClient.getApiService()
                .getAvailableFloors(parkingLotId, vehicleId, packageId, startDate)
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        for (ParkingFloor floor : response.getData()) {
                            if (selectedFloorId != null && floor.getId() == selectedFloorId) {
                                return floor;
                            }
                        }
                    }
                    throw new Exception("Không tìm thấy thông tin tầng");
                });
    }

    private Single<List<ParkingArea>> loadAreasData(String startDate) {
        Long floorId = viewModel.getSelectedFloorIdValue();
        Long vehicleId = viewModel.getVehicleIdValue();
        Long packageId = viewModel.getPackageIdValue();
        Long selectedAreaId = viewModel.getSelectedAreaIdValue();

        return ApiClient.getApiService()
                .getAvailableAreas(floorId, vehicleId, packageId, startDate)
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        List<ParkingArea> areas = response.getData();

                        // LỌC CHỈ LẤY AREA ĐƯỢC CHỌN
                        for (ParkingArea area : areas) {
                            if (selectedAreaId != null && area.getId() == selectedAreaId) {
                                List<ParkingArea> filteredAreas = new java.util.ArrayList<>();
                                filteredAreas.add(area);
                                Log.d(TAG, "Filtered area: " + area.getName() +
                                        " at (" + area.getAreaTopLeftX() + ", " + area.getAreaTopLeftY() + ")" +
                                        " size: " + area.getAreaWidth() + "x" + area.getAreaHeight());
                                return filteredAreas;
                            }
                        }
                        throw new Exception("Không tìm thấy area đã chọn");
                    }
                    throw new Exception("Không thể tải danh sách khu vực");
                });
    }

    private Single<List<ParkingSpot>> loadSpotsData(String startDate) {
        Long areaId = viewModel.getSelectedAreaIdValue();
        Long vehicleId = viewModel.getVehicleIdValue();
        Long packageId = viewModel.getPackageIdValue();

        return ApiClient.getApiService()
                .getAvailableSpots(areaId, vehicleId, packageId, startDate)
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        List<ParkingSpot> spots = response.getData();
                        // GÁN AREA ID cho mỗi spot để biết spot thuộc area nào
                        for (ParkingSpot spot : spots) {
                            spot.setAreaId(areaId);
                        }
                        return spots;
                    }
                    throw new Exception("Không thể tải danh sách chỗ đỗ");
                });
    }

    private void holdSpot(ParkingSpot spot) {
        compositeDisposable.add(
                ApiClient.getApiService()
                        .holdSpot(spot.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.isSuccess()) {
                                        selectedSpot = spot;
                                        heldSpotId = spot.getId();

                                        // Notify Activity về held spot
                                        SubscriptionLocationSelectionActivity activity = (SubscriptionLocationSelectionActivity) getActivity();
                                        if (activity != null) {
                                            activity.setHeldSpotId(spot.getId());
                                        }

                                        tvSelectedSpotName.setText("Chỗ đỗ: " + spot.getName());
                                        cardSelectedSpot.setVisibility(View.VISIBLE);

                                        Log.d(TAG, "Spot held successfully: " + spot.getName());
                                    } else {
                                        showError("Không thể giữ chỗ này: " + response.getError());
                                    }
                                },
                                throwable -> showError("Lỗi khi giữ chỗ: " + throwable.getMessage())));
    }

    private void releaseHoldSpot(Long spotId) {
        compositeDisposable.add(
                ApiClient.getApiService()
                        .releaseHoldSpot(spotId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> Log.d(TAG, "Previous spot released: " + spotId),
                                throwable -> Log.e(TAG, "Error releasing spot: " + throwable.getMessage())));
    }

    /**
     * Public method để Activity có thể gọi release từ bên ngoài
     */
    public void releaseCurrentHeldSpot() {
        selectedSpot = null;
        cardSelectedSpot.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.clear();
    }

    /**
     * Helper class để combine 3 loại data
     */
    private static class MapData {
        final ParkingFloor floor;
        final List<ParkingArea> areas;
        final List<ParkingSpot> spots;

        MapData(ParkingFloor floor, List<ParkingArea> areas, List<ParkingSpot> spots) {
            this.floor = floor;
            this.areas = areas;
            this.spots = spots;
        }
    }
}
