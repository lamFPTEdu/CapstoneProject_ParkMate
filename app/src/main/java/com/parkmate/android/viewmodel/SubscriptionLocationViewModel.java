package com.parkmate.android.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.parkmate.android.model.SubscriptionPackage;

/**
 * ViewModel cho Subscription Location Selection flow
 * Chia sẻ dữ liệu giữa: Activity + FloorFragment + AreaFragment + SpotFragment
 */
public class SubscriptionLocationViewModel extends ViewModel {

    // ===== Intent Data (từ SubscriptionSelectionActivity) =====
    private final MutableLiveData<Long> parkingLotId = new MutableLiveData<>();
    private final MutableLiveData<Long> vehicleId = new MutableLiveData<>();
    private final MutableLiveData<Long> packageId = new MutableLiveData<>();
    private final MutableLiveData<String> startDate = new MutableLiveData<>();
    private final MutableLiveData<SubscriptionPackage> subscriptionPackage = new MutableLiveData<>();

    // ===== User Selections =====
    private final MutableLiveData<Long> selectedFloorId = new MutableLiveData<>();
    private final MutableLiveData<Long> selectedAreaId = new MutableLiveData<>();
    private final MutableLiveData<Long> selectedSpotId = new MutableLiveData<>();
    private final MutableLiveData<String> selectedSpotName = new MutableLiveData<>();

    // ===== Hold State =====
    private final MutableLiveData<Long> heldSpotId = new MutableLiveData<>();

    // ===== Current Step (for UI state) =====
    private final MutableLiveData<Integer> currentStep = new MutableLiveData<>(0);

    // ========== Getters (LiveData for observation) ==========

    public LiveData<Long> getParkingLotId() {
        return parkingLotId;
    }

    public LiveData<Long> getVehicleId() {
        return vehicleId;
    }

    public LiveData<Long> getPackageId() {
        return packageId;
    }

    public LiveData<String> getStartDate() {
        return startDate;
    }

    public LiveData<SubscriptionPackage> getSubscriptionPackage() {
        return subscriptionPackage;
    }

    public LiveData<Long> getSelectedFloorId() {
        return selectedFloorId;
    }

    public LiveData<Long> getSelectedAreaId() {
        return selectedAreaId;
    }

    public LiveData<Long> getSelectedSpotId() {
        return selectedSpotId;
    }

    public LiveData<String> getSelectedSpotName() {
        return selectedSpotName;
    }

    public LiveData<Long> getHeldSpotId() {
        return heldSpotId;
    }

    public LiveData<Integer> getCurrentStep() {
        return currentStep;
    }

    // ========== Value Getters (for immediate access) ==========

    public Long getParkingLotIdValue() {
        return parkingLotId.getValue();
    }

    public Long getVehicleIdValue() {
        return vehicleId.getValue();
    }

    public Long getPackageIdValue() {
        return packageId.getValue();
    }

    public String getStartDateValue() {
        return startDate.getValue();
    }

    public SubscriptionPackage getSubscriptionPackageValue() {
        return subscriptionPackage.getValue();
    }

    public Long getSelectedFloorIdValue() {
        return selectedFloorId.getValue();
    }

    public Long getSelectedAreaIdValue() {
        return selectedAreaId.getValue();
    }

    public Long getSelectedSpotIdValue() {
        return selectedSpotId.getValue();
    }

    public String getSelectedSpotNameValue() {
        return selectedSpotName.getValue();
    }

    public Long getHeldSpotIdValue() {
        return heldSpotId.getValue();
    }

    public Integer getCurrentStepValue() {
        Integer value = currentStep.getValue();
        return value != null ? value : 0;
    }

    // ========== Setters ==========

    public void setParkingLotId(Long id) {
        parkingLotId.setValue(id);
    }

    public void setVehicleId(Long id) {
        vehicleId.setValue(id);
    }

    public void setPackageId(Long id) {
        packageId.setValue(id);
    }

    public void setStartDate(String date) {
        startDate.setValue(date);
    }

    public void setSubscriptionPackage(SubscriptionPackage pkg) {
        subscriptionPackage.setValue(pkg);
    }

    public void setSelectedFloorId(Long id) {
        selectedFloorId.setValue(id);
        // Reset downstream selections
        selectedAreaId.setValue(null);
        selectedSpotId.setValue(null);
        selectedSpotName.setValue(null);
    }

    public void setSelectedAreaId(Long id) {
        selectedAreaId.setValue(id);
        // Reset downstream selection
        selectedSpotId.setValue(null);
        selectedSpotName.setValue(null);
    }

    public void setSelectedSpot(Long id, String name) {
        selectedSpotId.setValue(id);
        selectedSpotName.setValue(name);
    }

    public void setHeldSpotId(Long id) {
        heldSpotId.setValue(id);
    }

    public void clearHeldSpotId() {
        heldSpotId.setValue(null);
    }

    /**
     * Clear chỉ spot selection - dùng khi quay lại từ Summary
     */
    public void clearSpotSelection() {
        selectedSpotId.setValue(null);
        selectedSpotName.setValue(null);
    }

    public void setCurrentStep(int step) {
        currentStep.setValue(step);
    }

    // ========== Utility Methods ==========

    /**
     * Check if có spot đang được hold
     */
    public boolean hasHeldSpot() {
        return heldSpotId.getValue() != null;
    }

    /**
     * Get formatted start date for API (thêm time nếu cần)
     */
    public String getFormattedStartDate() {
        String date = startDate.getValue();
        if (date != null && !date.contains("T")) {
            return date + "T00:00:00";
        }
        return date;
    }

    /**
     * Reset all selections (khi back về đầu)
     */
    public void resetSelections() {
        selectedFloorId.setValue(null);
        selectedAreaId.setValue(null);
        selectedSpotId.setValue(null);
        selectedSpotName.setValue(null);
        heldSpotId.setValue(null);
        currentStep.setValue(0);
    }
}
