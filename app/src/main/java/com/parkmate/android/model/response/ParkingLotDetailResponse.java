package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ParkingLotDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ParkingLotDetail data;

    @SerializedName("error")
    private String error;

    @SerializedName("meta")
    private Object meta;

    @SerializedName("timestamp")
    private String timestamp;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public ParkingLotDetail getData() { return data; }
    public void setData(ParkingLotDetail data) { this.data = data; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public Object getMeta() { return meta; }
    public void setMeta(Object meta) { this.meta = meta; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public static class ParkingLotDetail {
        @SerializedName("id")
        private Long id;

        @SerializedName("partnerId")
        private Long partnerId;

        @SerializedName("name")
        private String name;

        @SerializedName("streetAddress")
        private String streetAddress;

        @SerializedName("ward")
        private String ward;

        @SerializedName("city")
        private String city;

        @SerializedName("latitude")
        private Double latitude;

        @SerializedName("longitude")
        private Double longitude;

        @SerializedName("totalFloors")
        private Integer totalFloors;

        @SerializedName("openTime")
        private String openTime;

        @SerializedName("closeTime")
        private String closeTime;

        @SerializedName("is24Hour")
        private Boolean is24Hour;

        @SerializedName("boundaryTopLeftX")
        private Integer boundaryTopLeftX;

        @SerializedName("boundaryTopLeftY")
        private Integer boundaryTopLeftY;

        @SerializedName("boundaryWidth")
        private Integer boundaryWidth;

        @SerializedName("boundaryHeight")
        private Integer boundaryHeight;

        @SerializedName("status")
        private String status;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("parkingFloors")
        private List<ParkingFloor> parkingFloors;

        @SerializedName("pricingRules")
        private List<PricingRule> pricingRules;

        @SerializedName("lotCapacity")
        private List<Capacity> lotCapacity;

        @SerializedName("images")
        private List<ImageData> images;

        @SerializedName("defaultPricingRules")
        private List<DefaultPricingRule> defaultPricingRules;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getPartnerId() { return partnerId; }
        public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStreetAddress() { return streetAddress; }
        public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
        public String getWard() { return ward; }
        public void setWard(String ward) { this.ward = ward; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public Integer getTotalFloors() { return totalFloors; }
        public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }
        public String getOpenTime() { return openTime; }
        public void setOpenTime(String openTime) { this.openTime = openTime; }
        public String getCloseTime() { return closeTime; }
        public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
        public Boolean getIs24Hour() { return is24Hour; }
        public void setIs24Hour(Boolean is24Hour) { this.is24Hour = is24Hour; }
        public Integer getBoundaryTopLeftX() { return boundaryTopLeftX; }
        public void setBoundaryTopLeftX(Integer boundaryTopLeftX) { this.boundaryTopLeftX = boundaryTopLeftX; }
        public Integer getBoundaryTopLeftY() { return boundaryTopLeftY; }
        public void setBoundaryTopLeftY(Integer boundaryTopLeftY) { this.boundaryTopLeftY = boundaryTopLeftY; }
        public Integer getBoundaryWidth() { return boundaryWidth; }
        public void setBoundaryWidth(Integer boundaryWidth) { this.boundaryWidth = boundaryWidth; }
        public Integer getBoundaryHeight() { return boundaryHeight; }
        public void setBoundaryHeight(Integer boundaryHeight) { this.boundaryHeight = boundaryHeight; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
        public List<ParkingFloor> getParkingFloors() { return parkingFloors; }
        public void setParkingFloors(List<ParkingFloor> parkingFloors) { this.parkingFloors = parkingFloors; }
        public List<PricingRule> getPricingRules() { return pricingRules; }
        public void setPricingRules(List<PricingRule> pricingRules) { this.pricingRules = pricingRules; }
        public List<Capacity> getLotCapacity() { return lotCapacity; }
        public void setLotCapacity(List<Capacity> lotCapacity) { this.lotCapacity = lotCapacity; }
        public List<ImageData> getImages() { return images; }
        public void setImages(List<ImageData> images) { this.images = images; }
        public List<DefaultPricingRule> getDefaultPricingRules() { return defaultPricingRules; }
        public void setDefaultPricingRules(List<DefaultPricingRule> defaultPricingRules) { this.defaultPricingRules = defaultPricingRules; }

        // Helper methods
        public String getFullAddress() {
            return streetAddress + ", " + ward + ", " + city;
        }

        public boolean isActive() {
            return "ACTIVE".equals(status);
        }

        public String getOperatingHours() {
            if (is24Hour != null && is24Hour) {
                return "24/7";
            }
            return openTime + " - " + closeTime;
        }
    }

    public static class ParkingFloor {
        @SerializedName("id")
        private Long id;

        @SerializedName("floorNumber")
        private Integer floorNumber;

        @SerializedName("floorName")
        private String floorName;

        @SerializedName("isActive")
        private Boolean isActive;

        @SerializedName("parkingFloorCapacity")
        private List<Capacity> parkingFloorCapacity;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getFloorNumber() { return floorNumber; }
        public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
        public String getFloorName() { return floorName; }
        public void setFloorName(String floorName) { this.floorName = floorName; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public List<Capacity> getParkingFloorCapacity() { return parkingFloorCapacity; }
        public void setParkingFloorCapacity(List<Capacity> parkingFloorCapacity) { this.parkingFloorCapacity = parkingFloorCapacity; }
    }

    public static class Capacity {
        @SerializedName("capacity")
        private Integer capacity;

        @SerializedName("vehicleType")
        private String vehicleType;

        @SerializedName("supportElectricVehicle")
        private Boolean supportElectricVehicle;

        @SerializedName("isActive")
        private Boolean isActive;

        // Getters and Setters
        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
        public String getVehicleType() { return vehicleType; }
        public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
        public Boolean getSupportElectricVehicle() { return supportElectricVehicle; }
        public void setSupportElectricVehicle(Boolean supportElectricVehicle) { this.supportElectricVehicle = supportElectricVehicle; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }

    public static class PricingRule {
        @SerializedName("id")
        private Long id;

        @SerializedName("vehicleType")
        private String vehicleType;

        @SerializedName("ruleName")
        private String ruleName;

        @SerializedName("stepRate")
        private Integer stepRate;

        @SerializedName("stepMinute")
        private Integer stepMinute;

        @SerializedName("initialCharge")
        private Integer initialCharge;

        @SerializedName("initialDurationMinute")
        private Integer initialDurationMinute;

        @SerializedName("isActive")
        private Boolean isActive;

        @SerializedName("validFrom")
        private String validFrom;

        @SerializedName("validUntil")
        private String validUntil;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getVehicleType() { return vehicleType; }
        public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public Integer getStepRate() { return stepRate; }
        public void setStepRate(Integer stepRate) { this.stepRate = stepRate; }
        public Integer getStepMinute() { return stepMinute; }
        public void setStepMinute(Integer stepMinute) { this.stepMinute = stepMinute; }
        public Integer getInitialCharge() { return initialCharge; }
        public void setInitialCharge(Integer initialCharge) { this.initialCharge = initialCharge; }
        public Integer getInitialDurationMinute() { return initialDurationMinute; }
        public void setInitialDurationMinute(Integer initialDurationMinute) { this.initialDurationMinute = initialDurationMinute; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public String getValidFrom() { return validFrom; }
        public void setValidFrom(String validFrom) { this.validFrom = validFrom; }
        public String getValidUntil() { return validUntil; }
        public void setValidUntil(String validUntil) { this.validUntil = validUntil; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class ImageData {
        @SerializedName("id")
        private Long id;

        @SerializedName("path")
        private String path;

        @SerializedName("isActive")
        private Boolean isActive;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class DefaultPricingRule {
        @SerializedName("lotId")
        private Long lotId;

        @SerializedName("lotName")
        private String lotName;

        @SerializedName("pricingRule")
        private PricingRule pricingRule;

        @SerializedName("vehicleType")
        private String vehicleType;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        // Getters and Setters
        public Long getLotId() { return lotId; }
        public void setLotId(Long lotId) { this.lotId = lotId; }
        public String getLotName() { return lotName; }
        public void setLotName(String lotName) { this.lotName = lotName; }
        public PricingRule getPricingRule() { return pricingRule; }
        public void setPricingRule(PricingRule pricingRule) { this.pricingRule = pricingRule; }
        public String getVehicleType() { return vehicleType; }
        public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }
}
